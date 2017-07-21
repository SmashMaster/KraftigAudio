package kraftig.game.gui;

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
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class ScrollBox implements UIElement
{
    private static final float BAR_WIDTH = 24.0f;
    private static final float ARROW_BUTTON_HEIGHT = 12.0f;
    private static final float ARROW_BUTTON_RX = BAR_WIDTH/2.0f;
    private static final float ARROW_BUTTON_RY = ARROW_BUTTON_HEIGHT/2.0f;
    private static final float ARROW_RX = ARROW_BUTTON_RX*0.5f;
    private static final float ARROW_RY = ARROW_BUTTON_RY*0.5f;
    private static final float SCROLL_RATE = 19.0f;
    
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    private final Alignment internalAlign;
    private UIElement content;
    private final Mat4 matrix = new Mat4();
    private Panel panel;
    private boolean front;
    
    private final ArrowButton upButton = new ArrowButton(true), downButton = new ArrowButton(false);
    private final Thumb thumb = new Thumb();
    
    private float scrollPos = 0.0f;
    
    public ScrollBox(Vec2 radius, Alignment internalAlign)
    {
        this.radius.set(radius);
        this.internalAlign = Alignment.get(-internalAlign.x, -1.0f);
    }
    
    public ScrollBox(Vec2 radius, Alignment internalAlign, UIElement content)
    {
        this(radius, internalAlign);
        this.content = content;
    }
    
    public ScrollBox(Vec2 size, Alignment internalAlign, UIElement content, Vec2 pos, Alignment align)
    {
        this(size, internalAlign, content);
        setPos(pos, align);
    }
    
    public ScrollBox setContent(UIElement content)
    {
        this.content = content;
        
        if (content != null)
        {
            content.setPos(new Vec2(pos.x - radius.x, pos.y + radius.y), Alignment.SE);
            if (panel != null) content.updateMatrix(matrix, panel, front);
        }
        
        return this;
    }
    
    private float getContentRY()
    {
        return content != null ? content.getRadius().y : 0.0f;
    }
    
    private void setScrollPos(float pos)
    {
        scrollPos = Util.clamp(pos, 0.0f, Math.max((getContentRY() - radius.y)*2.0f, 0.0f));
    }
    
    @Override
    public Vec2 getPos()
    {
        return new Vec2(pos);
    }

    @Override
    public Vec2 getRadius()
    {
        return new Vec2(radius);
    }
    
    @Override
    public final ScrollBox setPos(Vec2 pos, Alignment align)
    {
        align.align(pos, radius, this.pos);
        if (content != null)
        {
            float x0 = this.pos.x - radius.x;
            float x1 = this.pos.x + radius.x - BAR_WIDTH;
            float x = Util.lerp(x0, x1, -internalAlign.x*0.5f + 0.5f);
            content.setPos(new Vec2(x, this.pos.y + radius.y), internalAlign);
        }
        return this;
    }

    @Override
    public void updateMatrix(Mat4 matrix, Panel panel, boolean front)
    {
        this.matrix.set(matrix);
        this.panel = panel;
        this.front = front;
        if (content != null) content.updateMatrix(matrix, panel, front);
    }

    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        UIFocusQuery f;
        if ((f = upButton.checkFocus(dist, p)) != null) return f;
        if ((f = downButton.checkFocus(dist, p)) != null) return f;
        if ((f = thumb.checkFocus(dist, p)) != null) return f;
        
        if (content == null) return null;
        if (p.x < pos.x - radius.x || p.x > pos.x + radius.x - BAR_WIDTH) return null;
        if (p.y < pos.y - radius.y || p.y > pos.y + radius.y) return null;
        
        return content.checkFocus(dist, new Vec2(p.x, p.y - scrollPos));
    }

    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
    }

    @Override
    public void delete()
    {
        if (content != null) content.delete();
    }

    @Override
    public void render(float alpha)
    {
        setScrollPos(scrollPos);
        
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
        
        float barX = x1 - BAR_WIDTH;
        
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha*0.25f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(barX, y0);
        GL11.glVertex2f(barX, y1);
        GL11.glEnd();
        
        upButton.render(alpha);
        downButton.render(alpha);
        thumb.render(alpha);
        
        if (content != null)
        {
            //Enable stencil writing, disable color writing.
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GL11.glColorMask(false, false, false, false);
            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

            //Draw stencil mask.
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(x0, y0);
            GL11.glVertex2f(x0, y1);
            GL11.glVertex2f(barX, y1);
            GL11.glVertex2f(barX, y0);
            GL11.glEnd();

            //Disable stencil writing, enable color writing.
            GL11.glColorMask(true, true, true, true);
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

            GL11.glPushMatrix();
            GL11.glTranslatef(0.0f, scrollPos, 0.0f);
            content.render(alpha);
            GL11.glPopMatrix();

            //Disable stencil.
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            
            //Clear stencil buffer.
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        }
    }
    
    private class ArrowButton implements Focusable
    {
        private final boolean top;
        
        private ArrowButton(boolean top)
        {
            this.top = top;
        }
        
        public UIFocusQuery checkFocus(float dist, Vec2 p)
        {
            float x1 = pos.x + radius.x;
            if (p.x > x1 || p.x <= x1 - BAR_WIDTH) return null;
            
            if (top)
            {
                float y1 = pos.y + radius.y;
                if (p.y <= y1 && p.y >= y1 - ARROW_BUTTON_HEIGHT) return new UIFocusQuery(this, dist, p);
            }
            else
            {
                float y0 = pos.y - radius.y;
                if (p.y >= y0 && p.y <= y0 + ARROW_BUTTON_HEIGHT) return new UIFocusQuery(this, dist, p);
            }
            
            return null;
        }
        
        @Override
        public void onMouseButton(FocusQuery query, int button, int action, int mods)
        {
            if (action != GLFW.GLFW_PRESS || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
            
            if (top) setScrollPos(scrollPos - SCROLL_RATE);
            else setScrollPos(scrollPos + SCROLL_RATE);
        }
        
        public void render(float alpha)
        {
            GL11.glPushMatrix();
            
            if (top) GL11.glTranslatef(pos.x + radius.x - ARROW_BUTTON_RX, pos.y + radius.y - ARROW_BUTTON_RY, 0.0f);
            else GL11.glTranslatef(pos.x + radius.x - ARROW_BUTTON_RX, pos.y - radius.y + ARROW_BUTTON_RY, 0.0f);
            
            GL11.glLineWidth(1.0f);
            float color = Main.instance().getFocus() == this ? 0.75f : 1.0f;
            GL11.glColor4f(color, color, 1.0f, alpha);
            GL11.glBegin(GL11.GL_LINE_LOOP);
            GL11.glVertex2f(-ARROW_BUTTON_RX, -ARROW_BUTTON_RY);
            GL11.glVertex2f(-ARROW_BUTTON_RX, ARROW_BUTTON_RY);
            GL11.glVertex2f(ARROW_BUTTON_RX, ARROW_BUTTON_RY);
            GL11.glVertex2f(ARROW_BUTTON_RX, -ARROW_BUTTON_RY);
            GL11.glEnd();
            
            GL11.glBegin(GL11.GL_LINE_STRIP);
            if (top)
            {
                GL11.glVertex2f(-ARROW_RX, -ARROW_RY);
                GL11.glVertex2f(0.0f, ARROW_RY);
                GL11.glVertex2f(ARROW_RX, -ARROW_RY);
            }
            else
            {
                GL11.glVertex2f(-ARROW_RX, ARROW_RY);
                GL11.glVertex2f(0.0f, -ARROW_RY);
                GL11.glVertex2f(ARROW_RX, ARROW_RY);
            }
            GL11.glEnd();
            
            GL11.glPopMatrix();
        }
    }
    
    private class Thumb implements Focusable
    {
        private AAB2 getBounds()
        {
            float x1 = pos.x + radius.x;
            
            float y0t, y1t;
            
            if (content != null)
            {
                float contentSize = getContentRY()*2.0f;
                y0t = Util.saturate((scrollPos + radius.y*2.0f)/contentSize);
                y1t = Util.saturate(scrollPos/contentSize);
            }
            else
            {
                y0t = 1.0f;
                y1t = 0.0f;
            }
            
            float barTop = pos.y + radius.y - ARROW_BUTTON_HEIGHT;
            float barBottom = pos.y - radius.y + ARROW_BUTTON_HEIGHT;
            
            return new AAB2(x1 - BAR_WIDTH, x1, Util.lerp(barTop, barBottom, y0t), Util.lerp(barTop, barBottom, y1t));
        }
        
        public UIFocusQuery checkFocus(float dist, Vec2 p)
        {
            return getBounds().contains(p) ? new UIFocusQuery(this, dist, p) : null;
        }
        
        @Override
        public void onMouseButton(FocusQuery query, int button, int action, int mods)
        {
            if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || action != GLFW.GLFW_PRESS) return;
            
            UIFocusQuery q = (UIFocusQuery)query;
            float initScrollPos = scrollPos;
            
            Main.instance().setState(new InteractionState()
            {
                private void update(float my)
                {
                    float contentSize = getContentRY()*2.0f;
                    float barHeight = (radius.y - ARROW_BUTTON_HEIGHT)*2.0f;
                    
                    if (panel != null)
                    {
                        boolean[] hit = {false};
                        float[] dist = {0.0f};
                        Vec2 rPos = new Vec2();
                        int[] side = {0};
                        panel.projectMouse(hit, dist, rPos, side);
                        
                        setScrollPos(initScrollPos + (q.p.y - rPos.y/Panel.UI_SCALE)*contentSize/barHeight);
                    }
                    else setScrollPos(initScrollPos + (q.p.y - my)*contentSize/barHeight);
                }
                
                @Override
                public boolean canPlayerAim()
                {
                    return true;
                }
                
                @Override
                public void onMouseMoved(float x, float y, float dx, float dy)
                {
                    update(y - Main.instance().getResolution().y*0.5f);
                }
                
                @Override
                public void onMouseButton(int button, int action, int mods)
                {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_RELEASE)
                        Main.instance().setDefaultState();
                }
                
                @Override
                public void step(float dt)
                {
                    if (panel != null) update(0.0f);
                }
            });
        }
        
        public void render(float alpha)
        {
            AAB2 bounds = getBounds();
            
            GL11.glLineWidth(1.0f);
            float color = Main.instance().getFocus() == this ? 0.75f : 1.0f;
            GL11.glColor4f(color, color, 1.0f, alpha);
            GL11.glBegin(GL11.GL_LINE_LOOP);
            GL11.glVertex2f(bounds.x0, bounds.y0);
            GL11.glVertex2f(bounds.x0, bounds.y1);
            GL11.glVertex2f(bounds.x1, bounds.y1);
            GL11.glVertex2f(bounds.x1, bounds.y0);
            GL11.glEnd();
        }
    }
}
