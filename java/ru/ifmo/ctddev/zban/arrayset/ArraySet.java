package ru.ifmo.ctddev.zban.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final ReversibleArray<T> data;

    public ArraySet() {
        data = new ReversibleArray<T>();
    }

    public ArraySet(ReversibleArray<T> data) {
        this.data = data;
    }

    public ArraySet(Collection<T> collection) {
        data = new ReversibleArray<T>();
        TreeSet<T> treeSet = new TreeSet<T>(collection);
        for (T elem : treeSet) {
            data.add(elem);
        }
    }

    public ArraySet(Collection<T> collection, Comparator<T> comparator) {
        data = new ReversibleArray<T>(comparator);
        TreeSet<T> treeSet = new TreeSet<T>(comparator);
        treeSet.addAll(collection);
        for (T elem : treeSet) {
            data.add(elem);
        }
    }

    @Override
    public T lower(T t) {
        return data.search(t, false, false);
    }

    @Override
    public T floor(T t) {
        return data.search(t, true, false);
    }

    @Override
    public T ceiling(T t) {
        return data.search(t, true, true);
    }

    @Override
    public T higher(T t) {
        return data.search(t, false, true);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<T>(data.reverse());
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<T> subSet(T t, boolean b, T e1, boolean b1) {
        return tailSet(t, b).headSet(e1, b1);
    }

    @Override
    public NavigableSet<T> headSet(T t, boolean b) {
        return new ArraySet<T>(data.headList(t, b));
    }

    @Override
    public NavigableSet<T> tailSet(T t, boolean b) {
        return new ArraySet<T>(data.tailList(t, b));
    }

    @Override
    public Comparator<? super T> comparator() {
        return data.comparator();
    }

    @Override
    public SortedSet<T> subSet(T t, T e1) {
        return subSet(t, true, e1, false);
    }

    @Override
    public SortedSet<T> headSet(T t) {
        return headSet(t, false);
    }

    @Override
    public SortedSet<T> tailSet(T t) {
        return tailSet(t, true);
    }

    @Override
    public T first() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(0);
    }

    @Override
    public T last() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(data.size() - 1);
    }

    @Override
    public int size() {
        return data.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        T item = (T) o;
        T other = ceiling(item);
        if (other == null) {
            return false;
        }
        Comparator<? super T> comparator = comparator();
        if (comparator == null) {
            return ((Comparable<T>)item).compareTo(other) == 0;
        } else {
            return comparator.compare(item, other) == 0;
        }
    }
}