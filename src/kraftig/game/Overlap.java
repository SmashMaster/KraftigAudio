package kraftig.game;

import com.samrj.devil.graphics.Camera3D;

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
        float da = b.rayHit(a.a, a.aCam);
        float db = b.rayHit(a.b, a.bCam);
        float doa = a.rayHit(b.a, b.aCam);
        float dob = a.rayHit(b.b, b.bCam);
        
        boolean behind = da < 0.0f || db < 0.0f || doa > 0.0f || dob > 0.0f;
        boolean inFront = da > 0.0f || db > 0.0f || doa < 0.0f || dob < 0.0f;
        
        if (behind && inFront)
        {
            //Figure out which is above/below.
            Panel above = a.pos.y > b.pos.y ? a : b;
            Panel below = a.pos.y > b.pos.y ? b : a;
            
            //y1 > b.y0 && b.y1 > y0
            float aboveY0 = above.pos.y - above.height;
            float aboveY1 = above.pos.y + above.height;
            float belowY0 = below.pos.y - below.height;
            float belowY1 = below.pos.y + below.height;
            
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
    
    public static Overlap get(Panel a, Wire b, Camera3D camera)
    {
        return NONE;
    }
    
    public static Overlap get(Wire a, Panel b, Camera3D camera)
    {
        return invert(get(b, a, camera));
    }
    
    public static Overlap get(Wire a, Wire b, Camera3D camera)
    {
        return NONE;
    }
    
    public static Overlap get(Drawable a, Drawable b, Camera3D camera)
    {
        boolean aPanel = a instanceof Panel, bPanel = b instanceof Panel;
        
        if (aPanel && bPanel) return get((Panel)a, (Panel)b, camera);
        else if (aPanel) return get((Panel)a, (Wire)b, camera);
        else if (bPanel) return get((Wire)a, (Panel)b, camera);
        else return get((Wire)a, (Wire)b, camera);
    }
}
