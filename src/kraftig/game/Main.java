package kraftig.game;

import com.samrj.devil.display.HintSet;
import com.samrj.devil.game.Game;
import com.samrj.devil.game.GameConfig;
import com.samrj.devil.gl.DGL;
import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.graphics.GraphicsUtil;
import com.samrj.devil.io.MemStack;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec2i;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.ui.Alignment;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.stream.Stream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.UIManager;
import kraftig.game.Wire.WireNode;
import kraftig.game.gui.Crosshair;
import kraftig.game.gui.UIElement;
import kraftig.game.util.VectorFont;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public final class Main extends Game
{
    private static final float Z_NEAR = 1.0f/64.0f, Z_FAR = 128.0f;
    private static final float FOV = Util.toRadians(90.0f);
    
    public static final int SAMPLE_RATE = 48000;
    public static final double SAMPLE_WIDTH = 1.0/SAMPLE_RATE;
    public static final AudioFormat AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE, 16, 2, true, false);
    public static final DataLine.Info AUDIO_INPUT_INFO = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
    public static final DataLine.Info AUDIO_OUTPUT_INFO = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
    public static final int BUFFER_SIZE = 16384;
    
    private static Main INSTANCE;
    
    public static Main instance()
    {
        return INSTANCE;
    }
    
    private static HintSet hints()
    {
        HintSet hints = new HintSet();
        hints.hint(GLFW.GLFW_RED_BITS, 16);
        hints.hint(GLFW.GLFW_GREEN_BITS, 16);
        hints.hint(GLFW.GLFW_BLUE_BITS, 16);
        hints.hint(GLFW.GLFW_ALPHA_BITS, 0);
        hints.hint(GLFW.GLFW_DEPTH_BITS, 0);
        hints.hint(GLFW.GLFW_STENCIL_BITS, 8);
        hints.hint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        return hints;
    }
    
    private static GameConfig config()
    {
        GameConfig config = new GameConfig();
        config.fullscreen = false;
        config.resolution.set(1600, 900);
        config.msaa = 8;
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
    
    private final VectorFont font;
    private final Crosshair crosshair;
    private Camera3D camera;
    private final Player player;
    private final Skybox skybox;
    private final Grid floor;
    private ProjectSpace space;
    private final InteractionState defaultState;
    
    private boolean displayMouse = false;
    private final Vec3 mouseDir = new Vec3(0.0f, 0.0f, -1.0f);
    private FocusQuery focus;
    private InteractionState interactionState;
    private UIElement menu;
    private long time;
    
    private long startNanoTime;
    private boolean onFirstFrame = true;
    private double sampleRemainder = 0.0f;
    
    private Main() throws Exception
    {
        super("Kr\u00E4ftig Audio",  hints(), config());
        
        if (INSTANCE != null) throw new IllegalStateException();
        INSTANCE = this;
        
        DGL.init();
        mouse.setGrabbed(!displayMouse());
        
        font = new VectorFont("kraftig/res/fonts/DejaVuSans.ttf");
        crosshair = new Crosshair();
        camera = new Camera3D(Z_NEAR, Z_FAR, FOV, getResolution());
        player = new Player(keyboard, camera);
        skybox = new Skybox();
        floor = new Grid();
        space = new ProjectSpace();
        
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
            
            private void updateFocus()
            {
                focus = space.getFocus(camera.pos, Vec3.normalize(mouseDir));
            }
            
            @Override
            public void onMouseMoved(float x, float y, float dx, float dy)
            {
                if (menu != null)
                {
                    Vec2i res = getResolution();
                    focus = menu.checkFocus(0.0f, new Vec2(x - res.x*0.5f, y - res.y*0.5f));
                }
                else updateFocus();
            }
            
            @Override
            public void step(float dt)
            {
                if (menu == null) updateFocus();
            }
            
            @Override
            public void onMouseButton(int button, int action, int mods)
            {
                if (focus != null) focus.focus.onMouseButton(focus, button, action, mods);
            }
            
            @Override
            public void onKey(int key, int action, int mods)
            {
                if (action != GLFW.GLFW_PRESS) return;
                
                if (key == GLFW.GLFW_KEY_SPACE)
                {
                    if (menu instanceof DeviceMenu) closeMenu();
                    else if (menu == null) openMenu(new DeviceMenu());
                    return;
                }
                
                if (key == GLFW.GLFW_KEY_ESCAPE)
                {
                    if (menu != null) closeMenu();
                    else openMenu(new EscapeMenu());
                    return;
                }
                
                if (key != GLFW.GLFW_KEY_DELETE) return;
                if (focus == null) return;
                
                if (focus.focus instanceof Panel)
                {
                    Panel p = (Panel)focus.focus;
                    p.delete();
                    space.remove(p);
                    focus = null;
                }
                else if (focus.focus instanceof WireNode)
                {
                    WireNode n = (WireNode)focus.focus;
                    n.delete();
                    Wire w = n.getWire();
                    if (w.isDegenerate())
                    {
                        if (w.getIn() != null) w.disconnectIn();
                        if (w.getOut() != null) w.disconnectOut();
                        space.remove(w);
                    }
                    focus = null;
                }
            }
        };
        
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    
    private boolean displayMouse()
    {
        return (menu != null) || (displayMouse && interactionState.isCursorVisible());
    }
    
    public VectorFont getFont()
    {
        return font;
    }
    
    public void setState(InteractionState state)
    {
        if (state != defaultState) focus = null;
        interactionState = state;
        mouse.setGrabbed(!displayMouse());
    }
    
    public void setDefaultState()
    {
        setState(defaultState);
    }
    
    public Camera3D getCamera()
    {
        return camera;
    }
    
    public Player getPlayer()
    {
        return player;
    }
    
    public Vec3 getMouseDir()
    {
        return new Vec3(mouseDir);
    }
    
    public Focusable getFocus()
    {
        return focus != null ? focus.focus : null;
    }
    
    public void addWire(Wire wire)
    {
        space.add(wire);
    }
    
    public void addPanel(Panel panel)
    {
        space.add(panel);
    }
    
    public Stream<FocusQuery> focusStream()
    {
        return space.focusStream(camera.pos, mouseDir);
    }
    
    public void openMenu(UIElement menu)
    {
        if (!displayMouse())
        {
            mouse.setPosDirty();
            mouse.setGrabbed(false);
            Vec2i res = getResolution();
            mouse.setPos(res.x/2.0f, res.y/2.0f);
        }

        this.menu = menu;
        menu.setPos(new Vec2(), Alignment.C);
    }
    
    public void closeMenu()
    {
        if (menu == null) return;
        
        menu = null;
        mouse.setGrabbed(!displayMouse());
    }
    
    public void newProject()
    {
        boolean result = TinyFileDialogs.tinyfd_messageBox("Kr\u00E4ftig Audio",
                "Are you sure you want to start a new project? All unsaved work will be lost.",
                "okcancel", "question", false);
        
        if (result)
        {
            space.delete();
            space = new ProjectSpace();
            closeMenu();
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    public void save()
    {
        PointerBuffer filterP = PointerBuffer.allocateDirect(1);
        filterP.put(MemStack.wrap("*.krpf"));
        filterP.rewind();
        String path = TinyFileDialogs.tinyfd_saveFileDialog("Save Project", null, filterP, null);
        MemStack.pop();
        
        if (path != null)
        {
            if (!path.toLowerCase().endsWith(".krpf")) path += ".krpf";
            
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(path)))
            {
                out.writeUTF("Kr\u00E4ftig");
                space.save(out);
                closeMenu();
            }
            catch (Exception e)
            {
                TinyFileDialogs.tinyfd_messageBox("Kr\u00E4ftig Audio",
                        "Failed to save project: " + e.toString(),
                        "ok", "error", false);
            }
        }
    }
    
    public void open()
    {
        PointerBuffer filterP = PointerBuffer.allocateDirect(1);
        filterP.put(MemStack.wrap("*.krpf"));
        filterP.rewind();
        String path = TinyFileDialogs.tinyfd_openFileDialog("Open Project", null, filterP, null, false);
        MemStack.pop();
        
        if (path != null)
        {
            try (DataInputStream in = new DataInputStream(new FileInputStream(path)))
            {
                try
                {
                    String header = in.readUTF();
                    if (!header.equals("Kr\u00E4ftig")) throw new IOException();
                }
                catch (IOException e)
                {
                    throw new IOException("Corrupt/unknown file format.");
                }
                
                ProjectSpace newSpace = new ProjectSpace();
                newSpace.load(in);
                
                space.delete();
                space = newSpace;
                
                closeMenu();
            }
            catch (Exception e)
            {
                TinyFileDialogs.tinyfd_messageBox("Kr\u00E4ftig Audio",
                        "Failed to load project: " + e.toString(),
                        "ok", "error", false);
            }
        }
    }
    // </editor-fold>
    
    public void exit()
    {
        boolean result = TinyFileDialogs.tinyfd_messageBox("Kr\u00E4ftig Audio",
                "Are you sure you want to exit? All unsaved work will be lost.",
                "okcancel", "question", false);
        
        if (result) stop();
    }
    
    public long getTime()
    {
        return time;
    }
    
    public long getStartNanoTime()
    {
        return startNanoTime;
    }
    
    @Override
    public void onMouseMoved(float x, float y, float dx, float dy)
    {
        if (menu == null)
        {
            if (!displayMouse && interactionState.canPlayerAim()) player.onMouseMoved(x, y, dx, dy);

            Vec2 mPos = new Vec2();
            if (displayMouse)
            {
                Vec2i res = getResolution();
                mPos.set((mouse.getX()/res.x)*2.0f - 1.0f, (mouse.getY()/res.y)*2.0f - 1.0f);
            }
            
            Vec3 fwd = camera.forward;
            Vec3.madd(fwd, camera.right, mPos.x*camera.hSlope, mouseDir);
            mouseDir.madd(camera.up, mPos.y*camera.vSlope);
            
            //Project mouse direction onto vertical plane.
            mouseDir.div((mouseDir.x*fwd.x + mouseDir.z*fwd.z)/camera.up.y);
        }
        
        interactionState.onMouseMoved(x, y, dx, dy);
    }
    
    @Override
    public void onMouseButton(int button, int action, int mods)
    {
        interactionState.onMouseButton(button, action, mods);
    }
    
    @Override
    public void onMouseScroll(float dx, float dy)
    {
        interactionState.onMouseScroll(dx, dy);
    }

    @Override
    public void onKey(int key, int action, int mods)
    {
        if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_TAB && menu == null)
        {
            displayMouse = !displayMouse;
            boolean display = displayMouse();
            mouse.setGrabbed(!display);

            if (!display) mouse.setPosDirty();
            Vec2i res = getResolution();
            mouse.setPos(res.x/2.0f, res.y/2.0f);
        }
        
        interactionState.onKey(key, action, mods);
    }
    
    @Override
    public void step(float dt)
    {
        player.step(dt);
        interactionState.step(dt);
        
        //Calculate number of samples to process.
        double exactSamples = dt*(double)SAMPLE_RATE;
        int samples = (int)Math.floor(exactSamples);
        sampleRemainder += exactSamples - samples;
        if (sampleRemainder > 1.0)
        {
            int extra = (int)Math.floor(sampleRemainder);
            samples += extra;
            sampleRemainder -= extra;
        }
        
        //Set first frame time if not set.
        if (onFirstFrame)
        {
            startNanoTime = System.nanoTime();
            onFirstFrame = false;
        }
        
        //Update all devices in topological order.
        for (Panel panel : space.sortPanels()) panel.process(samples);
        time += samples;
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
        space.render();
        
        //Load screen matrix to draw HUD.
        Vec2i res = getResolution();
        if (res.x <= 0 || res.y <= 0) return;
        
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(-res.x*0.5f, res.x*0.5f, -res.y*0.5f, res.y*0.5f, -1.0f, 1.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        
        if (menu == null && !displayMouse && interactionState == defaultState)
            crosshair.renderCrosshair();
        
        if (menu != null) menu.render(1.0f);
    }
    
    @Override
    public void onResized(int width, int height)
    {
        if (width > 0 && height > 0)
        {
            camera = new Camera3D(Z_NEAR, Z_FAR, FOV, getResolution());
            player.setCamera(camera);
        }
    }
    
    @Override
    public void onDestroy()
    {
        space.delete();
        DGL.destroy();
    }
}
