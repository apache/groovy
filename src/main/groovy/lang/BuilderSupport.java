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


import java.util.List;
import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * An abstract base class for creating arbitrary nested trees of objects
 * or events
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class BuilderSupport implements GroovyObject {

    private Object current;
    MetaClass metaClass = InvokerHelper.getMetaClass(this);

    public Object invokeMethod(String name, Object args) {
        Object node = null;
        Closure closure = null;
        List list = InvokerHelper.asList(args);

        //System.out.println("Called invokeMethod with arguments: " + list);

        if (!list.isEmpty()) {
            Object object = list.get(0);
            if (object instanceof Map) {
                node = createNode(name, (Map) object);
            }
            else if (object instanceof Closure) {
                closure = (Closure) object;
                node = createNode(name);
            }
            else {
                node = createNode(name, object);
            }
            if (list.size() > 1) {
                object = list.get(1);
                if (object instanceof Closure) {
                    closure = (Closure) object;
                }
            }
        }
        else {
            node = createNode(name);
        }
        if (current != null) {
            setParent(current, node);
        }

        //System.out.println("Created node: " + node);

        if (closure != null) {
            // push new node on stack
            Object oldCurrent = current;
            current = node;

            // lets register the builder as the delegate
            closure.setDelegate(this);
            closure.call();

            current = oldCurrent;
        }
        nodeCompleted(node);
        return node;
    }

    public MetaClass getMetaClass() {
        return metaClass;
    }

    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    protected abstract void setParent(Object parent, Object child);
    protected abstract Object createNode(Object name);
    protected abstract Object createNode(Object name, Object value);
    protected abstract Object createNode(Object name, Map attributes);

    /**
     * A hook to allow nodes to be processed once they have had all of their
     * children applied
     */
    protected void nodeCompleted(Object node) {
    }

    protected Object getCurrent() {
        return current;
    }
}
