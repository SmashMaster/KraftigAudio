package kraftig.game.audio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import kraftig.game.Main;
import kraftig.game.util.MidiReceiver;

public class MidiInstrument<T extends MidiNote> implements MidiReceiver
{
    public static final MidiInstrument<MidiNote> make()
    {
        return new MidiInstrument<>(MidiNote::new);
    }
    
    public final Envelope envelope = new Envelope();
    
    private final ArrayList<T> notes = new ArrayList();
    private final Function<Integer, T> noteFunction;
    
    public MidiInstrument(Function<Integer, T> noteFunction)
    {
        if (noteFunction == null) throw new NullPointerException();
        this.noteFunction = noteFunction;
    }
    
    @Override
    public synchronized void send(MidiMessage message, long timeStamp)
    {
        if (message instanceof ShortMessage)
        {
            ShortMessage msg = (ShortMessage)message;
            
            switch (msg.getCommand())
            {
                case ShortMessage.NOTE_ON:
                    notes.add(noteFunction.apply(msg.getData1()));
                    break;
                case ShortMessage.NOTE_OFF:
                    int midi = msg.getData1();
                    for (T note : notes) if (note.midi == midi) note.end();
                    break;
                default:
                    break;
            }
        }
    }
    
    public synchronized List<T> getNotes()
    {
        return (List<T>)notes.clone();
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
}
