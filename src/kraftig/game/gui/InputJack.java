package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.Wire;
import kraftig.game.Wire.WireNode;
import kraftig.game.WireDragState;
import org.lwjgl.glfw.GLFW;

public abstract class InputJack extends Jack
{
    public InputJack()
    {
        super();
    }
    
    public InputJack(Vec2 pos, Alignment align)
    {
        super(pos, align);
    }
    
    @Override
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
    {
        super.updateMatrix(matrix, panel, front);
        if (hasWire()) getWire().getLast().pos.set(getWirePos());
    }
    
    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
        if (action != GLFW.GLFW_PRESS || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        
        if (!hasWire())
        {
            new Wire().connectOut(this);
            Wire wire = getWire();
            WireNode dragNode = wire.getFirst();
            dragNode.pos.set(getWirePos());
            Main.instance().addWire(wire);
            Main.instance().setState(new WireDragState(dragNode));
        }
    }
    
    @Override
    public void delete()
    {
        if (hasWire()) getWire().disconnectOut();
    }
}
