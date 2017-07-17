package kraftig.game.gui;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.function.Supplier;
import kraftig.game.FocusQuery;
import kraftig.game.Panel;
import kraftig.game.gui.ListBox.Option;

public class ListBox<T extends Option> implements UIElement
{
    private final ColumnLayout container;
    
    public ListBox(Vec2 radius, Supplier<T[]> optionSupplier)
    {
        ScrollBox scrollBox = new ScrollBox(radius, null);
        LabelButton refreshButton = new LabelButton("\u27f3 Refresh", 6.0f, 1.0f);
        
        refreshButton.onClick(() ->
        {
            T[] options = optionSupplier.get();
            Entry[] entries = IOUtil.mapArray(options, Entry.class, Entry::new);
            scrollBox.setContent(new ColumnLayout(0.0f, Alignment.W, entries));
        });
        refreshButton.click();
        
        container = new ColumnLayout(1.0f, Alignment.W, scrollBox, refreshButton);
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
    
    private class Entry extends Label
    {
        private final T option;
        
        public Entry(T option)
        {
            super(option.getLabel(), 8.0f);
            this.option = option;
        }
    }
}
