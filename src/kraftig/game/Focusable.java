package kraftig.game;

public interface Focusable
{
    public default void onMouseButton(FocusQuery query, int button, int action, int mods)
    {
    }
    
    public default void onMouseScroll(FocusQuery query, float dx, float dy)
    {
    }
}
