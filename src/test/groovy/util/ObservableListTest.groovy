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

package groovy.util

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

/**
 * @author <a href="mailto:aalmiray@users.sourceforge.net">Andres Almiray</a>
 */
class ObservableListTest extends GroovyTestCase {
   void testFireEvent_add_withoutTest(){
      def list = new ObservableList()
      def listener = new SampleListPropertyChangeListener()
      list.addPropertyChangeListener( listener )

      def value1 = 'value1'
      def value2 = 'value2'

      list << value1
      assertNotNull( listener.event )
      assertEquals( list, listener.event.source )
      assertNull( listener.event.oldValue )
      assertEquals( value1, listener.event.newValue )

      list << value2
      assertNotNull( listener.event )
      assertEquals( list, listener.event.source )
      assertNull( listener.event.oldValue )
      assertEquals( value2, listener.event.newValue )

      listener.event = null
      list[1] = value1
      assertNotNull( listener.event )
      assertEquals( list, listener.event.source )
      assertEquals( value2, listener.event.oldValue )
      assertEquals( value1, listener.event.newValue )
      assertEquals( 1, listener.event.index )

      listener.event = null
      list[0] = value1
      assertNull( listener.event )
   }

   void testFireEvent_remove(){
      def list = new ObservableList()
      def listener = new SampleListPropertyChangeListener()
      list.addPropertyChangeListener( listener )

      def value1 = 'value1'
      def value2 = 'value2'

      list << value1
      assertNotNull( listener.event )
      assertEquals( list, listener.event.source )
      assertNull( listener.event.oldValue )
      assertEquals( value1, listener.event.newValue )

      list << value2
      assertNotNull( listener.event )
      assertEquals( list, listener.event.source )
      assertNull( listener.event.oldValue )
      assertEquals( value2, listener.event.newValue )

      list.remove(value2)
      assertNotNull( listener.event )
      assertTrue( listener.event instanceof ObservableList.ElementRemovedEvent )
      assertEquals( list, listener.event.source )
      assertEquals( value2, listener.event.newValue )
      assertEquals( 1, listener.event.index )
      
      list.remove(value1)
      assertNotNull( listener.event )
      assertTrue( listener.event instanceof ObservableList.ElementRemovedEvent )
      assertEquals( list, listener.event.source )
      assertEquals( value1, listener.event.newValue )
      assertEquals( 0, listener.event.index )
   }

   void testFireEvent_clear(){
      def list = new ObservableList()
      def listener = new SampleListPropertyChangeListener()
      list.addPropertyChangeListener( listener )

      def value1 = 'value1'
      def value2 = 'value2'
      list << value1
      list << value2
      list.clear()

      assertNotNull( listener.event )
      assertTrue( listener.event instanceof ObservableList.ElementClearedEvent )
      assertEquals( list, listener.event.source )
      def values = listener.event.values
      assertNotNull( values )
      assertEquals( 2, values.size() )
      assertEquals( value1, values[0] )
      assertEquals( value2, values[1] )
   }

   void testFireEvent_addAll(){
      def list = new ObservableList()
      def listener = new SampleListPropertyChangeListener()
      list.addPropertyChangeListener( listener )

      def value1 = 'value1'
      def value2 = 'value2'
      list << value1
      list.addAll( [value1, value2] )

      assertNotNull( listener.event )
      assertTrue( listener.event instanceof ObservableList.MultiElementAddedEvent )
      assertEquals( list, listener.event.source )
      def values = listener.event.values
      assertNotNull( values )
      assertEquals( 2, values.size() )
      assertEquals( value1, values[0] )
      assertEquals( value2, values[1] )
   }
   
   void testFireEvent_removeAll(){
      def list = new ObservableList()
      def listener = new SampleListPropertyChangeListener()
      list.addPropertyChangeListener( listener )

      def value1 = 'value1'
      def value2 = 'value2'
      list << value1
      list << value2
      list.removeAll( [value2] )

      assertNotNull( listener.event )
      assertTrue( listener.event instanceof ObservableList.MultiElementRemovedEvent )
      assertEquals( list, listener.event.source )
      def values = listener.event.values
      assertNotNull( values )
      assertEquals( 1, values.size() )
      assertEquals( value2, values[0] )
   }
   
   void testFireEvent_retainAll(){
      def list = new ObservableList()
      def listener = new SampleListPropertyChangeListener()
      list.addPropertyChangeListener( listener )

      def value1 = 'value1'
      def value2 = 'value2'
      def value3 = 'value3'
      list << value1
      list << value2
      list << value3
      list.retainAll( [value2] )

      assertNotNull( listener.event )
      assertTrue( listener.event instanceof ObservableList.MultiElementRemovedEvent )
      assertEquals( list, listener.event.source )
      def values = listener.event.values
      assertNotNull( values )
      assertEquals( 2, values.size() )
      assertEquals( value1, values[0] )
      assertEquals( value3, values[1] )
   }

   void testFireEvent_withTest(){
      def list = new ObservableList( { !(it instanceof String) } )
      def listener = new SampleListPropertyChangeListener()
      list.addPropertyChangeListener( listener )

      def value1 = 1
      def value2 = 'value2'
      list << value1
      assertNotNull( listener.event )
      assertEquals( list, listener.event.source )
      assertNull( listener.event.oldValue )
      assertEquals( value1, listener.event.newValue )

      listener.event = null
      list << value2
      assertNull( listener.event )
   }
}

class SampleListPropertyChangeListener implements PropertyChangeListener {
   PropertyChangeEvent event

   public void propertyChange( PropertyChangeEvent evt ){
      event = evt
   }
}
