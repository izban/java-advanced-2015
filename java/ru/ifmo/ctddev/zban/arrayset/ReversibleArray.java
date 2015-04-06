package ru.ifmo.ctddev.zban.arrayset;

import java.util.*;

public class ReversibleArray<T> {
    private List<T> data = new ArrayList<T>();
    private Comparator<T> comparator;
    private boolean reversed;

    public ReversibleArray() {
    }

    public ReversibleArray(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public ReversibleArray(Comparator<T> comparator, List<T> data) {
        this.comparator = comparator;
        this.data = data;
    }

    public ReversibleArray(Comparator<T> comparator, List<T> data, boolean reversed) {
        this.comparator = comparator;
        this.data = data;
        this.reversed = reversed;
    }

    public Comparator<T> comparator() {
        if (reversed) {
            return Collections.reverseOrder(comparator);
        }
        return comparator;
    }

    public T get(int i) {
        return data.get(getIndex(i));
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int i = reversed ? data.size() - 1 : 0;

            @Override
            public boolean hasNext() {
                if (reversed) {
                    return i > 0;
                }
                return i < data.size();
            }

            @Override
            public T next() {
                if (reversed) {
                    return data.get(--i);
                }
                return data.get(i++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    int getIndex(int i) {
        if (!reversed) {
            return i;
        }
        return data.size() - i - 1;
    }

    public ReversibleArray<T> subList(int fromIndex, int toIndex) {
        int l = getIndex(fromIndex);
        int r = getIndex(toIndex);
        if (l > r) {
            int o = l;
            l = r;
            r = o;
        }
        if (l < 0 || r >= data.size()) {
            return new ReversibleArray<T>(comparator, new ArrayList<T>(), reversed);
        }
        return new ReversibleArray<T>(comparator, data.subList(l, r + 1), reversed);
    }

    boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return data.size();
    }

    public void add(T t) {
        data.add(t);
    }

    public ReversibleArray<T> reverse() {
        return new ReversibleArray<T>(comparator, data, !reversed);
    }

    private int find(T key, boolean inclusive, boolean higher) {
        int id = Collections.binarySearch(data, key, comparator);
        if (id < 0) {
            id = -(id + 1);
            if (higher == reversed) {
                id--;
            }
        } else {
            if (!inclusive) {
                if (higher ^ reversed) {
                    id++;
                } else {
                    id--;
                }
            }
        }
        return id;
    }

    public T search(T key, boolean inclusive, boolean higher) {
        int result = find(key, inclusive, higher);
        if (result == -1 || result == data.size()) {
            return null;
        }
        return data.get(result);
    }

    public ReversibleArray<T> headList(T key, boolean inclusive) {
        int id = Collections.binarySearch(data, key, comparator);
        if (id < 0) {
            id = -(id + 1);
        } else {
            if (inclusive ^ reversed) {
                id++;
            }
        }
        if (reversed) {
            return subList(getIndex(id), getIndex(data.size() - 1));
        }
        return subList(0, id - 1);
    }

    public ReversibleArray<T> tailList(T key, boolean inclusive) {
        return reverse().headList(key, inclusive).reverse();
    }
}
