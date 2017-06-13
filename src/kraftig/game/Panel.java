package kraftig.game;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import org.lwjgl.opengl.GL11;

public class Panel
{
    private final Vec3 pos = new Vec3();
    private float yaw;
    private float sizeX, sizeY;
    
    private final Interface frontInterface, backInterface;
    
    public Panel(UI ui)
    {
        frontInterface = new Interface(ui);
        backInterface = new Interface(ui);
    }
    
    public Panel setPosition(Vec3 pos)
    {
        this.pos.set(pos);
        return this;
    }
    
    public Panel setYaw(float yaw)
    {
        this.yaw = yaw;
        return this;
    }
    
    public Panel setSize(float x, float y)
    {
        sizeX = x;
        sizeY = y;
        return this;
    }
    
    public Interface getFrontInterface()
    {
        return frontInterface;
    }
    
    public Interface getBackInterface()
    {
        return backInterface;
    }
    
    public void render(Vec3 cameraPos)
    {
        Vec2 cameraDir = new Vec2(cameraPos.x, cameraPos.z).sub(new Vec2(pos.x, pos.z));
        Vec2 frontDir = new Vec2((float)Math.cos(yaw), (float)Math.sin(yaw));
        boolean facingFront = cameraDir.dot(frontDir) >= 0.0f;
        
        GL11.glPushMatrix();
        GL11.glTranslatef(pos.x, pos.y, pos.z);
        GL11.glRotatef((float)Math.toDegrees(yaw), 0.0f, yaw, 0.0f);
        
        //Shadow
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.5f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(-sizeX, -pos.y, 0.0f);
        GL11.glVertex3f(sizeX, -pos.y, 0.0f);
        GL11.glEnd();
        
        //Background
        GL11.glColor4f(0.4375f, 0.4375f, 0.4375f, 0.875f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(-sizeX, -sizeY, 0.0f);
        GL11.glVertex3f(-sizeX, sizeY, 0.0f);
        GL11.glVertex3f(sizeX, sizeY, 0.0f);
        GL11.glVertex3f(sizeX, -sizeY, 0.0f);
        GL11.glEnd();
        
        //Outline
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3f(-sizeX, -sizeY, 0.0f);
        GL11.glVertex3f(-sizeX, sizeY, 0.0f);
        GL11.glVertex3f(sizeX, sizeY, 0.0f);
        GL11.glVertex3f(sizeX, -sizeY, 0.0f);
        GL11.glEnd();
        
        if (facingFront) frontInterface.render();
        else backInterface.render();
        
        GL11.glPopMatrix();
    }
}
