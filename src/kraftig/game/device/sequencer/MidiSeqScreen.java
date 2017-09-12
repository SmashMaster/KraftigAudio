package kraftig.game.device.sequencer;

import com.samrj.devil.geo2d.AAB2;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Focusable;
import kraftig.game.InteractionState;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.SongProperties;
import kraftig.game.audio.MidiReceiver;
import kraftig.game.gui.UIElement;
import kraftig.game.gui.UIFocusQuery;
import kraftig.game.util.DSPUtil;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class MidiSeqScreen implements UIElement
{
    private static final float END_MARGIN = 1.0f/64.0f;
    
    private final SongProperties properties;
    private final MidiReceiver midiOut;
    private final MidiSeqCamera camera;
    private final Track track;
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    
    private Panel panel;
    
    public MidiSeqScreen(MidiSequencer sequencer, Vec2 radius)
    {
        properties = Main.instance().getProperties();
        midiOut = sequencer.getMidiOut();
        camera = sequencer.getCamera();
        track = sequencer.getTrack();
        this.radius.set(radius);
    }
    
    @Override
    public final Vec2 getPos()
    {
        return new Vec2(pos);
    }
    
    @Override
    public final Vec2 getRadius()
    {
        return new Vec2(radius);
    }
    
    @Override
    public final MidiSeqScreen setPos(Vec2 pos, Alignment align)
    {
        align.align(pos, getRadius(), this.pos);
        return this;
    }

    @Override
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
    {
        this.panel = panel;
    }
    
    public Vec2 getMouse()
    {
        if (panel == null) return null;
        
        boolean[] hit = {false};
        float[] dist = {0.0f};
        Vec2 mPos = new Vec2();
        int[] side = {0};
        panel.projectMouse(hit, dist, mPos, side);
        if (dist[0] <= 0.0f) return null;
        
        if (side[0] == 1) mPos.x = -mPos.x;
        mPos.div(Panel.UI_SCALE);
        
        mPos.x = (mPos.x - pos.x)*0.5f/radius.x;
        mPos.y = (mPos.y - pos.y)*0.5f/radius.y;
        return mPos;
    }
    
    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        if (p.x < pos.x - radius.x || p.x > pos.x + radius.x) return null;
        if (p.y < pos.y - radius.y || p.y > pos.y + radius.y) return null;
        
        Vec2 world = camera.toWorld(Vec2.sub(p, pos).mult(0.5f).div(radius));
        
        //Check notes
        for (Note note : track.notes)
        {
            float start = note.start/(float)Main.SAMPLE_RATE;
            float end = note.end/(float)Main.SAMPLE_RATE;
            float bottom = note.midi;
            float top = bottom + 1.0f;
            
            if (world.x >= start && world.x < end && world.y >= bottom && world.y < top)
            {
                Vec2 offset = new Vec2(start - world.x, bottom - world.y);
                return new UIFocusQuery(new NoteMiddleFocus(note, offset), dist, p);
            }
        }
        
        //Check note ends
        for (Note note : track.notes)
        {
            float end = note.end/(float)Main.SAMPLE_RATE;
            float pastEnd = end + END_MARGIN/camera.getScale().x;
            float bottom = note.midi;
            float top = bottom + 1.0f;
            
            if (world.x >= end && world.x < pastEnd && world.y >= bottom && world.y < top)
            {
                Vec2 offset = new Vec2(end - world.x, bottom - world.y);
                return new UIFocusQuery(new NoteEndFocus(note, offset), dist, p);
            }
        }
        
        //Check note starts
        for (Note note : track.notes)
        {
            float start = note.start/(float)Main.SAMPLE_RATE;
            float beforeStart = start - END_MARGIN/camera.getScale().x;
            float bottom = note.midi;
            float top = bottom + 1.0f;
            
            if (world.x >= beforeStart && world.x < start && world.y >= bottom && world.y < top)
            {
                Vec2 offset = new Vec2(start - world.x, bottom - world.y);
                return new UIFocusQuery(new NoteStartFocus(note, offset), dist, p);
            }
        }
        
        return new UIFocusQuery(this, dist, p);
    }

    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
        if (action != GLFW.GLFW_PRESS) return;
        
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) camera.drag(true, true);
        
        //New note creation.
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            Vec2 mouse = camera.toWorld(getMouse());
            Note note = new Note();
            note.start = Math.round(mouse.x*Main.SAMPLE_RATE);
            note.end = note.start;
            note.midi = Util.floor(mouse.y);
            if (note.midi < 0 || note.midi > 127) return;
            
            track.notes.add(note);
            DSPUtil.midiOn(midiOut, note.midi);
            
            Main.instance().setState(new InteractionState()
            {
                private void update()
                {
                    long end = Math.round(camera.toWorld(getMouse()).x*Main.SAMPLE_RATE);
                    note.end = Math.max(note.start, end);
                }
                
                @Override
                public void onMouseMoved(float x, float y, float dx, float dy)
                {
                    update();
                }
                
                @Override
                public void onMouseButton(int button, int action, int mods)
                {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_RELEASE)
                    {
                        DSPUtil.midiOff(midiOut, note.midi);
                        Main.instance().setDefaultState();
                    }
                }
                
                @Override
                public void step(float dt)
                {
                    update();
                }
            });
        }
    }
    
    @Override
    public void onMouseScroll(FocusQuery query, float dx, float dy)
    {
        float factor = (float)Math.pow(1.5, dy);
        camera.zoomX(factor);
        camera.zoomY(factor);
    }

    @Override
    public void delete()
    {
    }

    @Override
    public void render(float alpha)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef(pos.x, pos.y, 0.0f);
        GL11.glScalef(radius.x, radius.y, 1.0f);
        
        //Enable stencil writing, disable color writing.
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glColorMask(false, false, false, false);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

        //Draw stencil mask.
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-1.0f, -1.0f);
        GL11.glVertex2f(-1.0f, 1.0f);
        GL11.glVertex2f(1.0f, 1.0f);
        GL11.glVertex2f(1.0f, -1.0f);
        GL11.glEnd();
        
        //Disable stencil writing, enable color writing.
        GL11.glColorMask(true, true, true, true);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        
        //Draw masked stuff here.
        GL11.glPushMatrix();
        camera.multMatrix();
        AAB2 bounds = camera.getBounds();
        
        //Black keys
        GL11.glColor4f(0.0f, 0.0f, 0.0f, alpha*0.125f);
        GL11.glBegin(GL11.GL_QUADS);
        for (int midi=0; midi<128; midi++) if (DSPUtil.isMidiBlack(midi))
        {
            GL11.glVertex2f(bounds.x0, midi);
            GL11.glVertex2f(bounds.x0, midi + 1);
            GL11.glVertex2f(bounds.x1, midi + 1);
            GL11.glVertex2f(bounds.x1, midi);
        }
        GL11.glEnd();
        
        GL11.glLineWidth(1.0f);
        GL11.glBegin(GL11.GL_LINES);
        
        //Song bars/beats
        int songBeats = properties.tsBeatsPerBar*properties.songLength;
        float beatLen = (float)(properties.getSamplesPerBeat()/Main.SAMPLE_RATE);
        for (int i=0; i<songBeats; i++)
        {
            if (i == 0) GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
            else if (i%properties.tsBeatsPerBar == 0) GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha*0.375f);
            else GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha*0.125f);
            
            float x = beatLen*i;
            GL11.glVertex2f(x, 0.0f);
            GL11.glVertex2f(x, 128.0f);
        }
        
        //MIDI lines
        for (float y = 0.0f; y <= 128.0f; y++)
        {
            if (y%12 == 0) GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha*0.375f);
            else GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha*0.125f);
            GL11.glVertex2f(bounds.x0, y);
            GL11.glVertex2f(bounds.x1, y);
        }
        
        //Song position
        float songPos = (float)((double)properties.position/Main.SAMPLE_RATE);
        GL11.glColor4f(0.75f, 0.75f, 1.0f, alpha*0.75f);
        GL11.glVertex2f(songPos, 0.0f);
        GL11.glVertex2f(songPos, 128.0f);
        
        GL11.glEnd();
        
        //Notes
        Focusable focus = Main.instance().getFocus();
        Note nStartFocus = focus instanceof NoteStartFocus ? ((NoteStartFocus)focus).note : null;
        Note nEndFocus = focus instanceof NoteEndFocus ? ((NoteEndFocus)focus).note : null;
        Note nFocus = focus instanceof NoteMiddleFocus ? ((NoteMiddleFocus)focus).note : null;
        
        GL11.glBegin(GL11.GL_LINES);
        for (Note note : track.notes)
        {
            float startColor = (nStartFocus == note || nFocus == note) ? 0.75f : 1.0f;
            float endColor = (nEndFocus == note || nFocus == note) ? 0.75f : 1.0f;
            float color = nFocus == note ? 0.75f : 1.0f;
            
            float start = (float)note.start/Main.SAMPLE_RATE;
            float end = (float)note.end/Main.SAMPLE_RATE;
            float bottom = note.midi;
            float top = bottom + 1.0f;
            
            GL11.glColor4f(startColor, startColor, 1.0f, alpha);
            GL11.glVertex2f(start, bottom);
            GL11.glVertex2f(start, top);
            
            GL11.glColor4f(endColor, endColor, 1.0f, alpha);
            GL11.glVertex2f(end, top);
            GL11.glVertex2f(end, bottom);
            
            GL11.glColor4f(color, color, 1.0f, alpha);
            GL11.glVertex2f(start, bottom);
            GL11.glVertex2f(end, bottom);
            GL11.glVertex2f(start, top);
            GL11.glVertex2f(end, top);
        }
        GL11.glEnd();
        
        //Return to normal stencil state.
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        
        GL11.glPopMatrix();
        
        //Draw outline.
        GL11.glLineWidth(1.0f);
        float color = focus == this ? 0.75f : 1.0f;
        GL11.glColor4f(color, color, 1.0f, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(-1.0f, -1.0f);
        GL11.glVertex2f(-1.0f, 1.0f);
        GL11.glVertex2f(1.0f, 1.0f);
        GL11.glVertex2f(1.0f, -1.0f);
        GL11.glEnd();
        
        GL11.glPopMatrix();
    }
    
    @FunctionalInterface
    private interface NoteDrag extends InteractionState
    {
        public void update();
        
        @Override
        public default boolean canPlayerAim()
        {
            return true;
        }

        @Override
        public default void onMouseMoved(float x, float y, float dx, float dy)
        {
            update();
        }
        
        @Override
        public default void onMouseButton(int button, int action, int mods)
        {
            if (action == GLFW.GLFW_RELEASE && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                Main.instance().setDefaultState();
        }
        
        @Override
        public default void step(float dt)
        {
            update();
        }
    }
    
    private abstract class NoteFocus implements Focusable
    {
        final Note note;
        final Vec2 offset;
        
        private NoteFocus(Note note, Vec2 offset)
        {
            this.note = note;
            this.offset = new Vec2(offset);
        }
        
        abstract NoteDrag drag();
        
        @Override
        public void onMouseButton(FocusQuery query, int button, int action, int mods)
        {
            if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
                MidiSeqScreen.this.onMouseButton(query, button, action, mods);
            
            if (action != GLFW.GLFW_PRESS) return;
            
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) Main.instance().setState(drag());
            else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) track.notes.remove(note);
        }
        
        @Override
        public void onMouseScroll(FocusQuery query, float dx, float dy)
        {
            MidiSeqScreen.this.onMouseScroll(query, dx, dy);
        }
    }
    
    private class NoteStartFocus extends NoteFocus
    {
        private NoteStartFocus(Note note, Vec2 offset)
        {
            super(note, offset);
        }
        
        @Override
        NoteDrag drag()
        {
            return () ->
            {
                Vec2 mouse = camera.toWorld(getMouse()).add(offset);
                note.start = Math.round(mouse.x*Main.SAMPLE_RATE);
            };
        }
    }
    
    private class NoteEndFocus extends NoteFocus
    {
        private NoteEndFocus(Note note, Vec2 offset)
        {
            super(note, offset);
        }
        
        @Override
        NoteDrag drag()
        {
            return () ->
            {
                Vec2 mouse = camera.toWorld(getMouse()).add(offset);
                note.end = Math.round(mouse.x*Main.SAMPLE_RATE);
            };
        }
    }
    
    private class NoteMiddleFocus extends NoteFocus
    {
        private NoteMiddleFocus(Note note, Vec2 offset)
        {
            super(note, offset);
        }
        
        @Override
        NoteDrag drag()
        {
            DSPUtil.midiOn(midiOut, note.midi);
            
            return new NoteDrag()
            {
                @Override
                public void update()
                {
                    Vec2 mouse = camera.toWorld(getMouse());
                    mouse.x += offset.x;
                    long length = note.end - note.start;
                    note.start = Math.round(mouse.x*Main.SAMPLE_RATE);
                    note.end = note.start + length;

                    int oldMidi = note.midi;
                    note.midi = DSPUtil.clampMidi(Util.floor(mouse.y));

                    if (oldMidi != note.midi)
                    {
                        DSPUtil.midiOff(midiOut, oldMidi);
                        DSPUtil.midiOn(midiOut, note.midi);
                    }
                }
                
                @Override
                public void onMouseButton(int button, int action, int mods)
                {
                    if (action == GLFW.GLFW_RELEASE && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                    {
                        Main.instance().setDefaultState();
                        DSPUtil.midiOff(midiOut, note.midi);
                    }
                }
            };
        }
    }
}
