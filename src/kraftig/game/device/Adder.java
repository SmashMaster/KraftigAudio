package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.List;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.AudioInputJack;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.Jack;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;
import kraftig.game.util.DSPUtil;

public class Adder extends Panel
{
    private final AudioInputJack[] inJacks = new AudioInputJack[4];
    private final AudioOutputJack outJack;
    
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    public Adder()
    {
        frontInterface.add(new RowLayout(4.0f, Alignment.C,
                    inJacks[0] = new AudioInputJack(),
                    new Label("+", 48.0f),
                    inJacks[1] = new AudioInputJack(),
                    new Label("+", 48.0f),
                    inJacks[2] = new AudioInputJack(),
                    new Label("+", 48.0f),
                    inJacks[3] = new AudioInputJack(),
                    new Label("=", 48.0f),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Adder", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(inJacks, outJack);
    }
    
    @Override
    public void process(int samples)
    {
        float[][][] buffers =  new float[4][][];
        
        for (int j=0; j<4; j++) buffers[j] = inJacks[j].getBuffer();
        
        for (int i=0; i<samples; i++)
        {
            float vl = 0.0f, vr = 0.0f;
            
            for (float[][] b : buffers) if (b != null)
            {
                vl += b[0][i];
                vr += b[1][i];
            }
            
            buffer[0][i] = vl;
            buffer[1][i] = vr;
        }
    }
}
