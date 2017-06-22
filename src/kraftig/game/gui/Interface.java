package kraftig.game.gui;

import com.samrj.devil.math.Vec2;
import java.util.ArrayList;
import java.util.List;
import kraftig.game.InteractionState;

public class Interface
{
    private final List<InterfaceElement> elements = new ArrayList<>();
    
    public Interface()
    {
    }
    
    public Interface add(InterfaceElement e)
    {
        elements.add(e);
        return this;
    }
    
    public InteractionState onMouseButton(Vec2 p, int button, int action, int mods)
    {
        for (InterfaceElement e : elements)
        {
            InteractionState result = e.onMouseButton(p, button, action, mods);
            if (result != null) return result;
        }
        
        return null;
    }
    
    public void render(float alpha)
    {
        for (InterfaceElement e : elements) e.render(alpha);
    }
}
