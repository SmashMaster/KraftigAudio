package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.stream.Stream;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.AudioInputJack;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;

public class StereoSplitter extends Panel implements AudioDevice
{
    private static final float[] EMPTY = new float[Main.BUFFER_SIZE];
    
    private final AudioInputJack inJack;
    private final float[][] lBuffer = new float[2][], rBuffer = new float[2][];
    
    public StereoSplitter()
    {
        frontInterface.add(new RowLayout(4.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    new Label("\u2192", 48.0f),
                    new ColumnLayout(4.0f, Alignment.E,
                        new RowLayout(4.0f, Alignment.C,
                            new Label("L", 48.0f),
                            new AudioOutputJack(this, lBuffer)),
                        new RowLayout(4.0f, Alignment.C,
                            new Label("R", 48.0f),
                            new AudioOutputJack(this, rBuffer))))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new ColumnLayout(4.0f, Alignment.C,
                    new Label("Stereo", 48.0f),
                    new Label("Splitter", 48.0f))
                .setPos(new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public Stream<AudioDevice> getInputDevices()
    {
        return inJack.getDevices();
    }
    
    @Override
    public void process(int samples)
    {
        float[][] in = inJack.getBuffer();
        
        if (in == null)
        {
            lBuffer[0] = EMPTY;
            lBuffer[1] = EMPTY;
            rBuffer[0] = EMPTY;
            rBuffer[1] = EMPTY;
        }
        else
        {
            lBuffer[0] = in[0];
            lBuffer[1] = in[0];
            rBuffer[0] = in[1];
            rBuffer[1] = in[1];
        }
    }
}
