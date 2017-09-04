package kraftig.game.device.sequencer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import kraftig.game.util.Savable;

public class Note implements Savable
{
    public int midi;
    public long start, end;

    @Override
    public void save(DataOutputStream out) throws IOException
    {
        out.writeInt(midi);
        out.writeLong(start);
        out.writeLong(end);
    }

    @Override
    public void load(DataInputStream in) throws IOException
    {
        midi = in.readInt();
        start = in.readLong();
        end = in.readLong();
    }
}
