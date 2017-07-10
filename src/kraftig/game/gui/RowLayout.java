package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import org.lwjgl.opengl.GL11;

public class RowLayout implements UIElement
{
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    private final float spacing;
    private final Alignment internalAlign;
    private final UIElement[] elements;
    
    public RowLayout(float spacing, Alignment internalAlign, UIElement... elements)
    {
        float w = -spacing, h = 0.0f;
        
        for (UIElement e : elements)
        {
            Vec2 size = e.getSize();
            w += size.x*2.0f + spacing;
            h = Math.max(size.y*2.0f, h);
        }
        
        radius.set(Math.max(w*0.5f, 0.0f), h*0.5f);
        
        this.spacing = spacing;
        this.internalAlign = Alignment.get(1.0f, -internalAlign.y);
        this.elements = elements;
    }
    
    @Override
    public Vec2 getPos()
    {
        return new Vec2();
    }

    @Override
    public Vec2 getSize()
    {
        return new Vec2(radius);
    }

    @Override
    public RowLayout setPos(Vec2 pos, Alignment align)
    {
        align.align(pos, radius, this.pos);
        Vec2 p = new Vec2(this.pos.x - radius.x, this.pos.y - internalAlign.y*radius.y);
        
        for (UIElement e : elements)
        {
            e.setPos(p, internalAlign);
            p.x += spacing + e.getSize().x*2.0f;
        }
        
        return this;
    }

    @Override
    public void updateMatrix(Mat4 matrix)
    {
        for (UIElement e : elements) e.updateMatrix(matrix);
    }

    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        for (UIElement e : elements)
        {
            UIFocusQuery query = e.checkFocus(dist, p);
            if (query != null) return query;
        }
        return null;
    }

    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
    }

    @Override
    public void delete()
    {
        for (UIElement e : elements) e.delete();
    }

    @Override
    public void render(float alpha)
    {
        for (UIElement e : elements) e.render(alpha);
        
//        GL11.glLineWidth(1.0f);
//        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
//        GL11.glBegin(GL11.GL_LINE_LOOP);
//        GL11.glVertex2f(pos.x - radius.x, pos.y - radius.y);
//        GL11.glVertex2f(pos.x - radius.x, pos.y + radius.y);
//        GL11.glVertex2f(pos.x + radius.x, pos.y + radius.y);
//        GL11.glVertex2f(pos.x + radius.x, pos.y - radius.y);
//        GL11.glEnd();
    }
}
