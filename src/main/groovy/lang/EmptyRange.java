package groovy.lang;

import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.*;

/**
 * Constructing Ranges like 0..<0
 * @author Dierk Koenig
 */
public class EmptyRange implements Range {
    protected Comparable at;
    protected final List EMPTY_LIST = new ArrayList();

    public EmptyRange(Comparable at) {
       this.at = at;
    }

    public Comparable getFrom() {
        return at;
    }

    public Comparable getTo() {
        return at;
    }

    public boolean isReverse() {
        return false;
    }

    public String inspect() {
        return InvokerHelper.inspect(at)+"..<"+InvokerHelper.inspect(at);
    }

    public String toString() {
        if (null == at) return "null..<null";
        return at.toString()+"..<"+at.toString();
    }

    public int size() {
        return 0;
    }

    public void clear() {
    }

    public boolean isEmpty() {
        return true;
    }

    public Object[] toArray() {
        return new Object[0];
    }

    public Object get(int index) {
        return null;
    }

    public Object remove(int index) {
        return null;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void add(int index, Object element) {
        throw new UnsupportedOperationException("cannot add to Empty Ranges");
    }

    public int indexOf(Object o) {
        return -1;
    }

    public int lastIndexOf(Object o) {
        return -1;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public boolean add(Object o) {
        throw new UnsupportedOperationException("cannot add to Empty Ranges");
    }

    public boolean contains(Object o) {
        return false;
    }

    public boolean remove(Object o) {
        return false;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public boolean addAll(int index, Collection c) {
        throw new UnsupportedOperationException("cannot add to Empty Ranges");
    }

     /**
     * @throws UnsupportedOperationException
     */
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException("cannot add to Empty Ranges");
    }

    public boolean containsAll(Collection c) {
        return false;
    }

    public boolean removeAll(Collection c) {
        return false;
    }

    public boolean retainAll(Collection c) {
        return false;
    }

    public Iterator iterator() {
        return EMPTY_LIST.iterator();
    }

    public List subList(int fromIndex, int toIndex) {
        return EMPTY_LIST.subList(fromIndex, toIndex);
    }

    public ListIterator listIterator() {
        return EMPTY_LIST.listIterator();
    }

    public ListIterator listIterator(int index) {
        return EMPTY_LIST.listIterator(index);
    }

     /**
     * @throws UnsupportedOperationException
     */
    public Object set(int index, Object element) {
        throw new UnsupportedOperationException("cannot set in Empty Ranges");
    }

    public Object[] toArray(Object a[]) {
        return new Object[0];
    }

    public void step(int step, Closure closure) {
    }

    public List step(int step) {
        return EMPTY_LIST;
    }
}
