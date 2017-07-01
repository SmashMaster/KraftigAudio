package kraftig.game;

public interface InteractionState
{
    public default boolean isCursorVisible()
    {
        return true;
    }
    
    public default boolean canPlayerAim()
    {
        return false;
    }
    
    public default void onMouseMoved(float x, float y, float dx, float dy) {}
    public default void onMouseButton(int button, int action, int mods) {}
    public default void onMouseScroll(float dx, float dy) {}
    public default void onKey(int key, int action, int mods) {}
    public default void step(float dt) {}
}
