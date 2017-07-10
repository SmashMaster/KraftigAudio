package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;

public class RadioButtons implements UIElement
{
    public RadioButtons(String[] options, Vec2 pos, Alignment align)
    {
        
    }
    
    @Override
    public final Vec2 getPos()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Vec2 getSize()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final RadioButtons setPos(Vec2 pos, Alignment align)
    {
        return this;
    }

    @Override
    public void updateMatrix(Mat4 matrix)
    {
    }

    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        return null;
    }

    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
    }

    @Override
    public void delete()
    {
    }

    @Override
    public void render(float alpha)
    {
    }
}
