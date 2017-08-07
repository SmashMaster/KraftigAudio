package kraftig.game.util;

public class CircularBuffer
{
    private final float[] array;
    
    private int read, write;
    private int size;
    
    public CircularBuffer(int capacity)
    {
        array = new float[capacity];
    }
    
    public int getCapacity()
    {
        return array.length;
    }
    
    public int getSize()
    {
        return size;
    }
    
    public void clear()
    {
        size = 0;
    }
    
    public void write(float v)
    {
        if (size == array.length) throw new ArrayIndexOutOfBoundsException();
        
        array[write++] = v;
        if (write == array.length) write = 0;
        size++;
    }
    
    public float read()
    {
        if (size == 0) throw new ArrayIndexOutOfBoundsException();
        
        float v = array[read++];
        if (read == array.length) read = 0;
        size--;
        
        return v;
    }
}
