package kraftig.game;

import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.topo.DAG;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import kraftig.game.device.AudioDevice;
import kraftig.game.util.ConcatList;
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
    
    public List<AudioDevice> sortDevices()
    {
        DAG<AudioDevice> dag = new DAG<>();
        for (Panel panel : panels) if (panel instanceof AudioDevice)
        {
            AudioDevice device = (AudioDevice)panel;
            dag.add(device);
            device.getInputDevices().forEach(in ->
            {
                dag.add(in);
                dag.addEdge(in, device);
            });
        }
        
        return dag.sort();
    }
    
    public void render()
    {
        Camera3D camera = Main.instance().getCamera();
        
        //Update panel positions.
        for (Panel p : panels) p.updateEdge();
        
        //Calculate wire splits.
        List<? extends Drawable> drawList = panels;
        for (Wire w : wires) drawList = new ConcatList<>(drawList, w.updateSplits(panels));
        
        //Sort and draw world objects.
        DAG<Drawable> overlapGraph = new DAG<>();
        for (Drawable draw : drawList) overlapGraph.add(draw);
        
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
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        out.writeInt(panels.size());
        for (Panel panel : panels) PanelSaver.save(panel, out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        int size = in.readInt();
        for (int i=0; i<size; i++) panels.add(PanelSaver.load(in));
    }
    // </editor-fold>
}
