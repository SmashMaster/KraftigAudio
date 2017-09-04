package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.Label;
import kraftig.game.gui.ListBox;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.MidiInputJack;
import kraftig.game.util.DSPUtil;

public class MidiOutput extends Panel
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
    
    private final MidiInputJack midiInJack;
    
    private MidiDevice outputDevice;
    private Receiver outputReceiver;
    
    public MidiOutput()
    {
        ListBox<OutputOption> listBox = new ListBox<>(new Vec2(128.0f, 64.0f), () ->
        {
            ArrayList<OutputOption> options = new ArrayList<>();
            
            for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo())
            {
                try
                {
                    MidiDevice device = MidiSystem.getMidiDevice(info);
                    if (device.getMaxReceivers()== 0) continue;
                    options.add(new OutputOption(info, device));
                }
                catch (MidiUnavailableException e) {}
            }
            
            return options;
        });
        
        listBox.onValueChanged((option) ->
        {
            if (outputReceiver != null)
            {
                outputReceiver.close();
                unuseDevice(outputDevice);
                outputDevice = null;
            }
            
            if (option != null) try
            {
                outputDevice = option.device;
                useDevice(outputDevice);
                outputReceiver = option.device.getReceiver();
            }
            catch (Exception e)
            {
                if (outputReceiver != null)
                {
                    outputReceiver.close();
                    outputReceiver = null;
                }
                unuseDevice(outputDevice);
                outputDevice = null;
                listBox.setValue(null);
            }
        });
        
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    midiInJack = new MidiInputJack(this::receive),
                    listBox)
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("MIDI In", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private void receive(MidiMessage message, long sample)
    {
        if (outputReceiver != null) outputReceiver.send(message, Math.round(sample*(1_000_000.0*Main.SAMPLE_WIDTH)));
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(midiInJack);
    }
    
    @Override
    public void delete()
    {
        super.delete();
        
        if (outputReceiver != null)
        {
            outputReceiver.close();
            outputReceiver = null;
        }
        
        if (outputDevice != null)
        {
            unuseDevice(outputDevice);
            outputDevice = null;
        }
    }
    
    private class OutputOption implements ListBox.Option
    {
        private final MidiDevice.Info info;
        private final MidiDevice device;
        
        private OutputOption(MidiDevice.Info info, MidiDevice device)
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
            OutputOption other = (OutputOption)obj;
            return info.getName().equals(other.info.getName());
        }
    }
}
