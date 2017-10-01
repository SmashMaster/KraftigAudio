package kraftig.game.device;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.audio.FFT;
import kraftig.game.audio.STFT;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.jacks.AudioInputJack;
import kraftig.game.gui.jacks.AudioOutputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.Knob;
import kraftig.game.util.DSPUtil;

public class PitchShift extends Panel
{
    private final AudioInputJack inJack;
    private final Knob shiftKnob;
    private final AudioOutputJack outJack;
    
    private final STFT stftLeft = new STFT(1024, this::shift);
    private final STFT stftRight = new STFT(1024, this::shift);
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private float shiftFactor;
    
    public PitchShift()
    {
        frontInterface.add(new RowLayout(16.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Shift", 6.0f),
                        shiftKnob = new Knob(24.0f)
                            .onValueChanged(v -> shiftFactor = (float)Math.pow(4.0, v*2.0 - 1.0))
                            .setValue(0.5f)),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Shifter", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private float[][] shift(float[][] fft)
    {
        int len = fft[0].length;
        int half = len/2;
        float[][] result = new float[2][len];
        
        //Shift using cubic resampling (lanczos would be better) up to middle bin
        for (int i = 0; i <= half; i++)
        {
            float freq = FFT.freqFromBin(i, len, Main.SAMPLE_RATE);
            float oldFreq = freq/shiftFactor;
            float oldBin = FFT.binFromFreq(oldFreq, len, Main.SAMPLE_RATE);
            
            float et = Util.saturate(oldBin/half);
            float envelope = (float)Math.pow(4.0*et*(1.0 - et), 0.0625);
            
            result[0][i] = DSPUtil.lanczosSamp(fft[0], oldBin)*envelope;
            result[1][i] = DSPUtil.lanczosSamp(fft[1], oldBin)*envelope;
        }
        
        //Copy remaining bins, with complex conjugate symmetry.
        for (int i = half + 1; i < len; i++)
        {
            int mirror = len - i;
            result[0][i] = result[0][mirror];
            result[1][i] = -result[1][mirror];
        }
        
        return result;
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(inJack, shiftKnob, outJack);
    }
    
    @Override
    public void process(int samples)
    {
        float[][] in = inJack.getBuffer();
        
        if (in != null) for (int i=0; i<samples; i++)
        {
            shiftKnob.updateValue(i);
            stftLeft.apply(in[0], buffer[0], i);
            stftRight.apply(in[1], buffer[1], i);
        }
        else DSPUtil.zero(buffer, samples);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        super.save(out);
        shiftKnob.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        shiftKnob.load(in);
    }
    // </editor-fold>
}
