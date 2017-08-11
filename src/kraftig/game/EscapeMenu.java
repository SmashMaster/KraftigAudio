package kraftig.game;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec2i;
import com.samrj.devil.ui.Alignment;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Label;
import kraftig.game.gui.LabelButton;
import kraftig.game.gui.UIElement;
import kraftig.game.gui.UIFocusQuery;
import org.lwjgl.opengl.GL11;

public class EscapeMenu implements UIElement
{
    private final ColumnLayout layout;
    
    public EscapeMenu()
    {
        Main instance = Main.instance();
        
        layout = new ColumnLayout(64.0f, Alignment.C,
                new Label("Kr\u00E4ftig Audio", 64.0f),
                new ColumnLayout(8.0f, Alignment.C,
                    new LabelButton("Resume", 24.0f, 4.0f)
                        .onClick(instance::closeMenu),
                    new LabelButton("Save", 24.0f, 4.0f)
                        .onClick(instance::save),
                    new LabelButton("Load", 24.0f, 4.0f)
                        .onClick(instance::load),
                    new LabelButton("Exit", 24.0f, 4.0f)
                        .onClick(instance::exit)));
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
    public EscapeMenu setPos(Vec2 pos, Alignment align)
    {
        layout.setPos(pos, align);
        return this;
    }

    @Override
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        UIFocusQuery query = layout.checkFocus(dist, p);
        if (query != null) return query;
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
        Vec2i res = Main.instance().getResolution();
        
        GL11.glColor4f(0.4375f, 0.4375f, 0.4375f, 0.875f*alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-res.x*0.5f, -res.y*0.5f);
        GL11.glVertex2f(-res.x*0.5f, res.y*0.5f);
        GL11.glVertex2f(res.x*0.5f, res.y*0.5f);
        GL11.glVertex2f(res.x*0.5f, -res.y*0.5f);
        GL11.glEnd();
        
        layout.render(alpha);
    }
}
