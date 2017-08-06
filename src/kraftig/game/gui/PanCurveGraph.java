package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Panel;
import org.lwjgl.opengl.GL11;

public class PanCurveGraph implements UIElement
{
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    
    private float fade, power;
    
    public PanCurveGraph(Vec2 radius)
    {
        this.radius.set(radius);
    }
    
    public PanCurveGraph update(float fade, float power)
    {
        this.fade = fade;
        this.power = power;
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
    public PanCurveGraph setPos(Vec2 pos, Alignment align)
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
        GL11.glVertex2f(-1.0f, power - 0.5f);
        GL11.glVertex2f(0.0f, 1.0f - 0.5f);
        GL11.glVertex2f(1.0f, power - 0.5f);
        GL11.glEnd();
        
        GL11.glPopMatrix();
    }
}
