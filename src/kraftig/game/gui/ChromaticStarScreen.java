package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.audio.FFT;
import kraftig.game.util.CircularBuffer;
import kraftig.game.util.DSPUtil;
import org.lwjgl.opengl.GL11;

public class ChromaticStarScreen implements UIElement
{
    private static final int WINDOW_LENGTH = 8192;
    private static final int HALF_WINDOW = WINDOW_LENGTH/2;
    private static final float BIN_RESOLUTION = Main.SAMPLE_RATE*0.5f/HALF_WINDOW;
    private static final float[][] TWIDDLE_TABLE = FFT.twiddle(WINDOW_LENGTH);
    private static final float[] WINDOW_TABLE = FFT.window(WINDOW_LENGTH);
    
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    
    private final CircularBuffer circle = new CircularBuffer(WINDOW_LENGTH);
    
    private float brightness;
    
    public ChromaticStarScreen(Vec2 radius)
    {
        this.radius.set(radius);
    }
    
    public ChromaticStarScreen(Vec2 radius, Vec2 pos, Alignment align)
    {
        this(radius);
        setPos(pos, align);
    }
    
    public void setBrightness(float b)
    {
        brightness = b;
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
    public final ChromaticStarScreen setPos(Vec2 pos, Alignment align)
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
    
    @Override
    public void render(float alpha)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef(pos.x, pos.y, 0.0f);
        GL11.glScalef(radius.x, radius.y, 1.0f);
        
        //Draw black background.
        GL11.glColor4f(0.0f, 0.0f, 0.0f, alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-1.0f, -1.0f);
        GL11.glVertex2f(-1.0f, 1.0f);
        GL11.glVertex2f(1.0f, 1.0f);
        GL11.glVertex2f(1.0f, -1.0f);
        GL11.glEnd();
        
        //Enable stencil writing, disable color writing.
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glColorMask(false, false, false, false);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

        //Draw stencil mask.
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-1.0f, -1.0f);
        GL11.glVertex2f(-1.0f, 1.0f);
        GL11.glVertex2f(1.0f, 1.0f);
        GL11.glVertex2f(1.0f, -1.0f);
        GL11.glEnd();

        //Disable stencil writing, enable color writing.
        GL11.glColorMask(true, true, true, true);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        
        float[] buffer = new float[WINDOW_LENGTH];
        circle.read(buffer, 0, Math.min(WINDOW_LENGTH, circle.getSize()));
        float[][] fft = FFT.fft(buffer, WINDOW_TABLE, TWIDDLE_TABLE);
        float[] fftAmps = new float[HALF_WINDOW + 1];
        
        //Calculate FFT amplitudes to be interpolated.
        for (int i=0; i<=HALF_WINDOW; i++)
        {
            float real = fft[0][i];
            float imag = fft[1][i];
            fftAmps[i] = (float)Math.sqrt(real*real + imag*imag);
        }
        
        //Draw chromatic star.
        GL11.glLineWidth(8.0f);
        GL11.glBegin(GL11.GL_LINES);
        for (float midi=0.0f; midi<=127.0f; midi += 0.0625f)
        {
            float freq = (float)DSPUtil.freqFromMidi(midi);
            float angle = (midi%12.0f)*Util.PIm2/12.0f;
            float dx = (float)Math.sin(angle);
            float dy = (float)Math.cos(angle);
            
            float index = freq/BIN_RESOLUTION;
            float amplitude = DSPUtil.cubicSamp(fftAmps, index);
            
            float colorF = Util.linstep(36, 108, midi);
            float r = 1.0f - Util.saturate(Util.linstep(0.0f, 0.5f, colorF));
            float g = 1.0f - Math.abs(0.5f - colorF)*2.0f;
            float b = Util.saturate(Util.linstep(0.5f, 1.0f, colorF));
            float a = (float)(Math.pow(midi/127.0f, 2.0)*amplitude*brightness);
            
            GL11.glColor4f(r, g, b, a*alpha);
            GL11.glVertex2f(0.0f, 0.0f);
            GL11.glVertex2f(dx*1.5f, dy*1.5f);
        }
        GL11.glEnd();

//        GL11.glLineWidth(1.0f);
//        GL11.glBegin(GL11.GL_LINE_STRIP);
//        for (float midi=0.0f; midi<=127.0f; midi += 0.25f)
//        {
//            float freq = (float)DSPUtil.freqFromMidi(midi);
//            float angle = (midi%12.0f)*Util.PIm2/12.0f;
//            float dx = (float)Math.sin(angle);
//            float dy = (float)Math.cos(angle);
//            
//            float index = freq/BIN_RESOLUTION;
//            float amplitude = DSPUtil.cubicSamp(fftAmps, index);
//            float rad = amplitude*8.0f/HALF_WINDOW;
//            
//            float colorF = Util.linstep(36, 108, midi);
//            float r = 1.0f - Util.saturate(Util.linstep(0.0f, 0.5f, colorF));
//            float g = 1.0f - Math.abs(0.5f - colorF)*2.0f;
//            float b = Util.saturate(Util.linstep(0.5f, 1.0f, colorF));
//            
//            GL11.glColor4f(r, g, b, alpha);
//            GL11.glVertex2f(dx*rad, dy*rad);
//        }
//        GL11.glEnd();
        
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        //Draw semitone overlay.
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
        GL11.glBegin(GL11.GL_LINES);
        for (int i=0; i<12; i++)
        {
            float angle = i*Util.PIm2/12.0f;
            float dx = (float)Math.sin(angle);
            float dy = (float)Math.cos(angle);

            GL11.glVertex2f(dx*0.75f, dy*0.75f);
            GL11.glVertex2f(dx, dy);

        }
        GL11.glEnd();
        
        GL11.glPopMatrix();
    }
}
