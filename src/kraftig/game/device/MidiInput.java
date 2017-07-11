package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.Label;

public class MidiInput extends Panel
{
    private static MidiReceiver receive(MidiReceiver f)
    {
        return f;
    }
    
    private final MidiDevice device;
    
    public MidiInput() throws Exception
    {
        MidiDevice d = null;
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo())
        {
            d = MidiSystem.getMidiDevice(info);
            if (d.getMaxTransmitters() != 0) break;
            else d = null;
        }
        device = d;
        
        device.getTransmitter().setReceiver(receive(this::receive));
        device.open();
        
        setSize(0.125f, 0.0625f);
        frontInterface.add(new Label(Main.instance().getFont(), "MIDI In", 32.0f, new Vec2(), Alignment.C));
    }
    
    private void receive(MidiMessage message, long timeStamp)
    {
    }
    
    @Override
    public void delete()
    {
        super.delete();
        device.close();
    }
    
    @FunctionalInterface
    private interface MidiReceiver extends Receiver
    {
        @Override
        public default void close() {}
    }
}
