package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.Label;
import kraftig.game.gui.RadioButtons;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.TextBox;
import kraftig.game.gui.jacks.AudioInputJack;
import kraftig.game.gui.jacks.AudioOutputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.Knob;
import kraftig.game.util.DSPUtil;

public class Gain extends Panel
{
    private final AudioInputJack inJack;
    private final RadioButtons displayRadio;
    private final Knob gainKnob;
    private final TextBox textBox = new TextBox(new Vec2(72.0f, 20.0f), Alignment.E, 32.0f);
    private final AudioOutputJack outJack;
    
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private int displayMode;
    private float gain;
    
    public Gain()
    {
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    displayRadio = new RadioButtons("dB", "ratio")
                            .onValueChanged(v -> set(v, gain))
                            .setValue(0),
                    gainKnob = new Knob(32.0f)
                            .onValueChanged(v -> set(displayMode, (float)Math.pow(16.0, v*2.0 - 1.0)))
                            .setValue(0.5f),
                    textBox,
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Gain", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private void set(int displayMode, float gain)
    {
        this.displayMode = displayMode;
        this.gain = gain;
        
        if (displayMode == 0) //dB
        {
            float db = DSPUtil.dB(gain);
            textBox.setText(String.format("%.1f", db) + " dB");
        }
        else
        {
            if (gain >= 1.0f) textBox.setText("\u00D7" + String.format("%.3f", gain));
            else textBox.setText("\u00F7" + String.format("%.3f", (1.0f/gain)));
        }
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(inJack, gainKnob, outJack);
    }
    
    @Override
    public void process(int samples)
    {
        float[][] in = inJack.getBuffer();
        
        if (in == null)
        {
            DSPUtil.updateKnobs(samples, gainKnob);
            DSPUtil.zero(buffer, samples);
        }
        else for (int i=0; i<samples; i++)
        {
            gainKnob.updateValue(i);
            buffer[0][i] = in[0][i]*gain;
            buffer[1][i] = in[1][i]*gain;
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        super.save(out);
        displayRadio.save(out);
        gainKnob.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        displayRadio.load(in);
        gainKnob.load(in);
    }
    // </editor-fold>
}
