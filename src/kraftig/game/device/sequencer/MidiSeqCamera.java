package kraftig.game.device.sequencer;

import com.samrj.devil.geo2d.AAB2;
import com.samrj.devil.io.MemStack;
import com.samrj.devil.math.Vec2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import kraftig.game.InteractionState;
import kraftig.game.Main;
import kraftig.game.util.Savable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class MidiSeqCamera implements Savable
{
    private final MidiSequencer seq;
    private final Axis x = new Axis(1.0f/256.0f, 2.0f), y = new Axis(1.0f/256.0f, 1.0f/4.0f);
    private final Deque<SmoothZoom> zooms = new ArrayDeque<>();
    
    public MidiSeqCamera(MidiSequencer seq)
    {
        this.seq = seq;
        
        y.pos = 60.5f;
        x.scale = 1.0f/8.0f; x.tgtScale = x.scale;
        y.scale = 1.0f/16.0f; y.tgtScale = y.scale;
    }
    
    public Vec2 toWorld(Vec2 v)
    {
        return new Vec2(v.x/x.scale + x.pos, v.y/y.scale + y.pos);
    }
    
    public Vec2 toScreen(Vec2 v)
    {
        return new Vec2((v.x - x.pos)*x.scale, (v.y - y.pos)*y.scale);
    }
    
    public void zoomX(float factor)
    {
        zooms.add(new SmoothZoom(x, factor, 0.25f));
    }
    
    public void zoomY(float factor)
    {
        zooms.add(new SmoothZoom(y, factor, 0.25f));
    }
    
    public Vec2 getScale()
    {
        return new Vec2(x.scale, y.scale);
    }
    
    public Vec2 getPos()
    {
        return new Vec2(x.pos, y.pos);
    }
    
    public AAB2 getBounds()
    {
        return new AAB2(x.pos - 0.5f/x.scale, x.pos + 0.5f/x.scale,
                        y.pos - 0.5f/y.scale, y.pos + 0.5f/y.scale);
    }
    
    public void drag(boolean dragX, boolean dragY)
    {
        Main.instance().setState(new InteractionState()
        {
            private Vec2 prev = seq.getMouse();
            
            private void update()
            {
                Vec2 pos = seq.getMouse();
                if (pos != null && prev != null)
                {
                    if (dragX) x.pos += (prev.x - pos.x)/x.scale;
                    if (dragY) y.pos += (prev.y - pos.y)/y.scale;
                }
                prev = pos;
            }
            
            @Override
            public boolean canPlayerAim()
            {
                return true;
            }
            
            @Override
            public void onMouseMoved(float x, float y, float dx, float dy)
            {
                update();
            }

            @Override
            public void onMouseButton(int button, int action, int mods)
            {
                if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && action == GLFW.GLFW_RELEASE)
                    Main.instance().setDefaultState();
            }

            @Override
            public void step(float dt)
            {
                update();
            }
        });
    }
    
    public void multMatrix()
    {
        float w = 2.0f*x.scale;
        float h = 2.0f*y.scale;
        long address = MemStack.wrapf(
                w, 0f, 0f, 0f,
                0f, h, 0f, 0f,
                0f, 0f, 0f, 0f,
                -x.pos*w, -y.pos*h, 0f, 1f);
        GL11.nglMultMatrixf(address);
        MemStack.pop();
    }
    
    public void multXMatrix()
    {
        float w = 2.0f*x.scale;
        long address = MemStack.wrapf(
                w, 0f, 0f, 0f,
                0f, 1.0f, 0f, 0f,
                0f, 0f, 0f, 0f,
                -x.pos*w, 0.0f, 0f, 1f);
        GL11.nglMultMatrixf(address);
        MemStack.pop();
    }
    
    public void multYMatrix()
    {
        float h = 2.0f*y.scale;
        long address = MemStack.wrapf(
                1.0f, 0f, 0f, 0f,
                0f, h, 0f, 0f,
                0f, 0f, 0f, 0f,
                0.0f, -y.pos*h, 0f, 1f);
        GL11.nglMultMatrixf(address);
        MemStack.pop();
    }
    
    public void step(float dt)
    {
        for (SmoothZoom zoom : zooms) zoom.step(dt);
        while (!zooms.isEmpty() && zooms.peek().finished) zooms.pop();
        
        if (zooms.isEmpty())
        {
            x.tgtScale = x.scale;
            y.tgtScale = y.scale;
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        out.writeFloat(x.pos);
        out.writeFloat(x.scale);
        out.writeFloat(y.pos);
        out.writeFloat(y.scale);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        x.pos = in.readFloat();
        x.scale = in.readFloat();
        x.tgtScale = x.scale;
        y.pos = in.readFloat();
        y.scale = in.readFloat();
        y.tgtScale = y.scale;
    }
    // </editor-fold>
    
    private class Axis
    {
        private final float minZoom, maxZoom;
        private float scale, tgtScale, pos;
        
        private Axis(float minZoom, float maxZoom)
        {
            this.minZoom = minZoom;
            this.maxZoom = maxZoom;
        }
    }
    
    private class SmoothZoom
    {
        private final Axis s;
        private final float factor, time;
        
        private float t, st;
        private boolean finished;
        
        private SmoothZoom(Axis s, float factor, float time)
        {
            this.s = s;
            
            if (s.tgtScale*factor > s.maxZoom) factor = s.maxZoom/s.tgtScale;
            else if (s.tgtScale*factor < s.minZoom) factor = s.minZoom/s.tgtScale;
            
            if (factor == 1.0f) finished = true;
            s.tgtScale *= factor;
            
            this.factor = factor;
            this.time = time;
        }
        
        public void step(float dt)
        {
            if (finished) return;
            
            if (t + dt >= time)
            {
                dt = time - t;
                finished = true;
            }
            
            float st0 = st;
            t += dt;
            st = (1.0f - (float)Math.cos(t*Math.PI/time))*time*0.5f;
            float sdt = st - st0;
            
            Vec2 pos = seq.getMouse();
            if (pos != null)
            {
                Vec2 oldPos = toWorld(pos);
                s.scale *= Math.pow(factor, sdt/time);
                Vec2 newPos = toWorld(pos);

                if (s == x) s.pos += oldPos.x - newPos.x;
                else if (s == y) s.pos += oldPos.y - newPos.y;
                else throw new IllegalStateException();
            }
        }
    }
}
