package kraftig.game;

public class LowPassFilter
{
    private final float b0, b1, b2, a1, a2;
    
    private float x1, x2, y1, y2;
    
    public LowPassFilter(int sampleRate, float freq, float q)
    {
        double w0 = Math.PI*2.0*freq/sampleRate;
        double cosw0 = Math.cos(w0);
        double alpha = Math.sin(w0)/(2.0*q);
        double a0 = 1.0 + alpha;
        
        b0 = (float)((1.0 - cosw0)/(2.0*a0));
        b1 = (float)((1.0 - cosw0)/a0);
        b2 = b0;
        a1 = (float)(-2.0*cosw0/a0);
        a2 = (float)((1.0 - alpha)/a0);
    }
    
    public void apply(float[] input, float[] output, int samples)
    {
        for (int i=0; i<samples; i++)
        {
            float x = input[i];
            float y = b0*x + b1*x1 + b2*x2 - a1*y1 - a2*y2;
            x2 = x1; x1 = x;
            y2 = y1; y1 = y;
            output[i] = y;
        }
    }
}
