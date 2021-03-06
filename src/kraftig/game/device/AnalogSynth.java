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
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.EnvelopeEditor;
import kraftig.game.gui.Label;
import kraftig.game.gui.RadioButtons;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.buttons.ToggleButton;
import kraftig.game.gui.buttons.ToggleLabelButton;
import kraftig.game.gui.jacks.AudioOutputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.Knob;
import kraftig.game.gui.jacks.MidiInputJack;
import kraftig.game.util.DSPUtil;

public class AnalogSynth extends Panel
{
    private final MidiInputJack midiInJack;
    private final EnvelopeEditor envEditor;
    private final RadioButtons waveRadio;
    private final Knob ampKnob, pitchKnob, phaseKnob;
    private final ToggleButton antiAliasButton;
    private final AudioOutputJack outJack;
    
    private final MidiInstrument<AnalogNote> instrument = new MidiInstrument(AnalogNote::new);
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private boolean antiAliasing;
    private float amplitude, pitchBend, phase;
    private int waveform;
    
    public AnalogSynth()
    {
        frontInterface.add(new RowLayout(12.0f, Alignment.C,
                    midiInJack = new MidiInputJack(instrument),
                    envEditor = new EnvelopeEditor(instrument.envelope),
                    waveRadio = new RadioButtons("Sine", "Triangle", "Sawtooth", "Square")
                        .onValueChanged(v -> waveform = v)
                        .setValue(0),
                    new ColumnLayout(4.0f, Alignment.W,
                        new RowLayout(8.0f, Alignment.C,
                            new ColumnLayout(4.0f, Alignment.C,
                                new Label("Amplitude", 6.0f),
                                ampKnob = new Knob(24.0f)
                                    .setValue(0.25f)
                                    .onValueChanged(v -> amplitude = v)),
                            new ColumnLayout(4.0f, Alignment.C,
                                new Label("Pitch", 6.0f),
                                pitchKnob = new Knob(24.0f)
                                    .setValue(0.5f)
                                    .onValueChanged(v -> pitchBend = (float)(Math.pow(2.0, v*4.0 - 2.0) - 1.0))),
                            new ColumnLayout(4.0f, Alignment.C,
                                new Label("Phase", 6.0f),
                                phaseKnob = new Knob(24.0f)
                                    .setValue(0.5f)
                                    .onValueChanged(v -> phase = v*4.0f - 2.0f))),
                        antiAliasButton = new ToggleLabelButton("Anti-aliasing", 12.0f, 2.0f)
                                .setValue(true)
                                .onValueChanged(v -> antiAliasing = v)),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Analog Synth", 48.0f, new Vec2(0.0f, 0.0f), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(midiInJack, envEditor.getJacks(), ampKnob, pitchKnob, phaseKnob, outJack);
    }
    
    @Override
    public synchronized void process(int samples)
    {
        AnalogNote[] notes = instrument.getNotes(AnalogNote.class);
        
        for (int i=0; i<samples; i++)
        {
            float v = 0.0f;
            
            double time = (Main.instance().getTime() + i)*Main.SAMPLE_WIDTH;
            
            envEditor.updateValues(i);
            ampKnob.updateValue(i);
            pitchKnob.updateValue(i);
            phaseKnob.updateValue(i);
            
            if (waveform == 0) for (AnalogNote note : notes)
            {
                double freq = DSPUtil.freqFromMidi(note.midi);
                if (antiAliasing && freq*(pitchBend + 1.0) > Main.SAMPLE_RATE*0.5) continue;
                
                double env = note.getEnvelope(instrument.envelope, time);
                double noteTime = time - note.getStartTime();
                if (noteTime < 0.0) continue;
                
                double p = phase + note.bend*freq;
                
                v += env*(Math.cos(Math.PI*2.0*(freq*noteTime + p)));
            }
            else if (antiAliasing) for (AnalogNote note : notes)
            {
                double freq = DSPUtil.freqFromMidi(note.midi);
                double env = note.getEnvelope(instrument.envelope, time);
                double noteTime = time - note.getStartTime();
                if (noteTime < 0.0) continue;
                
                double p = phase + note.bend*freq;
                double bFreq = freq*(pitchBend + 1.0);
                if (bFreq > Main.SAMPLE_RATE*0.5) continue; //Bandlimit to avoid undersampling.

                double blit = DSPUtil.blit(freq, bFreq, noteTime, p);
                double invBlit = -DSPUtil.blit(freq, bFreq, noteTime, p + 0.5);

                switch (waveform)
                {
                    case 1: v += env*note.triangle; break; //Triangle wave
                    case 2: v += env*note.sawtooth; break; //Sawtooth wave
                    case 3: v += env*note.square; break; //Square wave
                }

                double leak = Math.pow(0.99999, bFreq);
                note.sawtooth = note.sawtooth*leak + blit*2.0;
                note.square = note.square*leak + (blit + invBlit)*2.0;
                note.triangle = note.triangle*0.99 + note.square*4.0*bFreq/Main.SAMPLE_RATE;
            }
            else for (AnalogNote note : notes)
            {
                double freq = DSPUtil.freqFromMidi(note.midi);
                double len = 1.0/freq;
                double env = note.getEnvelope(instrument.envelope, time);
                double noteTime = time - note.getStartTime();
                if (noteTime < 0.0) continue;
                
                double p = phase + note.bend*freq;

                switch (waveform)
                {
                    case 1: v += env*(Math.abs(((noteTime + p*len) % len)/len - 0.5)*4.0 - 1.0); break; //Triangle wave
                    case 2: v += env*(1.0 - 2.0*(((noteTime + p*len) % len)/len)); break; //Sawtooth wave
                    case 3: v += env*(((noteTime + p*len) % len) > len*0.5 ? -1.0 : 1.0); break; //Square wave
                }
            }
            
            for (AnalogNote note : notes) note.bend += pitchBend*Main.SAMPLE_WIDTH;
            
            v *= amplitude;
            buffer[0][i] = v;
            buffer[1][i] = v;
        }
        
        instrument.update(samples);
    }
    
    private class AnalogNote extends MidiNote
    {
        //Integrators.
        private double triangle = -1.0;
        private double sawtooth = -1.0;
        private double square = -1.0;
        private double bend;
        
        private AnalogNote(int midi, long sample)
        {
            super(midi, sample);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        super.save(out);
        envEditor.save(out);
        waveRadio.save(out);
        ampKnob.save(out);
        pitchKnob.save(out);
        phaseKnob.save(out);
        antiAliasButton.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        envEditor.load(in);
        waveRadio.load(in);
        ampKnob.load(in);
        pitchKnob.load(in);
        phaseKnob.load(in);
        antiAliasButton.load(in);
    }
    // </editor-fold>
}
