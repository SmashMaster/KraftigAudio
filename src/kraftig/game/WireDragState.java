package kraftig.game;

import kraftig.game.Wire.WireNode;
import org.lwjgl.glfw.GLFW;

public class WireDragState implements InteractionState
{
    private WireNode node;
    private final float dist;
    
    public WireDragState(WireNode node)
    {
        this.node = node;
        dist = Main.instance().getCamera().pos.dist(node.pos);
    }
    
    private void update()
    {
        node.pos.set(Main.instance().getCamera().pos);
        node.pos.madd(Main.instance().getMouseDir(), dist);
    }

    @Override
    public boolean canPlayerAim()
    {
        return true;
    }
    
    @Override
    public void onMouseMoved(float x, float y, float dx, float dy)
    {
        update();
    }

    @Override
    public void onMouseButton(int button, int action, int mods)
    {
        if (action != GLFW.GLFW_PRESS) return;
        
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) Main.instance().setDefaultState();
        else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) node = node.makeCorner();
    }

    @Override
    public void step(float dt)
    {
        update();
    }
}
