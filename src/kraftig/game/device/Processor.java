package kraftig.game.device;

@FunctionalInterface
public interface Processor
{
    public void process(float[][] buffer, int samples);
}
