package kraftig.game.gui;

import com.samrj.devil.ui.AtlasFont;
import org.lwjgl.opengl.GL11;

public class GUI
{
    private static final int CROSSHAIR_WIDTH = 12;
    private static final int CROSSHAIR_INNER_WIDTH = 4;
    
    private final AtlasFont font;
    
    public GUI() throws Exception
    {
        font = new AtlasFont("kraftig/res/fonts/", "menu.fnt");
    }
    
    AtlasFont getFont()
    {
        return font;
    }
    
    public void renderCrosshair()
    {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(-CROSSHAIR_WIDTH, 0.0f);
        GL11.glVertex2f(-CROSSHAIR_INNER_WIDTH, 0.0f);
        GL11.glVertex2f(CROSSHAIR_WIDTH, 0.0f);
        GL11.glVertex2f(CROSSHAIR_INNER_WIDTH, 0.0f);
        GL11.glVertex2f(0.0f, -CROSSHAIR_WIDTH);
        GL11.glVertex2f(0.0f, -CROSSHAIR_INNER_WIDTH);
        GL11.glVertex2f(0.0f, CROSSHAIR_WIDTH);
        GL11.glVertex2f(0.0f, CROSSHAIR_INNER_WIDTH);
        GL11.glEnd();
    }
}
