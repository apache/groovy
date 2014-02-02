package groovy.json.internal;

import java.lang.reflect.Array;
import java.util.*;

/**
 * This maps only builds once you ask for a key for the first time.
 * It is designed to not incur the overhead of creating a map unless needed.
 */
public class LazyMap extends AbstractMap<String, Object> {


    /* Holds the actual map that will be lazily created. */
    private Map<String, Object> map;
    /* The size of the map. */
    private int size;
    /* The keys  stored in the map. */
    private String[] keys;
    /* The values stored in the map. */
    private Object[] values;



    public LazyMap() {
        keys = new String[ 5 ];
        values = new Object[ 5 ];
    }

    public LazyMap( int initialSize ) {
        keys = new String[ initialSize ];
        values = new Object[ initialSize ];

    }

    public Object put( String key, Object value ) {
        if ( map == null ) {
            keys[ size ] = key;
            values[ size ] = value;
            size++;
            if ( size == keys.length ) {
                keys = grow( keys );
                values = grow( values );
            }
            return null;
        } else {
            return map.put( key, value );
        }
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        buildIfNeeded();
        return map.entrySet();
    }

    @Override
    public int size() {
        if ( map == null ) {
            return size;
        } else {
            return map.size();
        }
    }

    @Override
    public boolean isEmpty() {
        if ( map == null ) {
            return size == 0;
        } else {
            return map.isEmpty();
        }
    }

    @Override
    public boolean containsValue( Object value ) {
        buildIfNeeded();
        return map.containsValue( value );
    }

    @Override
    public boolean containsKey( Object key ) {
        buildIfNeeded();
        return map.containsKey( key );
    }

    @Override
    public Object get( Object key ) {
        buildIfNeeded();
        return map.get( key );
    }

    private void buildIfNeeded() {
        if ( map == null ) {
            map = new LinkedHashMap<String, Object> ( size, 0.01f );
            for ( int index = 0; index < size; index++ ) {
                map.put( keys[ index ], values[ index ] );
            }
            this.keys = null;
            this.values = null;
        }
    }

    @Override
    public Object remove( Object key ) {
        buildIfNeeded();
        return map.remove( key );
    }

    @Override
    public void putAll( Map m ) {
        buildIfNeeded();
        map.putAll( m );
    }

    @Override
    public void clear() {
        if ( map == null ) {
            size = 0;
        } else {
            map.clear();
        }
    }

    @Override
    public Set<String> keySet() {
        buildIfNeeded();
        return map.keySet();

    }

    @Override
    public Collection<Object> values() {

        buildIfNeeded();

        return map.values();

    }

    @Override
    public boolean equals( Object o ) {
        buildIfNeeded();
        return map.equals( o );
    }

    @Override
    public int hashCode() {
        buildIfNeeded();
        return map.hashCode();
    }

    @Override
    public String toString() {

        buildIfNeeded();
        return map.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {

        if ( map == null ) {
            return null;
        } else {
            if (map instanceof LinkedHashMap)  {
                return ((LinkedHashMap)map).clone();
            } else {
                return new LinkedHashMap (this);
            }
        }
    }

    public LazyMap clearAndCopy() {
        LazyMap map = new LazyMap ();
        for ( int index = 0; index < size; index++ ) {
            map.put( keys[ index ], values[ index ] );
        }
        size = 0;
        return map;
    }

    public static <V> V[] grow( V[] array ) {
        Objects.requireNonNull( array );
        Object newArray = Array.newInstance( array.getClass().getComponentType(),
                array.length * 2 );
        System.arraycopy( array, 0, newArray, 0, array.length );
        return ( V[] ) newArray;
    }

}

