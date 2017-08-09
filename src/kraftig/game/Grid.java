package kraftig.game;

import org.lwjgl.opengl.GL11;

public class Grid
{
    public static final float FLOOR_HEIGHT = -0.25f;
    private static final float SIZE = 0.5f;
    private static final int WIDTH = 16;
    private static final float REAL_WIDTH = WIDTH*SIZE;
    private static final float COLOR = 0.675f;
    
    Grid()
    {
    }
    
    void render()
    {
        GL11.glLineWidth(1.0f);
        GL11.glColor3f(COLOR, COLOR, COLOR);
        GL11.glBegin(GL11.GL_LINES);
        for (int ix = -WIDTH; ix <= WIDTH; ix++)
        {
            float x = ix*SIZE;
            GL11.glVertex3f(x, FLOOR_HEIGHT, -REAL_WIDTH);
            GL11.glVertex3f(x, FLOOR_HEIGHT, REAL_WIDTH);
        }
        
        for (int iz = -WIDTH; iz <= WIDTH; iz++)
        {
            float z = iz*SIZE;
            GL11.glVertex3f(-REAL_WIDTH, FLOOR_HEIGHT, z);
            GL11.glVertex3f(REAL_WIDTH, FLOOR_HEIGHT, z);
        }
        GL11.glEnd();
    }
}
