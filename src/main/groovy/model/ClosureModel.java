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
package groovy.model;

import groovy.lang.Closure;

/**
 * Represents a value model using a closure to extract
 * the value from some source model and an optional write closure
 * for updating the value.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ClosureModel implements ValueModel, NestedValueModel {

    private ValueModel sourceModel;
    private Closure readClosure;
    private Closure writeClosure;
    private Class type;

    public ClosureModel(ValueModel sourceModel, Closure readClosure) {
        this(sourceModel, readClosure, null);
    }

    public ClosureModel(ValueModel sourceModel, Closure readClosure, Closure writeClosure) {
        this(sourceModel, readClosure, writeClosure, Object.class);
    }

    public ClosureModel(ValueModel sourceModel, Closure readClosure, Closure writeClosure, Class type) {
        this.sourceModel = sourceModel;
        this.readClosure = readClosure;
        this.writeClosure = writeClosure;
        this.type = type;
    }

    public ValueModel getSourceModel() {
        return sourceModel;
    }

    public Object getValue() {
        Object source = sourceModel.getValue();
        if (source != null) {
            return readClosure.call(source);
        }
        return null;
    }

    public void setValue(Object value) {
        if (writeClosure != null) {
            Object source = sourceModel.getValue();
            if (source != null) {
                writeClosure.call(new Object[] { source, value });
            }
        }
    }

    public Class getType() {
        return type;
    }

    public boolean isEditable() {
        return writeClosure != null;
    }
}
