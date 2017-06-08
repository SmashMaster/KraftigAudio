package kraftig.game;

import org.lwjgl.opengl.GL11;

public class Skybox
{
    private static final float GROUND_COLOR = 0.5f;
    private static final float SKY_COLOR = 0.375f;
    
    Skybox()
    {
    }
    
    public void render()
    {
        GL11.glClearColor(SKY_COLOR, SKY_COLOR, SKY_COLOR, 0.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        
        GL11.glColor3f(GROUND_COLOR, GROUND_COLOR, GROUND_COLOR);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(-1.0f, -1.0f, 1.0f);
        GL11.glVertex3f(-1.0f, 0.0f, 1.0f);
        GL11.glVertex3f(1.0f, 0.0f, 1.0f);
        GL11.glVertex3f(1.0f, -1.0f, 1.0f);
        
        GL11.glVertex3f(-1.0f, -1.0f, -1.0f);
        GL11.glVertex3f(-1.0f, 0.0f, -1.f);
        GL11.glVertex3f(1.0f, 0.0f, -1.0f);
        GL11.glVertex3f(1.0f, -1.0f, -1.0f);
        
        GL11.glVertex3f(1.0f, -1.0f, -1.0f);
        GL11.glVertex3f(1.0f, 0.0f, -1.0f);
        GL11.glVertex3f(1.0f, 0.0f, 1.0f);
        GL11.glVertex3f(1.0f, -1.0f, 1.0f);
        
        GL11.glVertex3f(-1.0f, -1.0f, -1.0f);
        GL11.glVertex3f(-1.0f, 0.0f, -1.0f);
        GL11.glVertex3f(-1.0f, 0.0f, 1.0f);
        GL11.glVertex3f(-1.0f, -1.0f, 1.0f);
        
        GL11.glVertex3f(-1.0f, -1.0f, -1.0f);
        GL11.glVertex3f(-1.0f, -1.0f, 1.0f);
        GL11.glVertex3f(1.0f, -1.0f, 1.0f);
        GL11.glVertex3f(1.0f, -1.0f, -1.0f);
        GL11.glEnd();
    }
}
