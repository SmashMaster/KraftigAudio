package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.stream.Stream;
import kraftig.game.Panel;
import kraftig.game.gui.AudioInputJack;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;

public class Adder extends Panel implements AudioDevice
{
    private final AudioInputJack inJackA, inJackB;
    
    private final float[][] buffer = new float[2][48000];
    
    public Adder()
    {
        setSize(0.125f, 0.0625f);
        rearInterface.add(new RowLayout(2.0f, Alignment.C,
                    inJackA = new AudioInputJack(),
                    new Label("+", 24.0f),
                    inJackB = new AudioInputJack(),
                    new Label("=", 24.0f),
                    new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        frontInterface.add(new Label("Adder", 32.0f, new Vec2(), Alignment.C));
    }
    
    @Override
    public Stream<AudioDevice> getInputDevices()
    {
        return Stream.concat(inJackA.getDevices(), inJackB.getDevices());
    }
    
    @Override
    public void process(int samples)
    {
        float[][] a = inJackA.getBuffer();
        float[][] b = inJackB.getBuffer();
        
        for (int i=0; i<samples; i++)
        {
            float vl = 0.0f, vr = 0.0f;
            
            if (a != null)
            {
                vl += a[0][i];
                vr += a[1][i];
            }
            
            if (b != null)
            {
                vl += b[0][i];
                vr += b[1][i];
            }
            
            buffer[0][i] = vl;
            buffer[1][i] = vr;
        }
    }
}
