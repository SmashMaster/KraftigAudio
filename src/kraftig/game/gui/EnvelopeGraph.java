package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Panel;
import kraftig.game.audio.Envelope;
import org.lwjgl.opengl.GL11;

public class EnvelopeGraph implements UIElement
{
    private static final float GRAPH_LENGTH = 10.0f;
    private static final float HOLD_TIME = 1.0f;
    private static final int SEGMENTS = 128;
    
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    
    private final float[] env = new float[SEGMENTS];
    
    public EnvelopeGraph(Vec2 radius)
    {
        this.radius.set(radius);
    }
    
    public EnvelopeGraph update(Envelope envelope)
    {
        double h = envelope.attack + envelope.hold + envelope.decay + HOLD_TIME;
        
        for (int i=0; i<SEGMENTS; i++)
        {
            double t = i*GRAPH_LENGTH/(SEGMENTS - 1.0);
            double r = (t > h) ? t - h : Double.NaN;
            env[i] = (float)envelope.evaluate(t, r);
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
    public EnvelopeGraph setPos(Vec2 pos, Alignment align)
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
            float y = env[i]*2.0f - 1.0f;
            GL11.glVertex2f(x, y);
        }
        GL11.glEnd();
        
        GL11.glPopMatrix();
    }
}
