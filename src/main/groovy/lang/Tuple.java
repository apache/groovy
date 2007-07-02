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
package groovy.lang;

import java.util.AbstractList;
import java.util.List;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

/**
 * Represents a list of Integer objects from a specified int up to but not including
 * a given and to.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Tuple extends AbstractList {

    private Object[] contents;
    private int hashCode;

    public Tuple(Object[] contents) {
        this.contents = contents;
    }

    public Object get(int index) {
        return contents[index];
    }

    public int size() {
        return contents.length;
    }

    public boolean equals(Object that) {
        if (that instanceof Tuple) {
            return equals((Tuple) that);
        }
        return false;
    }

    public boolean equals(Tuple that) {
        if (contents.length == that.contents.length) {
            for (int i = 0; i < contents.length; i++) {
                if (! DefaultTypeTransformation.compareEqual(this.contents[i], that.contents[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    public int hashCode() {
        if (hashCode == 0) {
            for (int i = 0; i < contents.length; i++ ) {
                Object value = contents[i];
                int hash = (value != null) ? value.hashCode() : 0xbabe;
                hashCode ^= hash;
            }
            if (hashCode == 0) {
                hashCode = 0xbabe;
            }
        }
        return hashCode;
    }

    public List subList(int fromIndex, int toIndex) {
        int size = toIndex - fromIndex;
        Object[] newContent = new Object[size];
        System.arraycopy(contents, fromIndex, newContent, 0, size);
        return new Tuple(newContent);
    }
}
