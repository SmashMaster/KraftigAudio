package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import javax.sound.midi.MidiMessage;
import kraftig.game.Panel;
import kraftig.game.gui.Label;
import kraftig.game.gui.MidiInputJack;
import kraftig.game.gui.MidiOutputJack;
import kraftig.game.gui.RowLayout;

public class MidiSplitter extends Panel
{
    private final MidiOutputJack[] outJacks = new MidiOutputJack[4];
    
    public MidiSplitter()
    {
        frontInterface.add(new RowLayout(4.0f, Alignment.C,
                    new MidiInputJack(this::receive),
                    new Label("\u2192", 48.0f),
                    outJacks[0] = new MidiOutputJack(),
                    outJacks[1] = new MidiOutputJack(),
                    outJacks[2] = new MidiOutputJack(),
                    outJacks[3] = new MidiOutputJack())
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("MIDI Splitter", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private void receive(MidiMessage message, long timeStamp)
    {
        for (MidiOutputJack jack : outJacks) jack.send(message, timeStamp);
    }
}
