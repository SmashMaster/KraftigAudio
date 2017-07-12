package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Main;
import kraftig.game.util.VectorFont;
import org.lwjgl.opengl.GL11;

public class Label implements UIElement
{
    private final VectorFont font;
    private final String text;
    private final float size;
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    private Alignment align = Alignment.C;
    
    public Label(VectorFont font, String text, float size)
    {
        this.font = font;
        this.text = text;
        this.size = size;
        radius.set(font.getSize(text).mult(0.5f*size));
    }
    
    public Label(String text, float size)
    {
        this(Main.instance().getFont(), text, size);
    }
    
    public Label(VectorFont font, String text, float size, Vec2 pos, Alignment align)
    {
        this(font, text, size);
        setPos(pos, align);
    }
    
    public Label(String text, float size, Vec2 pos, Alignment align)
    {
        this(Main.instance().getFont(), text, size, pos, align);
    }
    
    @Override
    public final Vec2 getPos()
    {
        return align.align(pos, radius);
    }
    
    @Override
    public final Vec2 getSize()
    {
        return new Vec2(radius);
    }
    
    @Override
    public final Label setPos(Vec2 pos, Alignment align)
    {
        this.pos.set(pos);
        this.align = align;
        return this;
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
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        font.render(text, pos, size, align);
    }
}
