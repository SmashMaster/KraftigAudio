package kraftig.game;

import com.samrj.devil.graphics.Camera3D;

public interface Drawable
{
    public void updatePlane(Camera3D camera, DrawPlane plane);
    public void render(Camera3D camera, float alpha);
}
