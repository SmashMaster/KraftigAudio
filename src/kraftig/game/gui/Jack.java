package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Main;
import kraftig.game.Wire;
import kraftig.game.Wire.WireNode;
import kraftig.game.WireDragState;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Jack implements UIElement
{
    public enum Type
    {
        INPUT, OUTPUT;
    }
    
    private static final int SEGMENTS = 32;
    private static final float DT = 8.0f/(SEGMENTS - 1);
    private static final float T_END = 8.0f + DT*0.5f;
    private static final float RADIUS = 16.0f;
    private static final float RADIUS_SQ = RADIUS*RADIUS;
    private static final float RADIUS_HALF = RADIUS/2.0f;
    private static final float WIRE_OFFSET = 8.0f;
    
    private final Type type;
    private final Vec2 pos = new Vec2();
    private final Mat4 matrix = new Mat4();
    
    private Wire wire;
    
    public Jack(Vec2 pos, Alignment align, Type type)
    {
        if (type == null) throw new NullPointerException();
        this.type = type;
        Vec2 av = new Vec2(align.x, align.y).mult(RADIUS);
        this.pos.set(pos).add(av);
    }
    
    public Type getType()
    {
        return type;
    }
    
    public void connect(Wire wire)
    {
        if (wire == null) throw new NullPointerException();
        if (this.wire != null) throw new IllegalStateException();
        this.wire = wire;
        updateMatrix(matrix);
    }
    
    public void disconnect(Wire wire)
    {
        if (wire == null) throw new NullPointerException();
        if (this.wire != wire) throw new IllegalStateException();
        this.wire = null;
    }
    
    public boolean hasWire()
    {
        return wire != null;
    }
    
    public Vec3 getWirePos()
    {
        return new Vec3(pos.x, pos.y, WIRE_OFFSET).mult(matrix);
    }
    
    @Override
    public void updateMatrix(Mat4 matrix)
    {
        this.matrix.set(matrix);
        if (wire != null) switch (type)
        {
            case INPUT: wire.getLast().pos.set(getWirePos()); break;
            case OUTPUT: wire.getFirst().pos.set(getWirePos()); break;
        }
    }
    
    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        if (wire != null) return null;
        else if (p.squareDist(pos) <= RADIUS_SQ) return new UIFocusQuery(this, dist, p);
        else return null;
    }

    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
        if (action != GLFW.GLFW_PRESS || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        
        if (wire == null)
        {
            WireNode dragNode;
            
            switch (type)
            {
                case INPUT:
                    new Wire().connectOut(Jack.this);
                    dragNode = wire.getFirst();
                    dragNode.pos.set(wire.getLast().pos);
                    break;
                case OUTPUT:
                    new Wire().connectIn(Jack.this);
                    dragNode = wire.getLast();
                    dragNode.pos.set(wire.getFirst().pos);
                    break;
                default: throw new IllegalArgumentException();
            }
            
            Main.instance().addWire(wire);
            Main.instance().setState(new WireDragState(dragNode));
        }
    }
    
    @Override
    public void delete()
    {
        if (wire != null) switch (type)
        {
            case INPUT: wire.disconnectOut(); break;
            case OUTPUT: wire.disconnectIn(); break;
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public void render(float alpha)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef(pos.x, pos.y, 0.0f);
        
        GL11.glLineWidth(1.0f);
        float color = Main.instance().getFocus() == this ? 0.75f : 1.0f;
        GL11.glColor4f(color, color, 1.0f, alpha*0.5f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (float t = 0.0f; t < T_END; t += DT)
        {
            Vec2 p = Util.squareDir(t).normalize().mult(RADIUS);
            GL11.glVertex2f(p.x, p.y);
        }
        GL11.glEnd();
        
        GL11.glColor4f(color, color, 1.0f, alpha);
        switch (type)
        {
            case INPUT:
                GL11.glBegin(GL11.GL_LINES);
                GL11.glVertex2f(0.0f, -RADIUS_HALF);
                GL11.glVertex2f(0.0f, RADIUS_HALF);
                GL11.glEnd();
                break;
            case OUTPUT:
                GL11.glBegin(GL11.GL_LINE_LOOP);
                for (float t = 0.0f; t < T_END; t += DT)
                {
                    Vec2 p = Util.squareDir(t).normalize().mult(RADIUS_HALF);
                    GL11.glVertex2f(p.x, p.y);
                }
                GL11.glEnd();
                break;
        }
        
        GL11.glPopMatrix();
    }
}
