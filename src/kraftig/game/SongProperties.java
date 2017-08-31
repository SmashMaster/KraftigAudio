package kraftig.game;

public class SongProperties
{
    public boolean playing = false;
    public long playStartSample;
    public double tempo = 120.0;
    public int tsBeatsPerBar = 4, tsBeatNoteValue = 4;
    
    public double getSamplesPerBeat()
    {
        return Main.SAMPLE_RATE*tsBeatNoteValue*15.0/tempo;
    }
    
    public double getSamplesPerBar()
    {
        return getSamplesPerBeat()*tsBeatsPerBar;
    }
}
