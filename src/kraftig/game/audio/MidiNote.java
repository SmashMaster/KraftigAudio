package kraftig.game.audio;

import kraftig.game.Main;

public class MidiNote
{
    public final int midi;

    private double startTime;
    private double endTime = Double.POSITIVE_INFINITY;

    public MidiNote(int midi, long sample)
    {
        this.midi = midi;
        startTime = sample*Main.SAMPLE_WIDTH;
    }
    
    public final void ensureNotInPast()
    {
        startTime = Math.max(startTime, Main.instance().getTime()*Main.SAMPLE_WIDTH);
    }
    
    public final void end(long sample)
    {
        long sampleNotInPast = Math.max(sample, Main.instance().getTime());
        endTime = Math.min(sampleNotInPast*Main.SAMPLE_WIDTH, endTime);
    }
    
    public final double getStartTime()
    {
        return startTime;
    }

    public final double getEnvelope(Envelope envelope, double time)
    {
        return envelope.evaluate(time - startTime, time - endTime);
    }
    
    public final boolean hasStopped(Envelope envelope, double time)
    {
        return (time >= endTime) && (time - endTime >= envelope.release);
    }
}
