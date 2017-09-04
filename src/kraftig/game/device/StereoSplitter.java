package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.List;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.jacks.AudioInputJack;
import kraftig.game.gui.jacks.AudioOutputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.util.DSPUtil;

public class StereoSplitter extends Panel
{
    private static final float[] EMPTY = new float[Main.BUFFER_SIZE];
    
    private final AudioInputJack inJack;
    private final AudioOutputJack lOutJack, rOutJack;
    
    private final float[][] lBuffer = new float[2][], rBuffer = new float[2][];
    
    public StereoSplitter()
    {
        frontInterface.add(new RowLayout(4.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    new Label("\u2192", 48.0f),
                    new ColumnLayout(4.0f, Alignment.E,
                        new RowLayout(4.0f, Alignment.C,
                            new Label("L", 48.0f),
                            lOutJack = new AudioOutputJack(this, lBuffer)),
                        new RowLayout(4.0f, Alignment.C,
                            new Label("R", 48.0f),
                            rOutJack = new AudioOutputJack(this, rBuffer))))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new ColumnLayout(4.0f, Alignment.C,
                    new Label("Stereo", 48.0f),
                    new Label("Splitter", 48.0f))
                .setPos(new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(inJack, lOutJack, rOutJack);
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
