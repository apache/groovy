import groovy.test.GroovyTestCase

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
class SwingASTTransformsTest extends GroovyTestCase {
    void testBindable() {
        assertScript '''// tag::bindable_on_class[]
import groovy.beans.Bindable

@Bindable
class Person {
    String name
    int age
}
// end::bindable_on_class[]
/*
// tag::bindable_on_class_equiv[]
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport

class Person {
    final private PropertyChangeSupport this$propertyChangeSupport

    String name
    int age

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this$propertyChangeSupport.addPropertyChangeListener(listener)
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        this$propertyChangeSupport.addPropertyChangeListener(name, listener)
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this$propertyChangeSupport.removePropertyChangeListener(listener)
    }

    public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
        this$propertyChangeSupport.removePropertyChangeListener(name, listener)
    }

    public void firePropertyChange(String name, Object oldValue, Object newValue) {
        this$propertyChangeSupport.firePropertyChange(name, oldValue, newValue)
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return this$propertyChangeSupport.getPropertyChangeListeners()
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String name) {
        return this$propertyChangeSupport.getPropertyChangeListeners(name)
    }
}
// end::bindable_on_class_equiv[]
*/
def p = new Person(name:'Jean-Luc Picard', age:51)
Set changed = []
p.addPropertyChangeListener { evt ->
    changed << evt.propertyName
}
p.name = 'Spock'
p.age = 145
assert changed == ['name', 'age'] as Set'''
    }

    void testBindableSingleProperty() {
        assertScript '''
// tag::bindable_single_property[]
import groovy.beans.Bindable

class Person {
    String name
    @Bindable int age
}
// end::bindable_single_property[]
def p = new Person(name:'Jean-Luc Picard', age:51)
Set changed = []
p.addPropertyChangeListener { evt ->
    changed << evt.propertyName
}
p.name = 'Spock'
p.age = 145
assert changed == ['age'] as Set
'''
    }

    void testListenerList() {
        assertScript '''import java.awt.event.ActionEvent

// tag::listenerlist_simple[]
import java.awt.event.ActionListener
import groovy.beans.ListenerList

class Component {
    @ListenerList
    List<ActionListener> listeners;
}
// end::listenerlist_simple[]

/*
// tag::listenerlist_simple_equiv[]
import java.awt.event.ActionEvent
import java.awt.event.ActionListener as ActionListener
import groovy.beans.ListenerList as ListenerList

public class Component {

    @ListenerList
    private List<ActionListener> listeners

    public void addActionListener(ActionListener listener) {
        if ( listener == null) {
            return
        }
        if ( listeners == null) {
            listeners = []
        }
        listeners.add(listener)
    }

    public void removeActionListener(ActionListener listener) {
        if ( listener == null) {
            return
        }
        if ( listeners == null) {
            listeners = []
        }
        listeners.remove(listener)
    }

    public ActionListener[] getActionListeners() {
        Object __result = []
        if ( listeners != null) {
            __result.addAll(listeners)
        }
        return (( __result ) as ActionListener[])
    }

    public void fireActionPerformed(ActionEvent param0) {
        if ( listeners != null) {
            ArrayList<ActionListener> __list = new ArrayList<ActionListener>(listeners)
            for (def listener : __list ) {
                listener.actionPerformed(param0)
            }
        }
    }
}
// end::listenerlist_simple_equiv[]
*/

def c = new Component()
c.addActionListener { evt ->
    assert evt.actionCommand == 'test'
}
c.fireActionPerformed(new ActionEvent(this, 0, 'test'))
'''

    }

    void testListenerListName() {
        assertScript '''import java.awt.event.ActionEvent

import java.awt.event.ActionListener
import groovy.beans.ListenerList

// tag::listenerlist_name[]
class Component {
    @ListenerList(name='item')
    List<ActionListener> listeners;
}
// end::listenerlist_name[]

def c = new Component()
c.addItem { evt ->
    assert evt.actionCommand == 'test'
}
c.fireActionPerformed(new ActionEvent(this, 0, 'test'))
'''

    }

    void testListenerListSynchronized() {
        assertScript '''import java.awt.event.ActionEvent

import java.awt.event.ActionListener
import groovy.beans.ListenerList

import java.lang.reflect.Modifier

// tag::listenerlist_synchronized[]
class Component {
    @ListenerList(synchronize = true)
    List<ActionListener> listeners;
}
// end::listenerlist_synchronized[]

def c = new Component()
c.addActionListener { evt ->
    assert evt.actionCommand == 'test'
}
c.fireActionPerformed(new ActionEvent(this, 0, 'test'))
assert Modifier.isSynchronized(Component.getDeclaredMethod('addActionListener', ActionListener).modifiers)
'''
    }

    void testVetoable() {
        assertScript '''// tag::vetoable_on_class[]
import groovy.beans.Vetoable

import java.beans.PropertyVetoException
import java.beans.VetoableChangeListener

@Vetoable
class Person {
    String name
    int age
}
// end::vetoable_on_class[]
/*
// tag::vetoable_on_class_equiv[]
public class Person {

    private String name
    private int age
    final private java.beans.VetoableChangeSupport this$vetoableChangeSupport

    public void addVetoableChangeListener(VetoableChangeListener listener) {
        this$vetoableChangeSupport.addVetoableChangeListener(listener)
    }

    public void addVetoableChangeListener(String name, VetoableChangeListener listener) {
        this$vetoableChangeSupport.addVetoableChangeListener(name, listener)
    }

    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        this$vetoableChangeSupport.removeVetoableChangeListener(listener)
    }

    public void removeVetoableChangeListener(String name, VetoableChangeListener listener) {
        this$vetoableChangeSupport.removeVetoableChangeListener(name, listener)
    }

    public void fireVetoableChange(String name, Object oldValue, Object newValue) throws PropertyVetoException {
        this$vetoableChangeSupport.fireVetoableChange(name, oldValue, newValue)
    }

    public VetoableChangeListener[] getVetoableChangeListeners() {
        return this$vetoableChangeSupport.getVetoableChangeListeners()
    }

    public VetoableChangeListener[] getVetoableChangeListeners(String name) {
        return this$vetoableChangeSupport.getVetoableChangeListeners(name)
    }

    public void setName(String value) throws PropertyVetoException {
        this.fireVetoableChange('name', name, value)
        name = value
    }

    public void setAge(int value) throws PropertyVetoException {
        this.fireVetoableChange('age', age, value)
        age = value
    }
}
// end::vetoable_on_class_equiv[]
*/
def p = new Person(name:'Jean-Luc Picard', age:51)
Set changed = []
p.addVetoableChangeListener { evt ->
    changed << evt.propertyName
}
p.name = 'Spock'
p.age = 145
assert changed == ['name', 'age'] as Set'''
    }

    void testVetoableSingleProperty() {
        assertScript '''
// tag::vetoable_single_property[]
import groovy.beans.Vetoable

class Person {
    String name
    @Vetoable int age
}
// end::vetoable_single_property[]
def p = new Person(name:'Jean-Luc Picard', age:51)
Set changed = []
p.addVetoableChangeListener { evt ->
    changed << evt.propertyName
}
p.name = 'Spock'
p.age = 145
assert changed == ['age'] as Set
'''
    }

}
