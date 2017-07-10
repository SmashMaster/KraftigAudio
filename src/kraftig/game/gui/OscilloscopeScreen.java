package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import org.lwjgl.opengl.GL11;

public class OscilloscopeScreen implements UIElement
{
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    
    private float[][] buffer;
    private int sampleCount;
    
    private float brightness;
    
    public OscilloscopeScreen(Vec2 radius)
    {
        this.radius.set(radius);
    }
    
    public OscilloscopeScreen(Vec2 radius, Vec2 pos, Alignment align)
    {
        this(radius);
        setPos(pos, align);
    }
    
    @Override
    public final Vec2 getPos()
    {
        return new Vec2(pos);
    }
    
    @Override
    public final Vec2 getSize()
    {
        return new Vec2(radius);
    }
    
    @Override
    public final OscilloscopeScreen setPos(Vec2 pos, Alignment align)
    {
        align.align(pos, getSize(), this.pos);
        return this;
    }
    
    public void process(float[][] buffer, int samples)
    {
        this.buffer = buffer;
        sampleCount = samples;
    }
    
    public void setBrightness(float b)
    {
        brightness = b;
    }
    
    @Override
    public void updateMatrix(Mat4 matrix)
    {
    }

    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        return null;
    }

    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
    }

    @Override
    public void delete()
    {
    }
    
    private static float clamp(float x)
    {
        return Util.clamp(x, -1.0f, 1.0f);
    }

    @Override
    public void render(float alpha)
    {
        GL11.glColor4f(0.0f, 0.0f, 0.0f, alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(pos.x - radius.x, pos.y - radius.y);
        GL11.glVertex2f(pos.x - radius.x, pos.y + radius.y);
        GL11.glVertex2f(pos.x + radius.x, pos.y + radius.y);
        GL11.glVertex2f(pos.x + radius.x, pos.y - radius.y);
        GL11.glEnd();
        
        if (buffer == null) return;
        
//        //Stereo oscilloscope
//        GL11.glLineWidth(1.5f);
//        GL11.glBegin(GL11.GL_LINES);
//        for (int s = 1; s < sampleCount; s++)
//        {
//            float x0 = clamp(buffer[0][s - 1]), x1 = clamp(buffer[0][s]);
//            float y0 = clamp(buffer[1][s - 1]), y1 = clamp(buffer[1][s]);
//            
//            float dx = (x1 - x0), dy = (y1 - y0);
//            float dist = (float)Math.sqrt(dx*dx + dy*dy);
//            float a = (s*brightness)/(sampleCount*(dist + 0.01f));
//            
//            GL11.glColor4f(0.125f, 1.0f, 0.125f, a*alpha);
//            GL11.glVertex2f(x0*radius.x, y0*radius.y);
//            GL11.glVertex2f(x0*radius.x, y1*radius.y);
//        }
//        GL11.glEnd();
        
        //Mono oscilloscope
        GL11.glLineWidth(1.5f);
        GL11.glColor4f(0.125f, 1.0f, 0.125f, alpha);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int s = 0; s < sampleCount; s++)
        {
            float left = clamp(buffer[0][s]), right = clamp(buffer[1][s]);
            float x = (s/(sampleCount - 1.0f))*2.0f - 1.0f;
            
            GL11.glVertex2f(x*radius.x, (left + right)*0.5f*radius.y);
        }
        GL11.glEnd();
    }
}
