package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.AudioOutputJack;
import kraftig.game.gui.Jack;
import kraftig.game.gui.Label;
import kraftig.game.gui.ListBox;
import kraftig.game.gui.RowLayout;
import kraftig.game.util.DSPUtil;

public class SystemInput extends Panel
{
    private final AudioOutputJack outJack;
    
    private final byte[] rawBytes = new byte[Main.BUFFER_SIZE*4];
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private TargetDataLine inputLine;
    
    public SystemInput()
    {
        ListBox<InputOption> listBox = new ListBox<>(new Vec2(128.0f, 64.0f), () ->
        {
            ArrayList<InputOption> options = new ArrayList<>();
            
            for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo())
            {
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                if (!mixer.isLineSupported(Main.AUDIO_INPUT_INFO)) continue;
                options.add(new InputOption(mixerInfo));
            }
            
            return options;
        });
        
        listBox.onValueChanged((option) ->
        {
            if (inputLine != null)
            {
                inputLine.stop();
                inputLine.close();
            }
            
            if (option != null) try
            {
                Mixer mixer = AudioSystem.getMixer(option.info);
                inputLine = (TargetDataLine)mixer.getLine(Main.AUDIO_INPUT_INFO);
                inputLine.open();
                inputLine.start();
            }
            catch (Exception e)
            {
                inputLine = null;
                listBox.setValue(null);
            }
        });
        
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    listBox,
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("System In", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(outJack);
    }
    
    @Override
    public void process(int samples)
    {
        //Buffer input byte data.
        int available = samples*4;
        
        if (inputLine != null)
        {
            available = Math.min(inputLine.available(), available);
            inputLine.read(rawBytes, 0, available);
        }
        else Arrays.fill(rawBytes, 0, available, (byte)0);
        
        //Convert to floating point PCM.
        for (int i=0, k=0; i<available;)
        {
            short left = (short)((rawBytes[i++] & 0xff) | ((rawBytes[i++] & 0xff) << 8));
            short right = (short)((rawBytes[i++] & 0xff) | ((rawBytes[i++] & 0xff) << 8));
            
            buffer[0][k] = (left + 0.5f)/32767.5f;
            buffer[1][k++] = (right + 0.5f)/32767.5f;
        }
    }
    
    @Override
    public void delete()
    {
        if (inputLine != null)
        {
            inputLine.stop();
            inputLine.close();
            inputLine = null;
        }
    }
    
    private class InputOption implements ListBox.Option
    {
        private final Mixer.Info info;
        
        private InputOption(Mixer.Info info)
        {
            this.info = info;
        }
        
        @Override
        public String getLabel()
        {
            return info.getName();
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            InputOption other = (InputOption)obj;
            return info.getName().equals(other.info.getName());
        }
    }
}
