package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.stream.Stream;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.AudioInputJack;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.Knob;
import kraftig.game.gui.Label;
import kraftig.game.gui.RadioButtons;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.TextBox;
import kraftig.game.util.DSPMath;

public class Gain extends Panel implements AudioDevice
{
    private final AudioInputJack inJack;
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    private final TextBox textBox = new TextBox(new Vec2(72.0f, 20.0f), Alignment.E, 32.0f);
    private int displayMode;
    private float gain;
    
    public Gain()
    {
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    new RadioButtons("dB", "ratio")
                            .onValueChanged(v -> set(v, gain))
                            .setValue(0),
                    new Knob(32.0f)
                            .onValueChanged(v -> set(displayMode, (float)Math.pow(16.0, v*2.0 - 1.0)))
                            .setValue(0.5f),
                    textBox,
                    new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Gain", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private void set(int displayMode, float gain)
    {
        this.displayMode = displayMode;
        this.gain = gain;
        
        if (displayMode == 0) //dB
        {
            float db = (float)(10.0*Math.log10(gain));
            textBox.setText(String.format("%.1f", db) + " dB");
        }
        else
        {
            if (gain >= 1.0f) textBox.setText("\u00D7" + String.format("%.3f", gain));
            else textBox.setText("\u00F7" + String.format("%.3f", (1.0f/gain)));
        }
    }
    
    @Override
    public Stream<AudioDevice> getInputDevices()
    {
        return inJack.getDevices();
    }
    
    @Override
    public void process(int samples)
    {
        DSPMath.apply(inJack.getBuffer(), buffer, samples, v -> v*gain);
    }
}
