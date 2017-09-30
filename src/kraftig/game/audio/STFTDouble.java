package kraftig.game.audio;

import java.util.function.BiFunction;

public class STFTDouble
{
    private final int windowSize;
    private final float[] window;
    private final float[][] twiddle;
    private final BiFunction<float[][], float[][], float[][]> action;
    
    private final Segment[] segments = new Segment[4];
    
    public STFTDouble(int windowSize, BiFunction<float[][], float[][], float[][]> action)
    {
        this.windowSize = windowSize;
        this.action = action;
        window = new float[windowSize];
        twiddle = FFT.twiddle(windowSize);
        
        int half = windowSize/2;
        
        for (int i=0; i<windowSize; i++)
        {
            if (i < half) window[i] = i/(float)half;
            else window[i] = (windowSize - i)/(float)half;
            window[i] = (float)Math.sqrt(window[i]);
        }
        
        segments[0] = new Segment(half, true); //Halfway done playing.
        segments[1] = new Segment(0, true); //Just began playing.
        segments[2] = new Segment(half, false); //Halfway done recording.
        segments[3] = new Segment(0, false); //Just began recording.
    }
    
    public void apply(float[] inA, float[] inB, float[] out, int samples)
    {
        for (int i=0; i<samples; i++)
        {
            float valA = inA[i];
            float valB = inB[i];
            float output = 0.0f;
            for (Segment segment : segments) output += segment.sample(valA, valB);
            out[i] = output;
        }
    }
    
    private class Segment
    {
        private final float[] bufferA, bufferB;
        
        private int index;
        private boolean playing;
        
        private Segment(int index, boolean playing)
        {
            bufferA = new float[windowSize];
            bufferB = new float[windowSize];
            
            this.index = index;
            this.playing = playing;
        }
        
        private float sample(float valA, float valB)
        {
            if (playing)
            {
                float out = bufferA[index]*window[index];
                if (++index == windowSize) //Done playing. Start recording.
                {
                    index = 0;
                    playing = false;
                }
                return out;
            }
            else
            {
                bufferA[index] = valA*window[index];
                bufferB[index] = valB*window[index];
                if (++index == windowSize) //Done recording. Start playing.
                {
                    float[][] fftA = FFT.fft(bufferA, twiddle);
                    float[][] fftB = FFT.fft(bufferB, twiddle);
                    float[][] processed = action.apply(fftA, fftB);
                    float[][] output = FFT.ifft(processed, twiddle);
                    System.arraycopy(output[0], 0, bufferA, 0, windowSize);
                    
                    index = 0;
                    playing = true;
                }
                return 0.0f;
            }
        }
    }
}
