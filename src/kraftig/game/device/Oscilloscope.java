package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.stream.Stream;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.InputJack;
import kraftig.game.gui.Knob;
import kraftig.game.gui.Label;
import kraftig.game.gui.OscilloscopeScreen;
import kraftig.game.gui.ToggleButton;

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
        frontInterface.add(screen = new OscilloscopeScreen(new Vec2(48, 48), new Vec2(), Alignment.C));
        frontInterface.add(new Knob(8.0f, new Vec2(64.0f, 0.0f), Alignment.C)
                .setValue(0.5f)
                .onValueChanged(v -> screen.setBrightness(MIN_BRIGHTNESS*(float)Math.pow(MAX_BRIGHTNESS/MIN_BRIGHTNESS, v))));
        
//        frontInterface.add(new ToggleButton(8.0f, new Vec2(-64.0f, 0.0f), Alignment.C));
        
        frontInterface.add(new ColumnLayout(2.0f, Alignment.C,
                    new Label(Main.instance().getFont(), "X"),
                    new ToggleButton(8.0f),
                    new ToggleButton(8.0f),
                    new ToggleButton(8.0f))
                .setPos(new Vec2(-64.0f, 0.0f), Alignment.W));
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
