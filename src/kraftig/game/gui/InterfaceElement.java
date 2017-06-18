package kraftig.game.gui;

import com.samrj.devil.math.Vec2;

public interface InterfaceElement
{
    public MouseCapture onClick(Vec2 p);
    public void render(float alpha);
}
