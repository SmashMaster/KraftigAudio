package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import kraftig.game.InteractionState;

public interface UIElement
{
    public void updateMatrix(Mat4 matrix);
    public InteractionState onMouseButton(Vec2 p, int button, int action, int mods);
    public void render(float alpha);
}
