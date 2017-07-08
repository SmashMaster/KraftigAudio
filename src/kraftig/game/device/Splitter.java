package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.stream.Stream;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.InputJack;
import kraftig.game.gui.Jack;
import kraftig.game.gui.Label;
import kraftig.game.gui.OutputJack;

public class Splitter extends Panel implements Device
{
    private final InputJack inJack;
    
    public Splitter()
    {
        inJack = new InputJack(new Vec2(0.0f, Jack.RADIUS*1.5f), Alignment.C);
        
        OutputJack[] outJacks = new OutputJack[4];
        for (int i=0; i<outJacks.length; i++)
        {
            float x = (i - (outJacks.length - 1)/2.0f)*Jack.RADIUS*2.5f;
            outJacks[i] = new OutputJack(this, inJack::getBuffer, new Vec2(x, -Jack.RADIUS*1.5f), Alignment.C);
        }
        
        setSize(0.125f, 0.0625f);
        for (OutputJack jack : outJacks) rearInterface.add(jack);
        rearInterface.add(inJack);
        frontInterface.add(new Label(Main.instance().getFont(), "Splitter", new Vec2(), Alignment.C));
    }
    
    @Override
    public Stream<Device> getInputDevices()
    {
        return inJack.hasLiveWire() ? Stream.of(inJack.getDevice()) : Stream.empty();
    }
    
    @Override
    public void process(int samples)
    {
    }
}
