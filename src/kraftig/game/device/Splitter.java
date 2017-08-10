package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.List;
import kraftig.game.Panel;
import kraftig.game.gui.AudioInputJack;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.Jack;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;
import kraftig.game.util.DSPUtil;

public class Splitter extends Panel
{
    private final AudioInputJack inJack;
    private final AudioOutputJack[] outJacks = new AudioOutputJack[4];
    
    public Splitter()
    {
        frontInterface.add(new RowLayout(4.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    new Label("\u2192", 48.0f),
                    outJacks[0] = new AudioOutputJack(this, inJack::getBuffer),
                    outJacks[1] = new AudioOutputJack(this, inJack::getBuffer),
                    outJacks[2] = new AudioOutputJack(this, inJack::getBuffer),
                    outJacks[3] = new AudioOutputJack(this, inJack::getBuffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Splitter", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(inJack, outJacks);
    }
    
    @Override
    public void process(int samples)
    {
    }
}
