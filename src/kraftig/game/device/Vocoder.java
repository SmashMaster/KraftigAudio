package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.List;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.audio.BiquadFilterKernel;
import kraftig.game.audio.BiquadFilterKernel.Settings;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.jacks.AudioInputJack;
import kraftig.game.gui.jacks.AudioOutputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.Knob;
import kraftig.game.util.DSPUtil;

public class Vocoder extends Panel
{
    private final double MIN_FREQ = 20.0, MAX_FREQ = 20000.0;
    private final Settings ENV_FOLLOWER = new Settings().lowPass(60.0, 1.0);
    
    private final AudioInputJack carrierJack, modulatorJack;
    private final Knob resolutionKnob;
    private final Knob qFactorKnob;
    private final AudioOutputJack outJack;
    
    private Band[][] bands = new Band[2][];
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private int resolution;
    private double qFactor;
    
    public Vocoder()
    {
        frontInterface.add(new RowLayout(16.0f, Alignment.C,
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Carrier", 6.0f),
                        carrierJack = new AudioInputJack(),
                        new Label("Modulator", 6.0f),
                        modulatorJack = new AudioInputJack()),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Resolution", 6.0f),
                        resolutionKnob = new Knob(24.0f)
                            .onValueChanged(v -> set(32, qFactor))
                            .setValue(0.5f),
                        new Label("Q factor", 6.0f),
                        qFactorKnob = new Knob(24.0f)
                            .onValueChanged(v -> set(resolution, Math.pow(64.0, v)))
                            .setValue(0.5f)),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Vocoder", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(carrierJack, modulatorJack, resolutionKnob, qFactorKnob, outJack);
    }
    
    private void set(int resolution, double qFactor)
    {
        this.resolution = resolution;
        this.qFactor = qFactor;
        
        bands[0] = new Band[resolution];
        bands[1] = new Band[resolution];
        
        double dt = 1.0/(resolution - 1.0);
        double t = 0.0;
        for (int b=0; b<resolution; b++)
        {
            double freq = DSPUtil.experp(MIN_FREQ, MAX_FREQ, t);
            Band left = new Band(freq);
            Band right = new Band(left);
            bands[0][b] = left;
            bands[1][b] = right;
            t += dt;
        }
        
    }
    
    @Override
    public void process(int samples)
    {
        float[][] carrier = carrierJack.getBuffer();
        float[][] modulator = modulatorJack.getBuffer();
        
        DSPUtil.updateKnobs(samples, resolutionKnob, qFactorKnob);
        
        if (carrier == null || modulator == null) DSPUtil.zero(buffer, samples);
        else for (int chan=0; chan<2; chan++)
            for (int i=0; i<samples; i++)
        {
            float sum = 0.0f;

            for (int b=0; b<resolution; b++)
            {
                Band band = bands[chan][b];
                float signal = band.carrier.apply(carrier[chan][i]);
                float bMod = band.modulator.apply(modulator[chan][i]);
                float envelope = band.modEnv.apply(Math.abs(bMod));

                sum += signal*envelope;
            }

            buffer[chan][i] = sum;
        }
    }
    
    private class Band
    {
        private final BiquadFilterKernel carrier = new BiquadFilterKernel();
        private final BiquadFilterKernel modulator = new BiquadFilterKernel();
        private final BiquadFilterKernel modEnv = new BiquadFilterKernel();
        
        private Band(double freq)
        {
            carrier.s.bandPass(freq, qFactor);
            modulator.s.set(carrier.s);
            modEnv.s.set(ENV_FOLLOWER);
        }
        
        private Band(Band other)
        {
            carrier.s.set(other.carrier.s);
            modulator.s.set(other.modulator.s);
            modEnv.s.set(other.modEnv.s);
        }
    }
}
