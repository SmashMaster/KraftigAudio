package kraftig.game;

import com.samrj.devil.display.HintSet;
import com.samrj.devil.game.Game;
import com.samrj.devil.game.GameConfig;
import com.samrj.devil.gl.DGL;
import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.graphics.GraphicsUtil;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec2i;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.ui.Alignment;
import java.util.ArrayList;
import java.util.ListIterator;
import kraftig.game.Panel.ClickResult;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Main extends Game
{
    private static HintSet hints()
    {
        HintSet hints = new HintSet();
        hints.hint(GLFW.GLFW_RED_BITS, 8);
        hints.hint(GLFW.GLFW_GREEN_BITS, 8);
        hints.hint(GLFW.GLFW_BLUE_BITS, 8);
        hints.hint(GLFW.GLFW_ALPHA_BITS, 0);
        hints.hint(GLFW.GLFW_DEPTH_BITS, 0);
        hints.hint(GLFW.GLFW_STENCIL_BITS, 0);
        return hints;
    }
    
    private static GameConfig config()
    {
        GameConfig config = new GameConfig();
        config.fullscreen = false;
        config.resolution.set(1600, 900);
        config.msaa = 4;
        return config;
    }
    
    public static void main(String[] args)
    {
        try
        {
            run(Main::new);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    private final UI ui;
    private final Player player;
    private final Camera3D camera;
    private final Skybox skybox;
    private final FloorGrid floor;
    
    private final ArrayList<Panel> panels = new ArrayList<>();
    
    private boolean mouseGrabbed = true;
    
    private Main() throws Exception
    {
        super("Kr\u00E4ftig Audio",  hints(), config());
        
        DGL.init();
        mouse.setGrabbed(true);
        
        ui = new UI();
        player = new Player(keyboard, getResolution());
        camera = player.getCamera();
        skybox = new Skybox();
        floor = new FloorGrid();
        
        Panel panel = new Panel();
        panel.setPosition(new Vec3(0.0f, 1.75f, -1.0f));
        panel.setSize(0.25f, 0.125f);
        panel.setYaw(Util.toRadians(0.0f));
        panel.getFrontInterface()
                .add(new Knob(new Vec2(8.0f, 0.0f), Alignment.E, 32.0f))
                .add(new Label(ui, "Front", new Vec2(-8.0f, 0.0f), Alignment.W));
        panel.getRearInterface().add(new Label(ui, "Rear", new Vec2(), Alignment.C));
        panels.add(panel);
        
        panel = new Panel();
        panel.setPosition(new Vec3(0.25f, 1.75f, -0.5f));
        panel.setSize(0.25f, 0.125f);
        panel.setYaw(Util.toRadians(-10.0f));
        panels.add(panel);
        
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL13.GL_MULTISAMPLE);
    }
    
    @Override
    public void onMouseMoved(float x, float y, float dx, float dy)
    {
        if (mouseGrabbed) player.onMouseMoved(x, y, dx, dy);
    }

    @Override
    public void onMouseButton(int button, int action, int mods)
    {
        if (action == GLFW.GLFW_PRESS && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            Vec2 mPos = new Vec2();
            
            if (!mouseGrabbed)
            {
                Vec2i res = getResolution();
                mPos.set((mouse.getX()/res.x)*2.0f - 1.0f, (mouse.getY()/res.y)*2.0f - 1.0f);
            }
            
            Vec3 dir = Vec3.madd(camera.forward, camera.right, mPos.x*camera.hSlope);
            dir.madd(camera.up, mPos.y*camera.vSlope);
            dir.normalize();
            
            ListIterator<Panel> it = panels.listIterator(panels.size());
            while (it.hasPrevious())
            {
                Panel panel = it.previous();
                ClickResult result = panel.onClick(camera.pos, dir);
                if (result != ClickResult.MISSED) break;
            }
        }
    }
    
    @Override
    public void onMouseScroll(float dx, float dy)
    {
    }

    @Override
    public void onKey(int key, int action, int mods)
    {
        if (action == GLFW.GLFW_PRESS)
        {
            if (key == GLFW.GLFW_KEY_ESCAPE) stop();
            else if (key == GLFW.GLFW_KEY_TAB)
            {
                mouse.setGrabbed(mouseGrabbed = !mouseGrabbed);
                if (!mouseGrabbed)
                {
                    Vec2i res = getResolution();
                    GLFW.glfwSetCursorPos(window, res.x/2.0, res.y/2.0);
                    mouse.cursorPos(res.x/2.0f, res.y/2.0f);
                }
            }
        }
    }
    
    @Override
    public void step(float dt)
    {
        player.step(dt);
        panels.sort((a, b) -> Util.compare(b.dist(camera.pos), a.dist(camera.pos)));
    }
    
    @Override
    public void render()
    {
        //Make camera rotation matrix (no translation) to draw skybox.
        GraphicsUtil.glLoadMatrix(camera.projMat, GL11.GL_PROJECTION);
        GraphicsUtil.glLoadMatrix(Mat3.rotation(Quat.invert(camera.dir)), GL11.GL_MODELVIEW);
        
        skybox.render();
        
        //Load proper camera matrix to draw world.
        GraphicsUtil.glLoadMatrix(camera.viewMat, GL11.GL_MODELVIEW);
        
        floor.render();
        
        for (Panel panel : panels) panel.render(camera.pos);
        
        //Load screen matrix to draw HUD.
        Vec2i res = getResolution();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(-res.x*0.5f, res.x*0.5f, -res.y*0.5f, res.y*0.5f, -1.0f, 1.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        
        ui.renderHUD(mouseGrabbed);
    }
    
    @Override
    public void onDestroy()
    {
        DGL.destroy();
    }
}
