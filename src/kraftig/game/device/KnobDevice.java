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
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.jacks.AudioOutputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.Knob;
import kraftig.game.util.DSPUtil;

public class KnobDevice extends Panel
{
    private final Knob knob;
    private final AudioOutputJack outJack;
    
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private float value;
    
    public KnobDevice()
    {
        frontInterface.add(new RowLayout(12.0f, Alignment.C,
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Value", 6.0f),
                        knob = new Knob(24.0f)
                            .setValue(0.5f)
                            .onValueChanged(v -> value = v*2.0f - 1.0f)),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Knob", 48.0f, new Vec2(0.0f, 0.0f), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(outJack);
    }
    
    @Override
    public void process(int samples)
    {
        for (int i=0; i<samples; i++)
        {
            knob.updateValue(i);
            buffer[0][i] = value;
            buffer[1][i] = value;
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        super.save(out);
        knob.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        knob.load(in);
    }
    // </editor-fold>
}
