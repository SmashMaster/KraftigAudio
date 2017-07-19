package kraftig.game.util;

public class DSPMath
{
    //exponential interpolation
    //returns a value between min and max, given t is between 0 and 1.
    public static final double experp(double min, double max, double t)
    {
        return min*Math.pow(max/min, t);
    }
    
    public static final float experp(float min, float max, float t)
    {
        return (float)experp((double)min, (double)max, (double)t);
    }
    
    //inverse of exponential interpolation
    //returns a value between 0 and 1, given x is between min and max.
    public static final double expstep(double min, double max, double x)
    {
        return Math.log(x/min)/Math.log(max/min);
    }
    
    public static final float expstep(float min, float max, float x)
    {
        return (float)expstep((double)min, (double)max, (double)x);
    }
    
    private DSPMath()
    {
    }
}
