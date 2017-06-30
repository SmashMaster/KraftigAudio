package kraftig.game;

import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.graphics.GraphicsUtil;
import com.samrj.devil.math.Vec3;
import kraftig.game.gui.Jack;
import org.lwjgl.opengl.GL11;

public class Wire implements Drawable
{
    public final Vec3 pos = new Vec3();
    
    private Jack start, end;
    private Wire prev, next;
    
    public Wire attachNext()
    {
        if (next != null) throw new IllegalStateException();
        next = new Wire();
        next.prev = this;
        next.start = start;
        return next;
    }
    
    @Override
    public void updateEdge(Camera3D camera)
    {
    }

    @Override
    public void render(Camera3D camera, float alpha)
    {
        GL11.glColor4f(0.0f, 0.0f, 0.0f, alpha);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (Wire n = this; n != null; n = n.next) GraphicsUtil.glVertex(n.pos);
        GL11.glEnd();
    }
}
