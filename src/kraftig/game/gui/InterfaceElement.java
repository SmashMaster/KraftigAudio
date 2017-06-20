package kraftig.game.gui;

import com.samrj.devil.math.Vec2;
import kraftig.game.InteractionMode;

public interface InterfaceElement
{
    public InteractionMode onClick(Vec2 p);
    public void render(float alpha);
}
