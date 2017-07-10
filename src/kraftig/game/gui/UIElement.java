package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Focusable;

public interface UIElement extends Focusable
{
    public Vec2 getPos();
    public Vec2 getSize();
    public UIElement setPos(Vec2 pos, Alignment align);
    
    public void updateMatrix(Mat4 matrix);
    public UIFocusQuery checkFocus(float dist, Vec2 p);
    
    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods);
    public void delete();
    public void render(float alpha);
}
