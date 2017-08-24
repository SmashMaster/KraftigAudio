package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.List;
import kraftig.game.Panel;
import kraftig.game.gui.AudioInputJack;
import kraftig.game.gui.ChromaticStarScreen;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Jack;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;
import kraftig.game.util.DSPUtil;

public class ChromaticStar extends Panel
{
    private final AudioInputJack inJack;
    private final ChromaticStarScreen screen;
    
    public ChromaticStar()
    {
        screen = new ChromaticStarScreen(new Vec2(96.0f, 96.0f));
        
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    screen)
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new ColumnLayout(4.0f, Alignment.C,
                    new Label("Chromatic", 48.0f),
                    new Label("Star", 48.0f))
                .setPos(new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(inJack);
    }
    
    @Override
    public void process(int samples)
    {
        float[][] buffer = inJack.getBuffer();
        screen.process(buffer, samples);
    }
}
