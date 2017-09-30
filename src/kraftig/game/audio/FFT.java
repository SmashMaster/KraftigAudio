package kraftig.game.audio;

import com.samrj.devil.math.Util;

public class FFT
{
    private static final int REAL = 0, IMAG = 1;
    
    public static final float[][] twiddle(int n)
    {
        if (!Util.isPower2(n)) throw new IllegalArgumentException();
        if (n < 2) throw new IllegalArgumentException();
        
        int len = n/2;
        float[] real = new float[len];
        float[] imag = new float[len];
        
        for (int k=0; k<len; k++)
        {
            double x = (2.0*Math.PI*k)/n;
            
            real[k] = (float)Math.cos(x);
            imag[k] = (float)-Math.sin(x);
        }
        
        return new float[][]{real, imag};
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
    
    public static final float[][] fft(float[][] input, float[][] twiddle)
    {
        //Check consistency of all input.
        int n = input[REAL].length;
        if (!Util.isPower2(n)) throw new IllegalArgumentException();
        if (n < 2) throw new IllegalArgumentException();
        if (twiddle[REAL].length != n/2) throw new IllegalArgumentException();
        
        //Prepare buffers.
        float[][] read = new float[2][n];
        float[][] write = new float[2][n];
        System.arraycopy(input[REAL], 0, read[REAL], 0, n);
        System.arraycopy(input[IMAG], 0, read[IMAG], 0, n);
        
        //Perform butterfly operations.
        int stages = 31 - Integer.numberOfLeadingZeros(n);
        int revOff = 32 - stages;
        
        for (int stage=0; stage<stages; stage++)
        {
            int stride = 1 << (stages - stage - 1);
            int twidFac = 1 << stage;
            
            for (int i=0; i<n; i++)
            {
                if ((i & stride) == 0)
                {
                    int fork = i + stride;
                    write[REAL][i] = read[REAL][i] + read[REAL][fork];
                    write[IMAG][i] = read[IMAG][i] + read[IMAG][fork];
                }
                else
                {
                    int fork = i - stride;
                    float realA = read[REAL][fork] - read[REAL][i];
                    float imagA = read[IMAG][fork] - read[IMAG][i];
                    
                    int twidIndex = (i%stride)*twidFac;
                    float realB = twiddle[REAL][twidIndex];
                    float imagB = twiddle[IMAG][twidIndex];
                    
                    write[REAL][i] = realA*realB - imagA*imagB;
                    write[IMAG][i] = realA*imagB + realB*imagA;
                }
            }
            
            //Swap buffers.
            float[][] temp = read;
            read = write;
            write = temp;
        }
        
        //Perform final index bit-reversal.
        for (int i=0; i<n; i++)
        {
            int reversed = Integer.reverse(i) >>> revOff;
            write[REAL][reversed] = read[REAL][i];
            write[IMAG][reversed] = read[IMAG][i];
        }
        
        return write;
    }
    
    public static final float[][] fft(float[] input, float[][] twiddle)
    {
        float[][] complex = new float[][]{input, new float[input.length]};
        return fft(complex, twiddle);
    }
    
    public static final float[][] fft(float[] input, float[] window, float[][] twiddle)
    {
        float[] windowed = new float[input.length];
        for (int i=0; i<input.length; i++) windowed[i] = input[i]*window[i];
        return fft(windowed, twiddle);
    }
    
    public static final float[][] ifft(float[][] input, float[][] twiddle)
    {
        float[][] swapped = {input[IMAG], input[REAL]};
        float[][] swapResult = fft(swapped, twiddle);
        float[][] result = {swapResult[IMAG], swapResult[REAL]};
        
        int len = result[REAL].length;
        float normalize = 1.0f/len;
        for (int i=0; i<len; i++)
        {
            result[REAL][i] *= normalize;
            result[IMAG][i] *= normalize;
        }
        
        return result;
    }
    
    public static final float[][] mult(float[][] a, float[][] b)
    {
        int len = a[REAL].length;
        if (b[REAL].length != len) throw new IllegalArgumentException();
        
        float[][] result = new float[2][len];
        
        for (int i=0; i<len; i++)
        {
            float realA = a[REAL][i];
            float imagA = a[IMAG][i];
            float realB = b[REAL][i];
            float imagB = b[IMAG][i];
            result[REAL][i] = realA*realB - imagA*imagB;
            result[IMAG][i] = realA*imagB + realB*imagA;
        }
        
        return result;
    }
    
    private FFT()
    {
    }
}
