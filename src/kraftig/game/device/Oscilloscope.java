package kraftig.game.device;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.stream.Stream;
import kraftig.game.Panel;
import kraftig.game.gui.InputJack;
import kraftig.game.gui.Knob;
import kraftig.game.gui.OscilloscopeScreen;

public class Oscilloscope extends Panel implements Device
{
    private final float MIN_BRIGHTNESS = 1.0f/128.0f;
    private final float MAX_BRIGHTNESS = 1.0f/4.0f;
    
    private final InputJack inJack;
    private final OscilloscopeScreen screen;
    
    public Oscilloscope()
    {
        inJack = new InputJack(new Vec2(), Alignment.C);
        
        setSize(0.125f, 0.0625f);
        rearInterface.add(inJack);
        frontInterface.add(screen = new OscilloscopeScreen(new Vec2(), new Vec2(48, 48), Alignment.C));
        frontInterface.add(new Knob(new Vec2(64.0f, 0.0f), Alignment.C, 8.0f)
                .setValue(0.5f)
                .onValueChanged(v -> screen.setBrightness(MIN_BRIGHTNESS*(float)Math.pow(MAX_BRIGHTNESS/MIN_BRIGHTNESS, v))));
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
