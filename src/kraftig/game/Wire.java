package kraftig.game;

import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.graphics.GraphicsUtil;
import com.samrj.devil.math.Vec3;
import kraftig.game.gui.Jack;
import org.lwjgl.opengl.GL11;

public class Wire implements Drawable
{
    private WireNode first, last;
    
    public Wire()
    {
        first = new WireNode();
        last = new WireNode();
        first.next = last;
        last.prev = first;
    }
    
    public WireNode getFirst()
    {
        return first;
    }
    
    public WireNode getLast()
    {
        return last;
    }
    
    public Wire connectIn(Jack jack)
    {
        if (jack.getType() != Jack.Type.OUTPUT) throw new IllegalArgumentException();
        jack.connect(this);
        return this;
    }
    
    public Wire connectOut(Jack jack)
    {
        if (jack.getType() != Jack.Type.INPUT) throw new IllegalArgumentException();
        jack.connect(this);
        return this;
    }
    
    @Override
    public void updateEdge(Camera3D camera)
    {
    }

    @Override
    public void render(Camera3D camera, float alpha)
    {
        GL11.glLineWidth(2.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, alpha*0.875f);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (WireNode n = first; n != null; n = n.next) GraphicsUtil.glVertex(n.pos);
        GL11.glEnd();
    }
    
    public class WireNode
    {
        public final Vec3 pos = new Vec3();
        
        private WireNode prev, next;
    }
}
