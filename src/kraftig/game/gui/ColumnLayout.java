package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.Arrays;
import java.util.List;
import kraftig.game.FocusQuery;
import kraftig.game.Panel;

public class ColumnLayout implements UIElement
{
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    private final float spacing;
    private final Alignment internalAlign;
    private final List<? extends UIElement> elements;
    
    public ColumnLayout(float spacing, Alignment internalAlign, List<? extends UIElement> elements)
    {
        float w = 0.0f, h = -spacing;
        
        for (UIElement e : elements)
        {
            Vec2 size = e.getRadius();
            w = Math.max(size.x*2.0f, w);
            h += size.y*2.0f + spacing;
        }
        
        radius.set(w*0.5f, Math.max(h*0.5f, 0.0f));
        
        this.spacing = spacing;
        this.internalAlign = Alignment.get(-internalAlign.x, -1.0f);
        this.elements = elements;
    }
    
    public ColumnLayout(float spacing, Alignment internalAlign, UIElement... elements)
    {
        this(spacing, internalAlign, Arrays.asList(elements));
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
    public ColumnLayout setPos(Vec2 pos, Alignment align)
    {
        align.align(pos, radius, this.pos);
        Vec2 p = new Vec2(this.pos.x - internalAlign.x*radius.x, this.pos.y + radius.y);
        
        for (UIElement e : elements)
        {
            e.setPos(p, internalAlign);
            p.y -= spacing + e.getRadius().y*2.0f;
        }
        
        return this;
    }

    @Override
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
    {
        for (UIElement e : elements) e.updateMatrix(matrix, panel, front);
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
    }
}
