package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.stream.Stream;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.audio.BiquadFilterKernel;
import kraftig.game.gui.AudioInputJack;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.BiquadResponseGraph;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Knob;
import kraftig.game.gui.Label;
import kraftig.game.gui.RadioButtons;
import kraftig.game.gui.RowLayout;
import kraftig.game.util.DSPUtil;

public class BiquadFilter extends Panel implements AudioDevice
{
    private final BiquadFilterKernel kernelLeft = new BiquadFilterKernel();
    private final BiquadFilterKernel kernelRight = new BiquadFilterKernel();
    private final BiquadResponseGraph responseGraph = new BiquadResponseGraph(new Vec2(64.0f, 48.0f));
    private final AudioInputJack inJack;
    private final RadioButtons typeRadio;
    private final Knob freqKnob, qKnob;
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private int filterMode;
    private float filterFreq;
    private float filterQ;
    
    public BiquadFilter()
    {
        frontInterface.add(new RowLayout(16.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    typeRadio = new RadioButtons("Low Pass", "Band Pass", "High Pass", "Band Reject", "All Pass")
                        .onValueChanged(mode -> set(mode, filterFreq, filterQ))
                        .setValue(0),
                    responseGraph,
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Freq", 6.0f),
                        freqKnob = new Knob(24.0f)
                            .onValueChanged(v -> set(filterMode, (float)DSPUtil.experp(20.0, 20000.0, v), filterQ))
                            .setValue(0.5f),
                        new Label("Q factor", 6.0f),
                        qKnob = new Knob(24.0f)
                            .onValueChanged(v -> set(filterMode, filterFreq, (float)Math.pow(16.0, v*2.0 - 1.0)))
                            .setValue(0.5f)),
                    new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Biquad Filter", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private void set(int mode, float freq, float q)
    {
        filterMode = mode;
        filterFreq = freq;
        filterQ = q;
        
        switch (mode)
        {
            case 0: kernelLeft.s.lowPass(freq, q); break;
            case 1: kernelLeft.s.bandPass(freq, q); break;
            case 2: kernelLeft.s.highPass(freq, q); break;
            case 3: kernelLeft.s.bandReject(freq, q); break;
            case 4: kernelLeft.s.allPass(freq, q); break;
        }
        
        kernelRight.s.set(kernelLeft.s);
    }
    
    @Override
    public Stream<AudioDevice> getInputDevices()
    {
        return DSPUtil.getDevices(inJack, freqKnob, qKnob);
    }
    
    @Override
    public void process(int samples)
    {
        float[][] in = inJack.getBuffer();
        
        float[] leftIn = in != null ? in[0] : null;
        float[] rightIn = in != null ? in[1] : null;
        
        for (int i=0; i<samples; i++)
        {
            freqKnob.updateValue(i);
            qKnob.updateValue(i);
            kernelLeft.apply(leftIn, buffer[0], i);
            kernelRight.apply(rightIn, buffer[1], i);
        }
    }
    
    @Override
    public void render()
    {
        responseGraph.update(kernelLeft.s);
        super.render();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        super.save(out);
        typeRadio.save(out);
        freqKnob.save(out);
        qKnob.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        typeRadio.load(in);
        freqKnob.load(in);
        qKnob.load(in);
    }
    // </editor-fold>
}
