package kraftig.game;

import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import kraftig.game.Wire.WireSplit;

public enum Overlap
{
    NONE, B_BEHIND_A, A_BEHIND_B, INTERSECTION;
    
    private static Overlap invert(Overlap o)
    {
        switch (o)
        {
            case B_BEHIND_A: return A_BEHIND_B;
            case A_BEHIND_B: return B_BEHIND_A;
            default: return o;
        }
    }
    
    public static Overlap get(Panel a, Panel b, Camera3D camera)
    {
        float daa = Util.signum(b.edgeRayHit(a.ea, a.eaCam));
        float dab = Util.signum(b.edgeRayHit(a.eb, a.ebCam));
        float dba = Util.signum(a.edgeRayHit(b.ea, b.eaCam));
        float dbb = Util.signum(a.edgeRayHit(b.eb, b.ebCam));
        float count = daa + dab - dba - dbb;
        
        if (daa == 0.0f && dab == 0.0f && dba == 0.0f && dbb == 0.0f) return NONE;
        else if (count == -2.0f) return A_BEHIND_B;
        else if (count == 2.0f) return B_BEHIND_A;
        else
        {
            //Figure out which is above/below.
            Panel above = a.getY() > b.getY() ? a : b;
            Panel below = a.getY() > b.getY() ? b : a;
            
            float aboveY0 = above.getY() - above.getHeight();
            float aboveY1 = above.getY() + above.getHeight();
            float belowY0 = below.getY() - below.getHeight();
            float belowY1 = below.getY() + below.getHeight();
            
            //Check if vertically intersecting. If not, resolve by vertical position.
            if (aboveY1 > belowY0 && belowY1 > aboveY0) return INTERSECTION;
            else if (aboveY0 > camera.pos.y && belowY1 > camera.pos.y)
                return below == a ? B_BEHIND_A : A_BEHIND_B;
            else if (aboveY0 < camera.pos.y && belowY1 < camera.pos.y)
                return above == a ? B_BEHIND_A : A_BEHIND_B;
            else return NONE;
        }
    }
    
    public static Overlap get(Panel a, WireSplit b, Camera3D camera)
    {
        float daa = Util.signum(b.edgeRayHit(a.ea, a.eaCam));
        float dab = Util.signum(b.edgeRayHit(a.eb, a.ebCam));
        float dba = Util.signum(a.edgeRayHit(b.ea, b.eaCam));
        float dbb = Util.signum(a.edgeRayHit(b.eb, b.ebCam));
        float count = daa + dab - dba - dbb;
        
        if (daa == 0.0f && dab == 0.0f && dba == 0.0f && dbb == 0.0f) return NONE;
        else if (count == -2.0f) return A_BEHIND_B;
        else if (count == 2.0f) return B_BEHIND_A;
        else
        {
            //Make sure the wire isn't parallel to the plane.
            float denom = Vec3.dot(b.ab, a.frontDir);
            if (Math.abs(denom) < 0.001f) return NONE;
            
            //Make sure the wire doesn't intersect the plane.
            float tIntersect = Vec3.dot(Vec3.sub(a.getPos(), b.a.pos), a.frontDir)/denom;
            float yIntersect = b.a.pos.y + b.ab.y*tIntersect;
            
            float ay0 = a.getY() - a.getHeight();
            float ay1 = a.getY() + a.getHeight();
            if (yIntersect > ay0 && yIntersect < ay1) return INTERSECTION;
            
            //Perform dark rituals to summon the elder god Linay Ar Al Gebbra,
            //who then tells us whether the line is behind the panel.
            float y = yIntersect > a.getY() ? ay1 : ay0;
            
            Vec3 p0 = new Vec3(a.ea.x, y, a.ea.y);
            Vec3 p1 = new Vec3(a.eb.x, y, a.eb.y);
            
            //Seriously I'm not completely sure how this works. Something about
            //intersecting two planes to find a line.
            Vec3 na = Vec3.sub(b.b.pos, camera.pos).cross(Vec3.sub(b.b.pos, b.a.pos));
            Vec3 nb = Vec3.sub(p1, camera.pos).cross(Vec3.sub(p1, p0));
            Vec3 dir = Vec3.cross(na, nb);
            
            if (denom < 0.0f) dir.negate();
            
            if (dir.y > 0.0f) return A_BEHIND_B;
            else return B_BEHIND_A;
        }
    }
    
    public static Overlap get(WireSplit a, Panel b, Camera3D camera)
    {
        return invert(get(b, a, camera));
    }
    
    public static Overlap get(WireSplit a, WireSplit b, Camera3D camera)
    {
        return NONE;
    }
    
    public static Overlap get(Drawable a, Drawable b, Camera3D camera)
    {
        boolean aPanel = a instanceof Panel, bPanel = b instanceof Panel;
        
        if (aPanel && bPanel) return get((Panel)a, (Panel)b, camera);
        else if (aPanel) return get((Panel)a, (WireSplit)b, camera);
        else if (bPanel) return get((WireSplit)a, (Panel)b, camera);
        else return get((WireSplit)a, (WireSplit)b, camera);
    }
}
