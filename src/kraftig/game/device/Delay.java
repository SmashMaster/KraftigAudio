package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.AudioInputJack;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Jack;
import kraftig.game.gui.Knob;
import kraftig.game.gui.Label;
import kraftig.game.gui.RadioButtons;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.TextBox;
import kraftig.game.util.CircularBuffer;
import kraftig.game.util.DSPUtil;

public class Delay extends Panel
{
    private static final int MAX_DELAY = 48000;
    
    private final AudioInputJack inJack;
    private final RadioButtons displayRadio;
    private final Knob delayKnob;
    private final TextBox textBox = new TextBox(new Vec2(48.0f, 16.0f), Alignment.E, 24.0f);
    private final Knob feedbackKnob;
    private final AudioOutputJack outJack;
    
    private final CircularBuffer left = new CircularBuffer(MAX_DELAY + 8);
    private final CircularBuffer right = new CircularBuffer(MAX_DELAY + 8);
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private int delay;
    private int displayMode;
    private float feedback;
    
    public Delay()
    {
        frontInterface.add(new RowLayout(12.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Delay", 6.0f),
                        delayKnob = new Knob(24.0f)
                            .onValueChanged(v -> set((int)Math.round(Math.pow(v, 3.0)*MAX_DELAY), displayMode, feedback))
                            .setValue(0.5f)),
                    displayRadio = new RadioButtons("N", "ms")
                        .onValueChanged(v -> set(delay, v, feedback))
                        .setValue(0),
                    textBox,
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Feedback", 6.0f),
                        feedbackKnob = new Knob(24.0f)
                            .onValueChanged(v -> set(delay, displayMode, v))
                            .setValue(0.0f)),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Delay", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private void set(int delay, int displayMode, float feedback)
    {
        this.delay = delay;
        this.displayMode = displayMode;
        this.feedback = feedback;
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(inJack, delayKnob, feedbackKnob, outJack);
    }
    
    @Override
    public void process(int samples)
    {
        float[][] in = inJack.getBuffer();
        
        if (in == null)
        {
            DSPUtil.updateKnobs(samples, delayKnob, feedbackKnob);
            DSPUtil.zero(buffer, samples);
            left.clear();
            right.clear();
        }
        else for (int i=0; i<samples; i++)
        {
            delayKnob.updateValue(i);
            feedbackKnob.updateValue(i);
            
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
                buffer[0][i] = 0.0f;
                buffer[1][i] = 0.0f;
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
                
                buffer[0][i] = fbl;
                buffer[1][i] = fbr;
                
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
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        delayKnob.load(in);
        displayRadio.load(in);
        feedbackKnob.load(in);
    }
    // </editor-fold>
}
