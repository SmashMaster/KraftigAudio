package kraftig.game.util;

public class DSPMath
{
    public static final double expstep(double min, double max, double t)
    {
        return min*Math.pow(max/min, t);
    }
    
    public static final float expstep(float min, float max, float t)
    {
        return min*(float)Math.pow(max/min, t);
    }
    
    private DSPMath()
    {
    }
}
