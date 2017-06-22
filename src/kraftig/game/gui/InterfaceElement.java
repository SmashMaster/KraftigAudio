package kraftig.game.gui;

import com.samrj.devil.math.Vec2;
import kraftig.game.InteractionState;

public interface InterfaceElement
{
    public InteractionState onMouseButton(Vec2 p, int button, int action, int mods);
    public void render(float alpha);
}
