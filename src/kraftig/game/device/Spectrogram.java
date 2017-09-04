package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.List;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.SpectrogramScreen;
import kraftig.game.gui.jacks.AudioInputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.util.DSPUtil;

public class Spectrogram extends Panel
{
    private final AudioInputJack inJack;
    private final SpectrogramScreen screen;
    
    public Spectrogram()
    {
        screen = new SpectrogramScreen(new Vec2(96.0f, 96.0f));
        
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    screen)
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label(Main.instance().getFont(), "Spectrogram", 48.0f, new Vec2(), Alignment.C));
        
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
