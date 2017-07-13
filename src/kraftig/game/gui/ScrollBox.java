package kraftig.game.gui;

import com.samrj.devil.geo2d.AAB2;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.ui.Alignment;
import kraftig.game.FocusQuery;
import kraftig.game.Focusable;
import kraftig.game.Main;
import org.lwjgl.opengl.GL11;

public class ScrollBox implements UIElement
{
    private static final float BAR_WIDTH = 12.0f;
    private static final float ARROW_BUTTON_HEIGHT = 6.0f;
    private static final float ARROW_BUTTON_RX = BAR_WIDTH/2.0f;
    private static final float ARROW_BUTTON_RY = ARROW_BUTTON_HEIGHT/2.0f;
    private static final float ARROW_RX = ARROW_BUTTON_RX*0.5f;
    private static final float ARROW_RY = ARROW_BUTTON_RY*0.5f;
    
    private final Vec2 pos = new Vec2();
    private final Vec2 radius = new Vec2();
    private final UIElement content;
    
    private final ArrowButton upButton = new ArrowButton(true), downButton = new ArrowButton(false);
    private final Thumb thumb = new Thumb();
    
    private float scrollPos = 10.0f;
    
    public ScrollBox(Vec2 radius, UIElement content)
    {
        this.radius.set(radius);
        this.content = content;
    }
    
    public ScrollBox(Vec2 size, UIElement content, Vec2 pos, Alignment align)
    {
        this(size, content);
        setPos(pos, align);
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
        content.setPos(new Vec2(pos.x - radius.x, pos.y + radius.y), Alignment.SE);
        return this;
    }

    @Override
    public void updateMatrix(Mat4 matrix)
    {
        content.updateMatrix(matrix);
    }

    @Override
    public UIFocusQuery checkFocus(float dist, Vec2 p)
    {
        UIFocusQuery f;
        if ((f = upButton.checkFocus(dist, p)) != null) return f;
        if ((f = downButton.checkFocus(dist, p)) != null) return f;
        if ((f = thumb.checkFocus(dist, p)) != null) return f;
        
        if (p.x < pos.x - radius.x || p.x > pos.x + radius.x - BAR_WIDTH) return null;
        if (p.y < pos.y - radius.y || p.y > pos.y + radius.y - BAR_WIDTH) return null;
        
        return content.checkFocus(dist, new Vec2(p.x, p.y - scrollPos));
    }

    @Override
    public void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
    }

    @Override
    public void delete()
    {
    }

    @Override
    public void render(float alpha)
    {
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
        
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(barX, y0);
        GL11.glVertex2f(barX, y1);
        GL11.glEnd();
        
        upButton.render(alpha);
        downButton.render(alpha);
        thumb.render(alpha);
        
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
            
            float contentSize = content.getRadius().y*2.0f;
            float y0t = Util.saturate((scrollPos + radius.y*2.0f)/contentSize);
            float y1t = Util.saturate(scrollPos/contentSize);
            
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
