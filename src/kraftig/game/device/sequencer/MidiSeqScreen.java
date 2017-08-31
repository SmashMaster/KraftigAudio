package kraftig.game.device.sequencer;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.UIElement;
import kraftig.game.gui.UIFocusQuery;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class MidiSeqScreen implements UIElement
{
    private final MidiSeqCamera camera;
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    
    private Panel panel;
    
    public MidiSeqScreen(MidiSeqCamera camera, Vec2 radius)
    {
        this.camera = camera;
        this.radius.set(radius);
    }
    
    public MidiSeqScreen(MidiSeqCamera camera, Vec2 radius, Vec2 pos, Alignment align)
    {
        this(camera, radius);
        setPos(pos, align);
    }
    
    @Override
    public final Vec2 getPos()
    {
        return new Vec2(pos);
    }
    
    @Override
    public final Vec2 getRadius()
    {
        return new Vec2(radius);
    }
    
    @Override
    public final MidiSeqScreen setPos(Vec2 pos, Alignment align)
    {
        align.align(pos, getRadius(), this.pos);
        return this;
    }

    @Override
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
    {
        this.panel = panel;
    }
    
    public Vec2 getMouse()
    {
        if (panel == null) return null;
        
        boolean[] hit = {false};
        float[] dist = {0.0f};
        Vec2 mPos = new Vec2();
        int[] side = {0};
        panel.projectMouse(hit, dist, mPos, side);
        if (dist[0] <= 0.0f) return null;
        
        if (side[0] == 1) mPos.x = -mPos.x;
        mPos.div(Panel.UI_SCALE);
        
        mPos.x = (mPos.x - pos.x)*0.5f/radius.x;
        mPos.y = (mPos.y - pos.y)*0.5f/radius.y;
        return mPos;
    }

    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        if (p.x < pos.x - radius.x || p.x > pos.x + radius.x) return null;
        if (p.y < pos.y - radius.y || p.y > pos.y + radius.y) return null;
        return new UIFocusQuery(this, dist, p);
    }

    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
        if (action == GLFW.GLFW_PRESS && button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) camera.drag(true, true);
    }
    
    @Override
    public void onMouseScroll(FocusQuery query, float dx, float dy)
    {
        float factor = (float)Math.pow(1.5, dy);
        camera.zoomX(factor);
        camera.zoomY(factor);
    }

    @Override
    public void delete()
    {
    }

    @Override
    public void render(float alpha)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef(pos.x, pos.y, 0.0f);
        GL11.glScalef(radius.x, radius.y, 1.0f);
        
        //Enable stencil writing, disable color writing.
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glColorMask(false, false, false, false);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

        //Draw stencil mask.
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-1.0f, -1.0f);
        GL11.glVertex2f(-1.0f, 1.0f);
        GL11.glVertex2f(1.0f, 1.0f);
        GL11.glVertex2f(1.0f, -1.0f);
        GL11.glEnd();
        
        //Disable stencil writing, enable color writing.
        GL11.glColorMask(true, true, true, true);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        
        //Draw masked stuff here.
        GL11.glPushMatrix();
        camera.multMatrix();
        
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        GL11.glBegin(GL11.GL_LINES);
        for (float x = -8.0f; x <= 8.0f; x++)
        {
            GL11.glVertex2f(x, -8.0f);
            GL11.glVertex2f(x, 8.0f);
        }
        
        for (float y = -8.0f; y <= 8.0f; y++)
        {
            GL11.glVertex2f(-8.0f, y);
            GL11.glVertex2f(8.0f, y);
        }
        GL11.glEnd();
        
        //Return to normal stencil state.
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        
        GL11.glPopMatrix();
        
        //Draw outline.
        GL11.glLineWidth(1.0f);
        float color = Main.instance().getFocus() == this ? 0.75f : 1.0f;
        GL11.glColor4f(color, color, 1.0f, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(-1.0f, -1.0f);
        GL11.glVertex2f(-1.0f, 1.0f);
        GL11.glVertex2f(1.0f, 1.0f);
        GL11.glVertex2f(1.0f, -1.0f);
        GL11.glEnd();
        
        GL11.glPopMatrix();
    }
}