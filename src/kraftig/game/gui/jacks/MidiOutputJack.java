package kraftig.game.gui.jacks;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import javax.sound.midi.MidiMessage;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.audio.MidiReceiver;
import kraftig.game.util.VectorFont;

public class MidiOutputJack extends OutputJack implements MidiReceiver
{
    public MidiOutputJack(Panel panel)
    {
        super(panel);
    }
    
    public MidiOutputJack(Panel panel, Vec2 pos, Alignment align)
    {
        super(panel, pos, align);
    }
    
    @Override
    public void send(MidiMessage message, long sample)
    {
        if (hasLiveWire()) ((MidiInputJack)getWire().getOut()).receive(message, sample);
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
        font.render("MIDI", new Vec2(0.0f, 1.0f), 12.0f, Alignment.N);
        font.render("OUT", new Vec2(0.0f, -1.0f), 12.0f, Alignment.S);
    }
}
