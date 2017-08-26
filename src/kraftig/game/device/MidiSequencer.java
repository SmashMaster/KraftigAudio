package kraftig.game.device;

import com.samrj.devil.graphics.GraphicsUtil;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.List;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import kraftig.game.Panel;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Jack;
import kraftig.game.gui.Label;
import kraftig.game.gui.MidiInputJack;
import kraftig.game.gui.MidiOutputJack;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.MidiSequencerScreen;
import kraftig.game.gui.SymbolButton;
import kraftig.game.util.DSPUtil;
import org.lwjgl.opengl.GL11;

public class MidiSequencer extends Panel
{
    private static final float CONTROL_BUTTON_SIZE = 8.0f;
    
    private final MidiInputJack midiInJack;
    private final MidiSequencerScreen screen;
    private final MidiOutputJack midiOutJack;
    
    public MidiSequencer()
    {
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    midiInJack = new MidiInputJack(this::receive),
                    new ColumnLayout(4.0f, Alignment.W,
                        new RowLayout(1.0f, Alignment.C,
                            new SymbolButton(new Vec2(CONTROL_BUTTON_SIZE), this::drawPlaySymbol),
                            new SymbolButton(new Vec2(CONTROL_BUTTON_SIZE), this::drawStopSymbol),
                            new SymbolButton(new Vec2(CONTROL_BUTTON_SIZE), this::drawRecordSymbol)),
                        screen = new MidiSequencerScreen()),
                    midiOutJack = new MidiOutputJack())
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Sequencer", 48.0f, new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    private void drawPlaySymbol()
    {
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(-0.75f, -0.75f);
        GL11.glVertex2f(-0.75f, 0.75f);
        GL11.glVertex2f(0.75f, 0.0f);
        GL11.glEnd();
    }
    
    private void drawStopSymbol()
    {
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(-0.75f, -0.75f);
        GL11.glVertex2f(-0.75f, 0.75f);
        GL11.glVertex2f(0.75f, 0.75f);
        GL11.glVertex2f(0.75f, -0.75f);
        GL11.glEnd();
    }
    
    private void drawRecordSymbol()
    {
        GraphicsUtil.drawCircle(new Vec2(), 0.75f, 16);
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
