package kraftig.game;

import com.samrj.devil.geo2d.AAB2;
import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import kraftig.game.gui.Jack;
import kraftig.game.gui.UI;
import kraftig.game.util.Savable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public abstract class Panel implements Savable, Drawable, Focusable
{
    public static final float UI_SCALE = 1.0f/2048.0f; //Pixels per meter.
    
    private final Vec3 pos = new Vec3();
    private float yaw;
    private float width, height;
    
    public final Vec3 frontDir = new Vec3(0.0f, 0.0f, 1.0f), rightDir = new Vec3(-1.0f, 0.0f, 0.0f);
    
    //2D top-down edge data.
    public final Vec2 ea = new Vec2(), eb = new Vec2();
    public final Vec2 eab = new Vec2();
    public final Vec2 eaCam = new Vec2(), ebCam = new Vec2();
    
    public final UI frontInterface = new UI();
    public final UI rearInterface = new UI();
    
    public boolean dragged;
    
    public Panel()
    {
        updateMatrices();
    }
    
    private void updateMatrices()
    {
        Mat4 rot = Mat4.rotation(new Vec3(0.0f, 1.0f, 0.0f), yaw);
        
        Mat4 frontMatrix = Mat4.translation(pos);
        frontMatrix.mult(rot);
        frontMatrix.mult(new Vec3(UI_SCALE));
        frontInterface.updateMatrix(frontMatrix, this, true);
        
        Mat4 rearMatrix = Mat4.translation(pos);
        rearMatrix.mult(rot);
        rearMatrix.mult(new Vec3(-UI_SCALE, UI_SCALE, -UI_SCALE));
        rearInterface.updateMatrix(rearMatrix, this, false);
    }
    
    public final void updateEdge()
    {
        Vec2 edge = new Vec2(rightDir.x, rightDir.z).mult(width);
        Vec2 p2 = new Vec2(pos.x, pos.z);
        Vec2.sub(p2, edge, ea);
        Vec2.add(p2, edge, eb);
        Vec2.mult(edge, -2.0f, eab);
        
        Camera3D camera = Main.instance().getCamera();
        Vec2 cam = new Vec2(camera.pos.x, camera.pos.z);
        Vec2.sub(cam, ea, eaCam);
        Vec2.sub(cam, eb, ebCam);
    }
    
    @Override
    public final float edgeRayHit(Vec2 p, Vec2 d)
    {
        //Calculate hit position and return zero if missed.
        Vec2 pa = Vec2.sub(p, ea);
        float t = (d.x*pa.y - d.y*pa.x)/(d.y*eab.x - d.x*eab.y);
        if (t < 0.0f || t > 1.0f) return 0.0f;
        
        //Return direction of hit.
        Vec2 dr = Vec2.madd(pa, eab, t);
        return dr.dot(d);
    }
    
    public final Vec3 getPos()
    {
        return new Vec3(pos);
    }
    
    public final float getYaw()
    {
        return yaw;
    }
    
    @Override
    public final float getY()
    {
        return pos.y;
    }
    
    @Override
    public final float getHeight()
    {
        return height;
    }
    
    public final Panel setPosYaw(Vec3 pos, float yaw)
    {
        this.pos.set(pos);
        this.yaw = yaw;
        frontDir.set((float)Math.sin(yaw), 0.0f, (float)Math.cos(yaw));
        rightDir.set(-frontDir.z, 0.0f, frontDir.x);
        updateMatrices();
        return this;
    }
    
    public final Panel setPosition(Vec3 pos)
    {
        this.pos.set(pos);
        updateMatrices();
        return this;
    }
    
    public final Panel setYaw(float yaw)
    {
        this.yaw = yaw;
        frontDir.set((float)Math.sin(yaw), 0.0f, (float)Math.cos(yaw));
        rightDir.set(-frontDir.z, 0.0f, frontDir.x);
        updateMatrices();
        return this;
    }
    
    public final Panel setSize(float w, float h)
    {
        width = w;
        height = h;
        return this;
    }
    
    public final Panel setSizeFromContents(float margin)
    {
        AAB2 bounds = frontInterface.getBounds().expand(rearInterface.getBounds());
        width = (Math.max(Math.abs(bounds.x0), Math.abs(bounds.x1)) + margin)*UI_SCALE;
        height = (Math.max(Math.abs(bounds.y0), Math.abs(bounds.y1)) + margin)*UI_SCALE;
        return this;
    }
    
    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
        //Panel drag.
        if (action == GLFW.GLFW_PRESS && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            Main.instance().setState(new PanelDragState(this));
    }
    
    public final void projectRay(Vec3 pos, Vec3 dir, boolean[] rHit, float[] rDist, Vec2 rPos, int[] rSide)
    {
        Vec3 pDir = Vec3.sub(pos, this.pos);
        float pDot = pDir.dot(frontDir);
        float dist = -pDot/dir.dot(frontDir);
        
        rHit[0] = false;
        rDist[0] = dist;
        
        if (dist <= 0.0f) return; //Panel is behind us.
        
        Vec3 hitPos = Vec3.madd(pDir, dir, dist);
        float x = hitPos.dot(rightDir);
        
        rPos.set(x, hitPos.y);
        
        if (Math.abs(pDot) < 0.001f) rSide[0] = 0; //Hit edge of panel.
        else if (pDot > 0.0f) rSide[0] = 1; //Hit front.
        else rSide[0] = -1; //Hit rear.
        
        //Check if actually hit.
        if (Math.abs(hitPos.y) <= height && Math.abs(x) <= width) rHit[0] = true;
    }
    
    public final void projectMouse(boolean[] rHit, float[] rDist, Vec2 rPos, int[] rSide)
    {
        Main main = Main.instance();
        projectRay(main.getCamera().pos, main.getMouseDir(), rHit, rDist, rPos, rSide);
    }
    
    public final FocusQuery checkFocus(Vec3 pos, Vec3 dir)
    {
        boolean[] hit = {false};
        float[] dist = {0.0f};
        Vec2 rPos = new Vec2();
        int[] side = {0};
        
        projectRay(pos, dir, hit, dist, rPos, side);
        
        if (!hit[0]) return null;
        
        FocusQuery panelFocus = new FocusQuery(this, dist[0]);
        FocusQuery uiFocus = null;
        
        switch (side[0])
        {
            case 0: return panelFocus;
            case 1: uiFocus = frontInterface.checkFocus(dist[0], new Vec2(-rPos.x, rPos.y).div(UI_SCALE)); break;
            case -1: uiFocus = rearInterface.checkFocus(dist[0], rPos.div(UI_SCALE)); break;
        }
        
        return uiFocus != null ? uiFocus : panelFocus;
    }
    
    public List<Jack> getJacks()
    {
        return Collections.EMPTY_LIST;
    }
    
    public void process(int samples)
    {
        
    }
    
    @Override
    public void render()
    {
        float alpha = dragged ? 0.5f : 1.0f;
        
        Camera3D camera = Main.instance().getCamera();
        Vec2 cameraDir = new Vec2(pos.x, pos.z).sub(new Vec2(camera.pos.x, camera.pos.z));
        Vec2 frontDir2 = new Vec2(frontDir.x, frontDir.z);
        boolean facingFront = cameraDir.dot(frontDir2) <= 0.0f;
        
        GL11.glPushMatrix();
        GL11.glTranslatef(pos.x, pos.y, pos.z);
        GL11.glRotatef(Util.toDegrees(yaw), 0.0f, 1.0f, 0.0f);
        
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
        
        //Shadow
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.5f*alpha);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(-width, Grid.FLOOR_HEIGHT - pos.y, 0.0f);
        GL11.glVertex3f(width, Grid.FLOOR_HEIGHT - pos.y, 0.0f);
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
    
    public void delete()
    {
        frontInterface.delete();
        rearInterface.delete();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        pos.write(out);
        out.writeFloat(yaw);
        out.writeFloat(width);
        out.writeFloat(height);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        pos.read(in);
        yaw = in.readFloat();
        width = in.readFloat();
        height = in.readFloat();
        setPosYaw(pos, yaw);
        updateMatrices();
    }
    // </editor-fold>
}
