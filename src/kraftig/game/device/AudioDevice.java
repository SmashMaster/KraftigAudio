package kraftig.game.device;

import java.util.stream.Stream;

public interface AudioDevice
{
    public default Stream<AudioDevice> getInputDevices()
    {
        return Stream.empty();
    };
    
    public void process(int samples);
}
