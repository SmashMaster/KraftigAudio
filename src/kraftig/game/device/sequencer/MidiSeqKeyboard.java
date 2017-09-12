package kraftig.game.device.sequencer;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Focusable;
import kraftig.game.InteractionState;
import kraftig.game.Main;
import kraftig.game.Panel;
import kraftig.game.gui.UIElement;
import kraftig.game.gui.UIFocusQuery;
import kraftig.game.util.DSPUtil;
import kraftig.game.util.VectorFont;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class MidiSeqKeyboard implements UIElement
{
    private final VectorFont font;
    private final MidiSequencer sequencer;
    private final MidiSeqCamera camera;
    private final Key[] keys = new Key[128];
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    
    public MidiSeqKeyboard(MidiSequencer sequencer, Vec2 radius)
    {
        font = Main.instance().getFont();
        this.sequencer = sequencer;
        camera = sequencer.getCamera();
        for (int i=0; i<128; i++) keys[i] = new Key(i);
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
    public final MidiSeqKeyboard setPos(Vec2 pos, Alignment align)
    {
        align.align(pos, getRadius(), this.pos);
        return this;
    }
    
    @Override
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
    {
    }
    
    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        if (p.x < pos.x - radius.x || p.x > pos.x + radius.x) return null;
        if (p.y < pos.y - radius.y || p.y > pos.y + radius.y) return null;
        
        Vec2 s = new Vec2(p.x, (p.y - pos.y)*0.5f/radius.y);
        Vec2 w = camera.toWorld(s);
        
        if (w.y >= 0.0f && w.y < 128.0f) return new UIFocusQuery(keys[Util.floor(w.y)], dist, p);
        
        return new UIFocusQuery(this, dist, p);
    }
    
    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
        if (action == GLFW.GLFW_PRESS && button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) camera.drag(false, true);
    }
    
    @Override
    public void onMouseScroll(FocusQuery query, float dx, float dy)
    {
        float factor = (float)Math.pow(1.5, dy);
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
        
        //Draw outline.
        GL11.glLineWidth(1.0f);
        float color = Main.instance().getFocus() == this ? 0.75f : 1.0f;
        GL11.glColor4f(color, color, 1.0f, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(-1.0f, -1.0f);
        GL11.glVertex2f(-1.0f, 1.0f);
        GL11.glVertex2f(1.0f, 1.0f);
        GL11.glVertex2f(1.0f, -1.0f);
        GL11.glEnd();
        
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
        camera.multYMatrix();
        
        GL11.glLineWidth(1.0f);
        Key focus = null;
        for (Key key : keys)
        {
            if (key == Main.instance().getFocus()) focus = key;
            else key.render(alpha);
        }
        if (focus != null) focus.render(alpha);
        
        //Return to normal stencil state.
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        
        GL11.glPopMatrix();
    }
    
    private class Key implements Focusable
    {
        private final int midi;
        private final String name;
        private final boolean isBlack;
        
        private Key(int midi)
        {
            this.midi = midi;
            name = DSPUtil.getMidiName(midi);
            isBlack = DSPUtil.isMidiBlack(midi);
        }
        
        @Override
        public void onMouseButton(FocusQuery query, int button, int action, int mods)
        {
            if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
            {
                MidiSeqKeyboard.this.onMouseButton(query, button, action, mods);
                return;
            }
            
            if (action == GLFW.GLFW_PRESS && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                DSPUtil.midiOn(sequencer, midi);
                
                Main.instance().setState(new InteractionState()
                {
                    @Override
                    public boolean canPlayerAim()
                    {
                        return true;
                    }

                    @Override
                    public void onMouseButton(int button, int action, int mods)
                    {
                        if (action == GLFW.GLFW_RELEASE && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                        {
                            DSPUtil.midiOff(sequencer, midi);
                            Main.instance().setDefaultState();
                        }
                    }
                });
            }
        }

        @Override
        public void onMouseScroll(FocusQuery query, float dx, float dy)
        {
            MidiSeqKeyboard.this.onMouseScroll(query, dx, dy);
        }
        
        private void render(float alpha)
        {
            float y0 = midi;
            float y1 = midi + 1;
            
            if (isBlack)
            {
                GL11.glColor4f(0.0f, 0.0f, 0.0f, alpha*0.5f);
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2f(-1.0f, y0);
                GL11.glVertex2f(-1.0f, y1);
                GL11.glVertex2f(1.0f, y1);
                GL11.glVertex2f(1.0f, y0);
                GL11.glEnd();
            }
            
            if (Main.instance().getFocus() == this)
            {
                GL11.glColor4f(0.75f, 0.75f, 1.0f, alpha);
                GL11.glBegin(GL11.GL_LINE_LOOP);
                GL11.glVertex2f(-1.0f, y0);
                GL11.glVertex2f(-1.0f, y1);
                GL11.glVertex2f(1.0f, y1);
                GL11.glVertex2f(1.0f, y0);
                GL11.glEnd();
            }
            else
            {
                GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha*0.5f);
                GL11.glBegin(GL11.GL_LINES);
                GL11.glVertex2f(-1.0f, y0);
                GL11.glVertex2f(1.0f, y0);
                GL11.glVertex2f(-1.0f, y1);
                GL11.glVertex2f(1.0f, y1);
                GL11.glEnd();
            }
            
            GL11.glPushMatrix();
            GL11.glTranslatef(-1.0f, 0.0f, 0.0f);
            GL11.glScalef(camera.getScale().y*8.0f, 1.0f, 0.0f);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
            font.render(name, new Vec2(0.0f, y0 + 0.5f), 0.875f, Alignment.E);
            GL11.glPopMatrix();
        }
    }
}
