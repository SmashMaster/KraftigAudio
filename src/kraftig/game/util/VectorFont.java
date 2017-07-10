package kraftig.game.util;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.res.Resource;
import com.samrj.devil.ui.Alignment;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import org.lwjgl.opengl.GL11;

public class VectorFont
{
    private final Font font;
    private final AffineTransform identity;
    private final FontRenderContext frc;
    
    public VectorFont(String fontFile) throws Exception
    {
        font = Font.createFont(Font.TRUETYPE_FONT, Resource.open(fontFile));
        identity = new AffineTransform();
        frc = new FontRenderContext(identity, true, true);
    }
    
    public Vec2 getSize(String text)
    {
        GlyphVector vector = font.createGlyphVector(frc, text);
        Rectangle2D bounds = vector.getLogicalBounds();
        return new Vec2((float)bounds.getWidth(), (float)bounds.getHeight());
    }
    
    public void render(String text, Vec2 pos, float size, Alignment align)
    {
        GlyphVector vector = font.createGlyphVector(frc, text);
        Shape shape = vector.getOutline();
        Rectangle2D bounds = shape.getBounds2D();
        
        Vec2 rad = new Vec2((float)bounds.getWidth(), (float)bounds.getHeight()).mult(0.5f);
        
        GL11.glPushMatrix();
        GL11.glScalef(size, -size, 0.0f);
        GL11.glTranslatef((align.x - 1.0f)*rad.x, (-align.y + 1.0f)*rad.y, 0.0f);
        
        float[] coords = new float[6];
        float loopX = 0.0f, loopY = 0.0f;
        float px = 0.0f, py = 0.0f;
        
        GL11.glBegin(GL11.GL_LINES);
        for (PathIterator path = shape.getPathIterator(identity); !path.isDone(); path.next())
        {
            int code = path.currentSegment(coords);
            float x = coords[0], y = coords[1];
            
            switch (code)
            {
                case PathIterator.SEG_MOVETO: //End segment and move to a new position.
                    px = x;
                    py = y;
                    loopX = x;
                    loopY = y;
                    break;
                default: //Draw line from previous to current.
                    GL11.glVertex2f(px, py);
                    GL11.glVertex2f(x, y);
                    px = x;
                    py = y;
                    break;
                case PathIterator.SEG_CLOSE: //Draw line to close loop.
                    GL11.glVertex2f(x, y);
                    GL11.glVertex2f(loopX, loopY);
                    px = 0.0f;
                    py = 0.0f;
                    loopX = 0.0f;
                    loopY = 0.0f;
                    break;
            }
        }
        GL11.glEnd();
        
        GL11.glPopMatrix();
    }
}
