package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Main;
import kraftig.game.Panel;
import org.lwjgl.opengl.GL11;

public class MidiSequencerScreen implements UIElement
{
    private final UIElement layout;
    
    public MidiSequencerScreen()
    {
        layout = new RowLayout(4.0f, Alignment.C, new Label("screen", 32.0f));
    }
    
    @Override
    public Vec2 getPos()
    {
        return layout.getPos();
    }

    @Override
    public Vec2 getRadius()
    {
        return layout.getRadius();
    }

    @Override
    public MidiSequencerScreen setPos(Vec2 pos, Alignment align)
    {
        layout.setPos(pos, align);
        return this;
    }

    @Override
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
    {
        layout.updateMatrix(matrix, panel, front);
    }

    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        UIFocusQuery out = layout.checkFocus(dist, p);
        if (out != null) return out;
        
        Vec2 pos = getPos();
        Vec2 radius = getRadius();
        if (p.x < pos.x - radius.x || p.x > pos.x + radius.x) return null;
        if (p.y < pos.y - radius.y || p.y > pos.y + radius.y) return null;
        return new UIFocusQuery(this, dist, p);
    }

    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
    }

    @Override
    public void delete()
    {
        layout.delete();
    }

    @Override
    public void render(float alpha)
    {
        layout.render(alpha);
        
        GL11.glLineWidth(1.0f);
        float color = Main.instance().getFocus() == this ? 0.75f : 1.0f;
        GL11.glColor4f(color, color, 1.0f, alpha);
        
        Vec2 pos = getPos();
        Vec2 radius = getRadius();
        
        GL11.glLineWidth(alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(pos.x - radius.x, pos.y - radius.y);
        GL11.glVertex2f(pos.x - radius.x, pos.y + radius.y);
        GL11.glVertex2f(pos.x + radius.x, pos.y + radius.y);
        GL11.glVertex2f(pos.x + radius.x, pos.y - radius.y);
        GL11.glEnd();
    }
}
