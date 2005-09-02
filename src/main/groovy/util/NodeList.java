/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package groovy.util;

import groovy.xml.QName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A List implementation which is returned by queries on a {@link Node} which provides some XPath like helper methods for GPath
 * 
 * @version $Revision$
 */
public class NodeList extends ArrayList {

    public NodeList() {
    }

    public NodeList(Collection collection) {
        super(collection);
    }

    public NodeList(int size) {
        super(size);
    }
    
    /**
     * Provides lookup of elements by non-namespaced name
     */
    public NodeList getAt(String name) {
        NodeList answer = new NodeList();
        for (Iterator iter = iterator(); iter.hasNext();) {
            Object child = iter.next();
            if (child instanceof Node) {
                Node childNode = (Node) child;
                Object temp = childNode.get(name);
                if (temp instanceof Collection) {
                    answer.addAll((Collection) temp);
                }
                else {
                    answer.add(temp);
                }
            }
        }
        return answer;
    }

    /**
     * Provides lookup of elements by QName
     */
    public NodeList getAt(QName name) {
        NodeList answer = new NodeList();
        for (Iterator iter = iterator(); iter.hasNext();) {
            Object child = iter.next();
            if (child instanceof Node) {
                Node childNode = (Node) child;
                NodeList temp = childNode.getAt(name);
                answer.addAll(temp);
            }
        }
        return answer;
    }

    /**
     * Returns the text value of all of the elements in the collection
     * 
     * @return the text value of all the elements in the collection or null
     */
    public String text() {
        String previousText = null;
        StringBuffer buffer = null;
        for (Iterator iter = this.iterator(); iter.hasNext();) {
            Object child = iter.next();
            String text = null;
            if (child instanceof String) {
                text = (String) child;
            }
            else if (child instanceof Node) {
                text = ((Node) child).text();
            }
            if (text != null) {
                if (previousText == null) {
                    previousText = text;
                }
                else {
                    if (buffer == null) {
                        buffer = new StringBuffer();
                        buffer.append(previousText);
                    }
                    buffer.append(text);
                }
            }
        }
        if (buffer != null) {
            return buffer.toString();
        }
        else {
            if (previousText != null) {
                return previousText;
            }
        }
        return "";
    }
}
