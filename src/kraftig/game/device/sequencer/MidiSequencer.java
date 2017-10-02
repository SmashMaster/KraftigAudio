package kraftig.game.device.sequencer;

import com.samrj.devil.graphics.GraphicsUtil;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.SongProperties;
import kraftig.game.audio.MidiReceiver;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.TimeSignature;
import kraftig.game.gui.buttons.SymbolButton;
import kraftig.game.gui.buttons.ToggleButton;
import kraftig.game.gui.buttons.ToggleLabelButton;
import kraftig.game.gui.buttons.ToggleSymbolButton;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.MidiInputJack;
import kraftig.game.gui.jacks.MidiOutputJack;
import kraftig.game.util.DSPUtil;
import org.lwjgl.opengl.GL11;

public class MidiSequencer extends Panel implements MidiReceiver
{
    private static final float CONTROL_BUTTON_SIZE = 8.0f;
    
    private final SongProperties properties = Main.instance().getProperties();
    private final MidiSeqCamera camera = new MidiSeqCamera(this);
    private final Track track = new Track();
    
    private final MidiInputJack midiInJack;
    private final ToggleButton recordButton;
    private final MidiSeqKeyboard keyboard;
    private final MidiSeqTimeline timeline;
    private final MidiSeqScreen screen;
    private final ToggleButton snapToGridButton;
    private final MidiOutputJack midiOutJack = new MidiOutputJack(this);
    
    private final Note[] activeNotes = new Note[128];
    private boolean recording, snapToGrid;
    
    public MidiSequencer()
    {
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    midiInJack = new MidiInputJack(this),
                    new ColumnLayout(4.0f, Alignment.W,
                        new RowLayout(1.0f, Alignment.C,
                            new SymbolButton(new Vec2(CONTROL_BUTTON_SIZE), this::drawBackSymbol)
                                .onClick(properties::back),
                            new SymbolButton(new Vec2(CONTROL_BUTTON_SIZE), this::drawFwdSymbol)
                                .onClick(properties::forward),
                            new SymbolButton(new Vec2(CONTROL_BUTTON_SIZE), this::drawPlaySymbol)
                                .onClick(properties::play),
                            new SymbolButton(new Vec2(CONTROL_BUTTON_SIZE), this::drawStopSymbol)
                                .onClick(properties::stop),
                            recordButton = new ToggleSymbolButton(new Vec2(CONTROL_BUTTON_SIZE), this::drawRecordSymbol)
                                .onValueChanged(v -> recording = v)
                                .setValue(false)),
                        new RowLayout(0.0f, Alignment.S,
                            keyboard = new MidiSeqKeyboard(this, new Vec2(12.0f, 64.0f)),
                            new ColumnLayout(0.0f, Alignment.C,
                                timeline = new MidiSeqTimeline(this, new Vec2(128.0f, 6.0f)),
                                screen = new MidiSeqScreen(this, new Vec2(128.0f, 64.0f)))),
                        new RowLayout(2.0f, Alignment.C, 
                            snapToGridButton = new ToggleLabelButton("Snap to Grid", 8.0f, 2.0f)
                                .onValueChanged(v -> snapToGrid = v)
                                .setValue(true),
                            new TimeSignature(Main.instance().getProperties()))),
                    midiOutJack)
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new ColumnLayout(4.0f, Alignment.C,
                    new Label("Midi", 48.0f),
                    new Label("Sequencer", 48.0f))
                .setPos(new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    public long snapToGrid(double timeInSeconds)
    {
        if (snapToGrid)
        {
            double beat = properties.getSamplesPerBeat();
            double samples = timeInSeconds*Main.SAMPLE_RATE;
            return Math.round(Math.round(samples/beat)*beat);
        }
        else return Math.round(timeInSeconds*Main.SAMPLE_RATE);
    }
    
    public SongProperties getProperties()
    {
        return properties;
    }
    
    public MidiSeqCamera getCamera()
    {
        return camera;
    }
    
    public Track getTrack()
    {
        return track;
    }
    
    public MidiReceiver getMidiOut()
    {
        return midiOutJack;
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
    
    @Override
    public void send(MidiMessage message, long sample)
    {
        if (message instanceof ShortMessage)
        {
            ShortMessage msg = (ShortMessage)message;
            int command = msg.getCommand();
            
            if (properties.playing && recording)
            {
                switch (command)
                {
                    case ShortMessage.NOTE_ON:
                    {
                        Note note = new Note();
                        note.midi = msg.getData1();
                        note.start = properties.position;
                        note.end = properties.position;
                        activeNotes[note.midi] = note;
                    }
                    break;
                    case ShortMessage.NOTE_OFF:
                    {
                        int midi = msg.getData1();
                        Note note = activeNotes[midi];
                        if (note != null)
                        {
                            note.end = properties.position;
                            activeNotes[midi] = null;
                            track.notes.add(note);
                        }
                    }
                    break;
                }
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
        if (properties.playing)
        {
            if (recording) for (Note note : activeNotes) if (note != null)
                note.end = properties.position + samples;
            
            long frameStart = properties.position;
            long frameEnd = frameStart + samples;
            
            for (Note note : track.notes)
            {
                if (note.start >= frameStart && note.start < frameEnd)
                {
                    try
                    {
                        ShortMessage on = new ShortMessage(ShortMessage.NOTE_ON, 0, note.midi, 0);
                        ShortMessage off = new ShortMessage(ShortMessage.NOTE_OFF, 0, note.midi, 0);
                        midiOutJack.send(on, properties.playStartTime + note.start);
                        midiOutJack.send(off, properties.playStartTime + note.end);
                    }
                    catch (InvalidMidiDataException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        
        camera.step((float)Main.SAMPLE_WIDTH*samples);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        super.save(out);
        camera.save(out);
        track.save(out);
        recordButton.save(out);
        snapToGridButton.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        camera.load(in);
        track.load(in);
        recordButton.load(in);
        snapToGridButton.load(in);
    }
    // </editor-fold>
}
