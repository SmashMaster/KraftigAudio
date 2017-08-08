package kraftig.game.gui;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.function.Supplier;
import kraftig.game.device.AudioDevice;
import org.lwjgl.opengl.GL11;

public class AudioOutputJack extends OutputJack
{
    private final AudioDevice device;
    private final Supplier<float[][]> bufferSupplier;
    
    public AudioOutputJack(AudioDevice device, Supplier<float[][]> bufferSupplier)
    {
        super();
        if (device == null || bufferSupplier == null) throw new NullPointerException();
        this.device = device;
        this.bufferSupplier = bufferSupplier;
    }
    
    public AudioOutputJack(AudioDevice device, Supplier<float[][]> bufferSupplier, Vec2 pos, Alignment align)
    {
        this(device, bufferSupplier);
        setPos(pos, align);
    }
    
    public AudioOutputJack(AudioDevice device, float[][] buffer)
    {
        this(device, () -> buffer);
    }
    
    public AudioOutputJack(AudioDevice device, float[][] buffer, Vec2 pos, Alignment align)
    {
        this(device, () -> buffer, pos, align);
    }
    
    public AudioDevice getDevice()
    {
        return device;
    }
    
    public float[][] getBuffer()
    {
        return bufferSupplier.get();
    }
    
    @Override
    public boolean canConnect(Jack other)
    {
        return other instanceof AudioInputJack;
    }
    
    @Override
    public void renderSymbol()
    {
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (float t = 0.0f; t < T_END; t += DT)
        {
            Vec2 p = Util.squareDir(t).normalize().mult(radius*0.5f);
            GL11.glVertex2f(p.x, p.y);
        }
        GL11.glEnd();
    }
}
