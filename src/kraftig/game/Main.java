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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import kraftig.game.Wire.WireNode;
import kraftig.game.device.*;
import kraftig.game.gui.Crosshair;
import kraftig.game.util.ConcatList;
import kraftig.game.util.VectorFont;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

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
    private final FloorGrid floor;
    private final ArrayList<Panel> panels = new ArrayList<>();
    private final ArrayList<Wire> wires = new ArrayList<>();
    private final InteractionState defaultState;
    
    private boolean displayMouse = false;
    private final Vec3 mouseDir = new Vec3(0.0f, 0.0f, -1.0f);
    private FocusQuery focus;
    private InteractionState interactionState;
    private DeviceMenu deviceMenu;
    private long time;
    
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
        floor = new FloorGrid();
        
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
                focus = Stream.concat(panels.stream().map(p -> p.checkFocus(camera.pos, mouseDir)),
                                      wires.stream().map(w -> w.checkFocus(camera.pos, mouseDir)))
                        .filter(q -> q != null)
                        .reduce((a, b) -> a.dist < b.dist ? a : b)
                        .orElse(null);
            }
            
            @Override
            public void onMouseMoved(float x, float y, float dx, float dy)
            {
                if (deviceMenu != null)
                {
                    Vec2i res = getResolution();
                    focus = deviceMenu.checkFocus(0.0f, new Vec2(x - res.x*0.5f, y - res.y*0.5f));
                }
                else updateFocus();
            }
            
            @Override
            public void step(float dt)
            {
                if (deviceMenu == null) updateFocus();
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
                    if (deviceMenu != null) closeDeviceMenu();
                    else
                    {
                        if (!displayMouse())
                        {
                            mouse.setPosDirty();
                            mouse.setGrabbed(false);
                            Vec2i res = getResolution();
                            mouse.setPos(res.x/2.0f, res.y/2.0f);
                        }
                        
                        deviceMenu = new DeviceMenu();
                        deviceMenu.setPos(new Vec2(), Alignment.C);
                    }
                    
                    return;
                }
                
                if (key != GLFW.GLFW_KEY_DELETE) return;
                if (focus == null) return;
                
                if (focus.focus instanceof Panel)
                {
                    Panel p = (Panel)focus.focus;
                    p.delete();
                    panels.remove(p);
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
                        wires.remove(w);
                    }
                    focus = null;
                }
            }
        };
    }
    
    private boolean displayMouse()
    {
        return (deviceMenu != null) || (displayMouse && interactionState.isCursorVisible());
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
        wires.add(wire);
    }
    
    public void addPanel(Panel panel)
    {
        panels.add(panel);
    }
    
    public Stream<FocusQuery> focusStream()
    {
        return Stream.concat(panels.stream().map(p -> p.checkFocus(camera.pos, mouseDir)),
                             wires.stream().map(w -> w.checkFocus(camera.pos, mouseDir)))
                .filter(q -> q != null);
    }
    
    public void closeDeviceMenu()
    {
        if (deviceMenu == null) return;
        
        deviceMenu = null;
        mouse.setGrabbed(!displayMouse());
    }
    
    public long getTime()
    {
        return time;
    }
    
    @Override
    public void onMouseMoved(float x, float y, float dx, float dy)
    {
        if (deviceMenu == null)
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
        if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_ESCAPE) stop();
        
        if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_TAB && deviceMenu == null)
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
        
        //Sort devices by topological order.
        DAG<AudioDevice> dag = new DAG<>();
        for (Panel panel : panels) if (panel instanceof AudioDevice)
        {
            AudioDevice device = (AudioDevice)panel;
            dag.add(device);
            device.getInputDevices().forEach(in ->
            {
                dag.add(in);
                dag.addEdgeSafe(in, device);
            });
        }
        
        //Update all devices.
        for (AudioDevice device : dag.sort()) device.process(samples);
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
        
        //Update panel positions.
        for (Panel p : panels) p.updateEdge();
        
        //Calculate wire splits.
        List<? extends Drawable> drawList = panels;
        for (Wire w : wires) drawList = new ConcatList<>(drawList, w.updateSplits(panels));
        
        //Sort and draw world objects.
        DAG<Drawable> overlapGraph = new DAG<>();
        for (Drawable draw : drawList) overlapGraph.add(draw);
        
        for (int i=0; i<drawList.size(); i++) for (int j=i+1; j<drawList.size(); j++)
        {
            Drawable a = drawList.get(i), b = drawList.get(j);
            
            switch (Overlap.get(a, b, camera))
            {
                case A_BEHIND_B: overlapGraph.addEdge(a, b); break;
                case B_BEHIND_A: overlapGraph.addEdge(b, a); break;
            }
        }
        
        for (Drawable draw : overlapGraph.sort()) draw.render();
        
        //Load screen matrix to draw HUD.
        Vec2i res = getResolution();
        if (res.x <= 0 || res.y <= 0) return;
        
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(-res.x*0.5f, res.x*0.5f, -res.y*0.5f, res.y*0.5f, -1.0f, 1.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        
        if (deviceMenu == null && !displayMouse && interactionState == defaultState)
            crosshair.renderCrosshair();
        
        if (deviceMenu != null) deviceMenu.render(1.0f);
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
        for (Panel panel : panels) panel.delete();
        DGL.destroy();
    }
}
