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
    
    public boolean isFull()
    {
        return size == array.length;
    }
    
    public void clear()
    {
        size = 0;
    }
    
    public void push(float v)
    {
        if (size == array.length) throw new ArrayIndexOutOfBoundsException();
        
        array[write++] = v;
        if (write == array.length) write = 0;
        size++;
    }
    
    public float poll()
    {
        if (size == 0) throw new ArrayIndexOutOfBoundsException();
        
        float v = array[read++];
        if (read == array.length) read = 0;
        size--;
        
        return v;
    }
    
    public void read(float[] array, int start, int length)
    {
        if (length < 0) throw new ArrayIndexOutOfBoundsException();
        if (length > size) throw new ArrayIndexOutOfBoundsException();
        
        int end = read + length;
        
        if (end > this.array.length)
        {
            int len0 = this.array.length - read;
            System.arraycopy(this.array, read, array, 0, len0);
            System.arraycopy(this.array, 0, array, len0, end - this.array.length);
        }
        else System.arraycopy(this.array, read, array, start, length);
    }
}
