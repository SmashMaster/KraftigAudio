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
    private final AudioInputJack[] inJacks = new AudioInputJack[4];
    
    private final float[][] buffer = new float[2][48000];
    
    public Adder()
    {
        frontInterface.add(new RowLayout(2.0f, Alignment.C,
                    inJacks[0] = new AudioInputJack(),
                    new Label("+", 24.0f),
                    inJacks[1] = new AudioInputJack(),
                    new Label("+", 24.0f),
                    inJacks[2] = new AudioInputJack(),
                    new Label("+", 24.0f),
                    inJacks[3] = new AudioInputJack(),
                    new Label("=", 24.0f),
                    new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Adder", 24.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(4.0f);
    }
    
    @Override
    public Stream<AudioDevice> getInputDevices()
    {
        return Stream.of(inJacks).flatMap(d -> d.getDevices());
    }
    
    @Override
    public void process(int samples)
    {
        float[][][] buffers =  new float[4][][];
        
        for (int j=0; j<4; j++) buffers[j] = inJacks[j].getBuffer();
        
        for (int i=0; i<samples; i++)
        {
            float vl = 0.0f, vr = 0.0f;
            
            for (float[][] b : buffers) if (b != null)
            {
                vl += b[0][i];
                vr += b[1][i];
            }
            
            buffer[0][i] = vl;
            buffer[1][i] = vr;
        }
    }
}
