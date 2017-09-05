package kraftig.game.gui.buttons;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.Main;
import kraftig.game.util.VectorFont;
import org.lwjgl.opengl.GL11;

public class ToggleLabelButton extends ToggleButton
{
    private final VectorFont font;
    private final String text;
    private final float size;
    
    public ToggleLabelButton(VectorFont font, String text, float size, float margin)
    {
        super(new Vec2(margin).madd(font.getSize(text), 0.5f*size));
        this.font = font;
        this.text = text;
        this.size = size;
    }
    
    public ToggleLabelButton(String text, float size, float margin)
    {
        this(Main.instance().getFont(), text, size, margin);
    }
    
    @Override
    public void render(float alpha)
    {
        Vec2 pos = getPos();
        Vec2 radius = getRadius();
        
        if (getValue())
        {
            GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha*0.5f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(pos.x - radius.x, pos.y - radius.y);
            GL11.glVertex2f(pos.x - radius.x, pos.y + radius.y);
            GL11.glVertex2f(pos.x + radius.x, pos.y + radius.y);
            GL11.glVertex2f(pos.x + radius.x, pos.y - radius.y);
            GL11.glEnd();
        }
        
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
