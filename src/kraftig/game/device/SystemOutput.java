package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.InputJack;
import kraftig.game.gui.Label;

public class SystemOutput extends Panel
{
    private final SourceDataLine outputLine;
    
    private final byte[] rawBytes;
    private final float[][] buffer;
    
    private final InputJack inJack;
    
    public SystemOutput() throws Exception
    {
        outputLine = AudioSystem.getSourceDataLine(Main.AUDIO_FORMAT);
        outputLine.open();
        outputLine.start();
        
        rawBytes = new byte[outputLine.getBufferSize()];
        buffer = new float[2][rawBytes.length/4];
        
        inJack = new InputJack(new Vec2(), Alignment.C);
        
        setSize(0.125f, 0.0625f);
        rearInterface.add(inJack);
        frontInterface.add(new Label(Main.instance().getFont(), "System Out", new Vec2(), Alignment.C));
    }
    
    private boolean first = true;
    
    public void process(int samples)
    {
        //Recursively process up device chain.
        inJack.process(buffer, samples);
        
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
