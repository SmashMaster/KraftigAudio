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
    
    private int mode = 0;
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
    
    public void setMode(int mode)
    {
        this.mode = mode;
    }
    
    @Override
    public final Vec2 getPos()
    {
        return new Vec2(pos);
    }
    
    @Override
    public final Vec2 getRadius()
    {
        return new Vec2(radius);
    }
    
    @Override
    public final OscilloscopeScreen setPos(Vec2 pos, Alignment align)
    {
        align.align(pos, getRadius(), this.pos);
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
        
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
        
        GL11.glLineWidth(1.5f);
        GL11.glBegin(GL11.GL_LINES);
        for (int s = 1; s < sampleCount; s++)
        {
            float l0 = clamp(buffer[0][s - 1]), l1 = clamp(buffer[0][s]);
            float r0 = clamp(buffer[1][s - 1]), r1 = clamp(buffer[1][s]);

            float x0, x1, y0, y1;

            if (mode == 0) //Mono oscilloscope
            {
                x0 = ((s - 1)/(sampleCount - 1.0f))*2.0f - 1.0f;
                x1 = (s/(sampleCount - 1.0f))*2.0f - 1.0f;
                y0 = (l0 + r0)*0.5f;
                y1 = (l1 + r1)*0.5f;
            }
            else //Stereo oscilloscope
            {
                x0 = l0;
                x1 = l1;
                y0 = r0;
                y1 = r1;
            }

            float dx = (l1 - l0), dy = (r1 - r0);
            float dist = (float)Math.sqrt(dx*dx + dy*dy);
            float a = alpha*brightness/(dist + 0.01f);

            GL11.glColor4f(0.125f*a, 1.0f*a, 0.125f*a, 1.0f);
            GL11.glVertex2f(x0*radius.x, y0*radius.y);
            GL11.glVertex2f(x1*radius.x, y1*radius.y);
        }
        GL11.glEnd();
        
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }
}
