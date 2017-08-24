package kraftig.game.util;

import com.samrj.devil.math.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import kraftig.game.gui.Jack;
import kraftig.game.gui.Knob;

public class DSPUtil
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
    
    public static final void zero(float[][] out, int samples)
    {
        zero(out[0], samples);
        zero(out[1], samples);
    }
    
    //decibels
    public static final float dB(float gain)
    {
        return (float)(10.0*Math.log10(gain));
    }
    
    public static void updateKnobs(int samples, Knob... knobs)
    {
        int last = samples - 1;
        for (Knob knob : knobs) knob.updateValue(last);
    }
    
    public static List<Jack> jacks(Object... array)
    {
        ArrayList<Jack> out = new ArrayList<>(8);
        
        for (Object object : array)
        {
            if (object instanceof Object[]) out.addAll(Arrays.asList((Jack[])object));
            else if (object instanceof Collection) out.addAll((Collection<Jack>)object);
            else out.add((Jack)object);
        }
        
        return out;
    }
    
    public static double freqFromMidi(double midi)
    {
        return (440.0/32.0)*Math.pow(2.0, (midi - 9)/12.0);
    }
    
    public static double midiFromFreq(double freq)
    {
        return Math.log(freq*(32.0/440.0))/Math.log(2.0)*12.0 + 9.0;
    }
    
    private static float samp(float[] array, int index)
    {
        if (index < 0 || index >= array.length) return 0.0f;
        return array[index];
    }
    
    public static float linearSamp(float[] array, float index)
    {
        int i0 = Util.floor(index);
        int i1 = Util.ceil(index);
        if (i0 == i1) return samp(array, i0);
        
        float t = index - i0;
        float p0 = samp(array, i0);
        float p1 = samp(array, i1);
        return Util.lerp(p0, p1, t);
    }
    
    public static float cubicSamp(float[] array, float index)
    {
        int i0 = Util.floor(index);
        int i1 = Util.ceil(index);
        if (i0 == i1) return samp(array, i0);
        
        float t = index - i0;
        float p0 = samp(array, i0);
        float p1 = samp(array, i1);
        float m0 = (p1 - samp(array, i0 - 1))*0.5f;
        float m1 = (samp(array, i1 + 1) - p0)*0.5f;
        
        float t2 = t*t;
        float t3 = t2*t;
        
        float hp0 = 2.0f*t3 - 3.0f*t2 + 1.0f;
        float hm0 = t3 - 2.0f*t2 + t;
        float hp1 = -2.0f*t3 + 3.0f*t2;
        float hm1 = t3 - t2;
        return hp0*p0 + hm0*m0 + hp1*p1 + hm1*m1;
    }
    
//    //Lanczos A=3 sampling
//    public static float sincSamp(float[] array, float index)
//    {
//        int i0 = Util.floor(index);
//        int i1 = Util.ceil(index);
//        if (i0 == i1) return samp(array, i0);
//        
//        //Do shit here
//        
//        return 0.0f;
//    }
    
    private DSPUtil()
    {
    }
}
