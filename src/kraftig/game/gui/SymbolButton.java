package kraftig.game.gui;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.Main;
import org.lwjgl.opengl.GL11;

public abstract class SymbolButton extends Button
{
    public SymbolButton(Vec2 radius)
    {
        super(radius);
    }
    
    public SymbolButton(Vec2 radius, Vec2 pos, Alignment align)
    {
        this(radius);
        setPos(pos, align);
    }
    
    public abstract void renderSymbol();
    
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
        
        renderSymbol();
        
        GL11.glPopMatrix();
    }
}
