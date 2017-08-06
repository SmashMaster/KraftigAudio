package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Panel;
import org.lwjgl.opengl.GL11;

public class TextBox implements UIElement
{
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    private final Alignment internalAlign;
    private Label label;
    private final float fontSize;
    
    public TextBox(Vec2 radius, Alignment internalAlign, float fontSize)
    {
        this.radius.set(radius);
        this.internalAlign = internalAlign;
        this.fontSize = fontSize;
    }
    
    public TextBox setText(String text)
    {
        label = text != null ? new Label(text, fontSize) : null;
        setPos(pos, Alignment.C);
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
    public TextBox setPos(Vec2 pos, Alignment align)
    {
        align.align(pos, radius, this.pos);
        if (label != null)
        {
            float x = Util.lerp(this.pos.x - radius.x, this.pos.x + radius.x, -internalAlign.x*0.5f + 0.5f);
            float y = Util.lerp(this.pos.y - radius.y, this.pos.y + radius.y, -internalAlign.y*0.5f + 0.5f);
            label.setPos(new Vec2(x, y), internalAlign);
        }
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
        float x0 = pos.x - radius.x, x1 = pos.x + radius.x;
        float y0 = pos.y - radius.y, y1 = pos.y + radius.y;
        
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x0, y0);
        GL11.glVertex2f(x0, y1);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y0);
        GL11.glEnd();
        
        if (label != null)
        {
            //Enable stencil writing, disable color writing.
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GL11.glColorMask(false, false, false, false);
            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

            //Draw stencil mask.
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(x0, y0);
            GL11.glVertex2f(x0, y1);
            GL11.glVertex2f(x1, y1);
            GL11.glVertex2f(x1, y0);
            GL11.glEnd();

            //Disable stencil writing, enable color writing.
            GL11.glColorMask(true, true, true, true);
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            label.render(alpha);

            //Disable stencil.
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            
            //Clear stencil buffer.
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        }
    }
}
