package kraftig.game.device;

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
import kraftig.game.gui.RadioButtons;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.TextBox;
import kraftig.game.gui.jacks.AudioInputJack;
import kraftig.game.gui.jacks.AudioOutputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.Knob;
import kraftig.game.util.CircularBuffer;
import kraftig.game.util.DSPUtil;

public class Delay extends Panel
{
    private static final int MAX_DELAY = 48000;
    
    private final AudioInputJack inJack;
    private final RadioButtons displayRadio;
    private final Knob delayKnob;
    private final TextBox textBox = new TextBox(new Vec2(48.0f, 16.0f), Alignment.E, 24.0f);
    private final Knob feedbackKnob, dryKnob, wetKnob;
    private final AudioOutputJack outJack;
    
    private final CircularBuffer left = new CircularBuffer(MAX_DELAY + 8);
    private final CircularBuffer right = new CircularBuffer(MAX_DELAY + 8);
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private int delay;
    private int displayMode;
    private float feedback;
    private float dry, wet;
    
    public Delay()
    {
        frontInterface.add(new RowLayout(12.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Delay", 6.0f),
                        delayKnob = new Knob(24.0f)
                            .onValueChanged(v -> delay = (int)Math.round(Math.pow(v, 3.0)*MAX_DELAY))
                            .setValue(0.5f)),
                    displayRadio = new RadioButtons("N", "ms")
                        .onValueChanged(v -> displayMode = v)
                        .setValue(0),
                    textBox,
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Feedback", 6.0f),
                        feedbackKnob = new Knob(24.0f)
                            .onValueChanged(v -> feedback = v)
                            .setValue(0.0f)),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Dry", 6.0f),
                        dryKnob = new Knob(24.0f)
                            .onValueChanged(v -> dry = v)
                            .setValue(0.75f)),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Wet", 6.0f),
                        wetKnob = new Knob(24.0f)
                            .onValueChanged(v -> wet = v)
                            .setValue(0.75f)),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Delay", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(inJack, delayKnob, feedbackKnob, dryKnob, wetKnob, outJack);
    }
    
    @Override
    public void process(int samples)
    {
        float[][] in = inJack.getBuffer();
        
        if (in == null)
        {
            DSPUtil.updateKnobs(samples, delayKnob, feedbackKnob, dryKnob, wetKnob);
            DSPUtil.zero(buffer, samples);
            left.clear();
            right.clear();
        }
        else for (int i=0; i<samples; i++)
        {
            delayKnob.updateValue(i);
            feedbackKnob.updateValue(i);
            dryKnob.updateValue(i);
            wetKnob.updateValue(i);
            
            if (delay == 0)
            {
                buffer[0][i] = in[0][i];
                buffer[1][i] = in[1][i];
                left.clear();
                right.clear();
                continue;
            }
            
            float l = in[0][i];
            float r = in[1][i];
            int size = left.getSize();
            
            if (size < delay)
            {
                buffer[0][i] = in[0][i]*dry;
                buffer[1][i] = in[1][i]*dry;
                left.push(l);
                right.push(r);
            }
            else
            {
                while (size > delay)
                {
                    left.poll();
                    right.poll();
                    size = left.getSize();
                }
                
                float fbl = left.poll();
                float fbr = right.poll();
                
                buffer[0][i] = in[0][i]*dry + fbl*wet;
                buffer[1][i] = in[1][i]*dry + fbr*wet;
                
                left.push(l + fbl*feedback);
                right.push(r + fbr*feedback);
            }
        }
    }
    
    @Override
    public void render()
    {
        if (displayMode == 0) textBox.setText("" + delay);
        else textBox.setText("" + ((delay*1000.0f)/Main.SAMPLE_RATE));
        super.render();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        super.save(out);
        delayKnob.save(out);
        displayRadio.save(out);
        feedbackKnob.save(out);
        dryKnob.save(out);
        wetKnob.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        delayKnob.load(in);
        displayRadio.load(in);
        feedbackKnob.load(in);
        dryKnob.load(in);
        wetKnob.load(in);
    }
    // </editor-fold>
}
