package kraftig.game.audio;

public class Envelope
{
    public float attack, aCurve, hold, decay, sustain, release, rCurve;
    
    private double active(double time)
    {
        if (time < 0.0) return 0.0;
        
        if (time < attack) return Math.pow(time/attack, aCurve);

        double endHold = attack + hold;
        if (time < endHold) return 1.0;

        double endDecay =  endHold + decay;
        if (time < endDecay) return Math.pow((endDecay - time)/decay, rCurve)*(1.0 - sustain) + sustain;

        return sustain;
    }
    
    public double evaluate(double timeSinceStart, double timeSinceEnd)
    {
        if (timeSinceEnd <= 0.0) return active(timeSinceStart); //Note is currently active.
        
        if (timeSinceEnd < release) //Note has been released.
        {
            double env = active(timeSinceStart);
            return env*Math.pow((release - timeSinceEnd)/release, rCurve);
        }
        
        return 0.0; //Note has ended.
    }
}
