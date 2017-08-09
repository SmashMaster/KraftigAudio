package kraftig.game;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import org.lwjgl.glfw.GLFW;

public class PanelDragState implements InteractionState
{
    private final Panel panel;
    private final float relYaw;
    private final Vec2 mp;
    private final float dist;
    
    public PanelDragState(Panel panel)
    {
        this.panel = panel;
        Player player = Main.instance().getPlayer();
        relYaw = Util.reduceAngle(panel.getYaw() - player.getYaw());
        
        boolean[] hit = {false};
        float[] rDist = {0.0f};
        mp = new Vec2();
        int[] side = {0};
        panel.projectMouse(hit, rDist, mp, side);
        
        dist = rDist[0];
        panel.dragged = true;
    }
    
    private void update()
    {
        Player player = Main.instance().getPlayer();
        
        Vec3 pos = new Vec3(Main.instance().getCamera().pos);
        pos.madd(Main.instance().getMouseDir(), dist);
        pos.madd(panel.rightDir, -mp.x);
        pos.y -= mp.y;
        
        float floor = Grid.FLOOR_HEIGHT + panel.getHeight();
        pos.y = Util.clamp(pos.y, floor, -floor);
        
        float yaw = Util.reduceAngle(player.getYaw() + relYaw);
        
        panel.setPosYaw(pos, yaw);
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

        panel.dragged = false;
        Main.instance().setDefaultState();
    }

    @Override
    public void step(float dt)
    {
        update();
    }
}
