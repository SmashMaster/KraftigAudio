package kraftig.game.gui;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import kraftig.game.FocusQuery;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.ListBox.Option;
import kraftig.game.util.VectorFont;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class ListBox<T extends Option> implements UIElement
{
    private static final float FONT_SIZE = 16.0f;
    
    private final VectorFont font;
    private final ColumnLayout container;
    private Consumer<T> callback;
    private T value;
    
    public ListBox(Vec2 radius, Supplier<List<? extends T>> optionSupplier)
    {
        font = Main.instance().getFont();
        ScrollBox scrollBox = new ScrollBox(radius, null);
        LabelButton refreshButton = new LabelButton("\u27f3 Refresh", 12.0f, 2.0f);
        
        refreshButton.onClick(() ->
        {
            List<? extends T> options = optionSupplier.get();
            List<Entry> entries = IOUtil.mapList(options, Entry::new);
            scrollBox.setContent(new ColumnLayout(0.0f, Alignment.W, entries));
        });
        refreshButton.click();
        
        container = new ColumnLayout(2.0f, Alignment.W, scrollBox, refreshButton);
    }
    
    public ListBox onValueChanged(Consumer<T> callback)
    {
        this.callback = callback;
        return this;
    }
    
    public ListBox setValue(T value)
    {
        if (this.value == value) return this;
        this.value = value;
        if (callback != null) callback.accept(value);
        return this;
    }

    @Override
    public Vec2 getPos()
    {
        return container.getPos();
    }

    @Override
    public Vec2 getRadius()
    {
        return container.getRadius();
    }

    @Override
    public ListBox setPos(Vec2 pos, Alignment align)
    {
        container.setPos(pos, align);
        return this;
    }

    @Override
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
    {
        container.updateMatrix(matrix, panel, front);
    }

    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        return container.checkFocus(dist, p);
    }

    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
    }

    @Override
    public void delete()
    {
        container.delete();
    }

    @Override
    public void render(float alpha)
    {
        container.render(alpha);
    }
    
    public interface Option
    {
        public String getLabel();
    }
    
    private class Entry implements UIElement
    {
        private final T option;
        private final String label;
        private final Vec2 pos = new Vec2();
        private final Vec2 radius = new Vec2();
        private Alignment align = Alignment.C;
        
        public Entry(T option)
        {
            this.option = option;
            label = option.getLabel();
            radius.set(font.getSize(label).mult(0.5f*FONT_SIZE));
        }

        @Override
        public Vec2 getPos()
        {
            return align.align(pos, radius);
        }

        @Override
        public Vec2 getRadius()
        {
            return new Vec2(radius);
        }

        @Override
        public Entry setPos(Vec2 pos, Alignment align)
        {
            this.pos.set(pos);
            this.align = align;
            return this;
        }

        @Override
        public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
        {
        }

        @Override
        public UIFocusQuery checkFocus(float dist, Vec2 p)
        {
            Vec2 center = getPos();
            if (p.x < center.x - radius.x || p.x > center.x + radius.x) return null;
            if (p.y < center.y - radius.y || p.y > center.y + radius.y) return null;
            return new UIFocusQuery(this, dist, p);
        }

        @Override
        public void onMouseButton(FocusQuery query, int button, int action, int mods)
        {
            if (action == GLFW.GLFW_PRESS && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                setValue(option);
        }

        @Override
        public void delete()
        {
        }

        @Override
        public void render(float alpha)
        {
            if (Objects.equals(option, value))
            {
                Vec2 center = getPos();
                
                GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha*0.25f);
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2f(center.x - radius.x, center.y - radius.y);
                GL11.glVertex2f(center.x - radius.x, center.y + radius.y);
                GL11.glVertex2f(center.x + radius.x, center.y + radius.y);
                GL11.glVertex2f(center.x + radius.x, center.y - radius.y);
                GL11.glEnd();
            }
            
            GL11.glLineWidth(1.0f);
            float color = Main.instance().getFocus() == this ? 0.75f : 1.0f;
            GL11.glColor4f(color, color, 1.0f, alpha);
            font.render(label, pos, FONT_SIZE, align);
        }
    }
}
