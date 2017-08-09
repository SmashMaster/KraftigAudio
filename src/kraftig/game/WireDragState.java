package kraftig.game;

import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.math.Vec3;
import kraftig.game.Wire.WireNode;
import kraftig.game.gui.InputJack;
import kraftig.game.gui.Jack;
import kraftig.game.gui.OutputJack;
import org.lwjgl.glfw.GLFW;

public class WireDragState implements InteractionState
{
    private WireNode node;
    private final float dist;
    
    private Jack connectJack;
    
    public WireDragState(WireNode node)
    {
        this.node = node;
        
        Camera3D camera = Main.instance().getCamera();
        Vec3 fwd = new Vec3(camera.forward.x, 0.0f, camera.forward.z).div(camera.up.y);
        dist = Vec3.sub(node.pos, camera.pos).dot(fwd);
        
        Wire wire = node.getWire();
        if (node.isFirst() && wire.getIn() != null) wire.disconnectIn();
        if (node.isLast() && wire.getOut() != null) wire.disconnectOut();
    }
    
    private void update()
    {
        if (!node.isCorner())
        {
            connectJack = null;
            
            Focusable focus = Main.instance().focusStream()
                    .filter(f -> f.focus != node)
                    .reduce((a, b) -> a.dist < b.dist ? a : b)
                    .map(f -> f.focus)
                    .orElse(null);

            if (focus instanceof Jack)
            {
                Jack jack = (Jack)focus;
                if (!jack.hasWire() && node.canConnect(jack))
                {
                    connectJack = jack;
                    node.pos.set(jack.getWirePos());
                }
            }
        }
        
        if (connectJack == null)
        {
            node.pos.set(Main.instance().getCamera().pos);
            node.pos.madd(Main.instance().getMouseDir(), dist);
        }
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
        
        if (connectJack != null)
        {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                if (node.isLast()) node.getWire().connectOut((InputJack)connectJack);
                else if (node.isFirst()) node.getWire().connectIn((OutputJack)connectJack);
                Main.instance().setDefaultState();
            }
        }
        else
        {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) Main.instance().setDefaultState();
            else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && !node.isCorner())
                node = node.makeCorner();
        }
    }

    @Override
    public void step(float dt)
    {
        update();
    }
}
