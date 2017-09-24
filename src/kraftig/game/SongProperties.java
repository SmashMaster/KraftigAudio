package kraftig.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import kraftig.game.util.Savable;

public class SongProperties implements Savable
{
    public boolean playing = false;
    public long playStartTime = 0L;
    public long position = 0L;
    public double tempo = 120.0;
    public int tsBeatsPerBar = 4, tsBeatNoteValue = 4;
    public int songLength = 64;
    
    private final Set<UpdateCallback> callbacks = Collections.newSetFromMap(new IdentityHashMap<>());

    public void init()
    {
        playing = false;
        position = 0L;
        tempo = 120.0;
        tsBeatsPerBar = 4;
        tsBeatNoteValue = 4;
        songLength = 64;
    }
    
    public double getSamplesPerBeat()
    {
        return Main.SAMPLE_RATE/((tempo/60.0)*(tsBeatNoteValue/4.0));
    }
    
    public double getSamplesPerBar()
    {
        return getSamplesPerBeat()*tsBeatsPerBar;
    }
    
    public double getSongSampleLength()
    {
        return getSamplesPerBar()*songLength;
    }
    
    public double getPositionBar()
    {
        return position/getSamplesPerBar();
    }
    
    //Update callbacks only happen upon tempo or time signature changes.
    public UpdateCallback onUpdate(Runnable action)
    {
        UpdateCallback callback = new UpdateCallback(action);
        callbacks.add(callback);
        return callback;
    }
    
    public void update()
    {
        for (UpdateCallback callback : callbacks) callback.action.run();
    }
    
    public void setTimeSignature(int beatsPerBar, int beatNoteValue)
    {
        if (beatsPerBar < 1) beatsPerBar = 1;
        if (beatsPerBar > 32) beatsPerBar = 32;
        if (beatNoteValue < 1) beatNoteValue = 1;
        if (beatNoteValue > 256) beatNoteValue = 256;
        
        tsBeatsPerBar = beatsPerBar;
        tsBeatNoteValue = beatNoteValue;
        update();
    }
    
    public void back()
    {
        double samplesPerBar = getSamplesPerBar();
        int bar = (int)Math.ceil(position/samplesPerBar);
        long newPos = Math.round((bar - 1)*samplesPerBar);
        playStartTime += position - newPos;
        position = newPos;
    }
    
    public void forward()
    {
        double samplesPerBar = getSamplesPerBar();
        int bar = (int)Math.floor(position/samplesPerBar);
        long newPos = Math.round((bar + 1)*samplesPerBar);
        playStartTime += position - newPos;
        position = newPos;
    }
    
    public void play()
    {
        playing = true;
        playStartTime = Main.instance().getTime() - position;
    }
    
    public void stop()
    {
        if (playing)
        {
            playing = false;
            playStartTime = 0L;
        }
        else position = 0L;
    }
    
    @Override
    public void save(DataOutputStream out) throws IOException
    {
        out.writeDouble(tempo);
        out.writeInt(tsBeatsPerBar);
        out.writeInt(tsBeatNoteValue);
    }

    @Override
    public void load(DataInputStream in) throws IOException
    {
        playing = false;
        position = 0L;
        tempo = in.readDouble();
        tsBeatsPerBar = in.readInt();
        tsBeatNoteValue = in.readInt();
    }
    
    public class UpdateCallback
    {
        private final Runnable action;
        
        private UpdateCallback(Runnable action)
        {
            this.action = action;
        }
        
        public void delete()
        {
            callbacks.remove(this);
        }
    }
}
