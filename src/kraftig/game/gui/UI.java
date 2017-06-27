package kraftig.game.gui;

import com.samrj.devil.math.Vec2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kraftig.game.InteractionState;

public class UI
{
    private final List<UIElement> elements = new ArrayList<>();
    
    public UI()
    {
    }
    
    public UI add(UIElement e)
    {
        elements.add(e);
        return this;
    }
    
    public List<UIElement> getAll()
    {
        return Collections.unmodifiableList(elements);
    }
    
    public InteractionState onMouseButton(Vec2 p, int button, int action, int mods)
    {
        for (UIElement e : elements)
        {
            InteractionState result = e.onMouseButton(p, button, action, mods);
            if (result != null) return result;
        }
        
        return null;
    }
    
    public void render(float alpha)
    {
        for (UIElement e : elements) e.render(alpha);
    }
}
