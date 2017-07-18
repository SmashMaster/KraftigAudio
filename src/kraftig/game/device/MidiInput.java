package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import kraftig.game.Panel;
import kraftig.game.gui.Label;
import kraftig.game.gui.ListBox;
import kraftig.game.gui.MidiOutputJack;
import kraftig.game.gui.RowLayout;

public class MidiInput extends Panel
{
    private static final Map<MidiDevice, Integer> deviceUsage = new IdentityHashMap<>();
    
    private static void useDevice(MidiDevice device) throws MidiUnavailableException
    {
        Integer i = deviceUsage.get(device);
        if (i == null)
        {
            device.open();
            deviceUsage.put(device, 1);
        }
        else deviceUsage.put(device, i + 1);
    }
    
    private static void unuseDevice(MidiDevice device)
    {
        Integer i = deviceUsage.get(device);
        if (i == null) return;
        else if (i <= 1)
        {
            device.close();
            deviceUsage.remove(device);
        }
        else deviceUsage.put(device, i - 1);
    }
    
    private MidiDevice inputDevice;
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
            if (inputTransmitter != null)
            {
                inputTransmitter.close();
                unuseDevice(inputDevice);
                inputDevice = null;
            }
            
            if (option != null) try
            {
                inputDevice = option.device;
                useDevice(inputDevice);
                inputTransmitter = option.device.getTransmitter();
                inputTransmitter.setReceiver(jack);
            }
            catch (Exception e)
            {
                if (inputTransmitter != null)
                {
                    inputTransmitter.close();
                    inputTransmitter = null;
                }
                unuseDevice(inputDevice);
                inputDevice = null;
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
        
        if (inputTransmitter != null)
        {
            inputTransmitter.close();
            inputTransmitter = null;
        }
        
        if (inputDevice != null)
        {
            unuseDevice(inputDevice);
            inputDevice = null;
        }
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
