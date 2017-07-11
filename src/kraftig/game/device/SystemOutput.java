package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.AudioInputJack;
import kraftig.game.gui.Label;

public class SystemOutput extends Panel implements AudioDevice
{
    private final SourceDataLine outputLine;
    
    private final byte[] rawBytes;
    
    private final AudioInputJack inJack;
    
    public SystemOutput() throws Exception
    {
        outputLine = AudioSystem.getSourceDataLine(Main.AUDIO_FORMAT);
        outputLine.open();
        outputLine.start();
        
        rawBytes = new byte[outputLine.getBufferSize()];
        
        setSize(0.125f, 0.0625f);
        rearInterface.add(inJack = new AudioInputJack(new Vec2(), Alignment.C));
        frontInterface.add(new Label(Main.instance().getFont(), "System Out", 32.0f, new Vec2(), Alignment.C));
    }
    
    @Override
    public Stream<AudioDevice> getInputDevices()
    {
        return inJack.getDevices();
    }
    
    @Override
    public void process(int samples)
    {
        float[][] buffer = inJack.getBuffer();
        
        if (buffer == null) Arrays.fill(rawBytes, 0, samples*4, (byte)0); //Send silence.
        else
        {
            //Convert back to byte data.
            for (int i=0, k=0; k<samples;)
            {
                float fLeft = Math.min(Math.max(buffer[0][k], -1.0f), 1.0f);
                float fRight = Math.min(Math.max(buffer[1][k++], -1.0f), 1.0f);

                short left = (short)Math.round(fLeft*32767.5f - 0.5f);
                short right = (short)Math.round(fRight*32767.5f - 0.5f);

                rawBytes[i++] = (byte)(left & 0xFF);
                rawBytes[i++] = (byte)((left >>> 8) & 0xFF);
                rawBytes[i++] = (byte)(right & 0xFF);
                rawBytes[i++] = (byte)((right >>> 8) & 0xFF);
            }
        }
        
        //Send to output.
        outputLine.write(rawBytes, 0, samples*4);
    }
    
    @Override
    public void delete()
    {
        outputLine.stop();
        outputLine.close();
    }
}
