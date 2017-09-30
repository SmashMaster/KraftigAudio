package kraftig.game.audio;

import java.util.function.Function;

public class STFT
{
    private final int windowSize;
    private final float[] window;
    private final float[][] twiddle;
    private final Function<float[][], float[][]> action;
    
    private final Segment[] segments = new Segment[4];
    
    public STFT(int windowSize, Function<float[][], float[][]> action)
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
    
    public STFT(int frame)
    {
        this(frame, array -> array);
    }
    
    public void apply(float[] in, float[] out, int samples)
    {
        for (int i=0; i<samples; i++)
        {
            float val = in[i];
            float output = 0.0f;
            for (Segment segment : segments) output += segment.sample(val);
            out[i] = output;
        }
    }
    
    private class Segment
    {
        private final float[] buffer;
        
        private int index;
        private boolean playing;
        
        private Segment(int index, boolean playing)
        {
            buffer = new float[windowSize];
            
            this.index = index;
            this.playing = playing;
        }
        
        private float sample(float value)
        {
            if (playing)
            {
                float out = buffer[index]*window[index];
                if (++index == windowSize) //Done playing. Start recording.
                {
                    index = 0;
                    playing = false;
                }
                return out;
            }
            else
            {
                buffer[index] = value*window[index];
                if (++index == windowSize) //Done recording. Start playing.
                {
                    float[][] fft = FFT.fft(buffer, twiddle);
                    float[][] processed = action.apply(fft);
                    float[][] output = FFT.ifft(processed, twiddle);
                    System.arraycopy(output[0], 0, buffer, 0, windowSize);
                    
                    index = 0;
                    playing = true;
                }
                return 0.0f;
            }
        }
    }
}
