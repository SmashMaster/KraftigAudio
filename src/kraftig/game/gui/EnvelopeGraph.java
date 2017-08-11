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
    private static final int CURVE_SEGMENTS = 16;
    
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    
    private Envelope envelope = new Envelope();
    
    public EnvelopeGraph(Vec2 radius)
    {
        this.radius.set(radius);
    }
    
    public EnvelopeGraph update(Envelope envelope)
    {
        this.envelope = envelope;
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
    
    private Vec2 curve(int i, float curve)
    {
        float t = i/(CURVE_SEGMENTS - 1.0f);
        float x, y;
        
        if (curve > 1.0f)
        {
            x = (float)Math.pow(t, curve);
            y = t;
        }
        else
        {
            x = t;
            y = (float)Math.pow(t, 1.0f/curve);
        }
        
        return new Vec2(x, y);
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
        
        float subtotal = envelope.attack + envelope.hold + envelope.decay + envelope.release;
        float sustainTime = Math.max(subtotal*0.25f, 1.0f/64.f);
        float total = subtotal + sustainTime;
        
        GL11.glTranslatef(-1.0f, -1.0f, 0.0f);
        GL11.glScalef(2.0f/total, 2.0f, 1.0f);
        
        float holdStart = envelope.attack;
        float decayStart = holdStart + envelope.hold;
        float releaseStart = decayStart + envelope.decay + sustainTime;
        
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i=0; i<CURVE_SEGMENTS; i++)
        {
            Vec2 p = curve(i, envelope.aCurve);
            
            GL11.glVertex2f(p.y*holdStart, p.x);
        }
        for (int i=0; i<CURVE_SEGMENTS; i++)
        {
            Vec2 p = curve(i, envelope.rCurve);
            
            GL11.glVertex2f(decayStart + p.x*envelope.decay, (envelope.sustain - 1.0f)*p.y + 1.0f);
        }
        for (int i=0; i<CURVE_SEGMENTS; i++)
        {
            Vec2 p = curve(i, envelope.rCurve);
            
            GL11.glVertex2f(releaseStart + p.x*envelope.release, envelope.sustain*(1.0f - p.y));
        }
        GL11.glEnd();
        
        GL11.glPopMatrix();
    }
}
