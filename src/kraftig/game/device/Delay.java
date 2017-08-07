package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.stream.Stream;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.AudioInputJack;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Knob;
import kraftig.game.gui.Label;
import kraftig.game.gui.RadioButtons;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.TextBox;
import kraftig.game.util.CircularBuffer;
import kraftig.game.util.DSPMath;

public class Delay extends Panel implements AudioDevice
{
    private static final int MAX_DELAY = 48000;
    
    private final AudioInputJack inJack;
    private final TextBox textBox = new TextBox(new Vec2(48.0f, 16.0f), Alignment.E, 24.0f);
    
    private final CircularBuffer left = new CircularBuffer(MAX_DELAY + 8);
    private final CircularBuffer right = new CircularBuffer(MAX_DELAY + 8);
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private int delay;
    private int displayMode;
    private float feedback;
    
    public Delay()
    {
        frontInterface.add(new RowLayout(12.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Delay", 6.0f),
                        new Knob(24.0f)
                            .onValueChanged(v -> set((int)Math.round(Math.pow(v, 3.0)*MAX_DELAY), displayMode, feedback))
                            .setValue(0.5f)),
                    new RadioButtons("N", "ms")
                        .onValueChanged(v -> set(delay, v, feedback))
                        .setValue(0),
                    textBox,
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Feedback", 6.0f),
                        new Knob(24.0f)
                            .onValueChanged(v -> set(delay, displayMode, v))
                            .setValue(0.0f)),
                    new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Delay", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private void set(int delay, int displayMode, float feedback)
    {
        this.delay = delay;
        this.displayMode = displayMode;
        this.feedback = feedback;
        
        if (displayMode == 0) textBox.setText("" + delay);
        else textBox.setText("" + ((delay*1000.0f)/Main.SAMPLE_RATE));
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
            DSPMath.zero(buffer, samples);
            left.clear();
            right.clear();
        }
        else if (delay == 0)
        {
            left.clear();
            right.clear();
            
            for (int i=0; i<samples; i++)
            { 
                buffer[0][i] = in[0][i];
                buffer[1][i] = in[1][i];
            }
        }
        else for (int i=0; i<samples; i++)
        {
            float l = in[0][i];
            float r = in[1][i];
            int size = left.getSize();
            
            if (size < delay)
            {
                buffer[0][i] = 0.0f;
                buffer[1][i] = 0.0f;
                left.write(l);
                right.write(r);
            }
            else
            {
                while (size > delay)
                {
                    left.read();
                    right.read();
                    size = left.getSize();
                }
                
                float fbl = left.read();
                float fbr = right.read();
                
                buffer[0][i] = fbl;
                buffer[1][i] = fbr;
                
                left.write(l + fbl*feedback);
                right.write(r + fbr*feedback);
            }
        }
    }
}
