package net.minestom.server.utils.fastutils;

import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;

import java.util.NoSuchElementException;

public class LongArrayPriorityQueue implements LongPriorityQueue, java.io.Serializable {
    private static final long serialVersionUID = 1L;
    /** The backing array. */

    protected transient long array[] = LongArrays.EMPTY_ARRAY;
    /** The number of elements in this queue. */
    protected int size;
    /** The type-specific comparator used in this queue. */
    protected LongComparator c;
    /** The first index, cached, if {@link #firstIndexValid} is true. */
    protected transient int firstIndex;
    /** Whether {@link #firstIndex} contains a valid value. */
    protected transient boolean firstIndexValid;

    /**
     * Creates a new empty queue with a given capacity and comparator.
     *
     * @param capacity the initial capacity of this queue.
     * @param c the comparator used in this queue, or {@code null} for the natural order.
     */

    public LongArrayPriorityQueue(int capacity, LongComparator c) {
        if (capacity > 0) this.array = new long[capacity];
        this.c = c;
    }

    /**
     * Creates a new empty queue with a given capacity and using the natural order.
     *
     * @param capacity the initial capacity of this queue.
     */
    public LongArrayPriorityQueue(int capacity) {
        this(capacity, null);
    }

    /**
     * Creates a new empty queue with a given comparator.
     *
     * @param c the comparator used in this queue, or {@code null} for the natural order.
     */
    public LongArrayPriorityQueue(LongComparator c) {
        this(0, c);
    }

    /**
     * Creates a new empty queue using the natural order.
     */
    public LongArrayPriorityQueue() {
        this(0, null);
    }

    /**
     * Wraps a given array in a queue using a given comparator.
     *
     * <p>
     * The queue returned by this method will be backed by the given array.
     *
     * @param a an array.
     * @param size the number of elements to be included in the queue.
     * @param c the comparator used in this queue, or {@code null} for the natural order.
     */
    public LongArrayPriorityQueue(final long[] a, int size, final LongComparator c) {
        this(c);
        this.array = a;
        this.size = size;
    }

    /**
     * Wraps a given array in a queue using a given comparator.
     *
     * <p>
     * The queue returned by this method will be backed by the given array.
     *
     * @param a an array.
     * @param c the comparator used in this queue, or {@code null} for the natural order.
     */
    public LongArrayPriorityQueue(final long[] a, final LongComparator c) {
        this(a, a.length, c);
    }

    /**
     * Wraps a given array in a queue using the natural order.
     *
     * <p>
     * The queue returned by this method will be backed by the given array.
     *
     * @param a an array.
     * @param size the number of elements to be included in the queue.
     */
    public LongArrayPriorityQueue(final long[] a, int size) {
        this(a, size, null);
    }

    /**
     * Wraps a given array in a queue using the natural order.
     *
     * <p>
     * The queue returned by this method will be backed by the given array.
     *
     * @param a an array.
     */
    public LongArrayPriorityQueue(final long[] a) {
        this(a, a.length);
    }

    /** Returns the index of the smallest element. */

    private int findFirst() {
        if (firstIndexValid) return this.firstIndex;
        firstIndexValid = true;
        int i = size;
        int firstIndex = --i;
        long first = array[firstIndex];
        if (c == null) {
            while (i-- != 0) if (((array[i]) < (first))) first = array[firstIndex = i];
        } else while (i-- != 0) {
            if (c.compare(array[i], first) < 0) first = array[firstIndex = i];
        }
        return this.firstIndex = firstIndex;
    }

    private void ensureNonEmpty() {
        if (size == 0) throw new NoSuchElementException();
    }

    @Override

    public void enqueue(long x) {
        if (size == array.length) array = LongArrays.grow(array, size + 1);
        if (firstIndexValid) {
            if (c == null) {
                if (((x) < (array[firstIndex]))) firstIndex = size;
            } else if (c.compare(x, array[firstIndex]) < 0) firstIndex = size;
        } else firstIndexValid = false;
        array[size++] = x;
    }

    @Override
    public long dequeueLong() {
        ensureNonEmpty();
        final int first = findFirst();
        final long result = array[first];
        System.arraycopy(array, first + 1, array, first, --size - first);
        firstIndexValid = false;
        return result;
    }

    @Override
    public long firstLong() {
        ensureNonEmpty();
        return array[findFirst()];
    }

    @Override
    public void changed() {
        ensureNonEmpty();
        firstIndexValid = false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        size = 0;
        firstIndexValid = false;
    }

    /** Trims the underlying array so that it has exactly {@link #size()} elements. */
    public void trim() {
        array = LongArrays.trim(array, size);
    }

    @Override
    public LongComparator comparator() {
        return c;
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();
        s.writeInt(array.length);
        for (int i = 0; i < size; i++) s.writeLong(array[i]);
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        array = new long[s.readInt()];
        for (int i = 0; i < size; i++) array[i] = s.readLong();
    }
}