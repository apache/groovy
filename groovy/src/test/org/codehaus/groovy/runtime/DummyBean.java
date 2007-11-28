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

package org.codehaus.groovy.runtime;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * A bean used by the test cases
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision$
 */
public class DummyBean {
    private String name = "James";
    private Integer i = new Integer(123);
    private Map dynamicProperties = new HashMap();
    private Point point;
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public DummyBean() {
    }

    public DummyBean(String name) {
        this.name = name;
    }

    public DummyBean(String name, Integer i) {
        this.name = name;
        this.i = i;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public Integer getI() {
        return i;
    }

    public void setI(Integer i) {
        changeSupport.firePropertyChange("i", this.i, this.i = i);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        changeSupport.firePropertyChange("name", this.name, this.name = name);
    }

    // dynamic properties
    public Object get(String property) {
        return dynamicProperties.get(property);
    }

    public void set(String property, Object newValue) {
        dynamicProperties.put(property, newValue);
    }

    public static String dummyStaticMethod(String text) {
        return text.toUpperCase();
    }

    public boolean equals(Object that) {
        if (that instanceof DummyBean) {
            return equals((DummyBean) that);
        }
        return false;
    }

    public boolean equals(DummyBean that) {
        return this.name.equals(that.name) && this.i.equals(that.i);
    }

    public String toString() {
        return super.toString() + "[name=" + name + ";i=" + i + "]";
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        changeSupport.firePropertyChange("point", this.point, this.point = point);
    }

}
