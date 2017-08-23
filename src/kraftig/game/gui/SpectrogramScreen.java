package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Panel;
import kraftig.game.audio.FFT;
import kraftig.game.util.CircularBuffer;
import org.lwjgl.opengl.GL11;

public class SpectrogramScreen implements UIElement
{
    private static final int WINDOW_LENGTH = 1024;
    private static final float[][] TWIDDLE_TABLE = FFT.twiddle(WINDOW_LENGTH);
    private static final float[] WINDOW_TABLE = FFT.window(WINDOW_LENGTH);
    
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    
    private final CircularBuffer circle = new CircularBuffer(WINDOW_LENGTH);
    
    public SpectrogramScreen(Vec2 radius)
    {
        this.radius.set(radius);
    }
    
    public SpectrogramScreen(Vec2 radius, Vec2 pos, Alignment align)
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
    public final Vec2 getRadius()
    {
        return new Vec2(radius);
    }
    
    @Override
    public final SpectrogramScreen setPos(Vec2 pos, Alignment align)
    {
        align.align(pos, getRadius(), this.pos);
        return this;
    }
    
    public void process(float[][] buffer, int samples)
    {
        if (buffer == null) circle.clear();
        else for (int i=0; i<samples; i++)
        {
            if (circle.isFull()) circle.poll();
            circle.push((buffer[0][i] + buffer[1][i])*0.5f);
        }
    }
    
    @Override
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
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
        GL11.glPushMatrix();
        GL11.glTranslatef(pos.x, pos.y, 0.0f);
        GL11.glScalef(radius.x, radius.y, 1.0f);
        
        GL11.glColor4f(0.0f, 0.0f, 0.0f, alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-1.0f, -1.0f);
        GL11.glVertex2f(-1.0f, 1.0f);
        GL11.glVertex2f(1.0f, 1.0f);
        GL11.glVertex2f(1.0f, -1.0f);
        GL11.glEnd();
        
        float[] buffer = new float[WINDOW_LENGTH];
        circle.read(buffer, 0, Math.min(WINDOW_LENGTH, circle.getSize()));
        float[][] fft = FFT.fft(buffer, TWIDDLE_TABLE);
        
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i=0; i<WINDOW_LENGTH; i++)
        {
            float real = fft[0][i];
            float imag = fft[1][i];
            
            float mag = (float)Math.sqrt(real*real + imag*imag);
            
            float x = (i/(WINDOW_LENGTH - 1.0f))*2.0f - 1.0f;
            float y = mag/8.0f - 1.0f;
            
            GL11.glVertex2f(x, clamp(y));
        }
        GL11.glEnd();
        
        GL11.glPopMatrix();
    }
}
