package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.audio.BiquadFilterKernel.Settings;
import kraftig.game.util.DSPMath;
import org.lwjgl.opengl.GL11;

public class BiquadResponseWindow implements UIElement
{
    private static final float MIN_FREQ = 20.0f, MAX_FREQ = 20000.0f;
    private static final int SEGMENTS = 128;
    
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    
    private final float[] response = new float[SEGMENTS];
    
    public BiquadResponseWindow(Vec2 radius)
    {
        this.radius.set(radius);
    }
    
    public BiquadResponseWindow update(Settings s)
    {
        double num0 = s.b0*s.b0 + s.b1*s.b1 + s.b2*s.b2;
        double num1 = (s.b0*s.b1 + s.b1*s.b2)*2.0f;
        double num2 = s.b0*s.b2*2.0f;
        double den0 = 1.0f + s.a1*s.a1 + s.a2*s.a2;
        double den1 = (s.a1 + s.a1*s.a2)*2.0f;
        double den2 = s.a2*2.0f;
        
        for (int i=0; i<SEGMENTS; i++)
        {
            double freq = DSPMath.experp(MIN_FREQ, MAX_FREQ, i/(SEGMENTS - 1.0));
            double w0 = Math.PI*2.0*freq/Main.SAMPLE_RATE;
            double cos1 = Math.cos(w0);
            double cos2 = Math.cos(w0*2.0);
            
            response[i] = (float)Math.sqrt((num0 + num1*cos1 + num2*cos2)/(den0 + den1*cos1 + den2*cos2));
        }
        
        return this;
    }

    @Override
    public Vec2 getPos()
    {
        return new Vec2(pos);
    }

    @Override
    public Vec2 getRadius()
    {
        return new Vec2(radius);
    }

    @Override
    public BiquadResponseWindow setPos(Vec2 pos, Alignment align)
    {
        align.align(pos, radius, this.pos);
        return this;
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
        
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(-1.0f, -1.0f);
        GL11.glVertex2f(-1.0f, 1.0f);
        GL11.glVertex2f(1.0f, 1.0f);
        GL11.glVertex2f(1.0f, -1.0f);
        GL11.glEnd();
        
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i=0; i<SEGMENTS; i++)
        {
            float x = i*2.0f/(SEGMENTS - 1.0f) - 1.0f;
            float y = Math.min(response[i] - 1.0f, 1.0f);
            GL11.glVertex2f(x, y);
        }
        GL11.glEnd();
        
        GL11.glPopMatrix();
    }
}
