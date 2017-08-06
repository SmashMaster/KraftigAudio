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
    
    //audio buffer utilities
    public static final void zero(float[] out, int samples)
    {
        for (int i=0; i<samples; i++) out[i] = 0.0f;
    }
    
    public static final void apply(float[] in, float[] out, int samples, FloatFunction function)
    {
        if (in == null)
            zero(out, samples);
        else for (int i=0; i<samples; i++)
            out[i] = function.apply(in[i]);
    }
    
    public static final void apply(float[][] in, float[][] out, int samples, FloatFunction function)
    {
        if (in == null)
        {
            zero(out[0], samples);
            zero(out[1], samples);
        }
        else
        {
            apply(in[0], out[0], samples, function);
            apply(in[1], out[1], samples, function);
        }
    }
    
    private DSPMath()
    {
    }
}
