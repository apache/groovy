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

package groovy.util;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Mix of BuilderSupport and SwingBuilder's factory support.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Andres Almiray <aalmiray@users.sourceforge.com>
 */
public abstract class FactoryBuilderSupport extends GroovyObjectSupport {
    public static final String CURRENT_FACTORY = "_CURRENT_FACTORY_";
    public static final String CURRENT_NODE = "_CURRENT_NODE_";
    private static final Logger log = Logger.getLogger( FactoryBuilderSupport.class.getName() );

    /**
     * Throws an exception if value is null.
     */
    public static void checkValueIsNull( Object value, Object name ) {
        if( value != null ){
            throw new RuntimeException( "'" + name + "' elements do not accept a value argument." );
        }
    }

    /**
     * Returns true if type is assignale to the value's class, false if value is
     * null.
     */
    public static boolean checkValueIsType( Object value, Object name, Class type ) {
        if( value != null ){
            if( type.isAssignableFrom( value.getClass() ) ){
                return true;
            }else{
                throw new RuntimeException( "The value argument of '" + name + "' must be of type "
                        + type.getName() );
            }
        }else{
            return false;
        }
    }

    /**
     * Returns true if type is assignale to the value's class, false if value is
     * null or a String.
     */
    public static boolean checkValueIsTypeNotString( Object value, Object name, Class type ) {
        if( value != null ){
            if( type.isAssignableFrom( value.getClass() ) ){
                return true;
            }else if( value instanceof String ){
                return false;
            }else{
                throw new RuntimeException( "The value argument of '" + name + "' must be of type "
                        + type.getName() + " or a String." );
            }
        }else{
            return false;
        }
    }

    private Stack contexts = new Stack();
    private Map factories = new HashMap();
    private Closure nameMappingClosure;
    private FactoryBuilderSupport proxyBuilder;

    public FactoryBuilderSupport() {
        this.proxyBuilder = this;
    }

    public FactoryBuilderSupport( Closure nameMappingClosure ) {
        this.nameMappingClosure = nameMappingClosure;
        this.proxyBuilder = this;
    }

    public Map getContext() {
        if( !contexts.isEmpty() ){
            return (Map) contexts.peek();
        }
        return null;
    }

    public Object getCurrent() {
        if( !contexts.isEmpty() ){
            Map context = (Map) contexts.peek();
            return context.get( CURRENT_NODE );
        }
        return null;
    }

    public Factory getCurrentFactory() {
        if( !contexts.isEmpty() ){
            Map context = (Map) contexts.peek();
            return (Factory) context.get( CURRENT_FACTORY );
        }
        return null;
    }

    /**
     * Convenience method when no arguments are required
     *
     * @return the result of the call
     * @param methodName the name of the method to invoke
     */
    public Object invokeMethod( String methodName ) {
        return invokeMethod( methodName, null );
    }

    public Object invokeMethod( String methodName, Object args ) {
        Object name = getName( methodName );
        return doInvokeMethod( methodName, name, args );
    }

    public void registerBeanFactory( String theName, final Class beanClass ) {
        registerFactory( theName, new AbstractFactory(){
            public Object newInstance( FactoryBuilderSupport builder, Object name, Object value,
                    Map properties ) throws InstantiationException, IllegalAccessException {
                if( checkValueIsTypeNotString( value, name, beanClass ) ){
                    return value;
                }else{
                    return beanClass.newInstance();
                }
            }
        } );
    }

    public void registerFactory( String name, Factory factory ) {
        factories.put( name, factory );
    }

    protected Object createNode( Object name, Map attributes, Object value ) {
        Object node = null;

        Factory factory = (Factory) factories.get( name );
        if( factory == null ){
            log.log( Level.WARNING, "Could not find match for name '" + name + "'" );
            return null;
        }
        getContext().put( CURRENT_FACTORY, factory );
        preInstantiate( name, attributes, value );
        try{
            node = factory.newInstance( this, name, value, attributes );
            if( node == null ){
                log.log( Level.WARNING, "GraphicsOperation for name '" + name + "' returned null" );
                return null;
            }

            if( log.isLoggable( Level.FINE ) ){
                log.fine( "For name: " + name + " created widget: " + node );
            }
        }catch( Exception e ){
            throw new RuntimeException( "Failed to create component for '" + name + "' reason: "
                    + e, e );
        }
        postIstantiate( name, attributes, node );
        handleNodeAttributes( node, attributes );
        return node;
    }

