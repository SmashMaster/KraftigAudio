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
import kraftig.game.util.DSPUtil;

public class Negate extends Panel implements AudioDevice
{
    private final AudioInputJack inJack;
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    public Negate()
    {
        frontInterface.add(new RowLayout(4.0f, Alignment.C,
                    new Label("-", 48.0f),
                    inJack = new AudioInputJack(),
                    new Label("=", 48.0f),
                    new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Negate", 48.0f, new Vec2(), Alignment.C));
        
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
        
        if (in == null) DSPUtil.zero(buffer, samples);
        else for (int i=0; i<samples; i++)
        {
            buffer[0][i] = -in[0][i];
            buffer[1][i] = -in[1][i];
        }
    }
}
