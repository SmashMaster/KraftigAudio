package kraftig.game.device;

import java.util.stream.Stream;

public interface Device
{
    public default Stream<Device> getInputDevices()
    {
        return Stream.empty();
    };
    
    public void process(int samples);
}
