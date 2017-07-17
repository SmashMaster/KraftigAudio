package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.ArrayList;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.ListBox;

public class SystemInput extends Panel implements AudioDevice
{
//    private final TargetDataLine inputLine;
//    private final byte[] rawBytes;
//    private final float[][] buffer;
    
    public SystemInput() throws Exception
    {
//        inputLine = AudioSystem.getTargetDataLine(Main.AUDIO_FORMAT);
//        inputLine.open();
//        inputLine.start();
//        
//        rawBytes = new byte[inputLine.getBufferSize()];
//        buffer = new float[2][rawBytes.length/4];
        
        setSize(0.125f, 0.0625f);
//        rearInterface.add(new AudioOutputJack(this, buffer, new Vec2(), Alignment.C));
        frontInterface.add(new ListBox(new Vec2(64.0f, 32.0f), () ->
        {
            ArrayList<InputOption> options = new ArrayList<>();
            
            for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo())
            {
                try
                {
                    Mixer mixer = AudioSystem.getMixer(mixerInfo);
                    if (!mixer.isLineSupported(Main.AUDIO_INPUT_INFO)) continue;
                    
                    TargetDataLine line = (TargetDataLine)mixer.getLine(Main.AUDIO_INPUT_INFO);
                    
                    options.add(new InputOption(mixerInfo.getName()));
                }
                catch (LineUnavailableException e) {}
            }
            
            return options.toArray(new InputOption[0]);
        }).setPos(new Vec2(), Alignment.C));
    }
    
    @Override
    public void process(int samples)
    {
//        //Buffer input byte data.
//        int available = Math.min(inputLine.available(), samples*4);
//        inputLine.read(rawBytes, 0, available);
//        
//        //Convert to floating point PCM.
//        for (int i=0, k=0; i<available;)
//        {
//            short left = (short)((rawBytes[i++] & 0xff) | ((rawBytes[i++] & 0xff) << 8));
//            short right = (short)((rawBytes[i++] & 0xff) | ((rawBytes[i++] & 0xff) << 8));
//            
//            buffer[0][k] = (left + 0.5f)/32767.5f;
//            buffer[1][k++] = (right + 0.5f)/32767.5f;
//        }
    }
    
    @Override
    public void delete()
    {
//        inputLine.stop();
//        inputLine.close();
    }
    
    private class InputOption implements ListBox.Option
    {
        private final String label;
        
        private InputOption(String label)
        {
            this.label = label;
        }
        
        @Override
        public String getLabel()
        {
            return label;
        }
    }
}
