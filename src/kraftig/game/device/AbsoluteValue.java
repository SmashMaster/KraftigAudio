package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.List;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.jacks.AudioInputJack;
import kraftig.game.gui.jacks.AudioOutputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.util.DSPUtil;

public class AbsoluteValue extends Panel
{
    private final AudioInputJack inJack;
    private final AudioOutputJack outJack;
    
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    public AbsoluteValue()
    {
        frontInterface.add(new RowLayout(4.0f, Alignment.C,
                    new Label("|", 48.0f),
                    inJack = new AudioInputJack(),
                    new Label("|=", 48.0f),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Abs", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(inJack, outJack);
    }
    
    @Override
    public void process(int samples)
    {
        float[][] in = inJack.getBuffer();
        
        if (in == null) DSPUtil.zero(buffer, samples);
        else for (int i=0; i<samples; i++)
        {
            buffer[0][i] = Math.abs(in[0][i]);
            buffer[1][i] = Math.abs(in[1][i]);
        }
    }
}
