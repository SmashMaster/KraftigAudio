package kraftig.game;

import com.samrj.devil.geo3d.Box3;
import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.topo.DAG;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import kraftig.game.gui.jacks.InputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.OutputJack;
import kraftig.game.util.Savable;

public class ProjectSpace implements Savable
{
    private final List<Panel> panels = new ArrayList<>();
    private final List<Wire> wires = new ArrayList<>();
    
    public void add(Panel panel)
    {
        panels.add(panel);
    }
    
    public void remove(Panel panel)
    {
        panels.remove(panel);
    }
    
    public List<Panel> getPanels()
    {
        return Collections.unmodifiableList(panels);
    }
    
    public void add(Wire wire)
    {
        wires.add(wire);
    }
    
    public void remove(Wire wire)
    {
        wires.remove(wire);
    }
    
    public List<Wire> getWires()
    {
        return Collections.unmodifiableList(wires);
    }
    
    public Stream<FocusQuery> focusStream(Vec3 pos, Vec3 dir)
    {
        return Stream.concat(panels.stream().map(p -> p.checkFocus(pos, dir)),
                             wires.stream().map(w -> w.checkFocus(pos, dir)))
                .filter(q -> q != null);
    }
    
    public FocusQuery getFocus(Vec3 pos, Vec3 dir)
    {
        return focusStream(pos, dir)
                .reduce((a, b) -> a.dist < b.dist ? a : b)
                .orElse(null);
    }
    
    public List<Panel> sortPanels()
    {
        DAG<Panel> dag = new DAG<>();
        for (Panel panel : panels)
        {
            dag.add(panel);
            for (Jack jack : panel.getJacks()) if (jack instanceof InputJack)
            {
                Panel inPanel = ((InputJack)jack).getPanel();
                
                if (inPanel != null)
                {
                    dag.add(inPanel);
                    dag.addEdge(inPanel, panel);
                }
            }
        }
        
        return dag.sort();
    }
    
    public void render()
    {
        Camera3D camera = Main.instance().getCamera();
        
        //Update panel positions.
        for (Panel p : panels) p.updateEdge();
        
        //Calculate camera frustum.
        Vec3[] frustum = camera.getFrustum();
        Box3 viewBox = Box3.empty();
        Mat4 invViewMat = Mat4.translation(camera.pos);
        invViewMat.rotate(camera.dir);
        for (Vec3 corner : frustum) viewBox.expand(corner.mult(invViewMat));
        
        //Add potentially visible geometry.
        List<Drawable> drawList = new ArrayList<>(1024);
        for (Panel panel : panels) if (panel.isVisible(viewBox))
                drawList.add(panel);
        
        for (Wire w : wires) for (Drawable d : w.updateSplits(panels))
            if (d.isVisible(viewBox)) drawList.add(d);
        
        //Sort and draw world objects.
        DAG<Drawable> overlapGraph = new DAG<>();
        for (Drawable draw : drawList) overlapGraph.add(draw);
        
        //OPTIMIZE - Remove cyclic check on each edge add.
        
        for (int i=0; i<drawList.size(); i++) for (int j=i+1; j<drawList.size(); j++)
        {
            Drawable a = drawList.get(i), b = drawList.get(j);
            
            switch (Overlap.get(a, b, camera))
            {
                case A_BEHIND_B: overlapGraph.addEdge(a, b); break;
                case B_BEHIND_A: overlapGraph.addEdge(b, a); break;
            }
        }
        
        for (Drawable draw : overlapGraph.sort()) draw.render();
    }
    
    public void delete()
    {
        for (Panel panel : panels) panel.delete();
        panels.clear();
        wires.clear();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        Map<Jack, JackID> jackMap = new IdentityHashMap<>();
        jackMap.put(null, new JackID(-1, -1));
        
        out.writeInt(panels.size());
        for (int pi=0; pi<panels.size(); pi++)
        {
            Panel panel = panels.get(pi);
            List<Jack> jacks = panel.getJacks();
            
            for (int ji=0; ji<jacks.size(); ji++)
            {
                Jack jack = jacks.get(ji);
                jackMap.put(jack, new JackID(pi, ji));
            }
            
            PanelSaver.save(panel, out);
        }
        
        out.writeInt(wires.size());
        for (Wire wire : wires)
        {
            jackMap.get(wire.getIn()).save(out);
            jackMap.get(wire.getOut()).save(out);
            wire.save(out);
        }
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        int panelCount = in.readInt();
        for (int i=0; i<panelCount; i++) panels.add(PanelSaver.load(in));
        
        int wireCount = in.readInt();
        for (int i=0; i<wireCount; i++)
        {
            Wire wire = new Wire();
            int pIn = in.readInt(), jIn = in.readInt();
            int pOut = in.readInt(), jOut = in.readInt();
            
            if (pIn >= 0)
            {
                Panel panel = panels.get(pIn);
                Jack jack = panel.getJacks().get(jIn);
                wire.connectIn((OutputJack)jack);
            }
            
            if (pOut >= 0)
            {
                Panel panel = panels.get(pOut);
                Jack jack = panel.getJacks().get(jOut);
                wire.connectOut((InputJack)jack);
            }
            
            wire.load(in);
            wires.add(wire);
        }
    }
    
    private class JackID
    {
        private final int panelIndex, index;
        
        private JackID(int panelIndex, int index)
        {
            this.panelIndex = panelIndex;
            this.index = index;
        }
        
        private void save(DataOutputStream out) throws IOException
        {
            out.writeInt(panelIndex);
            out.writeInt(index);
        }
    }
    // </editor-fold>
}
