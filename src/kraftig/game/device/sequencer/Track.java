package kraftig.game.device.sequencer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import kraftig.game.util.Savable;

public class Track implements Savable
{
    public final List<Note> notes = new ArrayList<>();

    @Override
    public void save(DataOutputStream out) throws IOException
    {
        out.writeInt(notes.size());
        for (Note note : notes) note.save(out);
    }

    @Override
    public void load(DataInputStream in) throws IOException
    {
        notes.clear();
        int size = in.readInt();
        for (int i=0; i<size; i++)
        {
            Note note = new Note();
            note.load(in);
            notes.add(note);
        }
    }
}
