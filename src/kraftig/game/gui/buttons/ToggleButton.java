package kraftig.game.gui.buttons;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Panel;
import kraftig.game.gui.UIElement;
import kraftig.game.gui.UIFocusQuery;
import org.lwjgl.glfw.GLFW;

public abstract class ToggleButton implements UIElement
{
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    private Runnable callback;
    
    public ToggleButton(Vec2 radius)
    {
        this.radius.set(radius);
    }
    
    public ToggleButton onClick(Runnable callback)
    {
        this.callback = callback;
        return this;
    }
    
    public ToggleButton click()
    {
        if (callback != null) callback.run();
        return this;
    }
    
    @Override
    public final Vec2 getPos()
    {
        return new Vec2(pos);
    }
    
    @Override
    public final Vec2 getRadius()
    {
        return new Vec2(radius);
    }
    
    @Override
    public final ToggleButton setPos(Vec2 pos, Alignment align)
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
        if (p.x < pos.x - radius.x || p.x > pos.x + radius.x) return null;
        if (p.y < pos.y - radius.y || p.y > pos.y + radius.y) return null;
        return new UIFocusQuery(this, dist, p);
    }
    
    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
        if (action == GLFW.GLFW_PRESS && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) click();
    }
    
    @Override
    public void delete()
    {
    }
}
