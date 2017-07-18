package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.AudioInputJack;
import kraftig.game.gui.Label;
import kraftig.game.gui.ListBox;
import kraftig.game.gui.RowLayout;

public class SystemOutput extends Panel implements AudioDevice
{
    private final ListBox<OutputOption> listBox;
    private final byte[] rawBytes = new byte[Main.BUFFER_SIZE*4];
    private final AudioInputJack inJack;
    
    private SourceDataLine outputLine;
    
    public SystemOutput() throws Exception
    {
        listBox = new ListBox<>(new Vec2(64.0f, 32.0f), () ->
        {
            ArrayList<OutputOption> options = new ArrayList<>();
            
            for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo())
            {
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                if (!mixer.isLineSupported(Main.AUDIO_OUTPUT_INFO)) continue;
                options.add(new OutputOption(mixerInfo));
            }
            
            return options;
        });
        
        listBox.onValueChanged((option) ->
        {
            if (outputLine != null)
            {
                outputLine.stop();
                outputLine.close();
            }
            
            if (option != null) try
            {
                Mixer mixer = AudioSystem.getMixer(option.info);
                outputLine = (SourceDataLine)mixer.getLine(Main.AUDIO_OUTPUT_INFO);
                outputLine.open();
                outputLine.start();
            }
            catch (Exception e)
            {
                outputLine = null;
                listBox.setValue(null);
            }
        });
        
        frontInterface.add(new RowLayout(4.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    listBox)
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("System In", 24.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(4.0f);
    }
    
    @Override
    public Stream<AudioDevice> getInputDevices()
    {
        return inJack.getDevices();
    }
    
    @Override
    public void process(int samples)
    {
        if (outputLine == null) return;
        
        float[][] buffer = inJack.getBuffer();
        
        int available = Math.min(outputLine.available(), samples*4);
        
        if (buffer == null) Arrays.fill(rawBytes, 0, available, (byte)0); //Send silence.
        else
        {
            //Convert back to byte data.
            for (int i=0, k=0; k<available/4;)
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
        outputLine.write(rawBytes, 0, available);
    }
    
    @Override
    public void delete()
    {
        if (outputLine != null)
        {
            outputLine.stop();
            outputLine.close();
            outputLine = null;
        }
    }
    
    private class OutputOption implements ListBox.Option
    {
        private final Mixer.Info info;
        
        private OutputOption(Mixer.Info info)
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
            OutputOption other = (OutputOption)obj;
            return info.getName().equals(other.info.getName());
        }
    }
}
