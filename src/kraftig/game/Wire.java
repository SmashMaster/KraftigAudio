package kraftig.game;

import com.samrj.devil.graphics.GraphicsUtil;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import kraftig.game.gui.Jack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Wire implements Drawable
{
    private static final float FOCUS_ANG_RADIUS = Util.toRadians(1.0f);
    private static final float FOCUS_SIN = (float)Math.sin(FOCUS_ANG_RADIUS);
    private static final float FOCUS_SIN_SQ = FOCUS_SIN*FOCUS_SIN;
    private static final float ARROW_LENGTH = 1.0f/48.0f;
    private static final float ARROW_WIDTH = 1.0f/192.0f;
    
    private Jack in, out;
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
    
    public void connectIn(Jack jack)
    {
        if (jack == null) throw new NullPointerException();
        if (in != null) throw new IllegalStateException();
        if (jack.getType() != Jack.Type.OUTPUT) throw new IllegalArgumentException();
        jack.connect(this);
        in = jack;
    }
    
    public Jack getIn()
    {
        return in;
    }
    
    public void disconnectIn()
    {
        if (in == null) throw new IllegalStateException();
        in.disconnect(this);
        in = null;
    }
    
    public WireNode getLast()
    {
        return last;
    }
    
    public void connectOut(Jack jack)
    {
        if (jack == null) throw new NullPointerException();
        if (out != null) throw new IllegalStateException();
        if (jack.getType() != Jack.Type.INPUT) throw new IllegalArgumentException();
        jack.connect(this);
        out = jack;
    }
    
    public Jack getOut()
    {
        return out;
    }
    
    public void disconnectOut()
    {
        if (out == null) throw new IllegalStateException();
        out.disconnect(this);
        out = null;
    }
    
    public boolean isDegenerate()
    {
        return first == last;
    }
    
    public FocusQuery checkFocus(Vec3 pos, Vec3 dir)
    {
        float closeDist = Float.POSITIVE_INFINITY;
        WireNode closest = null;
        
        float a = dir.squareLength();
        
        for (WireNode n = first; n != null; n = n.next)
        {
            Vec3 pp = Vec3.sub(pos, n.pos);
            
            float b = dir.dot(pp)*2.0f;
            float c = pp.squareLength()*(1.0f - FOCUS_SIN_SQ);
            
            float t = (-b - (float)Math.sqrt(b*b - 4.0f*a*c))/(2.0f*a);
            
            if (t > 0.0f && Float.isFinite(t) && t < closeDist)
            {
                closeDist = t;
                closest = n;
            }
        }
        
        return closest != null ? new FocusQuery(closest, closeDist) : null;
    }
    
    @Override
    public void updateEdge()
    {
    }

    @Override
    public void render()
    {
        //Lines
        GL11.glLineWidth(1.0f);
        GL11.glColor3f(0.0f, 0.0f, 0.0f);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (WireNode n = first; n != null; n = n.next) GraphicsUtil.glVertex(n.pos);
        GL11.glEnd();
        
        //Arrow
        {
            Vec3 d = Vec3.sub(last.prev.pos, last.pos).normalize();
            Vec3 n = Vec3.sub(Main.instance().getCamera().pos, last.pos).cross(d).normalize();

            GL11.glBegin(GL11.GL_TRIANGLES);
            GraphicsUtil.glVertex(Vec3.madd(last.pos, n, ARROW_WIDTH).madd(d, ARROW_LENGTH));
            GraphicsUtil.glVertex(last.pos);
            GraphicsUtil.glVertex(Vec3.madd(last.pos, n, -ARROW_WIDTH).madd(d, ARROW_LENGTH));
            GL11.glEnd();
        }
        
        //Nodes
        GL11.glPointSize(4.0f);
        GL11.glBegin(GL11.GL_POINTS);
        for (WireNode n = first; n != null; n = n.next)
        {
            if (Main.instance().getFocus() == n) GL11.glColor3f(0.75f, 0.75f, 1.0f);
            else GL11.glColor3f(0.0f, 0.0f, 0.0f);
            GraphicsUtil.glVertex(n.pos);
        }
        GL11.glEnd();
    }
    
    public class WireNode implements Focusable
    {
        public final Vec3 pos = new Vec3();
        
        private WireNode prev, next;
        
        public Wire getWire()
        {
            return Wire.this;
        }
        
        public boolean isFirst()
        {
            return first == this;
        }
        
        public boolean isLast()
        {
            return last == this;
        }
        
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

        public void delete()
        {
            if (prev == null)
            {
                first = next;
                if (in != null) disconnectIn();
            }
            else prev.next = next;
            
            if (next == null)
            {
                last = prev;
                if (out != null) disconnectOut();
            }
            else next.prev = prev;
        }
        
        @Override
        public void onMouseButton(FocusQuery query, int button, int action, int mods)
        {
            if (action == GLFW.GLFW_PRESS && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
                Main.instance().setState(new WireDragState(this));
        }
    }
}
