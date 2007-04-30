/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

/**
 * Represents a spreadable map which extends java.util.HashMap.
 * 
 * @author Pilho Kim
 * @version $Revision$
 */
public class SpreadMap extends HashMap {

    private Map mapData;
    private int hashCode;

    public SpreadMap(Object[] values) {
        mapData = new HashMap(values.length / 2);
        int i = 0;
        while (i < values.length) {
           mapData.put(values[i++], values[i++]);
        }
    }

    public SpreadMap(Map map) {
        this.mapData = map;
    }

    public Object get(Object obj) {
        return mapData.get(obj);
    }

    public Object put(Object key, Object value) {
        throw new RuntimeException("SpreadMap: " + this + " is an immutable map, and so ("
                                   + key + ": " + value + ") cannot be added.");
    }

    public Object remove(Object key) {
        throw new RuntimeException("SpreadMap: " + this + " is an immutable map, and so the key ("
                                   + key + ") cannot be deleteded.");
    }

    public void putAll(Map t) {
        throw new RuntimeException("SpreadMap: " + this + " is an immutable map, and so the map ("
                                   + t + ") cannot be put in this spreadMap.");
    }

    public int size() {
        return mapData.keySet().size();
    }

    public boolean equals(Object that) {
        if (that instanceof SpreadMap) {
            return equals((SpreadMap) that);
        }
        return false;
    }

    public boolean equals(SpreadMap that) {
        if (that == null) return false;        

        if (size() == that.size()) {
            SpreadMap other = (SpreadMap) that;
            Iterator iter = mapData.keySet().iterator();
            for (; iter.hasNext(); ) {
                Object key = iter.next();
                if (! DefaultTypeTransformation.compareEqual(get(key), other.get(key)) ) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    public int hashCode() {
        if (hashCode == 0) {
            Iterator iter = mapData.keySet().iterator();
            for (; iter.hasNext(); ) {
                Object key = iter.next();
                int hash = (key != null) ? key.hashCode() : 0xbabe;
                hashCode ^= hash;
            }
        }
        return hashCode;
    }

    /**
     * Returns the string expression of <code>this</code>.
     *
     * @return the string expression of <code>this</code>
     */
    public String toString() {
        if (mapData.isEmpty()) {
            return "*:[:]";
        }
        StringBuffer buff = new StringBuffer("*:[");
        Iterator iter = mapData.keySet().iterator();
        for (; iter.hasNext(); ) {
            Object key = iter.next();
            buff.append(key + ":" + mapData.get(key));
            if (iter.hasNext())
                buff.append(", ");
        }
        buff.append("]");
        return buff.toString();
    }
}
