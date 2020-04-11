/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.util;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class ComplexKeyHashMap
{
  public static class Entry {
    public int hash;
    public Entry next;
    public Object value;

    public Object getValue() {
      return value;
    }

    public void setValue(Object value) {
      this.value = value;
    }
  }

  protected Entry[] table;

  protected static final int DEFAULT_CAPACITY = 32;
  protected static final int MINIMUM_CAPACITY = 4;
  protected static final int MAXIMUM_CAPACITY = 1 << 28;

  protected int size;
  protected transient int threshold;

  public ComplexKeyHashMap() {
      init(DEFAULT_CAPACITY);
  }

    public ComplexKeyHashMap(boolean b) {
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
      Arrays.fill(tab, null);
      size = 0;
  }

  public void init(int initCapacity) {
      threshold = (initCapacity * 6)/8;
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
      threshold = (6 * newLength) / 8;
  }

  private static int capacity(int expectedMaxSize) {
      // Compute min capacity for expectedMaxSize given a load factor of 3/4
      int minCapacity = (8 * expectedMaxSize)/6;

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

  public interface EntryIterator {
      boolean hasNext ();
      Entry   next ();
  }


    public ComplexKeyHashMap.Entry[] getTable() {
        return table;
    }

  public EntryIterator  getEntrySetIterator() {
        return new EntryIterator() {
            Entry next;       // next entry to return
            int index;        // current slot

            {
                Entry[] t = table;
                int i = t.length;
                Entry n = null;
                if (size != 0) { // advance to first entry
                    while (i > 0 && (n = t[--i]) == null) {}
                }
                next = n;
                index = i;
            }

            public boolean hasNext() {
                return next != null;
            }

            public Entry next() {
                return nextEntry();
            }

            Entry nextEntry() {
                Entry e = next;
                if (e == null)
                    throw new NoSuchElementException();

                Entry n = e.next;
                Entry[] t = table;
                int i = index;
                while (n == null && i > 0)
                    n = t[--i];
                index = i;
                next = n;
                return e;
            }
        };
  }
}
