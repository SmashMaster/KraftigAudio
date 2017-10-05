package kraftig.game.audio;

import kraftig.game.audio.BiquadFilterKernel.Settings;

public class Reverb
{
    private static final Settings DEFAULT_FILTER = new Settings().lowPass(10000.0f, 0.5f);
    
    private final LowpassComb[] head = new LowpassComb[8];
    private final AllPass[] tail = new AllPass[4];
    
    private float dry = 0.5f, wet = 0.025f;
    
    public Reverb()
    {
        for (int i=0; i<8; i++) head[i] = new LowpassComb(2048);
        for (int i=0; i<4; i++) tail[i] = new AllPass(762);
        for (LowpassComb comb : head) comb.filter.s.set(DEFAULT_FILTER);
        setSeparation(0);
    }
    
    public void setFeedback(float f)
    {
        for (LowpassComb comb : head) comb.feedback = f;
    }
    
    public void setSeparation(int separation)
    {
        head[0].delay.setLength(1557 + separation);
        head[1].delay.setLength(1617 + separation);
        head[2].delay.setLength(1491 + separation);
        head[3].delay.setLength(1422 + separation);
        head[4].delay.setLength(1277 + separation);
        head[5].delay.setLength(1356 + separation);
        head[6].delay.setLength(1188 + separation);
        head[7].delay.setLength(1116 + separation);
        
        tail[0].delay.setLength(225 + separation);
        tail[1].delay.setLength(556 + separation);
        tail[2].delay.setLength(441 + separation);
        tail[3].delay.setLength(341 + separation);
    }
    
    public void setHFDamp(float freq)
    {
        Settings filter = new Settings();
        filter.lowPass(freq, 0.5f);
        for (LowpassComb comb : head) comb.filter.s.set(filter);
    }
    
    public void setDry(float dry)
    {
        this.dry = dry;
    }
    
    public void setWet(float wet)
    {
        this.wet = wet/10.0f;
    }
    
    public float apply(float value)
    {
        float result = 0.0f;
        for (LowpassComb comb : head) result += comb.apply(value);
        for (AllPass allpass : tail) result = allpass.apply(result);
        return value*dry + result*wet;
    }
    
    public void apply(float[] input, float[] output, int index)
    {
        float x = input != null ? input[index] : 0.0f;
        output[index] = apply(x);
    }
    
    private class AllPass
    {
        private final float COEFF = 0.5f;
        private float fv;
        private final DelayLine delay = new DelayLine(v -> -COEFF*(fv = v));
        
        private AllPass(int length)
        {
            delay.setLength(length);
        }
        
        private float apply(float value)
        {
            return (value + delay.apply(value))*COEFF + fv;
        }
    }
    
    private class LowpassComb
    {
        private final float GAIN = 0.2f;
        private float feedback = 0.85f;
        private final BiquadFilterKernel filter = new BiquadFilterKernel();
        private final DelayLine delay = new DelayLine(v -> feedback*filter.apply(v));
        
        private LowpassComb(int length)
        {
            delay.setLength(length);
        }
        
        private float apply(float value)
        {
            return delay.apply(value)*GAIN;
        }
    }
}
