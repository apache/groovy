package org.codehaus.groovy.reflection;

public class ComplexKeyHashMap
{
  public static class Entry {
    public int hash;
    public Entry next;
  }

  Entry table [];

  protected static final int DEFAULT_CAPACITY = 32;
  protected static final int MINIMUM_CAPACITY = 4;
  protected static final int MAXIMUM_CAPACITY = 1 << 28;

  protected int size;
  protected transient int threshold;

  public ComplexKeyHashMap() {
      init(DEFAULT_CAPACITY);
  }

  public ComplexKeyHashMap(int expectedMaxSize) {
    init (capacity(expectedMaxSize));
  }

  public static int hash(int h) {
    h += ~(h << 9);
    h ^=  (h >>> 14);
    h +=  (h << 4);
    h ^=  (h >>> 10);
    return h;
  }

  public int size() {
      return size;
  }

  public boolean isEmpty() {
      return size == 0;
  }

  public void clear() {
      Object[] tab = table;
      for (int i = 0; i < tab.length; i++)
          tab[i] = null;
      size = 0;
  }

  public void init(int initCapacity) {
      threshold = (initCapacity * 2)/8;
      table = new Entry[initCapacity];
  }

  public void resize(int newLength) {
      Entry[] oldTable = table;
      int oldLength = table.length;

      Entry[] newTable = new Entry[newLength];

      for (int j = 0; j < oldLength; j++) {

        for (Entry e = oldTable [j]; e != null;) {
          Entry next = e.next;
          int index = e.hash & (newLength-1);

          e.next = newTable[index];
          newTable [index] = e;

          e = next;
        }
      }

      table = newTable;
      threshold = (2 * newLength) / 8;
  }

  private int capacity(int expectedMaxSize) {
      // Compute min capacity for expectedMaxSize given a load factor of 1/4
      int minCapacity = (8 * expectedMaxSize)/2;

      // Compute the appropriate capacity
      int result;
      if (minCapacity > MAXIMUM_CAPACITY || minCapacity < 0) {
          result = MAXIMUM_CAPACITY;
      } else {
          result = MINIMUM_CAPACITY;
          while (result < minCapacity)
              result <<= 1;
      }
      return result;
  }
}
