package kraftig.game;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import org.lwjgl.opengl.GL11;

public class Panel
{
    private final Vec3 pos = new Vec3();
    private float yaw;
    private float width, height;
    
    private final Interface frontInterface = new Interface();
    private final Interface rearInterface = new Interface();
    
    public Panel()
    {
    }
    
    public float dist(Vec3 cameraPos)
    {
        return pos.squareDist(cameraPos);
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
    
    public Panel setSize(float w, float h)
    {
        width = w;
        height = h;
        frontInterface.setSize(w, h);
        rearInterface.setSize(w, h);
        return this;
    }
    
    public Interface getFrontInterface()
    {
        return frontInterface;
    }
    
    public Interface getRearInterface()
    {
        return rearInterface;
    }
    
    public ClickResult onClick(Vec3 cameraPos, Vec3 dir)
    {
        Vec3 frontDir = new Vec3((float)Math.sin(yaw), 0.0f, (float)Math.cos(yaw));
        Vec3 camDir = Vec3.sub(cameraPos, pos);
        float camDot = camDir.dot(frontDir);
        if (Math.abs(camDot) < 0.001f) return ClickResult.HIT; //Hit edge of panel.
        
        float dist = -camDot/dir.dot(frontDir);
        if (dist <= 0.0f) return new ClickResult(false); //Panel is behind us.
        
        Vec3 hitPos = Vec3.madd(camDir, dir, dist);
        if (Math.abs(hitPos.y) > height) return ClickResult.MISSED; //Hit above/below panel.
        
        float x = hitPos.dot(new Vec3(-frontDir.z, 0.0f, frontDir.x));
        if (Math.abs(x) > width) return ClickResult.MISSED; //Hit left/right of panel.
        
        if (camDot > 0.0f) return new ClickResult(frontInterface.onClick(-x, hitPos.y));
        else return new ClickResult(rearInterface.onClick(x, hitPos.y));
    }
    
    public void render(Vec3 cameraPos)
    {
        Vec2 cameraDir = new Vec2(pos.x, pos.z).sub(new Vec2(cameraPos.x, cameraPos.z));
        Vec2 frontDir = new Vec2((float)Math.sin(yaw), (float)Math.cos(yaw));
        boolean facingFront = cameraDir.dot(frontDir) <= 0.0f;
        
        GL11.glPushMatrix();
        GL11.glTranslatef(pos.x, pos.y, pos.z);
        GL11.glRotatef(Util.toDegrees(yaw), 0.0f, 1.0f, 0.0f);
        
        //Shadow
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.5f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(-width, -pos.y, 0.0f);
        GL11.glVertex3f(width, -pos.y, 0.0f);
        GL11.glEnd();
        
        //Background
        GL11.glColor4f(0.4375f, 0.4375f, 0.4375f, 0.875f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(-width, -height, 0.0f);
        GL11.glVertex3f(-width, height, 0.0f);
        GL11.glVertex3f(width, height, 0.0f);
        GL11.glVertex3f(width, -height, 0.0f);
        GL11.glEnd();
        
        //Outline
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3f(-width, -height, 0.0f);
        GL11.glVertex3f(-width, height, 0.0f);
        GL11.glVertex3f(width, height, 0.0f);
        GL11.glVertex3f(width, -height, 0.0f);
        GL11.glEnd();
        
        if (facingFront) frontInterface.render();
        else
        {
            GL11.glScalef(-1.0f, 1.0f, 1.0f);
            rearInterface.render();
        }
        
        GL11.glPopMatrix();
    }
    
    public static class ClickResult
    {
        public static final ClickResult HIT = new ClickResult(true);
        public static final ClickResult MISSED = new ClickResult(false);
        
        public final boolean hit;
        public final MouseCapture mouseCapture;
        
        ClickResult(boolean hit)
        {
            this.hit = hit;
            mouseCapture = null;
        }
        
        ClickResult(MouseCapture mouseCapture)
        {
            hit = true;
            this.mouseCapture = mouseCapture;
        }
    }
}
