package kraftig.game.device;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import kraftig.game.Panel;
import kraftig.game.gui.ChromaticStarScreen;
import kraftig.game.gui.ColumnLayout;
import kraftig.game.gui.Label;
import kraftig.game.gui.RowLayout;
import kraftig.game.gui.jacks.AudioInputJack;
import kraftig.game.gui.jacks.Jack;
import kraftig.game.gui.jacks.Knob;
import kraftig.game.util.DSPUtil;

public class ChromaticStar extends Panel
{
    private final float MIN_BRIGHTNESS = 1.0f/1024.0f;
    private final float MAX_BRIGHTNESS = 1.0f/8.0f;
    
    private final AudioInputJack inJack;
    private final ChromaticStarScreen screen;
    private final Knob brightnessKnob;
    
    public ChromaticStar()
    {
        screen = new ChromaticStarScreen(new Vec2(96.0f, 96.0f));
        
        frontInterface.add(new RowLayout(8.0f, Alignment.C,
                    inJack = new AudioInputJack(),
                    screen,
                    brightnessKnob = new Knob(16.0f)
                        .setValue(0.5f)
                        .onValueChanged(v -> screen.setBrightness(DSPUtil.experp(MIN_BRIGHTNESS, MAX_BRIGHTNESS, v))))
                .setPos(new Vec2(), Alignment.C));
        
        rearInterface.add(new ColumnLayout(4.0f, Alignment.C,
                    new Label("Chromatic", 48.0f),
                    new Label("Star", 48.0f))
                .setPos(new Vec2(), Alignment.C));
        
        setSizeFromContents(8.0f);
    }
    
    @Override
    public List<Jack> getJacks()
    {
        return DSPUtil.jacks(inJack, brightnessKnob);
    }
    
    @Override
    public void process(int samples)
    {
        float[][] buffer = inJack.getBuffer();
        DSPUtil.updateKnobs(samples, brightnessKnob);
        screen.process(buffer, samples);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        super.save(out);
        brightnessKnob.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        super.load(in);
        brightnessKnob.load(in);
    }
    // </editor-fold>
}
