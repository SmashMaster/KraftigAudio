package kraftig.game.gui;

import com.samrj.devil.geo2d.AAB2;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kraftig.game.FocusQuery;
import kraftig.game.Panel;

public class UI
{
    private final Mat4 matrix = new Mat4();
    private Panel panel;
    private boolean front;
    private final List<UIElement> elements = new ArrayList<>();
    
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
    {
        this.matrix.set(matrix);
        this.panel = panel;
        this.front = front;
        for (UIElement e : elements) e.updateMatrix(matrix, panel, front);
    }
    
    public AAB2 getBounds()
    {
        AAB2 result = AAB2.empty();
        for (UIElement e : elements)
            result.expand(AAB2.fromHalfWidth(e.getPos(), e.getRadius()));
        return result;
    }
    
    public UI add(UIElement e)
    {
        elements.add(e);
        e.updateMatrix(matrix, panel, front);
        return this;
    }
    
    public void delete()
    {
        for (UIElement e : elements) e.delete();
    }
    
    public List<UIElement> getAll()
    {
        return Collections.unmodifiableList(elements);
    }
    
    public FocusQuery checkFocus(float dist, Vec2 p)
    {
        for (UIElement e : elements)
        {
            FocusQuery query = e.checkFocus(dist, p);
            if (query != null) return query;
        }
        return null;
    }
    
    public void render(float alpha)
    {
        for (UIElement e : elements) e.render(alpha);
    }
}
