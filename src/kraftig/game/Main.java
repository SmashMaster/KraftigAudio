package kraftig.game;

import com.samrj.devil.display.HintSet;
import com.samrj.devil.game.Game;
import com.samrj.devil.game.GameConfig;
import com.samrj.devil.gl.DGL;
import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.graphics.GraphicsUtil;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Vec2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Main extends Game
{
    private static final int CROSSHAIR_WIDTH = 12;
    private static final int CROSSHAIR_INNER_WIDTH = 4;
    
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
        
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL13.GL_MULTISAMPLE);
    }
    
    @Override
    public void onMouseMoved(float x, float y, float dx, float dy)
    {
        player.onMouseMoved(x, y, dx, dy);
    }

    @Override
    public void onMouseButton(int button, int action, int mods)
    {
    }
    
    @Override
    public void onMouseScroll(float dx, float dy)
    {
    }

    @Override
    public void onKey(int key, int action, int mods)
    {
        if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_ESCAPE) stop();
    }
    
    @Override
    public void step(float dt)
    {
        player.step(dt);
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
        
        //Load screen matrix to draw HUD.
        Vec2i res = getResolution();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(-res.x*0.5f, res.x*0.5f, -res.y*0.5f, res.y*0.5f, -1.0f, 1.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(-CROSSHAIR_WIDTH, 0.0f);
        GL11.glVertex2f(-CROSSHAIR_INNER_WIDTH, 0.0f);
        GL11.glVertex2f(CROSSHAIR_WIDTH, 0.0f);
        GL11.glVertex2f(CROSSHAIR_INNER_WIDTH, 0.0f);
        GL11.glVertex2f(0.0f, -CROSSHAIR_WIDTH);
        GL11.glVertex2f(0.0f, -CROSSHAIR_INNER_WIDTH);
        GL11.glVertex2f(0.0f, CROSSHAIR_WIDTH);
        GL11.glVertex2f(0.0f, CROSSHAIR_INNER_WIDTH);
        GL11.glEnd();
    }
    
    @Override
    public void onDestroy()
    {
        DGL.destroy();
    }
}
