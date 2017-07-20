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
import kraftig.game.gui.RadioButtons;
import kraftig.game.gui.RowLayout;

public class AnalogSynth extends Panel implements AudioDevice
{
    private final float[][] buffer = new float[2][48000];
    
    private final IntSet notes = new IntSet();
    
    private double time;
    private float amplitude;
    private int waveform;
    
    public AnalogSynth()
    {
        frontInterface.add(new RowLayout(32.0f, Alignment.C,
                    new MidiInputJack(this::receive),
                    new RadioButtons("Sine", "Triangle", "Sawtooth", "Square")
                        .onValueChanged(i -> waveform = i)
                        .setValue(0),
                    new Knob(32.0f)
                        .setValue(0.25f)
                        .onValueChanged(f -> amplitude = f),
                    new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Analog Synth", 48.0f, new Vec2(0.0f, 0.0f), Alignment.C));
        
        setSizeFromContents(8.0f);
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
                double len = 1.0/freq;
                
                switch (waveform)
                {
                    case 0: v += Math.sin(Math.PI*2.0*freq*time); break; //Sine wave
                    case 1: v += Math.abs((time % len)/len - 0.5)*4.0 - 1.0; break; //Triangle wave
                    case 2: v += ((time % len)/len)*2.0 - 1.0; break; //Sawtooth wave
                    case 3: v += (time % len) > len*0.5 ? -1.0 : 1.0; break; //Square wave
                }
            }
            
            v *= amplitude;
            buffer[0][i] = v;
            buffer[1][i] = v;
            
            time += Main.SAMPLE_WIDTH;
        }
    }
}
