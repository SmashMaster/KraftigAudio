package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.List;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import kraftig.game.Panel;
import kraftig.game.gui.Jack;
import kraftig.game.gui.Label;
import kraftig.game.gui.MidiInputJack;
import kraftig.game.gui.MidiOutputJack;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.SequencerScreen;
import kraftig.game.util.DSPUtil;

public class Sequencer extends Panel
{
    private final MidiInputJack midiInJack;
    private final SequencerScreen screen;
    private final MidiOutputJack midiOutJack;
    
    public Sequencer()
    {
        frontInterface.add(new RowLayout(4.0f, Alignment.C,
                    midiInJack = new MidiInputJack(this::receive),
                    screen = new SequencerScreen(),
                    midiOutJack = new MidiOutputJack())
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Sequencer", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private void receive(MidiMessage message, long sample)
    {
        if (message instanceof ShortMessage)
        {
            ShortMessage msg = (ShortMessage)message;
            
            switch (msg.getCommand())
            {
                case ShortMessage.NOTE_ON:
                {
                    int midi = msg.getData1();
                }
                break;
                case ShortMessage.NOTE_OFF:
                {
                    int midi = msg.getData1();
                }
                break;
            }
        }
        
        midiOutJack.send(message, sample);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(midiInJack, midiOutJack);
    }
}
