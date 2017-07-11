package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.Label;

public class AnalogSynth extends Panel implements AudioDevice
{
    public AnalogSynth()
    {
        setSize(0.125f, 0.0625f);
        frontInterface.add(new Label(Main.instance().getFont(), "Analog Synth", 32.0f, new Vec2(), Alignment.C));
    }
    
    @Override
    public void process(int samples)
    {
    }
}
