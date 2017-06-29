package kraftig.game;

import com.samrj.devil.graphics.Camera3D;

public interface Drawable
{
    public void updateEdge(Camera3D camera);
    public void render(Camera3D camera, float alpha);
}
