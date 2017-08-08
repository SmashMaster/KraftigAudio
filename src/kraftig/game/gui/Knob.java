package kraftig.game.gui;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.function.Consumer;
import kraftig.game.FocusQuery;
import kraftig.game.InteractionState;
import kraftig.game.Main;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Knob extends AudioInputJack implements UIElement
{
    private static final float DEAD_ZONE = (float)Math.toRadians(45.0);
    private static final int NOTCHES = 9;
    private static final float NOTCH_ANG0 = DEAD_ZONE + Util.PId2;
    private static final float NOTCH_TOTAL_ANG = (Util.PIm2 - DEAD_ZONE*2.0f);
    private static final float NOTCH_DA = NOTCH_TOTAL_ANG/(NOTCHES - 1.0f);
    private static final float NOTCH_END = NOTCH_ANG0 + NOTCH_TOTAL_ANG + NOTCH_DA*0.5f;
    private static final float NOTCH_LENGTH = 1.25f;
    private static final float SENSITIVITY = 1.0f/256.0f;
    
    private float value = 0.0f;
    private Consumer<Float> callback;
    
    public Knob(float radius)
    {
        super(radius/NOTCH_LENGTH);
    }
    
    public Knob(float radius, Vec2 pos, Alignment align)
    {
        this(radius);
        setPos(pos, align);
    }
    
    @Override
    public final Vec2 getRadius()
    {
        return new Vec2(radius*NOTCH_LENGTH);
    }
    
    public Knob onValueChanged(Consumer<Float> callback)
    {
        this.callback = callback;
        callback.accept(value);
        return this;
    }
    
    public Knob setValue(float value)
    {
        value = Util.saturate(value);
        if (this.value != value)
        {
            this.value = value;
            if (callback != null) callback.accept(this.value);
        }
        return this;
    }
    
    public void updateValue(int sampleIndex)
    {
        float[][] buffer = getBuffer();
        if (buffer != null) setValue(buffer[0][sampleIndex]*0.5f + 0.5f);
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
                setValue(value + dy*SENSITIVITY);
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
    public void renderSymbol()
    {
        GL11.glBegin(GL11.GL_LINES);
        for (float ang = NOTCH_ANG0; ang < NOTCH_END; ang += NOTCH_DA)
        {
            float x = (float)Math.cos(-ang);
            float y = (float)Math.sin(-ang);
            GL11.glVertex2f(x*radius, y*radius);
            GL11.glVertex2f(x*radius*NOTCH_LENGTH, y*radius*NOTCH_LENGTH);
        }
        
        float vAng = NOTCH_ANG0 + value*NOTCH_TOTAL_ANG;
        float vx = (float)Math.cos(-vAng)*radius;
        float vy = (float)Math.sin(-vAng)*radius;
        GL11.glVertex2f(0.0f, 0.0f);
        GL11.glVertex2f(vx, vy);
        GL11.glEnd();
    }
}
