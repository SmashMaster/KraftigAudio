package kraftig.game.gui.jacks;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.function.Supplier;
import kraftig.game.Panel;
import org.lwjgl.opengl.GL11;

public class AudioOutputJack extends OutputJack
{
    private final Panel panel;
    private final Supplier<float[][]> bufferSupplier;
    
    public AudioOutputJack(Panel panel, Supplier<float[][]> bufferSupplier)
    {
        super();
        if (panel == null || bufferSupplier == null) throw new NullPointerException();
        this.panel = panel;
        this.bufferSupplier = bufferSupplier;
    }
    
    public AudioOutputJack(Panel panel, Supplier<float[][]> bufferSupplier, Vec2 pos, Alignment align)
    {
        this(panel, bufferSupplier);
        setPos(pos, align);
    }
    
    public AudioOutputJack(Panel panel, float[][] buffer)
    {
        this(panel, () -> buffer);
    }
    
    public AudioOutputJack(Panel panel, float[][] buffer, Vec2 pos, Alignment align)
    {
        this(panel, () -> buffer, pos, align);
    }
    
    public Panel getPanel()
    {
        return panel;
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
