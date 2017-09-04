package kraftig.game.gui.buttons;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.Main;
import org.lwjgl.opengl.GL11;

public class SymbolButton extends Button
{
    private final Runnable symbol;
    
    public SymbolButton(Vec2 radius, Runnable symbol)
    {
        super(radius);
        
        this.symbol = symbol;
    }
    
    public SymbolButton(Vec2 radius, Runnable symbol, Vec2 pos, Alignment align)
    {
        this(radius, symbol);
        setPos(pos, align);
    }
    
    @Override
    public void render(float alpha)
    {
        Vec2 pos = getPos();
        Vec2 radius = getRadius();
        
        GL11.glPushMatrix();
        GL11.glTranslatef(pos.x, pos.y, 0.0f);
        GL11.glScalef(radius.x, radius.y, 1.0f);
        
        GL11.glLineWidth(1.0f);
        float color = Main.instance().getFocus() == this ? 0.75f : 1.0f;
        GL11.glColor4f(color, color, 1.0f, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(-1.0f, -1.0f);
        GL11.glVertex2f(-1.0f, 1.0f);
        GL11.glVertex2f(1.0f, 1.0f);
        GL11.glVertex2f(1.0f, -1.0f);
        GL11.glEnd();
        
        symbol.run();
        
        GL11.glPopMatrix();
    }
}
