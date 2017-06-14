package kraftig.game;

import com.samrj.devil.ui.AtlasFont;
import org.lwjgl.opengl.GL11;

public class UI
{
    private static final int CROSSHAIR_WIDTH = 12;
    private static final int CROSSHAIR_INNER_WIDTH = 4;
    
    private final AtlasFont font;
    
    UI() throws Exception
    {
        font = new AtlasFont("kraftig/res/fonts/", "menu.fnt");
    }
    
    AtlasFont getFont()
    {
        return font;
    }
    
    void renderHUD(boolean mouseGrabbed)
    {
        if (mouseGrabbed)
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
}
