package kraftig.game;

public interface InteractionMode
{
    public boolean isDead();
    public boolean isCursorVisible();
    public void onMouseMoved(float x, float y, float dx, float dy);
    public void onMouseButton(int button, int action, int mods);
    public void onMouseScroll(float dx, float dy);
    public void onKey(int key, int action, int mods);
}
