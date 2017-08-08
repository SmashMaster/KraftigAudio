package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import com.samrj.devil.util.IntSet;
import java.util.stream.Stream;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Knob;
import kraftig.game.gui.Label;
import kraftig.game.gui.MidiInputJack;
import kraftig.game.gui.RadioButtons;
import kraftig.game.gui.RowLayout;
import kraftig.game.util.DSPUtil;

public class NoiseSynth extends Panel implements AudioDevice
{
    private final Knob ampKnob;
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private final IntSet notes = new IntSet();
    
    private float amplitude;
    private int color;
    
    //Violet noise variables
    private float violetPrev;
    
    //Pink noise variables
    private int pinkCounter;
    private final int[] pinkDice = new int[16];
    private int pinkSeed;
    private int pinkTotal;
    
    //Red noise variables
    private float redPrev;
    
    public NoiseSynth()
    {
        frontInterface.add(new RowLayout(12.0f, Alignment.C,
                    new MidiInputJack(this::receive),
                    new RadioButtons("Violet", "White", "Pink", "Red")
                        .onValueChanged(v -> color = v)
                        .setValue(1),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Amplitude", 6.0f),
                        ampKnob = new Knob(24.0f)
                            .setValue(0.25f)
                            .onValueChanged(v -> amplitude = v)),
                    new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Noise Synth", 48.0f, new Vec2(0.0f, 0.0f), Alignment.C));
        
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
    public Stream<AudioDevice> getInputDevices()
    {
        return DSPUtil.getDevices(ampKnob);
    }
    
    private float rand()
    {
        return (float)(Math.random()*2.0 - 1.0);
    }
    
    @Override
    public void process(int samples)
    {
        for (int i=0; i<samples; i++)
        {
            float v = 0.0f;
            
            ampKnob.updateValue(i);
            
            for (int n=0; n<notes.size(); n++) switch (color)
            {
                case 0: //Violet
                    {
                        float white = rand();
                        float out = white - violetPrev;
                        violetPrev = white;
                        v += out*0.5f;
                    }
                    break;
                case 1: v += rand(); break; //White
                case 2: //Pink
                    {
                        int k = Long.numberOfTrailingZeros(pinkCounter);
                        k &= 15;
                        
                        int prevRand = pinkDice[k];
                        
                        pinkSeed = 1664525*pinkSeed + 1013904223;
                        int newRand = pinkSeed >>> 13;
                        pinkDice[k] = newRand;
                        
                        pinkTotal += newRand - prevRand;
                        
                        pinkSeed = 1664525*pinkSeed + 1013904223;
                        newRand = pinkSeed >>> 13;
                        
                        int ifval = (pinkTotal + newRand) | 0x40000000;
                        v += (Float.intBitsToFloat(ifval) - 3.0f)*2.0f;
                        
                        pinkCounter++;
                    }
                    break;
                case 3: //Red
                    {
                        float white = rand();
                        float out = (redPrev + white*0.02f)/1.02f;
                        redPrev = out;
                        v += out*5.0f;
                    }
                    break;
            }
            
            v *= amplitude;
            buffer[0][i] = v;
            buffer[1][i] = v;
        }
    }
}
