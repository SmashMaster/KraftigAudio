package kraftig.game;

import com.samrj.devil.geo3d.Box3;
import com.samrj.devil.math.Vec2;

public interface Drawable
{
    public float edgeRayHit(Vec2 p, Vec2 d);
    public float getY();
    public float getHeight();
    public boolean isVisible(Box3 viewBox);
    public void render();
}
