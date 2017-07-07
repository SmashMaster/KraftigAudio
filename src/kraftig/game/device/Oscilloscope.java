package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.stream.Stream;
import kraftig.game.Panel;
import kraftig.game.gui.InputJack;
import kraftig.game.gui.Jack;
import kraftig.game.gui.OscilloscopeScreen;

public class Oscilloscope extends Panel implements Device
{
    private final InputJack inJack;
    private final OscilloscopeScreen screen;
    
    public Oscilloscope()
    {
        inJack = new InputJack(new Vec2(0.0f, Jack.RADIUS*1.5f), Alignment.C);
        
        setSize(0.125f, 0.0625f);
        rearInterface.add(inJack);
        frontInterface.add(screen = new OscilloscopeScreen(new Vec2(), new Vec2(48, 48), Alignment.C));
    }
    
    @Override
    public Stream<Device> getInputDevices()
    {
        return inJack.getDevices();
    }
    
    @Override
    public void process(int samples)
    {
        float[][] buffer = inJack.getBuffer();
        screen.process(buffer, samples);
    }
}
