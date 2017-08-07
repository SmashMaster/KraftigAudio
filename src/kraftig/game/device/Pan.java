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
import kraftig.game.util.DSPMath;

public class Pan extends Panel implements AudioDevice
{
    private final AudioInputJack inJack;
    private final CrossfadeCurveGraph curveGraph;
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private float fade, power;

    public Pan()
    {
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    curveGraph = new CrossfadeCurveGraph(new Vec2(64.0f, 48.0f)),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Pan", 6.0f),
                        new Knob(24.0f)
                            .onValueChanged(v -> set(v, power))
                            .setValue(0.5f),
                        new Label("Power", 6.0f),
                        new Knob(24.0f)
                            .onValueChanged(v -> set(fade, 1.0f - v*0.5f))
                            .setValue(0.5f)),
                    new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Pan", 48.0f, new Vec2(), Alignment.C));
        
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
        return inJack.getDevices();
    }
    
    @Override
    public void process(int samples)
    {
        float[][] in = inJack.getBuffer();
        
        if (in == null) DSPMath.zero(buffer, samples);
        else
        {
            float lf = (float)(Math.pow(1.0 - fade, power));
            float rf = (float)(Math.pow(fade, power));
            
            for (int i=0; i<samples; i++)
            {
                buffer[0][i] = in[0][i]*lf;
                buffer[1][i] = in[1][i]*rf;
            }
        }
    }
}
