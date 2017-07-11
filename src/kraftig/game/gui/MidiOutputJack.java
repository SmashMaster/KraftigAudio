package kraftig.game.gui;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import javax.sound.midi.MidiMessage;
import kraftig.game.Main;
import kraftig.game.util.MidiReceiver;
import kraftig.game.util.VectorFont;

public class MidiOutputJack extends OutputJack implements MidiReceiver
{
    public MidiOutputJack()
    {
        super();
    }
    
    public MidiOutputJack(Vec2 pos, Alignment align)
    {
        super(pos, align);
    }
    
    @Override
    public void send(MidiMessage message, long timeStamp)
    {
        if (hasLiveWire()) ((MidiInputJack)getWire().getOut()).receive(message, timeStamp);
    }
    
    @Override
    public boolean canConnect(Jack other)
    {
        return other instanceof MidiInputJack;
    }

    @Override
    public void renderSymbol()
    {
        VectorFont font = Main.instance().getFont();
        font.render("MIDI", new Vec2(0.0f, 1.0f), 10.0f, Alignment.N);
        font.render("OUT", new Vec2(0.0f, -1.0f), 10.0f, Alignment.S);
    }
}
