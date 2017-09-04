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
import kraftig.game.gui.CrossfadeCurveGraph;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.jacks.AudioInputJack;
import kraftig.game.gui.jacks.AudioOutputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.Knob;
import kraftig.game.util.DSPUtil;

public class Pan extends Panel
{
    private final AudioInputJack inJack;
    private final CrossfadeCurveGraph curveGraph;
    private final Knob panKnob, powerKnob;
    private final AudioOutputJack outJack;
    
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private float fade, power;
    private float fLeft, fRight;

    public Pan()
    {
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    curveGraph = new CrossfadeCurveGraph(new Vec2(64.0f, 48.0f)),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Pan", 6.0f),
                        panKnob = new Knob(24.0f)
                            .onValueChanged(v -> set(v, power))
                            .setValue(0.5f),
                        new Label("Power", 6.0f),
                        powerKnob = new Knob(24.0f)
                            .onValueChanged(v -> set(fade, 1.0f - v*0.5f))
                            .setValue(0.5f)),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Pan", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private void set(float fade, float power)
    {
        this.fade = fade;
        this.power = power;
        
        fLeft = (float)(Math.pow(1.0 - fade, power));
        fRight = (float)(Math.pow(fade, power));
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(inJack, panKnob, powerKnob, outJack);
    }
    
    @Override
    public void process(int samples)
    {
        float[][] in = inJack.getBuffer();
        
        if (in == null)
        {
            DSPUtil.updateKnobs(samples, panKnob, powerKnob);
            DSPUtil.zero(buffer, samples);
        }
        else for (int i=0; i<samples; i++)
        {
            panKnob.updateValue(i);
            powerKnob.updateValue(i);
            buffer[0][i] = in[0][i]*fLeft;
            buffer[1][i] = in[1][i]*fRight;
        }
    }
    
    @Override
    public void render()
    {
        curveGraph.update(fade, power);
        super.render();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        super.save(out);
        panKnob.save(out);
        powerKnob.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        panKnob.load(in);
        powerKnob.load(in);
    }
    // </editor-fold>
}
