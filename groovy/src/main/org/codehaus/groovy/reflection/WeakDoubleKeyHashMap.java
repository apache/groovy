/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.reflection;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class WeakDoubleKeyHashMap extends ComplexKeyHashMap
{
  private static class Ref extends WeakReference {
      public Ref(Object referent, ReferenceQueue q, Entry entry) {
          super(referent, q);
          this.entry = entry;
      }

      Entry entry;
  }

  public static class Entry extends ComplexKeyHashMap.Entry{
    public Ref key1, key2;
  }

  private final ReferenceQueue queue = new ReferenceQueue();

    private void expungeStaleEntries() {
	    Ref r;
        while ( (r = (Ref) queue.poll()) != null) {
            Entry e = r.entry;
            if (e == null)
              continue;

            r.entry = null;
            e.key1 = e.key2 = null;

            int h = e.hash;
            int i = h & (table.length-1);

            Entry prev = (Entry) table[i];
            Entry p = prev;
            while (p != null) {
                Entry next = (Entry) p.next;
                if (p == e) {
                    if (prev == e)
                        table[i] = next;
                    else
                        prev.next = next;
                    e.next = null;  // Help GC
                    e.value = null; //  "   "
                    size--;
                    break;
                }
                prev = p;
                p = next;
            }
        }
    }

  public final Object get(Object key1, Object key2) {
//    expungeStaleEntries();
    int h = hash (31*key1.hashCode()+key2.hashCode());
    ComplexKeyHashMap.Entry e = table [h & (table.length-1)];
    for (; e != null; e = e.next)
      if (e.hash == h && checkEquals((Entry) e, key1, key2))
        return e;

    return null;
  }

    public boolean checkEquals(ComplexKeyHashMap.Entry e, Object key1, Object key2) {
        Entry ee = (Entry) e;
        return ee.key1.get() == key1 && ee.key2.get() == key2;
    }

  public Entry getOrPut(Object key1, Object key2)
  {
//    expungeStaleEntries();
    int h = hash (31*key1.hashCode()+key2.hashCode());
      final ComplexKeyHashMap.Entry[] t = table;
      final int index = h & (t.length - 1);
    ComplexKeyHashMap.Entry e = t[index];
    for (; e != null; e = e.next)
      if (e.hash == h && checkEquals( e, key1, key2))
        return (Entry) e;

    ComplexKeyHashMap.Entry entry = createEntry(key1, key2, h, index);
    t[index] = entry;

    if ( ++size == threshold )
      resize(2* t.length);

    return (Entry) entry;
  }

  private ComplexKeyHashMap.Entry createEntry(Object key1, Object key2, int h, int index)
  {
    Entry entry = createEntry ();
    entry.next = table [index];
    entry.hash = h;
    entry.key1 = new Ref(key1, queue, entry);
    entry.key2 = new Ref(key2, queue, entry);
    return entry;
  }

  public Entry createEntry() {
      expungeStaleEntries();
      return new Entry ();
  }

    public int size() {
        expungeStaleEntries();
        return super.size();
    }

    public final ComplexKeyHashMap.Entry remove(Object key1, Object key2) {
    expungeStaleEntries();
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
