package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.stream.Stream;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.AudioInputJack;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.CrossfadeCurveGraph;
import kraftig.game.gui.Knob;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;

public class Crossfade extends Panel implements AudioDevice
{
    private final AudioInputJack inJackA, inJackB;
    private final CrossfadeCurveGraph curveGraph = new CrossfadeCurveGraph(new Vec2(64.0f, 48.0f));
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private float fade, power;

    public Crossfade()
    {
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    new ColumnLayout(4.0f, Alignment.C,
                        inJackA = new AudioInputJack(),
                        inJackB = new AudioInputJack()),
                    curveGraph,
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Fade", 6.0f),
                        new Knob(24.0f)
                            .onValueChanged(v -> set(v, power))
                            .setValue(0.5f),
                        new Label("Power", 6.0f),
                        new Knob(24.0f)
                            .onValueChanged(v -> set(fade, 1.0f - v*0.5f))
                            .setValue(0.5f)),
                    new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Crossfade", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private void set(float fade, float power)
    {
        this.fade = fade;
        this.power = power;
        
        curveGraph.update(fade, power);
    }
    
    @Override
    public Stream<AudioDevice> getInputDevices()
    {
        return Stream.of(inJackA, inJackB).flatMap(AudioInputJack::getDevices);
    }
    
    @Override
    public void process(int samples)
    {
        float[][] inA = inJackA.getBuffer();
        float[][] inB = inJackB.getBuffer();
        
        float a = (float)Math.pow(1.0 - fade, power);
        float b = (float)Math.pow(fade, power);
        
        for (int i=0; i<samples; i++)
        {
            float vl = 0.0f, vr = 0.0f;
            
            if (inA != null)
            {
                vl += inA[0][i]*a;
                vr += inA[1][i]*a;
            }
            
            if (inB != null)
            {
                vl += inB[0][i]*b;
                vr += inB[1][i]*b;
            }
            
            buffer[0][i] = vl;
            buffer[1][i] = vr;
        }
    }
}
