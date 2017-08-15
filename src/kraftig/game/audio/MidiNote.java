package kraftig.game.audio;

import kraftig.game.Main;

public class MidiNote
{
    public final int midi;

    private final double startTime;
    private double endTime = Double.NaN;

    public MidiNote(int midi)
    {
        this.midi = midi;
        startTime = Main.instance().getTime()*Main.SAMPLE_WIDTH;
    }
    
    public boolean hasEnded()
    {
        return endTime == endTime;
    }
    
    public void end()
    {
        if (!hasEnded()) endTime = Main.instance().getTime()*Main.SAMPLE_WIDTH;;
    }

    public double getEnvelope(Envelope envelope, double time)
    {
        return envelope.evaluate(time - startTime, time - endTime);
    }
    
    public boolean hasStopped(Envelope envelope, double time)
    {
        return hasEnded() && (time - endTime >= envelope.release);
    }
}
