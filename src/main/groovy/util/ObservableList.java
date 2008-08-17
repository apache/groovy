/*
 * Copyright 2003-2008 the original author or authors.
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

package groovy.util;

import groovy.lang.Closure;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * List decorator that will trigger PropertyChangeEvents when a value changes.<br>
 * An optional Closure may be specified and will work as a filter, if it returns true the property
 * will trigger an event (if the value indeed changed), otherwise it won't. The Closure may receive
 * 1 or 2 parameters, the single one being the value, the other one both the key and value, for
 * example:
 * 
 * <pre>
 * // skip all properties whose value is a closure
 * def map = new ObservableList( {!(it instanceof Closure)} )
 * &lt;p/&gt;
 * // skip all properties whose name matches a regex
 * def map = new ObservableList( { name, value -&gt; !(name =&tilde; /[A-Z+]/) } )
 * </pre>
 * 
 * <p>
 * The current implementation will trigger specialized events in the following scenarios, you need
 * not register a different listener as those events extend from PropertyChangeEvent
 * <ul>
 * <li>ObservableList.ElementAddedEvent - a new element is added to the list</li>
 * <li>ObservableList.ElementRemovedEvent - a element is removed from the list</li>
 * <li>ObservableList.ElementUpdatedEvent - a element changes value (same as regular
 * PropertyChangeEvent)</li>
 * <li>ObservableList.ElementClearedEvent - all elements have been removed from the list</li>
 * <li>ObservableList.MultiElementAddedEvent - triggered by calling list.addAll()</li>
 * <li>ObservableList.MultiElementRemoveedEvent - triggered by calling
 * list.removeAll()/list.retainAll()</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:aalmiray@users.sourceforge.net">Andres Almiray</a>
 */
public class ObservableList implements List {
   private List delegate;
   private PropertyChangeSupport pcs;
   private Closure test;

   public ObservableList() {
      this( new ArrayList(), null );
   }

   public ObservableList( List delegate ) {
      this( delegate, null );
   }

   public ObservableList( Closure test ) {
      this( new ArrayList(), test );
   }

   public ObservableList( List delegate, Closure test ) {
      this.delegate = delegate;
      this.test = test;
      pcs = new PropertyChangeSupport( this );
   }

   public void add( int index, Object element ) {
      delegate.add( index, element );
      if( test != null ) {
         Object result = test.call( element );
         if( result != null && result instanceof Boolean && ((Boolean) result).booleanValue() ) {
            pcs.firePropertyChange( new ElementAddedEvent( this, element, index ) );
         }
      } else {
         pcs.firePropertyChange( new ElementAddedEvent( this, element, index ) );
      }
   }

   public boolean add( Object o ) {
      boolean success = delegate.add( o );
      if( success ) {
         if( test != null ) {
            Object result = test.call( o );
            if( result != null && result instanceof Boolean && ((Boolean) result).booleanValue() ) {
               pcs.firePropertyChange( new ElementAddedEvent( this, o, size() - 1 ) );
            }
         } else {
            pcs.firePropertyChange( new ElementAddedEvent( this, o, size() - 1 ) );
         }
      }
      return success;
   }

   public boolean addAll( Collection c ) {
      int index = size() - 1;
      index = index < 0 ? 0 : index;

      boolean success = delegate.addAll( c );
      if( success && c != null ) {
         List values = new ArrayList();
         for( Iterator i = c.iterator(); i.hasNext(); ) {
            Object element = i.next();
            if( test != null ) {
               Object result = test.call( element );
               if( result != null && result instanceof Boolean && ((Boolean) result).booleanValue() ) {
                  values.add( element );
               }
            } else {
               values.add( element );
            }
         }
         if( values.size() > 0 ) {
            pcs.firePropertyChange( new MultiElementAddedEvent( this, index, values ) );
         }
      }

      return success;
   }

   public boolean addAll( int index, Collection c ) {
      boolean success = delegate.addAll( index, c );

      if( success && c != null ) {
         List values = new ArrayList();
         for( Iterator i = c.iterator(); i.hasNext(); ) {
            Object element = i.next();
            if( test != null ) {
               Object result = test.call( element );
               if( result != null && result instanceof Boolean && ((Boolean) result).booleanValue() ) {
                  values.add( element );
               }
            } else {
               values.add( element );
            }
         }
         if( values.size() > 0 ) {
            pcs.firePropertyChange( new MultiElementAddedEvent( this, index, values ) );
         }
      }

      return success;
   }

