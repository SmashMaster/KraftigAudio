package kraftig.game.audio;

import kraftig.game.Main;

public class BiquadFilterKernel
{
    public final Settings s = new Settings();
    
    private float x1, x2, y1, y2;
    
    public void apply(float[] input, float[] output, int samples)
    {
        for (int i=0; i<samples; i++)
        {
            float x = input != null ? input[i] : 0.0f;
            float y = s.b0*x + s.b1*x1 + s.b2*x2 - s.a1*y1 - s.a2*y2;
            x2 = x1; x1 = x;
            y2 = y1; y1 = y;
            output[i] = y;
        }
    }
    
    public class Settings
    {
        public float b0, b1, b2, a1, a2;
        
        public Settings set(Settings other)
        {
            this.b0 = other.b0;
            this.b1 = other.b1;
            this.b2 = other.b2;
            this.a1 = other.a1;
            this.a2 = other.a2;
            
            return this;
        }
        
        public Settings set(double b0, double b1, double b2, double a0, double a1, double a2)
        {
            this.b0 = (float)(b0/a0);
            this.b1 = (float)(b1/a0);
            this.b2 = (float)(b2/a0);
            this.a1 = (float)(a1/a0);
            this.a2 = (float)(a2/a0);
            
            return this;
        }
        
        public Settings lowPass(double freq, double q)
        {
            double w0 = Math.PI*2.0*freq/Main.SAMPLE_RATE;
            double cosw0 = Math.cos(w0);
            double alpha = Math.sin(w0)/(2.0*q);
            
            return set((1.0 - cosw0)/2.0,
                       1.0 - cosw0,
                       (1.0 - cosw0)/2.0,
                       1.0 + alpha,
                       -2.0*cosw0,
                       1.0 - alpha);
        }
        
        public Settings bandPass(double freq, double q)
        {
            double w0 = Math.PI*2.0*freq/Main.SAMPLE_RATE;
            double cosw0 = Math.cos(w0);
            double alpha = Math.sin(w0)/(2.0*q);
            
            return set(alpha,
                       0.0,
                       -alpha,
                       1.0 + alpha,
                       -2.0*cosw0,
                       1.0 - alpha);
        }
        
        public Settings highPass(double freq, double q)
        {
            double w0 = Math.PI*2.0*freq/Main.SAMPLE_RATE;
            double cosw0 = Math.cos(w0);
            double alpha = Math.sin(w0)/(2.0*q);
            
            return set((1.0 + cosw0)/2.0,
                       -1.0 - cosw0,
                       (1.0 + cosw0)/2.0,
                       1.0 + alpha,
                       -2.0*cosw0,
                       1.0 - alpha);
        }
        
        public Settings bandReject(double freq, double q)
        {
            double w0 = Math.PI*2.0*freq/Main.SAMPLE_RATE;
            double cosw0 = Math.cos(w0);
            double alpha = Math.sin(w0)/(2.0*q);
            
            return set(1.0,
                       -2.0*cosw0,
                       1.0,
                       1.0 + alpha,
                       -2.0*cosw0,
                       1.0 - alpha);
        }
        
        public Settings allPass(double freq, double q)
        {
            double w0 = Math.PI*2.0*freq/Main.SAMPLE_RATE;
            double cosw0 = Math.cos(w0);
            double alpha = Math.sin(w0)/(2.0*q);
            
            return set(1.0 - alpha,
                       -2.0*cosw0,
                       1.0 + alpha,
                       1.0 + alpha,
                       -2.0*cosw0,
                       1.0 - alpha);
        }
    }
}
