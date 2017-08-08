package kraftig.game.gui;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.stream.Stream;
import kraftig.game.device.AudioDevice;
import org.lwjgl.opengl.GL11;

public class AudioInputJack extends InputJack
{
    public AudioInputJack()
    {
        super();
    }
    
    public AudioInputJack(float radius)
    {
        super(radius);
    }
    
    public AudioInputJack(Vec2 pos, Alignment align)
    {
        super(pos, align);
    }
    
    private AudioOutputJack getIn()
    {
        return (AudioOutputJack)getWire().getIn();
    }
    
    public AudioDevice getDevice()
    {
        if (hasLiveWire()) return getIn().getDevice();
        else return null;
    }
    
    public Stream<AudioDevice> getDevices()
    {
        return hasLiveWire() ? Stream.of(getIn().getDevice()) : Stream.empty();
    }
    
    public float[][] getBuffer()
    {
        if (hasLiveWire()) return getIn().getBuffer();
        else return null;
    }
    
    @Override
    public boolean canConnect(Jack other)
    {
        return other instanceof AudioOutputJack;
    }

    @Override
    public void renderSymbol()
    {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(0.0f, -radius*0.5f);
        GL11.glVertex2f(0.0f, radius*0.5f);
        GL11.glEnd();
    }
}
