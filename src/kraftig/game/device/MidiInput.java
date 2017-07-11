package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.Label;
import kraftig.game.gui.MidiOutputJack;

public class MidiInput extends Panel
{
    private final MidiDevice device;
    private final MidiOutputJack jack;
    
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
        
        setSize(0.125f, 0.0625f);
        frontInterface.add(new Label(Main.instance().getFont(), "MIDI In", 32.0f, new Vec2(), Alignment.C));
        rearInterface.add(jack = new MidiOutputJack());
        
        device.getTransmitter().setReceiver(jack);
        device.open();
    }
    
    @Override
    public void delete()
    {
        super.delete();
        device.close();
    }
}
