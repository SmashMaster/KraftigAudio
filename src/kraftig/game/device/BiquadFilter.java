package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
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
import kraftig.game.util.DSPMath;

public class BiquadFilter extends Panel implements AudioDevice
{
    private final BiquadFilterKernel kernelLeft = new BiquadFilterKernel();
    private final BiquadFilterKernel kernelRight = new BiquadFilterKernel();
    private final BiquadResponseGraph responseGraph = new BiquadResponseGraph(new Vec2(64.0f, 48.0f));
    private final AudioInputJack inJack;
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private int filterMode;
    private float filterFreq;
    private float filterQ;
    
    public BiquadFilter()
    {
        frontInterface.add(new RowLayout(16.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    new RadioButtons("Low Pass", "Band Pass", "High Pass", "Band Reject", "All Pass")
                        .onValueChanged(mode -> set(mode, filterFreq, filterQ))
                        .setValue(0),
                    responseGraph,
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Freq", 6.0f),
                        new Knob(24.0f)
                            .onValueChanged(v -> set(filterMode, (float)DSPMath.experp(20.0, 20000.0, v), filterQ))
                            .setValue(0.5f),
                        new Label("Q factor", 6.0f),
                        new Knob(24.0f)
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
        responseGraph.update(kernelLeft.s);
    }
    
    @Override
    public Stream<AudioDevice> getInputDevices()
    {
        return inJack.getDevices();
    }
    
    @Override
    public void process(int samples)
    {
        float[][] in = inJack.getBuffer();
        
        kernelLeft.apply(in != null ? in[0] : null, buffer[0], samples);
        kernelRight.apply(in != null ? in[1] : null, buffer[1], samples);
    }
}
