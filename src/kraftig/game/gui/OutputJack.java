package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Main;
import kraftig.game.Wire;
import kraftig.game.Wire.WireNode;
import kraftig.game.WireDragState;
import kraftig.game.device.Processor;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class OutputJack extends Jack
{
    private final Processor processor;
    
    public OutputJack(Vec2 pos, Alignment align, Processor processor)
    {
        super(pos, align);
        if (processor == null) throw new NullPointerException();
        this.processor = processor;
    }
    
    public void process(float[][] buffer, int samples)
    {
        processor.process(buffer, samples);
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
