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
import com.samrj.devil.math.topo.DAG;
import com.samrj.devil.ui.Alignment;
import com.samrj.devil.ui.AtlasFont;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kraftig.game.Panel.ClickResult;
import kraftig.game.gui.Crosshair;
import kraftig.game.gui.Jack;
import kraftig.game.gui.Knob;
import kraftig.game.gui.Label;
import kraftig.game.gui.UI;
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
        hints.hint(GLFW.GLFW_DEPTH_BITS, 16);
        hints.hint(GLFW.GLFW_STENCIL_BITS, 0);
        hints.hint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
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
    
    private final AtlasFont font;
    private final Crosshair crosshair;
    private final Player player;
    private final Camera3D camera;
    private final Skybox skybox;
    private final FloorGrid floor;
    private final ArrayList<Panel> panels = new ArrayList<>();
    private final ArrayList<Wire> wires = new ArrayList<>();
    private final InteractionState defaultState;
    
    private List<DrawPlane> sortedPlanes = Collections.EMPTY_LIST;
    
    private boolean displayMouse = false;
    private final Vec3 mouseDir = new Vec3(0.0f, 0.0f, -1.0f);
    private InteractionState interactionState;
    
    private Main() throws Exception
    {
        super("Kr\u00E4ftig Audio",  hints(), config());
        
        DGL.init();
        mouse.setGrabbed(!displayMouse());
        
        font = new AtlasFont("kraftig/res/fonts/", "menu.fnt");
        crosshair = new Crosshair();
        player = new Player(keyboard, getResolution());
        camera = player.getCamera();
        skybox = new Skybox();
        floor = new FloorGrid();
        
        Panel panel = new Panel();
        panel.setPosition(new Vec3(0.25f, 1.75f, -1.0f));
        panel.setSize(0.25f, 0.125f);
        panel.setYaw(Util.toRadians(-20.0f));
        {
            UI front = panel.frontInterface;
            
            Knob knob = new Knob(new Vec2(0.0f, 0.0f), Alignment.C, 32.0f);
            Label vLabel = new Label(font, "", new Vec2(40.0f, 0.0f), Alignment.E);
            knob.onValueChanged(v -> vLabel.setText("" + Math.round(v*256.0f)));
            
            front.add(knob);
            front.add(new Label(font, "Value:", new Vec2(-40.0f, 0.0f), Alignment.W));
            front.add(vLabel);
            front.add(new Jack(new Vec2(0.0f, -40.0f), Alignment.S));
        }
        panel.rearInterface.add(new Label(font, "Rear", new Vec2(), Alignment.C));
        panels.add(panel);
        
        panel = new Panel();
        panel.setPosition(new Vec3(-0.25f, 1.75f, -1.0f));
        panel.setSize(0.25f, 0.125f);
        panel.setYaw(Util.toRadians(20.0f));
        panel.rearInterface.add(new Jack(new Vec2(), Alignment.C));
        panels.add(panel);
        
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL13.GL_MULTISAMPLE);
        
        interactionState = defaultState = new InteractionState()
        {
            @Override
            public boolean canPlayerAim()
            {
                return true;
            }
            
            @Override
            public void onMouseButton(Main main, int button, int action, int mods)
            {
                ListIterator<DrawPlane> it = sortedPlanes.listIterator(sortedPlanes.size());
                while (it.hasPrevious())
                {
                    DrawPlane p = it.previous();
                    if (p.drawable instanceof Panel)
                    {
                        Panel panel = (Panel)p.drawable;
                        ClickResult result = panel.onMouseButton(player, mouseDir, button, action, mods);

                        if (result.newState != null) setState(result.newState);
                        if (result.hit) break;
                    }
                }
            }
        };
    }
    
    private boolean displayMouse()
    {
        return displayMouse && interactionState.isCursorVisible(this);
    }
    
    public void setState(InteractionState state)
    {
        interactionState = state;
        mouse.setGrabbed(!displayMouse());
    }
    
    public void setDefaultState()
    {
        setState(defaultState);
    }
    
    public Vec3 getMouseDir()
    {
        return new Vec3(mouseDir);
    }
    
    public void addWire()
    {
    }
    
    @Override
    public void onMouseMoved(float x, float y, float dx, float dy)
    {
        if (!displayMouse && interactionState.canPlayerAim()) player.onMouseMoved(x, y, dx, dy);
        
        Vec2 mPos = new Vec2();
        if (displayMouse)
        {
            Vec2i res = getResolution();
            mPos.set((mouse.getX()/res.x)*2.0f - 1.0f, (mouse.getY()/res.y)*2.0f - 1.0f);
        }

        Vec3.madd(camera.forward, camera.right, mPos.x*camera.hSlope, mouseDir);
        mouseDir.madd(camera.up, mPos.y*camera.vSlope);
        mouseDir.normalize();
        
        interactionState.onMouseMoved(this, x, y, dx, dy);
    }
    
    @Override
    public void onMouseButton(int button, int action, int mods)
    {
        interactionState.onMouseButton(this, button, action, mods);
    }
    
    @Override
    public void onMouseScroll(float dx, float dy)
    {
        interactionState.onMouseScroll(this, dx, dy);
    }

    @Override
    public void onKey(int key, int action, int mods)
    {
        if (action == GLFW.GLFW_PRESS)
        {
            if (key == GLFW.GLFW_KEY_ESCAPE) stop();
            else if (key == GLFW.GLFW_KEY_TAB)
            {
                displayMouse = !displayMouse;
                boolean display = displayMouse();
                mouse.setGrabbed(!display);
                if (display)
                {
                    Vec2i res = getResolution();
                    GLFW.glfwSetCursorPos(window, res.x/2.0, res.y/2.0);
                    mouse.cursorPos(res.x/2.0f, res.y/2.0f);
                }
            }
        }
        
        interactionState.onKey(this, key, action, mods);
    }
    
    @Override
    public void step(float dt)
    {
        player.step(dt);
        
        DAG<DrawPlane> drawGraph = new DAG<>();
        List<DrawPlane> planes = Stream.concat(panels.stream(), wires.stream()).map(DrawPlane::new).collect(Collectors.toList());
        
        for (DrawPlane plane : planes)
        {
            plane.update(camera);
            drawGraph.add(plane);
        }
        
        for (int i=0; i<planes.size(); i++) for (int j=i+1; j<planes.size(); j++)
        {
            DrawPlane a = planes.get(i), b = planes.get(j);
            
            switch (Overlap.get(a, b, camera))
            {
                case A_BEHIND_B: drawGraph.addEdge(a, b); break;
                case B_BEHIND_A: drawGraph.addEdge(b, a); break;
            }
        }
        
        System.out.println(drawGraph.getEdges().size());
        
        sortedPlanes = drawGraph.sort();
        
        interactionState.step(this, dt);
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
        
        for (DrawPlane plane : sortedPlanes) plane.render(camera, 1.0f);
        
        //Load screen matrix to draw HUD.
        Vec2i res = getResolution();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(-res.x*0.5f, res.x*0.5f, -res.y*0.5f, res.y*0.5f, -1.0f, 1.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        
        if (!displayMouse && interactionState == defaultState) crosshair.renderCrosshair();
    }
    
    @Override
    public void onDestroy()
    {
        DGL.destroy();
    }
}
