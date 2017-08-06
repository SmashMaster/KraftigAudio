package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.stream.Stream;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.AudioInputJack;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Knob;
import kraftig.game.gui.Label;
import kraftig.game.gui.PanCurveGraph;
import kraftig.game.gui.RowLayout;
import kraftig.game.util.DSPMath;

public class Pan extends Panel implements AudioDevice
{
    private final AudioInputJack inJack;
    private final PanCurveGraph curveGraph = new PanCurveGraph(new Vec2(64.0f, 48.0f));
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private float pan, power;

    public Pan()
    {
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    curveGraph,
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Pan", 6.0f),
                        new Knob(24.0f)
                            .onValueChanged(v -> set(v, power))
                            .setValue(0.5f),
                        new Label("Power", 6.0f),
                        new Knob(24.0f)
                            .onValueChanged(v -> set(pan, v))
                            .setValue(0.5f)),
                    new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Pan", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private void set(float pan, float power)
    {
        this.pan = pan;
        this.power = power;
        
        curveGraph.update(pan, power);
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
        else if (pan >= 0.5) for (int i=0; i<samples; i++)
        {
            float l = in[0][i];
            float r = in[1][i];
            float t = (pan - 0.5f)*2.0f;

            buffer[0][i] = (1.0f - t)*l;
            buffer[1][i] = (1.0f - t)*r + t*(l + r)*(0.5f + power*0.5f);
        }
        else for (int i=0; i<samples; i++)
        {
            float l = in[0][i];
            float r = in[1][i];
            float t = (0.5f - pan)*2.0f;

            buffer[0][i] = (1.0f - t)*l + t*(l + r)*(0.5f + power*0.5f);
            buffer[1][i] = (1.0f - t)*r;
        }
    }
}
