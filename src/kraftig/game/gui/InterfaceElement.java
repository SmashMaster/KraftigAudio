package kraftig.game.gui;

import com.samrj.devil.math.Vec2;
import kraftig.game.InteractionState;

public interface InterfaceElement
{
    public InteractionState onClick(Vec2 p);
    public void render(float alpha);
}
