package kraftig.game;

public interface InteractionState
{
    public default boolean isCursorVisible(Main main)
    {
        return true;
    }
    
    public default boolean canPlayerAim()
    {
        return false;
    }
    
    public default void onMouseMoved(Main main, float x, float y, float dx, float dy) {}
    public default void onMouseButton(Main main, int button, int action, int mods) {}
    public default void onMouseScroll(Main main, float dx, float dy) {}
    public default void onKey(Main main, int key, int action, int mods) {}
    public default void step(Main main, float dt) {}
}
