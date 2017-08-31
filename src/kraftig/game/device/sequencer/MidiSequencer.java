package kraftig.game.device.sequencer;

import com.samrj.devil.graphics.GraphicsUtil;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.util.List;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Jack;
import kraftig.game.gui.Label;
import kraftig.game.gui.MidiInputJack;
import kraftig.game.gui.MidiOutputJack;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.SymbolButton;
import kraftig.game.util.DSPUtil;
import org.lwjgl.opengl.GL11;

public class MidiSequencer extends Panel
{
    private static final float CONTROL_BUTTON_SIZE = 8.0f;
    
    private final MidiSeqCamera camera = new MidiSeqCamera(this);
    
    private final MidiInputJack midiInJack;
    private final MidiSeqKeyboard keyboard;
    private final MidiSeqScreen screen;
    private final MidiOutputJack midiOutJack;
    
    public MidiSequencer()
    {
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    midiInJack = new MidiInputJack(this::receive),
                    new ColumnLayout(4.0f, Alignment.W,
                        new RowLayout(1.0f, Alignment.C,
                            new SymbolButton(new Vec2(CONTROL_BUTTON_SIZE), this::drawBackSymbol),
                            new SymbolButton(new Vec2(CONTROL_BUTTON_SIZE), this::drawFwdSymbol),
                            new SymbolButton(new Vec2(CONTROL_BUTTON_SIZE), this::drawPlaySymbol),
                            new SymbolButton(new Vec2(CONTROL_BUTTON_SIZE), this::drawStopSymbol),
                            new SymbolButton(new Vec2(CONTROL_BUTTON_SIZE), this::drawRecordSymbol)),
                        new RowLayout(0.0f, Alignment.C,
                            keyboard = new MidiSeqKeyboard(camera, new Vec2(12.0f, 64.0f)),
                            screen = new MidiSeqScreen(camera, new Vec2(128.0f, 64.0f)))),
                    midiOutJack = new MidiOutputJack())
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new ColumnLayout(4.0f, Alignment.C,
                    new Label("Midi", 48.0f),
                    new Label("Sequencer", 48.0f))
                .setPos(new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    public Vec2 getMouse()
    {
        return screen.getMouse();
    }
    
    private void drawBackSymbol()
    {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(0.0f, 0.75f);
        GL11.glVertex2f(-0.5f, 0.0f);
        GL11.glVertex2f(-0.5f, 0.0f);
        GL11.glVertex2f(0.0f, -0.75f);
        GL11.glVertex2f(0.5f, 0.75f);
        GL11.glVertex2f(0.0f, 0.0f);
        GL11.glVertex2f(0.0f, 0.0f);
        GL11.glVertex2f(0.5f, -0.75f);
        GL11.glEnd();
    }
    
    private void drawFwdSymbol()
    {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(-0.5f, 0.75f);
        GL11.glVertex2f(0.0f, 0.0f);
        GL11.glVertex2f(0.0f, 0.0f);
        GL11.glVertex2f(-0.5f, -0.75f);
        GL11.glVertex2f(0.0f, 0.75f);
        GL11.glVertex2f(0.5f, 0.0f);
        GL11.glVertex2f(0.5f, 0.0f);
        GL11.glVertex2f(0.0f, -0.75f);
        GL11.glEnd();
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
    
    @Override
    public void process(int samples)
    {
        camera.step((float)Main.SAMPLE_WIDTH*samples);
    }
}
