package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.audio.BiquadFilterKernel;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Label;
import kraftig.game.gui.RadioButtons;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.TextBox;
import kraftig.game.gui.buttons.ToggleButton;
import kraftig.game.gui.buttons.ToggleLabelButton;
import kraftig.game.gui.jacks.AudioOutputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.MidiInputJack;
import kraftig.game.util.DSPUtil;

public class MidiDecoder extends Panel
{
    private final MidiInputJack midiInJack;
    private final RadioButtons typeSelect;
    private final TextBox controllerBox;
    private final ToggleButton listenButton;
    private final AudioOutputJack outJack;
    
    private final BiquadFilterKernel smoother = new BiquadFilterKernel();
    private final float[][] buffer = new float[2][Main.BUFFER_SIZE];
    
    private boolean listening;
    private int type;
    private int controller;
    
    private float targetValue;
    
    public MidiDecoder()
    {
        frontInterface.add(new RowLayout(12.0f, Alignment.C,
                    midiInJack = new MidiInputJack(this::receive),
                    typeSelect = new RadioButtons("Control", "Pressure", "Pitch Bend")
                        .onValueChanged(v -> type = v)
                        .setValue(0),
                    new ColumnLayout(8.0f, Alignment.C,
                        new Label("Controller", 16.0f),
                        controllerBox = new TextBox(new Vec2(24.0f, 12.0f), Alignment.E, 16.0f)
                            .setText("0"),
                        listenButton = new ToggleLabelButton("Listen", 16.0f, 2.0f)
                            .onValueChanged(v -> listening = v)),
                    outJack = new AudioOutputJack(this, buffer))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new Label("Midi Decoder", 48.0f, new Vec2(0.0f, 0.0f), Alignment.C));
        
        setSizeFromContents(8.0f);
        
        smoother.s.lowPass(60.0f, 0.5f);
    }
    
    private void receive(MidiMessage message, long sample)
    {
        if (message instanceof ShortMessage)
        {
            ShortMessage msg = (ShortMessage)message;
            
            if (listening) switch (msg.getCommand())
            {
                case ShortMessage.CONTROL_CHANGE:
                    typeSelect.setValue(0);
                    controller = msg.getData1();
                    controllerBox.setText(Integer.toString(controller));
                    break;
                case ShortMessage.CHANNEL_PRESSURE:
                    typeSelect.setValue(1);
                    break;
                case ShortMessage.PITCH_BEND:
                    typeSelect.setValue(2);
                    break;
            }
            else switch (msg.getCommand())
            {
                case ShortMessage.CONTROL_CHANGE:
                    if (type == 0 && msg.getData1() == controller) targetValue = msg.getData2()/63.5f - 1.0f;
                    break;
                case ShortMessage.CHANNEL_PRESSURE:
                    if (type == 1) targetValue = msg.getData1()/63.5f - 1.0f;
                    break;
                case ShortMessage.PITCH_BEND:
                    if (type == 2) targetValue = ((msg.getData2() << 7) | msg.getData1())/8191.5f - 1.0f;
                    break;
            }
        }
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(midiInJack, outJack);
    }
    
    @Override
    public synchronized void process(int samples)
    {
        for (int i=0; i<samples; i++)
        {
            float v = smoother.apply(targetValue);
            buffer[0][i] = v;
            buffer[1][i] = v;
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        super.save(out);
        typeSelect.save(out);
        listenButton.save(out);
        out.writeInt(controller);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        typeSelect.load(in);
        listenButton.load(in);
        controller = in.readInt();
        controllerBox.setText(Integer.toString(controller));
    }
    // </editor-fold>
}
