package kraftig.game.gui;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import kraftig.game.FocusQuery;
import kraftig.game.Panel;
import kraftig.game.audio.Envelope;
import kraftig.game.util.Savable;
import org.lwjgl.opengl.GL11;

public class EnvelopeEditor implements UIElement, Savable
{
    private static final float KNOB_SEPARATION = 2.0f;
    private static final float KNOB_LABEL_SEPARATION = 2.0f;
    private static final float KNOB_LABEL_SIZE = 4.0f;
    private static final float KNOB_SIZE = 10.0f;
    
    private final Envelope env;
    
    private final ColumnLayout layout;
    private final EnvelopeGraph graph;
    private final Knob attackKnob, aCurveKnob, holdKnob, decayKnob, sustainKnob, releaseKnob, rCurveKnob;
    
    public EnvelopeEditor(Envelope env)
    {
        this.env = env;
        
        layout = new ColumnLayout(4.0f, Alignment.C,
                graph = new EnvelopeGraph(new Vec2(78.0f, 24.0f)),
                new RowLayout(KNOB_SEPARATION, Alignment.C,
                    new ColumnLayout(KNOB_LABEL_SEPARATION, Alignment.C,
                        new Label("Attack", KNOB_LABEL_SIZE),
                        attackKnob = new Knob(KNOB_SIZE)
                            .onValueChanged(v -> env.attack = v*v)
                            .setValue(0.125f)),
                    new ColumnLayout(KNOB_LABEL_SEPARATION, Alignment.C,
                        new Label("A-curve", KNOB_LABEL_SIZE),
                        aCurveKnob = new Knob(KNOB_SIZE)
                            .onValueChanged(v -> env.aCurve = (float)Math.pow(8.0, v*2.0 - 1.0))
                            .setValue(0.5f)),
                    new ColumnLayout(KNOB_LABEL_SEPARATION, Alignment.C,
                        new Label("Hold", KNOB_LABEL_SIZE),
                        holdKnob = new Knob(KNOB_SIZE)
                            .onValueChanged(v -> env.hold = v)
                            .setValue(0.0f)),
                    new ColumnLayout(KNOB_LABEL_SEPARATION, Alignment.C,
                        new Label("Decay", KNOB_LABEL_SIZE),
                        decayKnob = new Knob(KNOB_SIZE)
                            .onValueChanged(v -> env.decay = v)
                            .setValue(0.125f)),
                    new ColumnLayout(KNOB_LABEL_SEPARATION, Alignment.C,
                        new Label("Sustain", KNOB_LABEL_SIZE),
                        sustainKnob = new Knob(KNOB_SIZE)
                            .onValueChanged(v -> env.sustain = v)
                            .setValue(0.5f)),
                    new ColumnLayout(KNOB_LABEL_SEPARATION, Alignment.C,
                        new Label("Release", KNOB_LABEL_SIZE),
                        releaseKnob = new Knob(KNOB_SIZE)
                            .onValueChanged(v -> env.release = v)
                            .setValue(0.125f)),
                    new ColumnLayout(KNOB_LABEL_SEPARATION, Alignment.C,
                        new Label("D/R-curve", KNOB_LABEL_SIZE),
                        rCurveKnob = new Knob(KNOB_SIZE)
                            .onValueChanged(v -> env.rCurve = (float)Math.pow(8.0, v*2.0 - 1.0))
                            .setValue(0.5f))));
    }
    
    public List<Jack> getJacks()
    {
        return Arrays.asList(attackKnob, aCurveKnob, holdKnob, decayKnob, sustainKnob, releaseKnob, rCurveKnob);
    }
    
    public void updateValues(int sampleIndex)
    {
        attackKnob.updateValue(sampleIndex);
        aCurveKnob.updateValue(sampleIndex);
        holdKnob.updateValue(sampleIndex);
        decayKnob.updateValue(sampleIndex);
        sustainKnob.updateValue(sampleIndex);
        releaseKnob.updateValue(sampleIndex);
        rCurveKnob.updateValue(sampleIndex);
    }
    
    @Override
    public Vec2 getPos()
    {
        return layout.getPos();
    }

    @Override
    public Vec2 getRadius()
    {
        return layout.getRadius();
    }

    @Override
    public EnvelopeEditor setPos(Vec2 pos, Alignment align)
    {
        layout.setPos(pos, align);
        return this;
    }

    @Override
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
    {
        layout.updateMatrix(matrix, panel, front);
    }

    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        return layout.checkFocus(dist, p);
    }

    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
    }

    @Override
    public void delete()
    {
        layout.delete();
    }

    @Override
    public void render(float alpha)
    {
        graph.update(env);
        layout.render(alpha);
        
        Vec2 pos = getPos();
        Vec2 radius = getRadius();
        float x0 = pos.x - radius.x, x1 = pos.x + radius.x;
        float y0 = pos.y - radius.y, y1 = pos.y + radius.y;
        
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x0, y0);
        GL11.glVertex2f(x0, y1);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y0);
        GL11.glEnd();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Serialization">
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        attackKnob.save(out);
        aCurveKnob.save(out);
        holdKnob.save(out);
        decayKnob.save(out);
        sustainKnob.save(out);
        releaseKnob.save(out);
        rCurveKnob.save(out);
    }
    
    @Override
    public void load(DataInputStream in) throws IOException
    {
        attackKnob.load(in);
        aCurveKnob.load(in);
        holdKnob.load(in);
        decayKnob.load(in);
        sustainKnob.load(in);
        releaseKnob.load(in);
        rCurveKnob.load(in);
    }
    // </editor-fold>
}
