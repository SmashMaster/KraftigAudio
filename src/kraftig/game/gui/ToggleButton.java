package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.function.Consumer;
import kraftig.game.FocusQuery;
import kraftig.game.Main;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class ToggleButton implements UIElement
{
    private static final int SEGMENTS = 32;
    private static final float DT = 8.0f/(SEGMENTS - 1);
    private static final float T_END = 8.0f + DT*0.5f;
    
    private final Vec2 pos = new Vec2();
    private final float radius;
    
    private boolean value = false;
    private Consumer<Boolean> callback;
    
    public ToggleButton(float radius)
    {
        this.radius = radius;
    }
    
    public ToggleButton(float radius, Vec2 pos, Alignment align)
    {
        this(radius);
        setPos(pos, align);
    }
    
    @Override
    public final Vec2 getPos()
    {
        return new Vec2(pos);
    }
    
    @Override
    public final Vec2 getRadius()
    {
        return new Vec2(radius);
    }
    
    @Override
    public final ToggleButton setPos(Vec2 pos, Alignment align)
    {
        align.align(pos, getRadius(), this.pos);
        return this;
    }
    
    public ToggleButton onValueChanged(Consumer<Boolean> callback)
    {
        this.callback = callback;
        callback.accept(value);
        return this;
    }
    
    public ToggleButton setValue(boolean value)
    {
        this.value = value;
        if (callback != null) callback.accept(value);
        return this;
    }
    
    @Override
    public void updateMatrix(Mat4 matrix)
    {
    }

    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        if (p.squareDist(pos) <  radius*radius) return new UIFocusQuery(this, dist, p);
        else return null;
    }

    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
        if (action != GLFW.GLFW_PRESS || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        
        setValue(!value);
    }

    @Override
    public void delete()
    {
    }

    @Override
    public void render(float alpha)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef(pos.x, pos.y, 0.0f);
        
        GL11.glLineWidth(1.0f);
        float color = Main.instance().getFocus() == this ? 0.75f : 1.0f;
        GL11.glColor4f(color, color, 1.0f, alpha);
        GL11.glBegin(value ? GL11.GL_TRIANGLE_FAN : GL11.GL_LINE_LOOP);
        for (float t = 0.0f; t < T_END; t += DT)
        {
            Vec2 p = Util.squareDir(t).normalize().mult(radius);
            GL11.glVertex2f(p.x, p.y);
        }
        GL11.glEnd();
        
        GL11.glPopMatrix();
    }
}
