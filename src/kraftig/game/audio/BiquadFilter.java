package kraftig.game.audio;

public class BiquadFilter
{
    private final BiquadCoefficients c = new BiquadCoefficients();
    private float x1, x2, y1, y2;
    
    public BiquadFilter(int sampleRate, float freq, float q)
    {
        double w0 = Math.PI*2.0*freq/sampleRate;
        double cosw0 = Math.cos(w0);
        double alpha = Math.sin(w0)/(2.0*q);
        
        c.a0 = (float)(1.0 + alpha);
        c.b0 = (float)((1.0 - cosw0)/2.0);
        c.b1 = (float)(1.0 - cosw0);
        c.b2 = (float)((1.0 - cosw0)/2.0);
        c.a1 = (float)(-2.0*cosw0);
        c.a2 = (float)(1.0 - alpha);
    }
    
    public void apply(float[] input, float[] output, int samples)
    {
        for (int i=0; i<samples; i++)
        {
            float x = input[i];
            float y = (c.b0*x + c.b1*x1 + c.b2*x2 - c.a1*y1 - c.a2*y2)/c.a0;
            x2 = x1; x1 = x;
            y2 = y1; y1 = y;
            output[i] = y;
        }
    }
    
    public class BiquadCoefficients
    {
        public float a0, b0, b1, b2, a1, a2;
    }
}
