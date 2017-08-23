package kraftig.game.audio;

import com.samrj.devil.math.Util;
import java.util.Arrays;

public class FFT
{
    public static final float[][] twiddle(int n)
    {
        if (!Util.isPower2(n)) throw new IllegalArgumentException();
        if (n < 2) throw new IllegalArgumentException();
        
        int len = n/2;
        float[] real = new float[len];
        float[] complex = new float[len];
        
        for (int k=0; k<len; k++)
        {
            double x = (2.0*Math.PI*k)/n;
            
            real[k] = (float)Math.cos(x);
            complex[k] = (float)-Math.sin(x);
        }
        
        return new float[][]{real, complex};
    }
    
    public static final float[] window(int n)
    {
        int end = n - 1;
        float[] out = new float[n];
        
        for (int i=0; i<n; i++) out[i] = (float)(0.355768
                        - 0.487396*Math.cos(2.0*Math.PI*i/end)
                        + 0.144232*Math.cos(4.0*Math.PI*i/end)
                        - 0.012604*Math.cos(6.0*Math.PI*i/end));
        
        return out;
    }
    
    public static final float[] fft(float[] input, float[] twiddle)
    {
        //Check consistency of all input.
        int n = input.length;
        if (!Util.isPower2(n)) throw new IllegalArgumentException();
        if (n < 2) throw new IllegalArgumentException();
        if (twiddle.length != n/2) throw new IllegalArgumentException();
        
        //Prepare buffers.
        float[] read = new float[n];
        float[] write = new float[n];
        System.arraycopy(input, 0, read, 0, n);
        
        //Perform butterfly operations.
        int stages = 31 - Integer.numberOfLeadingZeros(n);
        int revOff = 32 - stages;
        
        for (int stage=0; stage<stages; stage++)
        {
            int stride = 1 << (stages - stage - 1);
            int twidFac = 1 << stage;
            
            System.out.println(stage + " " + stride);
            
            for (int i=0; i<n; i++)
            {
                if ((i & stride) == 0)
                {
                    System.out.println("    " + i + " " + (i & stride));
                    write[i] = read[i] + read[i + stride];
                }
                else
                {
                    System.out.println("    " + i + " " + (i & stride) + " " + (i%stride)*twidFac);
                    write[i] = (read[i - stride] - read[i])*twiddle[(i%stride)*twidFac];
                }
            }
            
            //Swap buffers.
            float[] temp = read;
            read = write;
            write = temp;
        }
        
        //Perform final index bit-reversal.
        for (int i=0; i<n; i++) write[Integer.reverse(i) >>> revOff] = read[i];
        
        return write;
    }
    
    public static final float[][] fft(float[] input, float[][] twiddle)
    {
        float[] real = fft(input, twiddle[0]);
        float[] complex = fft(input, twiddle[1]);
        
        return new float[][]{real, complex};
    }
    
    public static final float[][] fft(float[] input, float[] window, float[][] twiddle)
    {
        float[] windowed = new float[input.length];
        for (int i=0; i<input.length; i++) windowed[i] = input[i]*window[i];
        return fft(windowed, twiddle);
    }
    
    private FFT()
    {
    }
}
