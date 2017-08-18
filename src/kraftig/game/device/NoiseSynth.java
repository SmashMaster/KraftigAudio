package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.audio.MidiInstrument;
import kraftig.game.audio.MidiNote;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.EnvelopeEditor;
import kraftig.game.gui.Jack;
import kraftig.game.gui.Knob;
import kraftig.game.gui.Label;
import kraftig.game.gui.MidiInputJack;
import kraftig.game.gui.RadioButtons;
import kraftig.game.gui.RowLayout;
import kraftig.game.util.DSPUtil;

public class NoiseSynth extends Panel
{
    private final MidiInputJack miniInJack;
    private final EnvelopeEditor envEditor;
    private final RadioButtons colorRadio;
    private final Knob ampKnob;
    private final AudioOutputJack outJack;
    
    private final MidiInstrument<NoiseNote> instrument = new MidiInstrument<>(NoiseNote::new);
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private float amplitude;
    private int color;
    
    public NoiseSynth()
    {
        frontInterface.add(new RowLayout(12.0f, Alignment.C,
                    miniInJack = new MidiInputJack(instrument),
                    envEditor = new EnvelopeEditor(instrument.envelope),
                    colorRadio = new RadioButtons("Violet", "White", "Pink", "Red")
                        .onValueChanged(v -> color = v)
                        .setValue(1),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Amplitude", 6.0f),
                        ampKnob = new Knob(24.0f)
                            .setValue(0.25f)
                            .onValueChanged(v -> amplitude = v)),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Noise Synth", 48.0f, new Vec2(0.0f, 0.0f), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(miniInJack, envEditor.getJacks(), ampKnob, outJack);
    }
    
    private float rand()
    {
        return (float)(Math.random()*2.0 - 1.0);
    }
    
    @Override
    public void process(int samples)
    {
        List<NoiseNote> notes = instrument.getNotes();
        
        for (int i=0; i<samples; i++)
        {
            float v = 0.0f;
            double time = (Main.instance().getTime() + i)*Main.SAMPLE_WIDTH;
            
            ampKnob.updateValue(i);
            
            for (NoiseNote note : notes)
            {
                double env = note.getEnvelope(instrument.envelope, time);
                
                switch (color)
                {
                    case 0: //Violet
                        {
                            float white = rand();
                            float out = white - note.prev;
                            note.prev = white;
                            v += env*out*0.5f;
                        }
                        break;
                    case 1: v += env*rand(); break; //White
                    case 2: //Pink
                        {
                            int k = Long.numberOfTrailingZeros(note.pinkCounter++);
                            k &= 15;

                            int prevRand = note.pinkDice[k];

                            note.pinkSeed = 1664525*note.pinkSeed + 1013904223;
                            int newRand = note.pinkSeed >>> 13;
                            note.pinkDice[k] = newRand;

                            note.pinkTotal += newRand - prevRand;

                            note.pinkSeed = 1664525*note.pinkSeed + 1013904223;
                            newRand = note.pinkSeed >>> 13;

                            int ifval = (note.pinkTotal + newRand) | 0x40000000;
                            v += env*(Float.intBitsToFloat(ifval) - 3.0f)*2.0f;
                        }
                        break;
                    case 3: //Red
                        {
                            float white = rand();
                            float out = (note.prev + white*0.02f)/1.02f;
                            note.prev = out;
                            v += env*out*5.0f;
                        }
                        break;
                }
            }
            
            v *= amplitude;
            buffer[0][i] = v;
            buffer[1][i] = v;
        }
        
        instrument.update(samples);
    }
    
    private class NoiseNote extends MidiNote
    {
        //Pink noise variables
        private int pinkCounter;
        private final int[] pinkDice = new int[16];
        private int pinkSeed;
        private int pinkTotal;

        //Violet/Red noise variables
        private float prev;
        
        private NoiseNote(int midi, long sample)
        {
            super(midi, sample);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        super.save(out);
        colorRadio.save(out);
        ampKnob.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        colorRadio.load(in);
        ampKnob.load(in);
    }
    // </editor-fold>
}
