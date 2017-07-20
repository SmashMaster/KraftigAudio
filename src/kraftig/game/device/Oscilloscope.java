package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.stream.Stream;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.AudioInputJack;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Knob;
import kraftig.game.gui.Label;
import kraftig.game.gui.OscilloscopeScreen;
import kraftig.game.gui.RadioButtons;
import kraftig.game.gui.RowLayout;
import kraftig.game.util.DSPMath;

public class Oscilloscope extends Panel implements AudioDevice
{
    private final float MIN_BRIGHTNESS = 1.0f/128.0f;
    private final float MAX_BRIGHTNESS = 1.0f/4.0f;
    
    private final AudioInputJack inJack;
    private final OscilloscopeScreen screen;
    
    public Oscilloscope()
    {
        screen = new OscilloscopeScreen(new Vec2(96.0f, 96.0f));
        
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    new ColumnLayout(8.0f, Alignment.C,
                        new RadioButtons("X:Time, Y:Amp", "X:Left, Y:Right")
                            .onValueChanged(screen::setMode),
                        inJack = new AudioInputJack()),
                    screen,
                    new Knob(16.0f)
                        .setValue(0.5f)
                        .onValueChanged(v -> screen.setBrightness(DSPMath.experp(MIN_BRIGHTNESS, MAX_BRIGHTNESS, v))))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label(Main.instance().getFont(), "Oscilloscope", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public Stream<AudioDevice> getInputDevices()
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
