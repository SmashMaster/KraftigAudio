package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Panel;
import org.lwjgl.opengl.GL11;

public class CrossfadeCurveGraph implements UIElement
{
    private static final int SEGMENTS = 64;
    
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    
    private float fade;
    
    private final float[] curve = new float[SEGMENTS];
    
    public CrossfadeCurveGraph(Vec2 radius)
    {
        this.radius.set(radius);
    }
    
    public CrossfadeCurveGraph update(float fade, float power)
    {
        this.fade = fade;
        for (int i=0; i<SEGMENTS; i++)
            curve[i] = (float)Math.pow(i/(SEGMENTS - 1.0), power);
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
    public CrossfadeCurveGraph setPos(Vec2 pos, Alignment align)
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
        
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(fade*2.0f - 1.0f, -1.0f);
        GL11.glVertex2f(fade*2.0f - 1.0f, 1.0f);
        GL11.glEnd();
        
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i=0; i<SEGMENTS; i++)
        {
            float x = i*2.0f/(SEGMENTS - 1.0f) - 1.0f;
            float y = curve[i]*2.0f - 1.0f;
            GL11.glVertex2f(x, y);
        }
        GL11.glEnd();
        
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i=0; i<SEGMENTS; i++)
        {
            float x = 1.0f - i*2.0f/(SEGMENTS - 1.0f);
            float y = curve[i]*2.0f - 1.0f;
            GL11.glVertex2f(x, y);
        }
        GL11.glEnd();
        
        GL11.glPopMatrix();
    }
}
