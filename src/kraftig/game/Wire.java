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
    
    public FocusQuery checkFocus(Vec3 pos, Vec3 dir)
    {
        return null;
    }
    
    @Override
    public void updateEdge(Camera3D camera)
    {
    }

    @Override
    public void render(Camera3D camera, float alpha)
    {
        GL11.glLineWidth(1.5f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, alpha*0.875f);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (WireNode n = first; n != null; n = n.next) GraphicsUtil.glVertex(n.pos);
        GL11.glEnd();
        
        GL11.glPointSize(4.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, alpha);
        GL11.glBegin(GL11.GL_POINTS);
        for (WireNode n = first; n != null; n = n.next) GraphicsUtil.glVertex(n.pos);
        GL11.glEnd();
    }
    
    public class WireNode implements Focusable
    {
        public final Vec3 pos = new Vec3();
        
        private WireNode prev, next;
        
        public boolean isCorner()
        {
            return prev != null && next != null;
        }
        
        public WireNode makeCorner()
        {
            if (prev == null && next == null) throw new IllegalStateException();
            else if (prev == null)
            {
                prev = new WireNode();
                prev.next = this;
                prev.pos.set(pos);
                first = prev;
                return prev;
            }
            else if (next == null)
            {
                next = new WireNode();
                next.prev = this;
                next.pos.set(pos);
                last = next;
                return next;
            }
            else throw new IllegalStateException();
        }

        @Override
        public void onMouseButton(FocusQuery query, int button, int action, int mods)
        {
        }
    }
}
