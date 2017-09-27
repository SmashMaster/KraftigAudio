package kraftig.game.audio;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import kraftig.game.Main;

public class MidiInstrument<T extends MidiNote> implements MidiReceiver
{
    public static final MidiInstrument<MidiNote> make()
    {
        return new MidiInstrument<>(MidiNote::new);
    }
    
    public final Envelope envelope = new Envelope();
    
    private final ArrayList<T> newNotes = new ArrayList();
    private final ArrayList<T> notes = new ArrayList();
    private final MidiNoteMaker<T> noteFunction;
    
    public MidiInstrument(MidiNoteMaker<T> noteMaker)
    {
        if (noteMaker == null) throw new NullPointerException();
        this.noteFunction = noteMaker;
    }
    
    @Override
    public synchronized void send(MidiMessage message, long sample)
    {
        if (message instanceof ShortMessage)
        {
            ShortMessage msg = (ShortMessage)message;
            
            switch (msg.getCommand())
            {
                case ShortMessage.NOTE_ON:
                    newNotes.add(noteFunction.make(msg.getData1(), sample));
                    break;
                case ShortMessage.NOTE_OFF:
                    int midi = msg.getData1();
                    for (T note : notes) if (note.midi == midi) note.end(sample);
                    for (T note : newNotes) if (note.midi == midi) note.end(sample);
                    break;
                default:
                    break;
            }
        }
    }
    
    public synchronized T[] getNotes(Class<T> noteClass)
    {
        for (T note : newNotes) note.ensureNotInPast();
        notes.addAll(newNotes);
        newNotes.clear();
        
        T[] out = (T[])Array.newInstance(noteClass, notes.size());
        notes.toArray(out);
        return out;
    }
    
    public synchronized void update(int samples)
    {
        double time = (Main.instance().getTime() + samples)*Main.SAMPLE_WIDTH;
        
        for (Iterator<T> it = notes.iterator(); it.hasNext();)
            if (it.next().hasStopped(envelope, time))
                it.remove();
    }

    @Override
    public void close()
    {
    }
    
    @FunctionalInterface
    public static interface MidiNoteMaker<T extends MidiNote>
    {
        public T make(int midi, long sample);
    }
}
