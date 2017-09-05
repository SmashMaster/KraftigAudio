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
import javax.sound.midi.Transmitter;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.audio.MidiReceiver;
import kraftig.game.gui.Label;
import kraftig.game.gui.ListBox;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.MidiOutputJack;
import kraftig.game.util.DSPUtil;

public class MidiInput extends Panel
{
    private static final Map<MidiDevice, Integer> USAGE = new IdentityHashMap<>();
    private static final Map<MidiDevice, Long> TIME = new IdentityHashMap<>();
    
    private static void useDevice(MidiDevice device) throws MidiUnavailableException
    {
        Integer i = USAGE.get(device);
        if (i == null)
        {
            device.open();
            long time = System.nanoTime();
            TIME.put(device, time);
            USAGE.put(device, 1);
        }
        else USAGE.put(device, i + 1);
    }
    
    private static void unuseDevice(MidiDevice device)
    {
        Integer i = USAGE.get(device);
        if (i == null) return;
        else if (i <= 1)
        {
            device.close();
            TIME.remove(device);
            USAGE.remove(device);
        }
        else USAGE.put(device, i - 1);
    }
    
    private final MidiOutputJack outJack = new MidiOutputJack(this);
    
    private MidiDevice inputDevice;
    private Transmitter inputTransmitter;
    
    public MidiInput()
    {
        ListBox<InputOption> listBox = new ListBox<>(new Vec2(128.0f, 64.0f), () ->
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
                inputTransmitter.setReceiver(MidiReceiver.of(this::receive));
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
        
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    listBox,
                    outJack)
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("MIDI In", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private void receive(MidiMessage message, long timeStamp)
    {
        long nanoTime = TIME.get(inputDevice) - Main.instance().getStartNanoTime() + timeStamp*1000L;
        long sampleTime = Math.round(nanoTime*(Main.SAMPLE_RATE/1_000_000_000.0));
        outJack.send(message, sampleTime);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(outJack);
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
