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
import kraftig.game.audio.Reverb;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.jacks.AudioInputJack;
import kraftig.game.gui.jacks.AudioOutputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.Knob;
import kraftig.game.util.DSPUtil;

public class ReverbDevice extends Panel
{
    private final AudioInputJack inJack;
    private final Knob feedbackKnob, stereoKnob, hfDampKnob, dryKnob, wetKnob;
    private final AudioOutputJack outJack;
    
    private final Reverb left = new Reverb();
    private final Reverb right = new Reverb();
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    public ReverbDevice()
    {
        frontInterface.add(new RowLayout(16.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Feedback", 6.0f),
                        feedbackKnob = new Knob(24.0f)
                            .onValueChanged(this::setFeedback)
                            .setValue(0.5f)),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Stereo", 6.0f),
                        stereoKnob = new Knob(24.0f)
                            .onValueChanged(this::setStereo)
                            .setValue(0.5f)),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("HF Damp", 6.0f),
                        hfDampKnob = new Knob(24.0f)
                            .onValueChanged(v -> setHFDamp((float)DSPUtil.experp(20.0, 20000.0, v)))
                            .setValue(0.5f)),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Dry", 6.0f),
                        dryKnob = new Knob(24.0f)
                            .onValueChanged(this::setDry)
                            .setValue(0.5f)),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Wet", 6.0f),
                        wetKnob = new Knob(24.0f)
                            .onValueChanged(this::setWet)
                            .setValue(0.5f)),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Biquad Filter", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private void setFeedback(float feedback)
    {
        left.setFeedback(feedback);
        right.setFeedback(feedback);
    }
    
    private void setStereo(float stereo)
    {
        right.setSeparation(Util.floor(stereo*100));
    }
    
    private void setHFDamp(float freq)
    {
        left.setHFDamp(freq);
        right.setHFDamp(freq);
    }
    
    private void setDry(float dry)
    {
        left.setDry(dry);
        right.setDry(dry);
    }
    
    private void setWet(float wet)
    {
        left.setWet(wet);
        right.setWet(wet);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(inJack, feedbackKnob, stereoKnob, hfDampKnob, dryKnob, wetKnob, outJack);
    }
    
    @Override
    public void process(int samples)
    {
        float[][] in = inJack.getBuffer();
        
        float[] leftIn = in != null ? in[0] : null;
        float[] rightIn = in != null ? in[1] : null;
        
        for (int i=0; i<samples; i++)
        {
            feedbackKnob.updateValue(i);
            stereoKnob.updateValue(i);
            hfDampKnob.updateValue(i);
            dryKnob.updateValue(i);
            wetKnob.updateValue(i);
            left.apply(leftIn, buffer[0], i);
            right.apply(rightIn, buffer[1], i);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        super.save(out);
        feedbackKnob.save(out);
        stereoKnob.save(out);
        hfDampKnob.save(out);
        dryKnob.save(out);
        wetKnob.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        feedbackKnob.load(in);
        stereoKnob.load(in);
        hfDampKnob.load(in);
        dryKnob.load(in);
        wetKnob.load(in);
    }
    // </editor-fold>
}
