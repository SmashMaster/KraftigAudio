package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UI
{
    private final Mat4 matrix = new Mat4();
    private final List<UIElement> elements = new ArrayList<>();
    
    public void updateMatrix(Mat4 matrix)
    {
        this.matrix.set(matrix);
        for (UIElement e : elements) e.updateMatrix(matrix);
    }
    
    public UI add(UIElement e)
    {
        elements.add(e);
        e.updateMatrix(matrix);
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
    
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        for (UIElement e : elements)
        {
            UIFocusQuery query = e.checkFocus(dist, p);
            if (query != null) return query;
        }
        return null;
    }
    
    public void render(float alpha)
    {
        for (UIElement e : elements) e.render(alpha);
    }
}
