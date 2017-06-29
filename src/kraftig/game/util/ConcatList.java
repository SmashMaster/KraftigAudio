package kraftig.game.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ConcatList<T> implements List<T>
{
    private final List<? extends T> a, b;
    
    public ConcatList(List<? extends T> a,  List<? extends T> b)
    {
        if (a == null || b == null) throw new NullPointerException();
        
        this.a = a;
        this.b = b;
    }
    
    @Override
    public int size()
    {
        return a.size() + b.size();
    }

    @Override
    public boolean isEmpty()
    {
        return a.isEmpty() && b.isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        return a.contains(o) || b.contains(o);
    }

    @Override
    public Iterator<T> iterator()
    {
        return new ConcatIterator();
    }

    @Override
    public Object[] toArray()
    {
        Object[] out = new Object[size()];
        int i = 0;
        for (T e : a) out[i++] = e;
        for (T e : b) out[i++] = e;
        return out;
    }

    @Override
    public <T> T[] toArray(T[] out)
    {
        if (out.length < size()) out = (T[])new Object[size()];
        int i = 0;
        for (Object e : a) out[i++] = (T)e;
        for (Object e : b) out[i++] = (T)e;
        return out;
    }

    @Override
    public boolean add(T e)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        for (Object e : c) if (!contains(e)) return false;
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int index)
    {
        return index < a.size() ? a.get(index) : b.get(index - a.size());
    }

    @Override
    public T set(int index, T element)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o)
    {
        int i = a.indexOf(o);
        if (i < 0) i = b.indexOf(o);
        return i < 0 ? i : i - a.size();
    }

    @Override
    public int lastIndexOf(Object o)
    {
        int i = a.indexOf(o);
        if (i < 0) i = b.indexOf(o);
        return i < 0 ? i : i - a.size();
    }

    @Override
    public ListIterator<T> listIterator()
    {
        return new ConcatListIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index)
    {
        return new ConcatListIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex)
    {
        boolean startInA = fromIndex < a.size();
        boolean endInB = toIndex > a.size();
        
        if (startInA)
        {
            if (endInB) return new ConcatList<>(a.subList(fromIndex, a.size()), b.subList(0, toIndex - a.size()));
            else return (List<T>)a.subList(fromIndex, toIndex);
        }
        else return (List<T>)b.subList(fromIndex - a.size(), toIndex - a.size());
    }
    
    private class ConcatIterator implements Iterator<T>
    {
        private final Iterator<? extends T> ia = a.iterator(), ib = b.iterator();
        
        @Override
        public boolean hasNext()
        {
            return ia.hasNext() || ib.hasNext();
        }

        @Override
        public T next()
        {
            return ia.hasNext() ? ia.next() : ib.next();
        }
    }
    
    private class ConcatListIterator implements ListIterator<T>
    {
        private final ListIterator<? extends T> ia, ib;
        
        private ConcatListIterator(int index)
        {
            ia = a.listIterator(Math.min(Math.max(index, 0), a.size() - 1));
            ib = b.listIterator(Math.min(Math.max(index - a.size(), 0), b.size() - 1));
        }
        
        @Override
        public boolean hasNext()
        {
            return ia.hasNext() || ib.hasNext();
        }

        @Override
        public T next()
        {
            return ia.hasNext() ? ia.next() : ib.next();
        }

        @Override
        public boolean hasPrevious()
        {
            return ib.hasPrevious() || ia.hasPrevious();
        }

        @Override
        public T previous()
        {
            return ib.hasPrevious() ? ib.previous() : ia.previous();
        }

        @Override
        public int nextIndex()
        {
            return ia.hasNext() ? ia.nextIndex() : ib.nextIndex() + a.size();
        }

        @Override
        public int previousIndex()
        {
            return ib.hasPrevious() ? ib.previousIndex() + a.size() : ia.previousIndex();
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(T e)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(T e)
        {
            throw new UnsupportedOperationException();
        }
    }
}
