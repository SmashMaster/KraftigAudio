package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import kraftig.game.FocusQuery;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.util.Savable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class RadioButtons implements UIElement, Savable
{
    private final RadioButton[] buttons;
    private final ColumnLayout columns;
    
    private int value = 0;
    private IntConsumer callback;
    
    public RadioButtons(String... options)
    {
        buttons = new RadioButton[options.length];
        RowLayout[] rows = new RowLayout[options.length];
        
        for (int i=0; i<options.length; i++)
        {
            buttons[i] = new RadioButton(8.0f);
            Label label = new Label(Main.instance().getFont(), options[i], 16.0f);
            rows[i] = new RowLayout(4.0f, Alignment.C, label, buttons[i]);
        }
        
        buttons[value].setValue(true);
        for (int i=0; i<buttons.length; i++)
        {
            int index = i; //Need final local copy for lambda.
            RadioButton button = buttons[index];
            
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
        
        columns = new ColumnLayout(2.0f, Alignment.E, rows);
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
    public final Vec2 getRadius()
    {
        return columns.getRadius().add(new Vec2(1.0f));
    }

    @Override
    public final RadioButtons setPos(Vec2 pos, Alignment align)
    {
        columns.setPos(pos, align);
        return this;
    }

    @Override
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
    {
        columns.updateMatrix(matrix, panel, front);
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
        Vec2 radius = getRadius();
        
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(pos.x - radius.x, pos.y - radius.y);
        GL11.glVertex2f(pos.x - radius.x, pos.y + radius.y);
        GL11.glVertex2f(pos.x + radius.x, pos.y + radius.y);
        GL11.glVertex2f(pos.x + radius.x, pos.y - radius.y);
        GL11.glEnd();
    }

    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        out.writeInt(value);
    }

    @Override
    public void load(DataInputStream in) throws IOException
    {
        setValue(in.readInt());
    }
    // </editor-fold>

    private class RadioButton implements UIElement
    {
        private static final int SEGMENTS = 32;
        private static final float DT = 8.0F / (SEGMENTS - 1);
        private static final float T_END = 8.0F + DT * 0.5F;
        private final Vec2 pos = new Vec2();
        private final float radius;
        private boolean value = false;
        private Consumer<Boolean> callback;

        private RadioButton(float radius)
        {
            super();
            this.radius = radius;
        }

        private RadioButton(float radius, Vec2 pos, Alignment align)
        {
            this(radius);
            setPos(pos, align);
        }

        @Override
        public final Vec2 getPos()
        {
            return new Vec2(pos);
        }

        @Override
        public final Vec2 getRadius()
        {
            return new Vec2(radius);
        }

        @Override
        public final RadioButton setPos(Vec2 pos, Alignment align)
        {
            align.align(pos, getRadius(), this.pos);
            return this;
        }

        public RadioButton onValueChanged(Consumer<Boolean> callback)
        {
            this.callback = callback;
            callback.accept(value);
            return this;
        }

        public RadioButton setValue(boolean value)
        {
            this.value = value;
            if (callback != null)
            {
                callback.accept(value);
            }
            return this;
        }

        @Override
        public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
        {
        }

        @Override
        public UIFocusQuery checkFocus(float dist, Vec2 p)
        {
            if (p.squareDist(pos) < radius * radius)
            {
                return new UIFocusQuery(this, dist, p);
            }
            else
            {
                return null;
            }
        }

        @Override
        public void onMouseButton(FocusQuery query, int button, int action, int mods)
        {
            if (action != GLFW.GLFW_PRESS || button != GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                return;
            }
            setValue(!value);
        }

        @Override
        public void delete()
        {
        }

        @Override
        public void render(float alpha)
        {
            GL11.glPushMatrix();
            GL11.glTranslatef(pos.x, pos.y, 0.0F);
            GL11.glLineWidth(1.0F);
            float color = Main.instance().getFocus() == this ? 0.75F : 1.0F;
            GL11.glColor4f(color, color, 1.0F, alpha);
            GL11.glBegin(value ? GL11.GL_TRIANGLE_FAN : GL11.GL_LINE_LOOP);
            for (float t = 0.0F; t < T_END; t += DT)
            {
                Vec2 p = Util.squareDir(t).normalize().mult(radius);
                GL11.glVertex2f(p.x, p.y);
            }
            GL11.glEnd();
            GL11.glPopMatrix();
        }
    }
}
