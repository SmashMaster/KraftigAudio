package kraftig.game;

public class FocusQuery
{
    public final Focusable focus;
    public final float dist;
    
    public FocusQuery(Focusable focus, float dist)
    {
        this.focus = focus;
        this.dist = dist;
    }
}
