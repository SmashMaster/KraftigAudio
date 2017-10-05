package kraftig.game.audio;

import kraftig.game.util.CircularBuffer;
import kraftig.game.util.FloatFunction;

public class DelayLine
{
    private CircularBuffer buffer = new CircularBuffer(512);
    private int length;
    private FloatFunction feedbackFunc = null;
    
    public DelayLine()
    {
    }
    
    public DelayLine(FloatFunction feedbackFunc)
    {
        this.feedbackFunc = feedbackFunc;
    }
    
    public void setLength(int length)
    {
        if (buffer.getCapacity() < length)
            buffer = new CircularBuffer(buffer, length*2);
        
        this.length = length;
    }
    
    public void setFeedback(FloatFunction func)
    {
        feedbackFunc = func;
    }
    
    public void clear()
    {
        buffer.clear();
    }
    
    public float apply(float value)
    {
        if (length == 0)
        {
            buffer.clear();
            return value;
        }
        
        if (buffer.getSize() < length)
        {
            buffer.push(value);
            return 0.0f;
        }
        else while (buffer.getSize() > length) buffer.poll();

        float result = buffer.poll();
        float feedback = feedbackFunc != null ? feedbackFunc.apply(result) : 0.0f;
        buffer.push(value + feedback);
        
        return result;
    }
}
