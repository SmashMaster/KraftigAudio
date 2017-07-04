package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import kraftig.game.FocusQuery;
import kraftig.game.Focusable;

public interface UIElement extends Focusable
{
    public void updateMatrix(Mat4 matrix);
    public UIFocusQuery checkFocus(float dist, Vec2 p);
    
    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods);
    public void render(float alpha);
}
