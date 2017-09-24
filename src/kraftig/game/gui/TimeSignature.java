package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Panel;
import kraftig.game.SongProperties;
import kraftig.game.SongProperties.UpdateCallback;
import kraftig.game.gui.buttons.SymbolButton;
import org.lwjgl.opengl.GL11;

public class TimeSignature implements UIElement
{
    private static void drawUpSymbol()
    {
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex2f(-0.75f, -0.5f);
        GL11.glVertex2f(0.0f, 0.5f);
        GL11.glVertex2f(0.75f, -0.5f);
        GL11.glEnd();
    }
    
    private static void drawDownSymbol()
    {
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex2f(-0.75f, 0.5f);
        GL11.glVertex2f(0.0f, -0.5f);
        GL11.glVertex2f(0.75f, 0.5f);
        GL11.glEnd();
    }
    
    private static final float WIDTH = 16.0f, HEIGHT = 6.5f, BUTTON_SIZE = HEIGHT/2.0f;
    
    private final RowLayout layout;
    private final TextBox textBox;
    
    private final UpdateCallback callback;
    
    public TimeSignature(SongProperties props)
    {
        layout = new RowLayout(0.0f, Alignment.C,
            new ColumnLayout(0.0f, Alignment.C,
                new SymbolButton(new Vec2(BUTTON_SIZE), TimeSignature::drawUpSymbol)
                    .onClick(() -> props.setTimeSignature(props.tsBeatsPerBar + 1, props.tsBeatNoteValue)),
                new SymbolButton(new Vec2(BUTTON_SIZE), TimeSignature::drawDownSymbol)
                    .onClick(() -> props.setTimeSignature(props.tsBeatsPerBar - 1, props.tsBeatNoteValue))),
            textBox = new TextBox(new Vec2(WIDTH - BUTTON_SIZE, HEIGHT), Alignment.C, 8.0f),
            new ColumnLayout(0.0f, Alignment.C,
                new SymbolButton(new Vec2(BUTTON_SIZE), TimeSignature::drawUpSymbol)
                    .onClick(() -> props.setTimeSignature(props.tsBeatsPerBar, props.tsBeatNoteValue*2)),
                new SymbolButton(new Vec2(BUTTON_SIZE), TimeSignature::drawDownSymbol)
                    .onClick(() -> props.setTimeSignature(props.tsBeatsPerBar, props.tsBeatNoteValue/2))));
        
        callback = props.onUpdate(() -> textBox.setText(props.tsBeatsPerBar + "/" + props.tsBeatNoteValue));
        props.update();
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
    public TimeSignature setPos(Vec2 pos, Alignment align)
    {
        layout.setPos(pos, align);
        return this;
    }

    @Override
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
    {
    }

    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        return layout.checkFocus(dist, p);
    }

    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
    }

    @Override
    public void delete()
    {
        layout.delete();
        callback.delete();
    }

    @Override
    public void render(float alpha)
    {
        layout.render(alpha);
    }
}
