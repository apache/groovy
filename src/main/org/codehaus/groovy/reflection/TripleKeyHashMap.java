package org.codehaus.groovy.reflection;

public class TripleKeyHashMap extends ComplexKeyHashMap
{
  public static class Entry extends ComplexKeyHashMap.Entry{
    public Object key1, key2, key3;
  }

  public final Object get(Object key1, Object key2, Object key3) {
    int h = hash (31*(31*key1.hashCode()+key2.hashCode())+key3.hashCode());
    ComplexKeyHashMap.Entry e = table [h & (table.length-1)];
    for (; e != null; e = e.next)
      if (e.hash == h && checkEquals((Entry) e, key1, key2, key3))
        return e;

    return null;
  }

  public boolean checkEquals(Entry e, Object key1, Object key2, Object key3) {
      return e.key1.equals(key1) && e.key2.equals(key2) && e.key3.equals(key3);
  }

  public Entry getOrPut(Object key1, Object key2, Object key3)
  {
    int h = hash (31*(31*key1.hashCode()+key2.hashCode())+key3.hashCode());
    final int index = h & (table.length - 1);
    ComplexKeyHashMap.Entry e = table [index];
    for (; e != null; e = e.next)
      if (e.hash == h && checkEquals((Entry) e, key1, key2, key3))
        return (Entry) e;

    Entry entry = createEntry ();
    entry.next = table [index];
    entry.hash = h;
    entry.key1 = key1;
    entry.key2 = key2;
    entry.key3 = key3;
    table [index] = entry;

    if ( ++size == threshold )
      resize(2*table.length);

    return entry;
  }

  public Entry createEntry() {
      return new Entry ();
  }

  public final ComplexKeyHashMap.Entry remove(Object key1, Object key2, Object key3) {
    int h = hash (31*(31*key1.hashCode()+key2.hashCode())+key3.hashCode());
    int index = h & (table.length -1);
    for (ComplexKeyHashMap.Entry e = table [index], prev = null; e != null; prev = e, e = e.next ) {
      if (e.hash == h && checkEquals((Entry) e, key1, key2, key3)) {
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
