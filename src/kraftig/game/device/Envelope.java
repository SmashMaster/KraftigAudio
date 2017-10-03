package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.audio.MidiInstrument;
import kraftig.game.audio.MidiNote;
import kraftig.game.gui.EnvelopeEditor;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.jacks.AudioOutputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.MidiInputJack;
import kraftig.game.util.DSPUtil;

public class Envelope extends Panel
{
    private final MidiInputJack midiInJack;
    private final EnvelopeEditor envEditor;
    private final AudioOutputJack outJack;
    
    private final MidiInstrument<MidiNote> instrument = MidiInstrument.make();
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    public Envelope()
    {
        frontInterface.add(new RowLayout(12.0f, Alignment.C,
                    midiInJack = new MidiInputJack(instrument),
                    envEditor = new EnvelopeEditor(instrument.envelope),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Envelope", 48.0f, new Vec2(0.0f, 0.0f), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(midiInJack, envEditor.getJacks(), outJack);
    }
    
    @Override
    public synchronized void process(int samples)
    {
        MidiNote[] notes = instrument.getNotes(MidiNote.class);
        
        if (notes.length > 0) for (int i=0; i<samples; i++)
        {
            envEditor.updateValues(i);
            double time = (Main.instance().getTime() + i)*Main.SAMPLE_WIDTH;
            MidiNote note = notes[notes.length - 1];
            float v = (float)(note.getEnvelope(instrument.envelope, time)*2.0 - 1.0);
            buffer[0][i] = v;
            buffer[1][i] = v;
        }
        else
        {
            Arrays.fill(buffer[0], -1.0f);
            Arrays.fill(buffer[1], -1.0f);
        }
        
        instrument.update(samples);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        super.save(out);
        envEditor.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        envEditor.load(in);
    }
    // </editor-fold>
}
