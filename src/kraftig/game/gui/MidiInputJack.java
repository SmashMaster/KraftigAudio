package kraftig.game.gui;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import javax.sound.midi.MidiMessage;
import kraftig.game.Main;
import kraftig.game.audio.MidiReceiver;
import kraftig.game.util.VectorFont;

public class MidiInputJack extends InputJack
{
    private final MidiReceiver receiver;
    
    public MidiInputJack(MidiReceiver receiver)
    {
        super();
        this.receiver = receiver;
    }
    
    public MidiInputJack(MidiReceiver receiver, Vec2 pos, Alignment align)
    {
        this(receiver);
        setPos(pos, align);
    }
    
    public void receive(MidiMessage message, long sample)
    {
        receiver.send(message, sample);
    }
    
    @Override
    public boolean canConnect(Jack other)
    {
        return other instanceof MidiOutputJack;
    }

    @Override
    public void renderSymbol()
    {
        VectorFont font = Main.instance().getFont();
        font.render("MIDI", new Vec2(0.0f, 1.0f), 12.0f, Alignment.N);
        font.render("IN", new Vec2(0.0f, -1.0f), 12.0f, Alignment.S);
    }
}
