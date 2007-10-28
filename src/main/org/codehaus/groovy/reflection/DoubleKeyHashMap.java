package org.codehaus.groovy.reflection;

public class DoubleKeyHashMap extends ComplexKeyHashMap
{
  public static class Entry extends ComplexKeyHashMap.Entry{
    public Object key1, key2;
  }

  public final Object get(Object key1, Object key2) {
    int h = hash (31*key1.hashCode()+key2.hashCode());
    ComplexKeyHashMap.Entry e = table [h & (table.length-1)];
    for (; e != null; e = e.next)
      if (e.hash == h && checkEquals((Entry) e, key1, key2))
        return e;

    return null;
  }

    public boolean checkEquals(ComplexKeyHashMap.Entry e, Object key1, Object key2) {
        Entry ee = (Entry) e;
        return ee.key1 == key1 && ee.key2 == key2;
    }

  public Entry getOrPut(Object key1, Object key2)
  {
    int h = hash (31*key1.hashCode()+key2.hashCode());
    final int index = h & (table.length - 1);
    ComplexKeyHashMap.Entry e = table [index];
    for (; e != null; e = e.next)
      if (e.hash == h && checkEquals( e, key1, key2))
        return (Entry) e;

    ComplexKeyHashMap.Entry entry = createEntry(key1, key2, h, index);
    table [index] = entry;

    if ( ++size == threshold )
      resize(2*table.length);

    return (Entry) entry;
  }

  private ComplexKeyHashMap.Entry createEntry(Object key1, Object key2, int h, int index)
  {
    Entry entry = createEntry ();
    entry.next = table [index];
    entry.hash = h;
    entry.key1 = key1;
    entry.key2 = key2;
    return entry;
  }

  public Entry createEntry() {
      return new Entry ();
  }

  public final ComplexKeyHashMap.Entry remove(Object key1, Object key2) {
    int h = hash (31*key1.hashCode()+key2.hashCode());
    int index = h & (table.length -1);
    for (ComplexKeyHashMap.Entry e = table [index], prev = null; e != null; prev = e, e = e.next ) {
      if (e.hash == h && checkEquals((Entry) e, key1, key2)) {
        if (prev == null)
          table [index] = e.next;
        else
          prev.next = e.next;
        size--;

        e.next = null;
        return e;
      }
    }

    return null;
  }

}
