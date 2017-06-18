package kraftig.game.gui;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import com.samrj.devil.ui.AtlasFont;
import org.lwjgl.opengl.GL11;

public class Label implements InterfaceElement
{
    private final AtlasFont font;
    private String text;
    private final Vec2 pos = new Vec2();
    private final Alignment align;
    
    public Label(GUI ui, String text, Vec2 pos, Alignment align)
    {
        if (text == null) throw new NullPointerException();
        
        font = ui.getFont();
        this.text = text;
        this.pos.set(pos);
        this.align = align;
    }
    
    public void setText(String text)
    {
        if (text == null) throw new NullPointerException();
        
        this.text = text;
    }
    
    @Override
    public MouseCapture onClick(Vec2 mPos)
    {
        return null;
    }
    
    @Override
    public void render(float alpha)
    {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        font.drawDeprecated(text, pos, align);
    }
}
