package kraftig.game.gui;

import com.samrj.devil.math.Vec2;
import kraftig.game.FocusQuery;
import kraftig.game.Focusable;

public class UIFocusQuery extends FocusQuery
{
    public final Vec2 p;
    
    public UIFocusQuery(Focusable focus, float dist, Vec2 p)
    {
        super(focus, dist);
        
        this.p = p;
    }
}
