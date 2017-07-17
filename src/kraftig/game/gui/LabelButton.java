package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.util.VectorFont;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class LabelButton implements UIElement
{
    private final VectorFont font;
    private final String text;
    private final float size;
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    private Runnable callback;
    
    public LabelButton(String text, float size, float margin)
    {
        this.font = Main.instance().getFont();
        this.text = text;
        this.size = size;
        radius.set(margin).madd(font.getSize(text), 0.5f*size);
    }
    
    public LabelButton onClick(Runnable callback)
    {
        this.callback = callback;
        return this;
    }
    
    public LabelButton click()
    {
        if (callback != null) callback.run();
        return this;
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
    public final LabelButton setPos(Vec2 pos, Alignment align)
    {
        align.align(pos, radius, this.pos);
        return this;
    }
    
    @Override
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
    {
    }
    
    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        if (p.x < pos.x - radius.x || p.x > pos.x + radius.x) return null;
        if (p.y < pos.y - radius.y || p.y > pos.y + radius.y) return null;
        return new UIFocusQuery(this, dist, p);
    }
    
    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
        if (action == GLFW.GLFW_PRESS && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) click();
    }
    
    @Override
    public void delete()
    {
    }
    
    @Override
    public void render(float alpha)
    {
        GL11.glLineWidth(1.0f);
        float color = Main.instance().getFocus() == this ? 0.75f : 1.0f;
        GL11.glColor4f(color, color, 1.0f, alpha);
        font.render(text, pos, size, Alignment.C);
        
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(pos.x - radius.x, pos.y - radius.y);
        GL11.glVertex2f(pos.x - radius.x, pos.y + radius.y);
        GL11.glVertex2f(pos.x + radius.x, pos.y + radius.y);
        GL11.glVertex2f(pos.x + radius.x, pos.y - radius.y);
        GL11.glEnd();
    }
}
