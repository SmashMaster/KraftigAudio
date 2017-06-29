package kraftig.game;

import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.math.Vec2;

public class DrawPlane
{
    public final Drawable drawable;
    
    public final Vec2 a = new Vec2(), b = new Vec2();
    public float y, height;
    
    private final Vec2 ab = new Vec2();
    private final Vec2 aCam = new Vec2(), bCam = new Vec2();
    
    public DrawPlane(Drawable drawable)
    {
        this.drawable = drawable;
    }
    
    public Vec2 getACam()
    {
        return new Vec2(aCam);
    }
    
    public Vec2 getBCam()
    {
        return new Vec2(bCam);
    }
    
    public void update(Camera3D camera)
    {
        drawable.updatePlane(camera, this);
        
        Vec2.sub(b, a, ab);
        Vec2 cam = new Vec2(camera.pos.x, camera.pos.z);
        Vec2.sub(cam, a, aCam);
        Vec2.sub(cam, b, bCam);
    }
    
    public float rayHit(Vec2 p, Vec2 d)
    {
        //Calculate hit position and return zero if missed.
        Vec2 pa = Vec2.sub(p, a);
        float t = (d.x*pa.y - d.y*pa.x)/(d.y*ab.x - d.x*ab.y);
        if (t < 0.0f || t > 1.0f) return 0.0f;
        
        //Return direction of hit.
        Vec2 dr = Vec2.madd(pa, ab, t);
        return Math.signum(dr.dot(d));
    }
    
    public void render(Camera3D camera, float alpha)
    {
        drawable.render(camera, alpha);
    }
}
