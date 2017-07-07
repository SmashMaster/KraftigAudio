package kraftig.game.device;

public interface SourceDevice
{
    public void startFrame();
    public boolean hasProcessedThisFrame();
    public void flush();
}
