package kraftig.game;

import com.samrj.devil.math.Vec2;

public interface Drawable
{
    public float edgeRayHit(Vec2 p, Vec2 d);
    public float getY();
    public float getHeight();
    public void render();
}
