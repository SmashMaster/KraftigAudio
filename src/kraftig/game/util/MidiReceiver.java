package kraftig.game.util;

import javax.sound.midi.Receiver;

@FunctionalInterface
public interface MidiReceiver extends Receiver
{
    public static MidiReceiver of(MidiReceiver r)
    {
        return r;
    }
    
    @Override
    public default void close()
    {
    }
}
