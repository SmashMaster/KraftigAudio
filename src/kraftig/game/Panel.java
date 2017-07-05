package kraftig.game;

import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import kraftig.game.gui.UI;
import kraftig.game.gui.UIFocusQuery;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Panel implements Drawable, Focusable
{
    private static final float UI_SCALE = 1.0f/1024.0f; //Pixels per meter.
    
    private final Vec3 pos = new Vec3();
    private float yaw;
    private float width, height;
    
    public final Vec2 a = new Vec2(), b = new Vec2();
    public final Vec2 ab = new Vec2();
    public final Vec2 aCam = new Vec2(), bCam = new Vec2();
    
    public final UI frontInterface = new UI();
    public final UI rearInterface = new UI();
    
    private boolean dragged;
    
    public Panel()
    {
        updateMatrices();
    }
    
    private void updateMatrices()
    {
        Mat4 rot = Mat4.rotation(new Vec3(0.0f, 1.0f, 0.0f), yaw);
        
        Mat4 frontMatrix = Mat4.translation(pos);
        frontMatrix.mult(rot);
        frontMatrix.mult(new Vec3(UI_SCALE, UI_SCALE, UI_SCALE));
        frontInterface.updateMatrix(frontMatrix);
        
        Mat4 rearMatrix = Mat4.translation(pos);
        rearMatrix.mult(rot);
        rearMatrix.mult(new Vec3(-UI_SCALE, UI_SCALE, -UI_SCALE));
        rearInterface.updateMatrix(rearMatrix);
    }
    
    @Override
    public void updateEdge()
    {
        Vec2 edge = new Vec2((float)Math.cos(yaw), -(float)Math.sin(yaw)).mult(width);
        Vec2 p2 = new Vec2(pos.x, pos.z);
        Vec2.sub(p2, edge, a);
        Vec2.add(p2, edge, b);
        Vec2.mult(edge, -2.0f, ab);
        
        Camera3D camera = Main.instance().getCamera();
        Vec2 cam = new Vec2(camera.pos.x, camera.pos.z);
        Vec2.sub(cam, a, aCam);
        Vec2.sub(cam, b, bCam);
    }
    
    public float edgeRayHit(Vec2 p, Vec2 d)
    {
        //Calculate hit position and return zero if missed.
        Vec2 pa = Vec2.sub(p, a);
        float t = (d.x*pa.y - d.y*pa.x)/(d.y*ab.x - d.x*ab.y);
        if (t < 0.0f || t > 1.0f) return 0.0f;
        
        //Return direction of hit.
        Vec2 dr = Vec2.madd(pa, ab, t);
        return Math.signum(dr.dot(d));
    }
    
    public Panel setPosition(Vec3 pos)
    {
        this.pos.set(pos);
        updateMatrices();
        return this;
    }
    
    public float getY()
    {
        return pos.y;
    }
    
    public float getHeight()
    {
        return height;
    }
    
    public Panel setYaw(float yaw)
    {
        this.yaw = yaw;
        updateMatrices();
        return this;
    }
    
    public Panel setSize(float w, float h)
    {
        width = w;
        height = h;
        updateMatrices();
        return this;
    }
    
    public class PanelFocusQuery extends FocusQuery
    {
        private final float x, y;
        
        private PanelFocusQuery(float dist, float x, float y)
        {
            super(Panel.this, dist);
            
            this.x = x; this.y = y;
        }
    }
    
    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
        PanelFocusQuery q = (PanelFocusQuery)query;
        
        //Panel drag.
        if (action == GLFW.GLFW_PRESS && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            dragged = true;
            Player player = Main.instance().getPlayer();
            float relYaw = Util.reduceAngle(yaw - player.getYaw());
            
            Main.instance().setState(new InteractionState()
            {
                private void update()
                {
                    yaw = Util.reduceAngle(player.getYaw() + relYaw);
                    pos.set(player.getCamera().pos);
                    pos.madd(Main.instance().getMouseDir(), q.dist);
                    pos.madd(new Vec3((float)Math.cos(yaw), 0.0f, -(float)Math.sin(yaw)), q.x);
                    pos.y -= q.y;
                    updateMatrices();
                }
                
                @Override
                public boolean canPlayerAim()
                {
                    return true;
                }
                
                @Override
                public void onMouseMoved(float x, float y, float dx, float dy)
                {
                    update();
                }
                
                @Override
                public void onMouseButton(int button, int action, int mods)
                {
                    if (action != GLFW.GLFW_PRESS || button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;
                    
                    dragged = false;
                    Main.instance().setDefaultState();
                }
                
                @Override
                public void step(float dt)
                {
                    update();
                }
            });
        }
    }
    
    public FocusQuery checkFocus(Vec3 pos, Vec3 dir)
    {
        Vec3 frontDir = new Vec3((float)Math.sin(yaw), 0.0f, (float)Math.cos(yaw));
        Vec3 camDir = Vec3.sub(pos, this.pos);
        float camDot = camDir.dot(frontDir);
        
        float dist = -camDot/dir.dot(frontDir);
        if (dist <= 0.0f) return null; //Panel is behind us.
        
        Vec3 hitPos = Vec3.madd(camDir, dir, dist);
        if (Math.abs(hitPos.y) > height) return null; //Hit above/below panel.
        
        float x = hitPos.dot(new Vec3(-frontDir.z, 0.0f, frontDir.x));
        if (Math.abs(x) > width) return null; //Hit left/right of panel.
        
        PanelFocusQuery panelFocus = new PanelFocusQuery(dist, x, hitPos.y);
        UIFocusQuery uiFocus;
        
        if (Math.abs(camDot) < 0.001f) return panelFocus;
        
        if (camDot > 0.0f) uiFocus = frontInterface.checkFocus(dist, new Vec2(-x, hitPos.y).div(UI_SCALE));
        else uiFocus = rearInterface.checkFocus(dist, new Vec2(x, hitPos.y).div(UI_SCALE));
        
        return uiFocus != null ? uiFocus : panelFocus;
    }
    
    public void delete()
    {
        frontInterface.delete();
        rearInterface.delete();
    }
    
    @Override
    public void render()
    {
        float alpha = dragged ? 0.5f : 1.0f;
        
        Camera3D camera = Main.instance().getCamera();
        Vec2 cameraDir = new Vec2(pos.x, pos.z).sub(new Vec2(camera.pos.x, camera.pos.z));
        Vec2 frontDir = new Vec2((float)Math.sin(yaw), (float)Math.cos(yaw));
        boolean facingFront = cameraDir.dot(frontDir) <= 0.0f;
        
        GL11.glPushMatrix();
        GL11.glTranslatef(pos.x, pos.y, pos.z);
        GL11.glRotatef(Util.toDegrees(yaw), 0.0f, 1.0f, 0.0f);
        
        //Shadow
        GL11.glLineWidth(0.5f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.5f*alpha);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(-width, -pos.y, 0.0f);
        GL11.glVertex3f(width, -pos.y, 0.0f);
        GL11.glEnd();
        
        //Background
        GL11.glColor4f(0.4375f, 0.4375f, 0.4375f, 0.875f*alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(-width, -height, 0.0f);
        GL11.glVertex3f(-width, height, 0.0f);
        GL11.glVertex3f(width, height, 0.0f);
        GL11.glVertex3f(width, -height, 0.0f);
        GL11.glEnd();
        
        //Outline
        GL11.glLineWidth(1.0f);
        float color = Main.instance().getFocus() == this ? 0.75f : 1.0f;
        GL11.glColor4f(color, color, 1.0f, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3f(-width, -height, 0.0f);
        GL11.glVertex3f(-width, height, 0.0f);
        GL11.glVertex3f(width, height, 0.0f);
        GL11.glVertex3f(width, -height, 0.0f);
        GL11.glEnd();
        
        GL11.glScalef(UI_SCALE, UI_SCALE, UI_SCALE);
        if (facingFront) frontInterface.render(alpha);
        else
        {
            GL11.glScalef(-1.0f, 1.0f, -1.0f);
            rearInterface.render(alpha);
        }
        
        GL11.glPopMatrix();
    }
}
