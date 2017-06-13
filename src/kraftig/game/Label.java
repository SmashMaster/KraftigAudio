package kraftig.game;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import com.samrj.devil.ui.AtlasFont;
import org.lwjgl.opengl.GL11;

public class Label implements InterfaceElement
{
    private final AtlasFont font;
    private final String text;
    private final Vec2 pos = new Vec2();
    private final Alignment align;
    
    public Label(UI ui, String text, Vec2 pos, Alignment align)
    {
        font = ui.getFont();
        this.text = text;
        this.pos.set(pos);
        this.align = align;
    }
    
    @Override
    public void render()
    {
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        font.drawDeprecated(text, pos, align);
    }
}
