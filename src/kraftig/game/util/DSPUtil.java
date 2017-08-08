package kraftig.game.util;

import java.util.stream.Stream;
import kraftig.game.device.AudioDevice;
import kraftig.game.gui.AudioInputJack;
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
    
    public static Stream<AudioDevice> getDevices(AudioInputJack... jacks)
    {
        return Stream.of(jacks).flatMap(AudioInputJack::getDevices);
    }
    
    public static void updateKnobs(int samples, Knob... knobs)
    {
        int last = samples - 1;
        for (Knob knob : knobs) knob.updateValue(last);
    }
    
    private DSPUtil()
    {
    }
}
