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
        float daa = b.edgeRayHit(a.ea, a.eaCam);
        float dab = b.edgeRayHit(a.eb, a.ebCam);
        float dba = a.edgeRayHit(b.ea, b.eaCam);
        float dbb = a.edgeRayHit(b.eb, b.ebCam);
        
        boolean behind = daa < 0.0f || dab < 0.0f || dba > 0.0f || dbb > 0.0f;
        boolean inFront = daa > 0.0f || dab > 0.0f || dba < 0.0f || dbb < 0.0f;
        
        if (behind && inFront)
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
        else if (behind) return A_BEHIND_B;
        else if (inFront) return B_BEHIND_A;
        else return NONE;
    }
    
    public static Overlap get(Panel a, WireSplit b, Camera3D camera)
    {
        float daa = b.edgeRayHit(a.ea, a.eaCam);
        float dab = b.edgeRayHit(a.eb, a.ebCam);
        float dba = a.edgeRayHit(b.ea, b.eaCam);
        float dbb = a.edgeRayHit(b.eb, b.ebCam);
        
        boolean behind = daa < 0.0f || dab < 0.0f || dba > 0.0f || dbb > 0.0f;
        boolean inFront = daa > 0.0f || dab > 0.0f || dba < 0.0f || dbb < 0.0f;
        
        if (behind && inFront)
        {
            //First check if we're definitely not intersecting.
            float aY0 = a.getY() - a.getHeight();
            float aY1 = a.getY() + a.getHeight();
            float bY0 = b.getY() - b.getHeight();
            float bY1 = b.getY() + b.getHeight();
            
            if (bY1 < aY0 || bY0 > aY1) return NONE;
            
            //Then check if we're actually intersecting.
            float denom = Vec3.dot(b.ab, a.frontDir);
            if (denom < 0.001f) return NONE;
            
            float tIntersect = Vec3.dot(Vec3.sub(a.pos, b.a.pos), a.frontDir)/denom;
            float yIntersect = b.a.pos.y + b.ab.y*tIntersect;
            
            if (yIntersect > aY0 && yIntersect < aY1) return INTERSECTION;
            
            //Then find an arbitrary overlapping spot and see which side it's on.
            float y = (Math.min(aY1, bY1) + Math.max(aY0, bY0))*0.5f;
            float t = (y - bY0)/(bY1 - bY0);
            
            float signP = Util.signum(Vec3.lerp(b.a.pos, b.b.pos, t).sub(a.pos).dot(a.frontDir));
            float signCam = Util.signum(Vec3.dot(Vec3.sub(camera.pos, a.pos), a.frontDir));
            
            if (signP == signCam) return A_BEHIND_B;
            else return B_BEHIND_A;
        }
        else if (behind) return A_BEHIND_B;
        else if (inFront) return B_BEHIND_A;
        else return NONE;
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
