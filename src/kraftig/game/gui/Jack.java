package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.ui.Alignment;
import kraftig.game.Main;
import kraftig.game.Wire;
import org.lwjgl.opengl.GL11;

public abstract class Jack implements UIElement
{
    protected static final int SEGMENTS = 32;
    protected static final float DT = 8.0f/(SEGMENTS - 1);
    protected static final float T_END = 8.0f + DT*0.5f;
    protected static final float RADIUS = 16.0f;
    protected static final float RADIUS_SQ = RADIUS*RADIUS;
    protected static final float RADIUS_HALF = RADIUS/2.0f;
    protected static final float WIRE_OFFSET = 8.0f;
    
    private final Vec2 pos = new Vec2();
    private final Mat4 matrix = new Mat4();
    
    private Wire wire;
    
    public Jack(Vec2 pos, Alignment align)
    {
        Vec2 av = new Vec2(align.x, align.y).mult(RADIUS);
        this.pos.set(pos).add(av);
    }
    
    public final void connect(Wire wire)
    {
        if (wire == null) throw new NullPointerException();
        if (this.wire != null) throw new IllegalStateException();
        this.wire = wire;
        updateMatrix(matrix);
    }
    
    public final void disconnect(Wire wire)
    {
        if (wire == null) throw new NullPointerException();
        if (this.wire != wire) throw new IllegalStateException();
        this.wire = null;
    }
    
    public final boolean hasWire()
    {
        return wire != null;
    }
    
    public final boolean hasLiveWire()
    {
        return hasWire() && wire.isLive();
    }
    
    public final Wire getWire()
    {
        return wire;
    }
    
    public final Vec3 getWirePos()
    {
        return new Vec3(pos.x, pos.y, WIRE_OFFSET).mult(matrix);
    }
    
    @Override
    public void updateMatrix(Mat4 matrix)
    {
        this.matrix.set(matrix);
    }
    
    @Override
    public final UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        if (wire != null) return null;
        else if (p.squareDist(pos) <= RADIUS_SQ) return new UIFocusQuery(this, dist, p);
        else return null;
    }
    
    public abstract void renderSymbol();
    
    @Override
    public final void render(float alpha)
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
        renderSymbol();
        GL11.glPopMatrix();
    }
}