   public void clear() {
      List values = new ArrayList();
      values.addAll( delegate );
      delegate.clear();
      if( !values.isEmpty() ) {
         pcs.firePropertyChange( new ElementClearedEvent( this, values ) );
      }
   }

   public boolean contains( Object o ) {
      return delegate.contains( o );
   }

   public boolean containsAll( Collection c ) {
      return delegate.containsAll( c );
   }

   public boolean equals( Object o ) {
      return delegate.equals( o );
   }

   public Object get( int index ) {
      return delegate.get( index );
   }

   public int hashCode() {
      return delegate.hashCode();
   }

   public int indexOf( Object o ) {
      return delegate.indexOf( o );
   }

   public boolean isEmpty() {
      return delegate.isEmpty();
   }

   public Iterator iterator() {
      return new ObservableIterator( delegate.iterator() );
   }

   public int lastIndexOf( Object o ) {
      return delegate.lastIndexOf( o );
   }

   public ListIterator listIterator() {
      return new ObservableListIterator( delegate.listIterator(), 0 );
   }

   public ListIterator listIterator( int index ) {
      return new ObservableListIterator( delegate.listIterator( index ), index );
   }

   public Object remove( int index ) {
      Object element = delegate.remove( index );
      pcs.firePropertyChange( new ElementRemovedEvent( this, element, index ) );
      return element;
   }

   public boolean remove( Object o ) {
      int index = delegate.indexOf( o );
      boolean success = delegate.remove( o );
      if( success ) {
         pcs.firePropertyChange( new ElementRemovedEvent( this, o, index ) );
      }
      return success;
   }

   public boolean removeAll( Collection c ) {
      if( c == null ) {
         return false;
      }
      
      List values = new ArrayList();
      if( c != null ) {
         for( Iterator i = c.iterator(); i.hasNext(); ) {
            Object element = i.next();
            if( delegate.contains( element ) ) {
               values.add( element );
            }
         }
      }

      boolean success = delegate.removeAll( c );
      if( success && !values.isEmpty() ) {
         pcs.firePropertyChange( new MultiElementRemovedEvent( this, values ) );
      }

      return success;
   }

   public boolean retainAll( Collection c ) {
      if( c == null ) {
         return false;
      }
      
      List values = new ArrayList();
      if( c != null ) {
         for( Iterator i = delegate.iterator(); i.hasNext(); ) {
            Object element = i.next();
            if( !c.contains( element ) ) {
               values.add( element );
            }
         }
      }

      boolean success = delegate.retainAll( c );
      if( success && !values.isEmpty() ) {
         pcs.firePropertyChange( new MultiElementRemovedEvent( this, values ) );
      }

      return success;
   }

   public Object set( int index, Object element ) {
      Object oldValue = delegate.set( index, element );
      if( test != null ) {
         Object result = test.call( element );
         if( result != null && result instanceof Boolean && ((Boolean) result).booleanValue() ) {
            pcs.firePropertyChange( new ElementUpdatedEvent( this, oldValue, element, index ) );
         }
      } else {
         pcs.firePropertyChange( new ElementUpdatedEvent( this, oldValue, element, index ) );
      }
      return oldValue;
   }

   public int size() {
      return delegate.size();
   }

   public List subList( int fromIndex, int toIndex ) {
      return delegate.subList( fromIndex, toIndex );
   }

   public Object[] toArray() {
      return delegate.toArray();
   }

   public Object[] toArray( Object[] a ) {
      return delegate.toArray( a );
   }

   private class ObservableIterator implements Iterator {
      private Iterator iterDelegate;
      protected int cursor = 0;

      public ObservableIterator( Iterator iterDelegate ) {
         this.iterDelegate = iterDelegate;
      }

      public Iterator getDelegate() {
         return iterDelegate;
      }

      public boolean hasNext() {
         return iterDelegate.hasNext();
      }

      public Object next() {
         cursor++;
         return iterDelegate.next();
      }

      public void remove() {
         ObservableList.this.remove( cursor-- );
      }
   }

   private class ObservableListIterator extends ObservableIterator implements ListIterator {
      public ObservableListIterator( ListIterator iterDelegate, int index ) {
         super( iterDelegate );
         cursor = index;
      }

      public ListIterator getListIterator() {
         return (ListIterator) getDelegate();
      }

      public void add( Object o ) {
         ObservableList.this.add( o );
         cursor++;
      }

      public boolean hasPrevious() {
         return getListIterator().hasPrevious();
      }

      public int nextIndex() {
         return getListIterator().nextIndex();
      }

