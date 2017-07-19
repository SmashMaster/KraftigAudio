package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.stream.Stream;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.AudioInputJack;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;

public class Splitter extends Panel implements AudioDevice
{
    private final AudioInputJack inJack;
    
    public Splitter()
    {
        frontInterface.add(new RowLayout(2.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    new Label("\u2192", 24.0f),
                    new AudioOutputJack(this, inJack::getBuffer),
                    new AudioOutputJack(this, inJack::getBuffer),
                    new AudioOutputJack(this, inJack::getBuffer),
                    new AudioOutputJack(this, inJack::getBuffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label(Main.instance().getFont(), "Splitter", 24.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(4.0f);
    }
    
    @Override
    public Stream<AudioDevice> getInputDevices()
    {
        return inJack.getDevices();
    }
    
    @Override
    public void process(int samples)
    {
    }
}
