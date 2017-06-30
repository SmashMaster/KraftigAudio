package kraftig.game;

import org.lwjgl.opengl.GL11;

public class FloorGrid
{
    private static final int WIDTH = 16;
    private static final float COLOR = 0.675f;
    
    FloorGrid()
    {
    }
    
    void render()
    {
        GL11.glLineWidth(1.0f);
        GL11.glColor3f(COLOR, COLOR, COLOR);
        GL11.glBegin(GL11.GL_LINES);
        for (int x = -WIDTH; x <= WIDTH; x++)
        {
            GL11.glVertex3f(x, 0.0f, -WIDTH);
            GL11.glVertex3f(x, 0.0f, WIDTH);
        }
        
        for (int z = -WIDTH; z <= WIDTH; z++)
        {
            GL11.glVertex3f(-WIDTH, 0.0f, z);
            GL11.glVertex3f(WIDTH, 0.0f, z);
        }
        GL11.glEnd();
    }
}
