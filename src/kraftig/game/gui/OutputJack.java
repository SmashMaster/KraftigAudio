package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.function.Supplier;
import kraftig.game.FocusQuery;
import kraftig.game.Main;
import kraftig.game.Wire;
import kraftig.game.Wire.WireNode;
import kraftig.game.WireDragState;
import kraftig.game.device.Device;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class OutputJack extends Jack
{
    private final Device device;
    private final Supplier<float[][]> bufferSupplier;
    
    public OutputJack(Device device, Supplier<float[][]> bufferSupplier)
    {
        super();
        if (device == null || bufferSupplier == null) throw new NullPointerException();
        this.device = device;
        this.bufferSupplier = bufferSupplier;
    }
    
    public OutputJack(Device device, Supplier<float[][]> bufferSupplier, Vec2 pos, Alignment align)
    {
        this(device, bufferSupplier);
        setPos(pos, align);
    }
    
    public OutputJack(Device device, float[][] buffer)
    {
        this(device, () -> buffer);
    }
    
    public OutputJack(Device device, float[][] buffer, Vec2 pos, Alignment align)
    {
        this(device, () -> buffer, pos, align);
    }
    
    public Device getDevice()
    {
        return device;
    }
    
    public float[][] getBuffer()
    {
        return bufferSupplier.get();
    }
    
    @Override
    public void updateMatrix(Mat4 matrix)
    {
        super.updateMatrix(matrix);
        if (hasWire()) getWire().getFirst().pos.set(getWirePos());
    }
    
    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
        if (action != GLFW.GLFW_PRESS || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        
        if (!hasWire())
        {
            new Wire().connectIn(this);
            Wire wire = getWire();
            WireNode dragNode = wire.getLast();
            dragNode.pos.set(wire.getFirst().pos);
            Main.instance().addWire(wire);
            Main.instance().setState(new WireDragState(dragNode));
        }
    }
    
    @Override
    public void delete()
    {
        if (hasWire()) getWire().disconnectIn();
    }
    
    @Override
    public void renderSymbol()
    {
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (float t = 0.0f; t < T_END; t += DT)
        {
            Vec2 p = Util.squareDir(t).normalize().mult(RADIUS_HALF);
            GL11.glVertex2f(p.x, p.y);
        }
        GL11.glEnd();
    }
}
