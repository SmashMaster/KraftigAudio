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
    
    public InteractionState onClick(Vec2 p)
    {
        for (InterfaceElement e : elements)
        {
            InteractionState result = e.onClick(p);
            if (result != null) return result;
        }
        
        return null;
    }
    
    public void render(float alpha)
    {
        for (InterfaceElement e : elements) e.render(alpha);
    }
}
