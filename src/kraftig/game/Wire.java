package kraftig.game;

import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.graphics.GraphicsUtil;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import java.util.ArrayList;
import java.util.List;
import kraftig.game.gui.InputJack;
import kraftig.game.gui.Jack;
import kraftig.game.gui.OutputJack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Wire
{
    private static final float FOCUS_ANG_RADIUS = Util.toRadians(1.0f);
    private static final float FOCUS_SIN = (float)Math.sin(FOCUS_ANG_RADIUS);
    private static final float FOCUS_SIN_SQ = FOCUS_SIN*FOCUS_SIN;
    private static final float ARROW_LENGTH = 1.0f/48.0f;
    private static final float ARROW_WIDTH = 1.0f/192.0f;
    
    private OutputJack in;
    private InputJack out;
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
    
    public void connectIn(OutputJack jack)
    {
        if (jack == null) throw new NullPointerException();
        if (in != null) throw new IllegalStateException();
        in = jack;
        jack.connect(this);
    }
    
    public OutputJack getIn()
    {
        return in;
    }
    
    public void disconnectIn()
    {
        if (in == null) throw new IllegalStateException();
        OutputJack j = in;
        in = null;
        j.disconnect(this);
    }
    
    public WireNode getLast()
    {
        return last;
    }
    
    public void connectOut(InputJack jack)
    {
        if (jack == null) throw new NullPointerException();
        if (out != null) throw new IllegalStateException();
        out = jack;
        jack.connect(this);
    }
    
    public InputJack getOut()
    {
        return out;
    }
    
    public void disconnectOut()
    {
        if (out == null) throw new IllegalStateException();
        InputJack j = out;
        out = null;
        j.disconnect(this);
    }
    
    public boolean isDegenerate()
    {
        return first == last;
    }
    
    public boolean isLive()
    {
        return in != null && out != null;
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
        
        return closest != null ? new FocusQuery(closest, pos.dist(closest.pos)) : null;
    }
    
    public List<WireSplit> updateSplits(List<Panel> panels)
    {
        ArrayList<WireSplit> splits = new ArrayList<>();
        
        for (WireNode a = first, b = first.next; b != null; a = b, b = b.next)
        {
            WireSplit.Type type = b == last ? WireSplit.Type.END : WireSplit.Type.REGULAR;
            splits.add(new WireSplit(type, a, b));
        }
        
        return splits;
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
        
        public boolean canConnect(Jack jack)
        {
            if (first == this) return (jack instanceof OutputJack) && (out == null || out.canConnect(jack));
            else if (last == this) return (jack instanceof InputJack) && (in == null || in.canConnect(jack));
            else return false;
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
    
    public static class WireSplit implements Drawable
    {
        public enum Type
        {
            REGULAR, SPLIT, END;
        }
        
        public final WireNode a, b;
        private final Type type;
        
        public final Vec3 ab;
        public final float abLen;
        
        public final Vec2 ea, eb;
        public final Vec2 eab;
        public final Vec2 eaCam, ebCam;
        
        private WireSplit(Type type, WireNode a, WireNode b)
        {
            this.type = type;
            this.a = a;
            this.b = b;
            
            ab = Vec3.sub(b.pos, a.pos);
            abLen = ab.length();
            ab.div(abLen);
            
            ea = new Vec2(a.pos.x, a.pos.z);
            eb = new Vec2(b.pos.x, b.pos.z);
            eab = Vec2.sub(ea, eb);
            
            Camera3D camera = Main.instance().getCamera();
            Vec2 cam = new Vec2(camera.pos.x, camera.pos.z);
            eaCam = Vec2.sub(cam, ea);
            ebCam = Vec2.sub(cam, eb);
        }
        
        @Override
        public float edgeRayHit(Vec2 p, Vec2 d)
        {
            //Calculate hit position and return zero if missed.
            Vec2 pa = Vec2.sub(p, ea);
            float t = (d.x*pa.y - d.y*pa.x)/(d.y*eab.x - d.x*eab.y);
            if (t < 0.0f || t > 1.0f) return 0.0f;

            //Return direction of hit.
            Vec2 dr = Vec2.madd(pa, eab, t);
            return dr.dot(d);
        }
        
        @Override
        public float getY()
        {
            return (a.pos.y + b.pos.y)*0.5f;
        }
        
        @Override
        public float getHeight()
        {
            return (b.pos.y - a.pos.y)*0.5f;
        }
        
        @Override
        public void render()
        {
            //Line
            GL11.glLineWidth(1.0f);
            GL11.glColor3f(0.0f, 0.0f, 0.0f);
            GL11.glBegin(GL11.GL_LINES);
            GraphicsUtil.glVertex(a.pos);
            GraphicsUtil.glVertex(b.pos);
            GL11.glEnd();
            
            //Arrow
            if (type == Type.END && abLen > ARROW_LENGTH)
            {
                Vec3 n = Vec3.sub(Main.instance().getCamera().pos, b.pos).cross(ab).normalize();

                GL11.glBegin(GL11.GL_TRIANGLES);
                GraphicsUtil.glVertex(Vec3.madd(b.pos, n, ARROW_WIDTH).madd(ab, -ARROW_LENGTH));
                GraphicsUtil.glVertex(b.pos);
                GraphicsUtil.glVertex(Vec3.madd(b.pos, n, -ARROW_WIDTH).madd(ab, -ARROW_LENGTH));
                GL11.glEnd();
            }
            
            //Point
            GL11.glPointSize(4.0f);
            GL11.glBegin(GL11.GL_POINTS);
            if (Main.instance().getFocus() == a) GL11.glColor3f(0.75f, 0.75f, 1.0f);
            else GL11.glColor3f(0.0f, 0.0f, 0.0f);
            GraphicsUtil.glVertex(a.pos);
            
            if (type == Type.END)
            {
                if (Main.instance().getFocus() == b) GL11.glColor3f(0.75f, 0.75f, 1.0f);
                else GL11.glColor3f(0.0f, 0.0f, 0.0f);
                GraphicsUtil.glVertex(b.pos);
            }
            GL11.glEnd();
        }
    }
}
