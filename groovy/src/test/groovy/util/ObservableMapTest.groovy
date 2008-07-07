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
class ObservableMapTest extends GroovyTestCase {
   void testFireEvent_withoutTest(){
      def map = new ObservableMap()
      def listener = new SamplePropertyChangeListener()
      map.addPropertyChangeListener( listener )

      def key = 'key'
      def value1 = 'value1'
      def value2 = 'value2'
      map[key] = null
      assertNotNull( listener.event )
      assertTrue( listener.event instanceof ObservableMap.PropertyAddedEvent )
      assertEquals( key, listener.event.propertyName )
      assertNull( listener.event.newValue )
      assertNull( listener.event.oldValue )

      map[key] = value1
      assertNotNull( listener.event )
      assertEquals( map, listener.event.source )
      assertEquals( key, listener.event.propertyName )
      assertNull( listener.event.oldValue )
      assertEquals( value1, listener.event.newValue )

      map[key] = value2
      assertNotNull( listener.event )
      assertEquals( map, listener.event.source )
      assertEquals( key, listener.event.propertyName )
      assertEquals( value1, listener.event.oldValue )
      assertEquals( value2, listener.event.newValue )

      listener.event = null
      map[key] = value2
      assertNull( listener.event )

   }

   void testFireEvent_removeKey(){
      def map = new ObservableMap()
      def listener = new SamplePropertyChangeListener()
      map.addPropertyChangeListener( listener )

      def key = 'key'
      def value1 = 'value1'
      def value2 = 'value2'
      map[key] = null
      assertNotNull( listener.event )
      assertTrue( listener.event instanceof ObservableMap.PropertyAddedEvent )
      assertEquals( key, listener.event.propertyName )
      assertNull( listener.event.newValue )

      map[key] = value1
      assertNotNull( listener.event )
      assertEquals( map, listener.event.source )
      assertEquals( key, listener.event.propertyName )
      assertNull( listener.event.oldValue )
      assertEquals( value1, listener.event.newValue )

      map.remove(key)
      assertNotNull( listener.event )
      assertTrue( listener.event instanceof ObservableMap.PropertyRemovedEvent )
      assertEquals( map, listener.event.source )
      assertEquals( key, listener.event.propertyName )
      assertEquals( value1, listener.event.oldValue )
   }

   void testFireEvent_clearMap(){
      def map = new ObservableMap()
      def listener = new SamplePropertyChangeListener()
      map.addPropertyChangeListener( listener )

      def key1 = 'key1'
      def key2 = 'key2'
      def value1 = 'value1'
      def value2 = 'value2'
      map[key1] = value1
      map[key2] = value2
      map.clear()

      assertNotNull( listener.event )
      assertTrue( listener.event instanceof ObservableMap.PropertyClearedEvent )
      assertEquals( map, listener.event.source )
      def values = listener.event.values
      assertNotNull( values )
      assertEquals( 2, values.size() )
      assertEquals( value1, values[key1] )
      assertEquals( value2, values[key2] )
   }

   void testFireEvent_putAll(){
      def map = new ObservableMap()
      def listener = new SamplePropertyChangeListener()
      map.addPropertyChangeListener( listener )

      def key1 = 'key1'
      def key2 = 'key2'
      def value1 = 'value1'
      def value2 = 'value2'
      map[key1] = null
      map.putAll( [key1: value1, key2: value2] )

      assertNotNull( listener.event )
      assertTrue( listener.event instanceof ObservableMap.MultiPropertyEvent )
      assertEquals( map, listener.event.source )
      assertEquals( 2, listener.event.events.size() )
      assertTrue( listener.event.events[0] instanceof ObservableMap.PropertyUpdatedEvent )
      assertEquals( key1, listener.event.events[0].propertyName )
      assertEquals( value1, listener.event.events[0].newValue )
      assertTrue( listener.event.events[1] instanceof ObservableMap.PropertyAddedEvent )
      assertEquals( key2, listener.event.events[1].propertyName )
      assertEquals( value2, listener.event.events[1].newValue )
   }

   void testFireEvent_withTest(){
      def map = new ObservableMap( { it != 'value2' } )
      def listener = new SamplePropertyChangeListener()
      map.addPropertyChangeListener( listener )

      def key = 'key'
      def value1 = 'value1'
      def value2 = 'value2'
      map[key] = value1
      assertNotNull( listener.event )
      assertEquals( map, listener.event.source )
      assertEquals( key, listener.event.propertyName )
      assertEquals( value1, listener.event.newValue )

      listener.event = null
      map[key] = value2
      assertNull( listener.event )
   }

   void testFireEvent_withTestOnKey(){
      def map = new ObservableMap( { name, value -> name != 'key' } )
      def listener = new SamplePropertyChangeListener()
      map.addPropertyChangeListener( listener )

      def key = 'key'
      def value1 = 'value1'
      def value2 = 'value2'
      map[key] = value1
      assertNull( listener.event )
      map[key] = value2
      assertNull( listener.event )

      map['key2'] = value1
      assertNotNull( listener.event )
      assertEquals( map, listener.event.source )
      assertEquals( 'key2', listener.event.propertyName )
      assertEquals( value1, listener.event.newValue )
   }
}

class SamplePropertyChangeListener implements PropertyChangeListener {
   PropertyChangeEvent event

   public void propertyChange( PropertyChangeEvent evt ){
      event = evt
   }
}
