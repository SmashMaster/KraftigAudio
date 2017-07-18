package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import com.samrj.devil.util.IntSet;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.Knob;
import kraftig.game.gui.Label;
import kraftig.game.gui.MidiInputJack;
import kraftig.game.gui.RowLayout;

public class AnalogSynth extends Panel implements AudioDevice
{
    private final float[][] buffer = new float[2][48000];
    
    private final IntSet notes = new IntSet();
    
    private double time;
    private float amplitude;
    
    public AnalogSynth()
    {
        frontInterface.add(new RowLayout(16.0f, Alignment.C,
                    new MidiInputJack(this::receive),
                    new Knob(16.0f, new Vec2(0.0f, -8.0f), Alignment.S)
                        .setValue(0.25f)
                        .onValueChanged(f -> amplitude = f),
                    new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Analog Synth", 24.0f, new Vec2(0.0f, 0.0f), Alignment.C));
        
        setSizeFromContents(4.0f);
    }
    
    private void receive(MidiMessage message, long timeStamp)
    {
        if (message instanceof ShortMessage)
        {
            ShortMessage msg = (ShortMessage)message;
            
            switch (msg.getCommand())
            {
                case ShortMessage.NOTE_ON:
                    notes.add(msg.getData1());
                    break;
                case ShortMessage.NOTE_OFF:
                    notes.remove(msg.getData1());
                    break;
                default:
                    break;
            }
        }
    }
    
    @Override
    public void process(int samples)
    {
        for (int i=0; i<samples; i++)
        {
            float v = 0.0f;
            
            for (int ni=0; ni<notes.size(); ni++)
            {
                int note = notes.get(ni);
                double freq = (440.0/32.0)*Math.pow(2.0, (note - 9)/12.0);
                v += (float)Math.sin(Math.PI*2.0*freq*time);
            }
            
            v *= amplitude;
            buffer[0][i] = v;
            buffer[1][i] = v;
            
            time += Main.SAMPLE_WIDTH;
        }
    }
}
