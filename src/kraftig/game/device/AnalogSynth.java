package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.audio.Envelope;
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

public class AnalogSynth extends Panel
{
    private final MidiInputJack midiInJack;
    private final EnvelopeEditor envEditor;
    private final RadioButtons waveRadio;
    private final Knob ampKnob, phaseKnob;
    private final AudioOutputJack outJack;
    
    private final Envelope envelope = new Envelope();
    private final List<Note> notes = new ArrayList();
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private float amplitude, phase;
    private int waveform;
    
    public AnalogSynth()
    {
        frontInterface.add(new RowLayout(12.0f, Alignment.C,
                    midiInJack = new MidiInputJack(this::receive),
                    envEditor = new EnvelopeEditor(envelope),
                    waveRadio = new RadioButtons("Sine", "Triangle", "Sawtooth", "Square")
                        .onValueChanged(v -> waveform = v)
                        .setValue(0),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Amplitude", 6.0f),
                        ampKnob = new Knob(24.0f)
                            .setValue(0.25f)
                            .onValueChanged(v -> amplitude = v)),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Phase", 6.0f),
                        phaseKnob = new Knob(24.0f)
                            .setValue(0.5f)
                            .onValueChanged(v -> phase = v*4.0f)),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Analog Synth", 48.0f, new Vec2(0.0f, 0.0f), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private synchronized void receive(MidiMessage message, long timeStamp)
    {
        if (message instanceof ShortMessage)
        {
            ShortMessage msg = (ShortMessage)message;
            
            switch (msg.getCommand())
            {
                case ShortMessage.NOTE_ON:
                    notes.add(new Note(msg.getData1()));
                    break;
                case ShortMessage.NOTE_OFF:
                    int midi = msg.getData1();
                    for (Note note : notes) if (note.midi == midi) note.end();
                    break;
                default:
                    break;
            }
        }
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(midiInJack, envEditor.getJacks(), ampKnob, phaseKnob, outJack);
    }
    
    @Override
    public synchronized void process(int samples)
    {
        for (int i=0; i<samples; i++)
        {
            float v = 0.0f;
            
            double time = (Main.instance().getTime() + i)*Main.SAMPLE_WIDTH;
            
            ampKnob.updateValue(i);
            phaseKnob.updateValue(i);
            
            for (Note note : notes)
            {
                int midi = note.midi;
                double freq = (440.0/32.0)*Math.pow(2.0, (midi - 9)/12.0);
                double len = 1.0/freq;
                double env = envelope.evaluate(time - note.startTime, time - note.endTime);
                
                switch (waveform)
                {
                    case 0: v += env*(Math.sin(Math.PI*2.0*(freq*time + phase))); break; //Sine wave
                    case 1: v += env*(Math.abs(((time + phase*len) % len)/len - 0.5)*4.0 - 1.0); break; //Triangle wave
                    case 2: v += env*((((time + phase*len) % len)/len)*2.0 - 1.0); break; //Sawtooth wave
                    case 3: v += env*(((time + phase*len) % len) > len*0.5 ? -1.0 : 1.0); break; //Square wave
                }
            }
            
            v *= amplitude;
            buffer[0][i] = v;
            buffer[1][i] = v;
        }
        
        double time = (Main.instance().getTime() + samples)*Main.SAMPLE_WIDTH;
        
        for (Iterator<Note> it = notes.iterator(); it.hasNext();)
        {
            Note note = it.next();
            if (note.hasEnded() && (time - note.endTime >= envelope.release)) it.remove();
        }
    }
    
    private class Note
    {
        private final int midi;
        private final double startTime;
        private double endTime = Double.NaN;
        
        private Note(int midi)
        {
            this.midi = midi;
            startTime = Main.instance().getTime()*Main.SAMPLE_WIDTH;
        }
        
        private boolean hasEnded()
        {
            return endTime == endTime;
        }
        
        private void end()
        {
            if (!hasEnded()) endTime = Main.instance().getTime()*Main.SAMPLE_WIDTH;;
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
        phaseKnob.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        envEditor.load(in);
        waveRadio.load(in);
        ampKnob.load(in);
        phaseKnob.load(in);
    }
    // </editor-fold>
}
