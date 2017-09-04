package kraftig.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import kraftig.game.util.Savable;

public class SongProperties implements Savable
{
    public boolean playing = false;
    public long playStartSample = 0L;
    public double tempo = 120.0;
    public int tsBeatsPerBar = 4, tsBeatNoteValue = 4;
    public int songLength = 64;

    public void init()
    {
        playing = false;
        playStartSample = 0L;
        tempo = 120.0;
        tsBeatsPerBar = 4;
        tsBeatNoteValue = 4;
        songLength = 64;
    }
    
    public double getSamplesPerBeat()
    {
        return Main.SAMPLE_RATE*tsBeatNoteValue*15.0/tempo;
    }
    
    public double getSamplesPerBar()
    {
        return getSamplesPerBeat()*tsBeatsPerBar;
    }
    
    public double getSongSampleLength()
    {
        return getSamplesPerBar()*songLength;
    }

    @Override
    public void save(DataOutputStream out) throws IOException
    {
        out.writeDouble(tempo);
        out.writeInt(tsBeatsPerBar);
        out.writeInt(tsBeatNoteValue);
    }

    @Override
    public void load(DataInputStream in) throws IOException
    {
        playing = false;
        playStartSample = 0L;
        tempo = in.readDouble();
        tsBeatsPerBar = in.readInt();
        tsBeatNoteValue = in.readInt();
    }
}
