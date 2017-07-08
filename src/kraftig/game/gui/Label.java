package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import com.samrj.devil.ui.AtlasFont;
import kraftig.game.FocusQuery;
import org.lwjgl.opengl.GL11;

public class Label implements UIElement
{
    private final AtlasFont font;
    private String text;
    private final Vec2 pos = new Vec2();
    private Alignment align = Alignment.C;
    
    public Label(AtlasFont font, String text)
    {
        this.font = font;
        this.text = text;
    }
    
    public Label(AtlasFont font, String text, Vec2 pos, Alignment align)
    {
        this(font, text);
        setPos(pos, align);
    }
    
    @Override
    public final Vec2 getPos()
    {
        return new Vec2(pos);
    }
    
    @Override
    public final Vec2 getSize()
    {
        return new Vec2(font.getWidth(text), font.getHeight());
    }
    
    @Override
    public final void setPos(Vec2 pos, Alignment align)
    {
        this.pos.set(pos);
        this.align = align;
    }
    
    public void setText(String text)
    {
        if (text == null) throw new NullPointerException();
        
        this.text = text;
    }
    
    @Override
    public void updateMatrix(Mat4 matrix)
    {
    }
    
    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        return null;
    }
    
    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
    }
    
    @Override
    public void delete()
    {
    }
    
    @Override
    public void render(float alpha)
    {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        font.drawDeprecated(text, pos, align);
    }
}
