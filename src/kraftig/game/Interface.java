package kraftig.game;

import com.samrj.devil.math.Vec2;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.GL11;

public class Interface
{
    private static final float INTERFACE_SCALE = 1.0f/1024.0f; //Pixels per meter.
    
    private final List<InterfaceElement> elements = new ArrayList<>();
    private float width, height;
    
    public Interface()
    {
    }
    
    public void setSize(float w, float h)
    {
        width = w/INTERFACE_SCALE;
        height = h/INTERFACE_SCALE;
    }
    
    public float getWidth()
    {
        return width;
    }
    
    public float getHeight()
    {
        return height;
    }
    
    public Interface add(InterfaceElement e)
    {
        elements.add(e);
        return this;
    }
    
    public MouseCapture onClick(float x, float y)
    {
        Vec2 mPos = new Vec2(x, y).div(INTERFACE_SCALE);
        
        for (InterfaceElement e : elements)
        {
            MouseCapture result = e.onClick(mPos);
            if (result != null) return result;
        }
        
        return null;
    }
    
    public void render()
    {
        GL11.glPushMatrix();
        GL11.glScalef(INTERFACE_SCALE, INTERFACE_SCALE, 0.0f);
        for (InterfaceElement e : elements) e.render();
        GL11.glPopMatrix();
    }
}
