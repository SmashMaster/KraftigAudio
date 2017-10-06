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
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.jacks.AudioInputJack;
import kraftig.game.gui.jacks.AudioOutputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.Knob;
import kraftig.game.util.DSPUtil;

public class Clipper extends Panel
{
    private final AudioInputJack inJack;
    private final Knob hardnessKnob, threshKnob;
    private final AudioOutputJack outJack;
    
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private float gain;
    private float threshold;
    
    public Clipper()
    {
        frontInterface.add(new RowLayout(16.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Gain", 6.0f),
                        hardnessKnob = new Knob(24.0f)
                            .onValueChanged(v -> gain = (float)DSPUtil.experp(0.25, 128.0, v))
                            .setValue(0.5f)),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Threshold", 6.0f),
                        threshKnob = new Knob(24.0f)
                            .onValueChanged(v -> threshold = v*0.999f + .001f)
                            .setValue(0.5f)),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Clipper", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private float apply(float value)
    {
        return (2.0f/(1.0f + (float)Math.exp(gain*value/threshold)) - 1.0f)*threshold;
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(inJack, hardnessKnob, threshKnob, outJack);
    }
    
    @Override
    public void process(int samples)
    {
        float[][] in = inJack.getBuffer();
        
        if (in == null)
        {
            DSPUtil.updateKnobs(samples, hardnessKnob, threshKnob);
            DSPUtil.zero(buffer, samples);
        }
        else for (int i=0; i<samples; i++)
        {
            hardnessKnob.updateValue(i);
            threshKnob.updateValue(i);
            
            buffer[0][i] = apply(in[0][i]);
            buffer[1][i] = apply(in[1][i]);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        super.save(out);
        hardnessKnob.save(out);
        threshKnob.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        hardnessKnob.load(in);
        threshKnob.load(in);
    }
    // </editor-fold>
}
