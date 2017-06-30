package kraftig.game.gui;

import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.InteractionState;
import kraftig.game.Main;
import kraftig.game.Wire;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Jack implements UIElement
{
    private static final int SEGMENTS = 32;
    private static final float DT = 8.0f/(SEGMENTS - 1);
    private static final float T_END = 8.0f + DT*0.5f;
    private static final float RADIUS = 16.0f;
    private static final float RADIUS_SQ = RADIUS*RADIUS;
    private static final float RADIUS_HALF = RADIUS/2.0f;
    private static final float WIRE_OFFSET = 8.0f;
    
    private final Vec2 pos = new Vec2();
    private final Mat4 matrix = new Mat4();
    
    private Wire wire;
    
    public Jack(Vec2 pos, Alignment align)
    {
        Vec2 av = new Vec2(align.x, align.y).mult(RADIUS);
        this.pos.set(pos).add(av);
    }
    
    @Override
    public void updateMatrix(Mat4 matrix)
    {
        this.matrix.set(matrix);
        if (wire != null) wire.pos.set(pos.x, pos.y, WIRE_OFFSET).mult(matrix);
    }
    
    @Override
    public InteractionState onMouseButton(Vec2 mPos, int button, int action, int mods)
    {
        if (action != GLFW.GLFW_PRESS || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return null;
        if (wire != null) return null;
        
        if (mPos.squareDist(pos) <= RADIUS_SQ) return new InteractionState()
        {
            private Runnable updater;
            private Wire next;
            
            @Override
            public boolean canPlayerAim()
            {
                return true;
            }
            
            @Override
            public void init(Main main)
            {
                Camera3D camera = main.getCamera();
                
                wire = new Wire();
                next = wire.attachNext();
                main.addWire(wire);
                updateMatrix(matrix);
                
                float dist = camera.pos.dist(wire.pos);
                
                updater = () ->
                {
                    next.pos.set(camera.pos);
                    next.pos.madd(main.getMouseDir(), dist);
                };
            }
            
            @Override
            public void onMouseMoved(Main main, float x, float y, float dx, float dy)
            {
                updater.run();
            }
            
            @Override
            public void onMouseButton(Main main, int button, int action, int mods)
            {
                if (action == GLFW.GLFW_PRESS && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                    main.setDefaultState();
            }
            
            @Override
            public void step(Main main, float dt)
            {
                updater.run();
            }
        };
        else return null;
    }

    @Override
    public void render(float alpha)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef(pos.x, pos.y, 0.0f);
        
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha*0.5f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (float t = 0.0f; t < T_END; t += DT)
        {
            Vec2 p = Util.squareDir(t).normalize().mult(RADIUS);
            GL11.glVertex2f(p.x, p.y);
        }
        GL11.glEnd();
        
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (float t = 0.0f; t < T_END; t += DT)
        {
            Vec2 p = Util.squareDir(t).normalize().mult(RADIUS_HALF);
            GL11.glVertex2f(p.x, p.y);
        }
        GL11.glEnd();
        
        GL11.glPopMatrix();
    }
}