      public Object previous() {
         return getListIterator().previous();
      }

      public int previousIndex() {
         return getListIterator().previousIndex();
      }

      public void set( Object o ) {
         ObservableList.this.set( cursor, o );
      }
   }

   // observable interface

   public void addPropertyChangeListener( PropertyChangeListener listener ) {
      pcs.addPropertyChangeListener( listener );
   }

   public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener ) {
      pcs.addPropertyChangeListener( propertyName, listener );
   }

   public PropertyChangeListener[] getPropertyChangeListeners() {
      return pcs.getPropertyChangeListeners();
   }

   public PropertyChangeListener[] getPropertyChangeListeners( String propertyName ) {
      return pcs.getPropertyChangeListeners( propertyName );
   }

   public void removePropertyChangeListener( PropertyChangeListener listener ) {
      pcs.removePropertyChangeListener( listener );
   }

   public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener ) {
      pcs.removePropertyChangeListener( propertyName, listener );
   }

   public boolean hasListeners( String propertyName ) {
      return pcs.hasListeners( propertyName );
   }

   public abstract static class ElementEvent extends PropertyChangeEvent {
      public static final int ADDED = 0;
      public static final int UPDATED = 1;
      public static final int REMOVED = 2;
      public static final int CLEARED = 3;
      public static final int MULTI_ADD = 4;
      public static final int MULTI_REMOVE = 5;

      private static final String PROPERTY_NAME = "groovy_util_ObservableList__element";
      protected static final Object OLDVALUE = new Object();
      protected static final Object NEWVALUE = new Object();

      private int type;
      private int index;

      public ElementEvent( Object source, Object oldValue, Object newValue, int index, int type ) {
         super( source, PROPERTY_NAME, oldValue, newValue );
         switch( type ) {
            case ADDED:
            case UPDATED:
            case REMOVED:
            case CLEARED:
            case MULTI_ADD:
            case MULTI_REMOVE:
               this.type = type;
               break;
            default:
               this.type = UPDATED;
               break;
         }
         this.index = index;
      }

      public int getIndex() {
         return index;
      }
      
      public int getType() {
         return type;
      }

      public String getTypeAsString() {
         switch( type ) {
            case ADDED:
               return "ADDED";
            case UPDATED:
               return "UPDATED";
            case REMOVED:
               return "REMOVED";
            case CLEARED:
               return "CLEARED";
            case MULTI_ADD:
               return "MULTI_ADD";
            case MULTI_REMOVE:
               return "MULTI_REMOVE";
            default:
               return "UPDATED";
         }
      }
   }

   public static class ElementAddedEvent extends ElementEvent {
      public ElementAddedEvent( Object source, Object newValue, int index ) {
         super( source, null, newValue, index, ElementEvent.ADDED );
      }
   }

   public static class ElementUpdatedEvent extends ElementEvent {
      public ElementUpdatedEvent( Object source, Object oldValue, Object newValue, int index ) {
         super( source, oldValue, newValue, index, ElementEvent.UPDATED );
      }
   }

   public static class ElementRemovedEvent extends ElementEvent {
      public ElementRemovedEvent( Object source, Object newValue, int index ) {
         super( source, null, newValue, index, ElementEvent.REMOVED );
      }
   }

   public static class ElementClearedEvent extends ElementEvent {
      private List values = new ArrayList();

      public ElementClearedEvent( Object source, List values ) {
         super( source, OLDVALUE, NEWVALUE, ElementEvent.CLEARED, 0 );
         if( values != null ) {
            this.values.addAll( values );
         }
      }

      public List getValues() {
         return Collections.unmodifiableList( values );
      }
   }

   public static class MultiElementAddedEvent extends ElementEvent {
      private List values = new ArrayList();

      public MultiElementAddedEvent( Object source, int index, List values ) {
         super( source, OLDVALUE, NEWVALUE, ElementEvent.MULTI_ADD, index );
         if( values != null ) {
            this.values.addAll( values );
         }
      }

      public List getValues() {
         return Collections.unmodifiableList( values );
      }
   }

   public static class MultiElementRemovedEvent extends ElementEvent {
      private List values = new ArrayList();

      public MultiElementRemovedEvent( Object source, List values ) {
         super( source, OLDVALUE, NEWVALUE, ElementEvent.MULTI_ADD, 0 );
         if( values != null ) {
            this.values.addAll( values );
         }
      }

      public List getValues() {
         return Collections.unmodifiableList( values );
      }
   }
}