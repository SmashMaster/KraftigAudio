package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.function.Consumer;
import kraftig.game.FocusQuery;
import kraftig.game.InteractionState;
import kraftig.game.Main;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Knob implements UIElement
{
    private static final int SEGMENTS = 32;
    private static final float DT = 8.0f/(SEGMENTS - 1);
    private static final float T_END = 8.0f + DT*0.5f;
    private static final float DEAD_ZONE = (float)Math.toRadians(45.0);
    private static final int NOTCHES = 9;
    private static final float NOTCH_ANG0 = DEAD_ZONE + Util.PId2;
    private static final float NOTCH_TOTAL_ANG = (Util.PIm2 - DEAD_ZONE*2.0f);
    private static final float NOTCH_DA = NOTCH_TOTAL_ANG/(NOTCHES - 1.0f);
    private static final float NOTCH_END = NOTCH_ANG0 + NOTCH_TOTAL_ANG + NOTCH_DA*0.5f;
    private static final float NOTCH_LENGTH = 6f;
    private static final float SENSITIVITY = 1.0f/256.0f;
    
    private final Vec2 pos = new Vec2();
    private final float radius;
    
    private float value = 0.0f;
    private Consumer<Float> callback;
    
    public Knob(Vec2 pos, Alignment align, float radius)
    {
        radius -= NOTCH_LENGTH;
        this.pos.set(pos).add(new Vec2(align.x, align.y).mult(radius));
        this.radius = radius;
    }
    
    public Knob onValueChanged(Consumer<Float> callback)
    {
        this.callback = callback;
        callback.accept(value);
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
        
        Main.instance().setState(new InteractionState()
        {
            @Override
            public boolean isCursorVisible()
            {
                return false;
            }
            
            @Override
            public void onMouseMoved(float x, float y, float dx, float dy)
            {
                value = Util.saturate(value + dy*SENSITIVITY);
                if (callback != null) callback.accept(value);
            }
            
            @Override
            public void onMouseButton(int button, int action, int mods)
            {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_RELEASE)
                    Main.instance().setDefaultState();
            }
        });
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
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (float t = 0.0f; t < T_END; t += DT)
        {
            Vec2 p = Util.squareDir(t).normalize().mult(radius);
            GL11.glVertex2f(p.x, p.y);
        }
        GL11.glEnd();
        
        GL11.glBegin(GL11.GL_LINES);
        for (float ang = NOTCH_ANG0; ang < NOTCH_END; ang += NOTCH_DA)
        {
            float x = (float)Math.cos(-ang);
            float y = (float)Math.sin(-ang);
            GL11.glVertex2f(x*radius, y*radius);
            GL11.glVertex2f(x*(radius + NOTCH_LENGTH), y*(radius + NOTCH_LENGTH));
        }
        
        float vAng = NOTCH_ANG0 + value*NOTCH_TOTAL_ANG;
        float vx = (float)Math.cos(-vAng)*radius;
        float vy = (float)Math.sin(-vAng)*radius;
        GL11.glVertex2f(0.0f, 0.0f);
        GL11.glVertex2f(vx, vy);
        GL11.glEnd();
        
        GL11.glPopMatrix();
    }
}
