package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.function.IntConsumer;
import kraftig.game.FocusQuery;
import kraftig.game.Main;
import org.lwjgl.opengl.GL11;

public class RadioButtons implements UIElement
{
    private final ToggleButton[] buttons;
    private final ColumnLayout columns;
    
    private int value = 0;
    private IntConsumer callback;
    
    public RadioButtons(float fontSize, String... options)
    {
        buttons = new ToggleButton[options.length];
        RowLayout[] rows = new RowLayout[options.length];
        
        for (int i=0; i<options.length; i++)
        {
            buttons[i] = new ToggleButton(fontSize);
            Label label = new Label(Main.instance().getFont(), options[i], fontSize);
            rows[i] = new RowLayout(1.0f, Alignment.E, label, buttons[i]);
        }
        
        buttons[value].setValue(true);
        for (int i=0; i<buttons.length; i++)
        {
            int index = i; //Need final local copy for lambda.
            ToggleButton button = buttons[index];
            
            button.onValueChanged(bValue ->
            {
                if (bValue)
                {
                    if (value != index)
                    {
                        int ov = value;
                        value = index;
                        buttons[ov].setValue(false);
                        if (callback != null) callback.accept(value);
                    }
                }
                else if (value == index) button.setValue(true);
            });
        }
        
        columns = new ColumnLayout(1.0f, Alignment.N, rows);
    }
    
    public int getValue()
    {
        return value;
    }
    
    public RadioButtons setValue(int value)
    {
        buttons[value].setValue(true);
        if (callback != null) callback.accept(value);
        return this;
    }
    
    public RadioButtons onValueChanged(IntConsumer callback)
    {
        this.callback = callback;
        callback.accept(value);
        return this;
    }
    
    @Override
    public final Vec2 getPos()
    {
        return columns.getPos();
    }

    @Override
    public final Vec2 getSize()
    {
        return columns.getSize().add(new Vec2(1.0f));
    }

    @Override
    public final RadioButtons setPos(Vec2 pos, Alignment align)
    {
        columns.setPos(pos, align);
        return this;
    }

    @Override
    public void updateMatrix(Mat4 matrix)
    {
        columns.updateMatrix(matrix);
    }

    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        return columns.checkFocus(dist, p);
    }

    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
    }

    @Override
    public void delete()
    {
        columns.delete();
    }

    @Override
    public void render(float alpha)
    {
        columns.render(alpha);
        
        Vec2 pos = getPos();
        Vec2 radius = getSize();
        
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(pos.x - radius.x, pos.y - radius.y);
        GL11.glVertex2f(pos.x - radius.x, pos.y + radius.y);
        GL11.glVertex2f(pos.x + radius.x, pos.y + radius.y);
        GL11.glVertex2f(pos.x + radius.x, pos.y - radius.y);
        GL11.glEnd();
    }
}
