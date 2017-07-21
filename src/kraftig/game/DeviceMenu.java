package kraftig.game;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec2i;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.ui.Alignment;
import java.util.function.Supplier;
import kraftig.game.device.SystemInput;
import kraftig.game.gui.LabelButton;
import kraftig.game.gui.ScrollBox;
import kraftig.game.gui.UIElement;
import kraftig.game.gui.UIFocusQuery;
import org.lwjgl.opengl.GL11;

public class DeviceMenu implements UIElement
{
    private final ScrollBox scrollBox;
    
    public DeviceMenu()
    {
        Vec2i res = Main.instance().getResolution();
        float height = res.y/2.0f - 16.0f;
        scrollBox = new ScrollBox(new Vec2(height*0.75f, height));
        
        scrollBox.setContent(new SpawnButton("System Input", SystemInput::new));
    }
    
    @Override
    public Vec2 getPos()
    {
        return scrollBox.getPos();
    }

    @Override
    public Vec2 getRadius()
    {
        return scrollBox.getRadius();
    }

    @Override
    public DeviceMenu setPos(Vec2 pos, Alignment align)
    {
        scrollBox.setPos(pos, align);
        return this;
    }

    @Override
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
    {
        scrollBox.updateMatrix(matrix, panel, front);
    }

    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        return scrollBox.checkFocus(dist, p);
    }

    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
    }

    @Override
    public void delete()
    {
        scrollBox.delete();
    }

    @Override
    public void render(float alpha)
    {
        Vec2 p = scrollBox.getPos();
        Vec2 r = scrollBox.getRadius();
        
        float x0 = p.x - r.x, x1 = p.x + r.x;
        float y0 = p.y - r.y, y1 = p.y + r.y;
        
        GL11.glColor4f(0.4375f, 0.4375f, 0.4375f, 0.875f*alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x0, y0);
        GL11.glVertex2f(x0, y1);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y0);
        GL11.glEnd();
        
        scrollBox.render(alpha);
    }
    
    private class SpawnButton extends LabelButton
    {
        private final Supplier<Panel> constructor;
        
        private SpawnButton(String text, Supplier<Panel> constructor)
        {
            super(text, 16.0f, 4.0f);
            this.constructor = constructor;
            onClick(this::spawn);
        }
        
        private void spawn()
        {
            Main main = Main.instance();
            
            Panel panel = constructor.get();
            main.addPanel(panel);
            main.closeDeviceMenu();
            main.onMouseMoved(0.0f, 0.0f, 0.0f, 0.0f);
            
            panel.setPosYaw(Vec3.madd(main.getCamera().pos, main.getMouseDir(), 0.5f), main.getPlayer().getYaw());
            main.setState(new PanelDragState(panel));
        }
    }
}
