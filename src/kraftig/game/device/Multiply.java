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

public class Multiply extends Panel
{
    private final AudioInputJack inJackA, inJackB;
    private final AudioOutputJack outJack;
    
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    public Multiply()
    {
        frontInterface.add(new RowLayout(4.0f, Alignment.C,
                    inJackA = new AudioInputJack(),
                    new Label("\u00D7", 48.0f),
                    inJackB = new AudioInputJack(),
                    new Label("=", 48.0f),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Multiply", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(inJackA, inJackB, outJack);
    }
    
    @Override
    public void process(int samples)
    {
        float[][] inA = inJackA.getBuffer();
        float[][] inB = inJackB.getBuffer();
        
        if (inA == null || inB == null) DSPUtil.zero(buffer, samples);
        else for (int i=0; i<samples; i++)
        {
            buffer[0][i] = inA[0][i]*inB[0][i];
            buffer[1][i] = inA[1][i]*inB[1][i];
        }
    }
}
