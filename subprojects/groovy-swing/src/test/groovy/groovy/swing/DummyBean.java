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
package groovy.swing;

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * A bean used by the test cases.
 */
public class DummyBean {

    private String name = "James";
    private Integer i = Integer.valueOf(123);
    private Map<String, Object> dynamicProperties = new HashMap<>();
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
