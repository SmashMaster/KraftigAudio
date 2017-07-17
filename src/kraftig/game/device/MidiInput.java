package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.Label;
import kraftig.game.gui.ListBox;
import kraftig.game.gui.MidiOutputJack;
import kraftig.game.gui.RowLayout;

public class MidiInput extends Panel
{
    private MidiDevice.Info inputDeviceInfo;
    private Transmitter inputTransmitter;
    private final MidiOutputJack jack = new MidiOutputJack();
    
    public MidiInput() throws Exception
    {
        ListBox<InputOption> listBox = new ListBox<>(new Vec2(64.0f, 32.0f), () ->
        {
            ArrayList<InputOption> options = new ArrayList<>();
            
            for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo())
            {
                try
                {
                    MidiDevice device = MidiSystem.getMidiDevice(info);
                     if (device.getMaxTransmitters() == 0) continue;
                     options.add(new InputOption(info, device));
                }
                catch (MidiUnavailableException e) {}
            }
            
            return options;
        });
        
        listBox.onValueChanged((option) ->
        {
            if (option != null && option.info == inputDeviceInfo) return;
            
            if (inputTransmitter != null) inputTransmitter.close();
            
            if (option != null) try
            {
                if (!option.device.isOpen()) option.device.open();
                inputTransmitter = option.device.getTransmitter();
                inputTransmitter.setReceiver(jack);
            }
            catch (Exception e)
            {
                inputTransmitter = null;
                listBox.setValue(null);
            }
        });
        
        frontInterface.add(new RowLayout(4.0f, Alignment.C,
                    listBox,
                    jack)
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("MIDI Input", 24.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(4.0f);
    }
    
    @Override
    public void delete()
    {
        super.delete();
        if (inputTransmitter != null) inputTransmitter.close();
    }
    
    private class InputOption implements ListBox.Option
    {
        private final MidiDevice.Info info;
        private final MidiDevice device;
        
        private InputOption(MidiDevice.Info info, MidiDevice device)
        {
            this.info = info;
            this.device = device;
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
