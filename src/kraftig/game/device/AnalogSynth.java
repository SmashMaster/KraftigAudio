package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import javax.sound.midi.MidiMessage;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Label;
import kraftig.game.gui.MidiInputJack;

public class AnalogSynth extends Panel implements AudioDevice
{
    private final float[][] buffer = new float[2][48000];
    
    private double time;
    
    public AnalogSynth()
    {
        setSize(0.125f, 0.0625f);
        frontInterface.add(new Label(Main.instance().getFont(), "Analog Synth", 32.0f, new Vec2(), Alignment.C));
        rearInterface.add(new ColumnLayout(4.0f, Alignment.C,
                    new MidiInputJack(this::receive),
                    new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
    }
    
    private void receive(MidiMessage message, long timeStamp)
    {
    }
    
    @Override
    public void process(int samples)
    {
        for (int i=0; i<samples; i++)
        {
            float v = (float)Math.sin(Math.PI*2.0*440.0*time);
            buffer[0][i] = v;
            buffer[1][i] = v;
            time += Main.SAMPLE_WIDTH;
        }
    }
}
