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

public class Crossfade extends Panel
{
    private final AudioInputJack inJackA, inJackB;
    private final CrossfadeCurveGraph curveGraph = new CrossfadeCurveGraph(new Vec2(64.0f, 48.0f));
    private final Knob fadeKnob, powerKnob;
    private final AudioOutputJack outJack;
    
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
                        fadeKnob = new Knob(24.0f)
                            .onValueChanged(v -> set(v, power))
                            .setValue(0.5f),
                        new Label("Power", 6.0f),
                        powerKnob = new Knob(24.0f)
                            .onValueChanged(v -> set(fade, 1.0f - v*0.5f))
                            .setValue(0.5f)),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Crossfade", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private void set(float fade, float power)
    {
        this.fade = fade;
        this.power = power;
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(inJackA, inJackB, fadeKnob, powerKnob, outJack);
    }
    
    @Override
    public void process(int samples)
    {
        float[][] inA = inJackA.getBuffer();
        float[][] inB = inJackB.getBuffer();
        
        for (int i=0; i<samples; i++)
        {
            float vl = 0.0f, vr = 0.0f;
            
            fadeKnob.updateValue(i);
            powerKnob.updateValue(i);
            float a = (float)Math.pow(1.0 - fade, power);
            float b = (float)Math.pow(fade, power);
            
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
        fadeKnob.save(out);
        powerKnob.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        fadeKnob.load(in);
        powerKnob.load(in);
    }
    // </editor-fold>
}