    protected Object doInvokeMethod( String methodName, Object name, Object args ) {
        Object node = null;
        Closure closure = null;
        List list = InvokerHelper.asList( args );

        if( contexts.isEmpty() ){
            // should be called on first build method only
            newContext();
        }
        switch( list.size() ){
            case 0:
                node = proxyBuilder.createNode( name, Collections.EMPTY_MAP, null );
                break;
            case 1: {
                Object object = list.get( 0 );
                if( object instanceof Map ){
                    node = proxyBuilder.createNode( name, (Map) object, null );
                }else if( object instanceof Closure ){
                    closure = (Closure) object;
                    node = proxyBuilder.createNode( name, Collections.EMPTY_MAP, null );
                }else{
                    node = proxyBuilder.createNode( name, Collections.EMPTY_MAP, object );
                }
            }
                break;
            case 2: {
                Object object1 = list.get( 0 );
                Object object2 = list.get( 1 );
                if( object1 instanceof Map ){
                    if( object2 instanceof Closure ){
                        closure = (Closure) object2;
                        node = proxyBuilder.createNode( name, (Map) object1, null );
                    }else{
                        node = proxyBuilder.createNode( name, (Map) object1, object2 );
                    }
                }else{
                    if( object2 instanceof Closure ){
                        closure = (Closure) object2;
                        node = proxyBuilder.createNode( name, Collections.EMPTY_MAP, object1 );
                    }else if( object2 instanceof Map ){
                        node = proxyBuilder.createNode( name, (Map) object2, object1 );
                    }else{
                        throw new MissingMethodException( name.toString(), getClass(),
                                list.toArray(), false );
                    }
                }
            }
                break;
            case 3: {
                Object arg0 = list.get( 0 );
                Object arg1 = list.get( 1 );
                Object arg2 = list.get( 2 );
                if( arg0 instanceof Map && arg2 instanceof Closure ){
                    closure = (Closure) arg2;
                    node = proxyBuilder.createNode( name, (Map) arg0, arg1 );
                }else if( arg1 instanceof Map && arg2 instanceof Closure ){
                    closure = (Closure) arg2;
                    node = proxyBuilder.createNode( name, (Map) arg1, arg0 );
                }else{
                    throw new MissingMethodException( name.toString(), getClass(), list.toArray(),
                            false );
                }
            }
                break;
            default: {
                throw new MissingMethodException( name.toString(), getClass(), list.toArray(),
                        false );
            }

        }

        if( node == null ){
            if( contexts.size() == 1 ){
                // pop the first context
                popContext();
            }
            return node;
        }

        Object current = getCurrent();
        if( current != null ){
            proxyBuilder.setParent( current, node );
        }

        if( closure != null ){
            if( getCurrentFactory().isLeaf() ){
                throw new RuntimeException( "'" + name + "' doesn't support nesting." );
            }
            // push new node on stack
            newContext();
            getContext().put( CURRENT_NODE, node );
            // lets register the builder as the delegate
            setClosureDelegate( closure, node );
            closure.call();
            popContext();
        }

        proxyBuilder.nodeCompleted( current, node );
        if( contexts.size() == 1 ){
            // pop the first context
            popContext();
        }
        return proxyBuilder.postNodeCompletion( current, node );
    }

    /**
     * A hook to allow names to be converted into some other object such as a
     * QName in XML or ObjectName in JMX.
     *
     * @param methodName the name of the desired method
     * @return the object representing the name
     */
    protected Object getName( String methodName ) {
        if( nameMappingClosure != null ){
            return nameMappingClosure.call( methodName );
        }
        return methodName;
    }

    protected FactoryBuilderSupport getProxyBuilder() {
        return proxyBuilder;
    }

    protected void handleNodeAttributes( Object node, Map attributes ) {
        // first, short circuit
        if( attributes.isEmpty() || (node == null) ){
            return;
        }

        if( getCurrentFactory().onHandleNodeAttributes( this, node, attributes ) ){
            setNodeAttributes( node, attributes );
        }
    }

    protected void newContext() {
        contexts.push( new HashMap() );
    }

    /**
     * A hook to allow nodes to be processed once they have had all of their
     * children applied.
     *
     * @param node the current node being processed
     * @param parent the parent of the node being processed
     */
    protected void nodeCompleted( Object parent, Object node ) {
        getCurrentFactory().onNodeCompleted( this, parent, node );
    }

    protected Map popContext() {
        return (Map) contexts.pop();
    }

    /**
     * A hook after the factory creates the node and before attributes are set
     */
    protected void postIstantiate( Object name, Map attributes, Object node ) {
    }

    /**
     * A hook to allow nodes to be processed once they have had all of their
     * children applied and allows the actual node object that represents the
     * Markup element to be changed
     *
     * @param node the current node being processed
     * @param parent the parent of the node being processed
     * @return the node, possibly new, that represents the markup element
     */
    protected Object postNodeCompletion( Object parent, Object node ) {
        return node;
    }

    /**
     * A hook before the factory creates the node
     */
    protected void preInstantiate( Object name, Map attributes, Object value ) {
    }

    /**
     * A strategy method to allow derived builders to use builder-trees and
     * switch in different kinds of builders. This method should call the
     * setDelegate() method on the closure which by default passes in this but
     * if node is-a builder we could pass that in instead (or do something wacky
     * too)
     *
     * @param closure the closure on which to call setDelegate()
     * @param node the node value that we've just created, which could be a
     *        builder
     */
    protected void setClosureDelegate( Closure closure, Object node ) {
        closure.setDelegate( this );
    }

    /**
     * Maps attributes key/values to properties on node.
     */
    protected void setNodeAttributes( Object node, Map attributes ) {
        // set the properties
        for( Iterator iter = attributes.entrySet()
                .iterator(); iter.hasNext(); ){
            Map.Entry entry = (Map.Entry) iter.next();
            String property = entry.getKey()
                    .toString();
            Object value = entry.getValue();
            InvokerHelper.setProperty( node, property, value );
        }
    }

    protected void setParent( Object parent, Object child ) {
        getCurrentFactory().setParent( this, parent, child );
    }

    protected void setProxyBuilder( FactoryBuilderSupport proxyBuilder ) {
        this.proxyBuilder = proxyBuilder;
    }
}
