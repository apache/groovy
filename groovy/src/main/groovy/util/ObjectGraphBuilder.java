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
import groovy.lang.MetaProperty;
import groovy.lang.MissingPropertyException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * A builder for creating object graphs.<br>
 * Each node defines the class to be created and the property on its parent (if
 * any) at the same time.
 *
 * @author Scott Vlaminck (http://refactr.com)
 * @author Andres Almiray <aalmiray@users.sourceforge.com>
 */
public class ObjectGraphBuilder extends FactoryBuilderSupport {
    public static final String NODE_CLASS = "_NODE_CLASS_";
    public static final String NODE_NAME = "_NODE_NAME_";
    public static final String OBJECT_ID = "_OBJECT_ID_";
    public static final String LAZY_REF = "_LAZY_REF_";

    private ChildPropertySetter childPropertySetter;
    private ClassNameResolver classNameResolver;
    private IdentifierResolver identifierResolver;
    private NewInstanceResolver newInstanceResolver;
    private ObjectFactory objectFactory = new ObjectFactory();
    private ObjectRefFactory objectRefFactory = new ObjectRefFactory();
    private ReferenceResolver referenceResolver;
    private RelationNameResolver relationNameResolver;
    private Map/*<String,Class>*/ resolvedClasses = new HashMap/*<String,Class>*/();
    private ClassLoader classLoader;
    private boolean lazyReferencesAllowed = true;
    private List/*<NodeReference>*/ lazyReferences = new ArrayList/*<NodeReference>*/();

    public ObjectGraphBuilder() {
        classNameResolver = new DefaultClassNameResolver();
        newInstanceResolver = new DefaultNewInstanceResolver();
        relationNameResolver = new DefaultRelationNameResolver();
        childPropertySetter = new DefaultChildPropertySetter();
        identifierResolver = new DefaultIdentifierResolver();
        referenceResolver = new DefaultReferenceResolver();

        addPostNodeCompletionDelegate( new Closure(this,this){
            public void doCall( ObjectGraphBuilder builder, Object parent, Object node ) {
                if( parent == null ) {
                    builder.resolveLazyReferences();
                    builder.dispose();
                }
            }
        });
    }

    /**
     * Returns the current ChildPropertySetter.
     */
    public ChildPropertySetter getChildPropertySetter() {
        return childPropertySetter;
    }

    /**
     * Returns the classLoader used to load a node's class.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Returns the current ClassNameResolver.
     */
    public ClassNameResolver getClassNameResolver() {
        return classNameResolver;
    }

    /**
     * Returns the current NewInstanceResolver.
     */
    public NewInstanceResolver getNewInstanceResolver() {
        return newInstanceResolver;
    }

    /**
     * Returns the current RelationNameResolver.
     */
    public RelationNameResolver getRelationNameResolver() {
        return relationNameResolver;
    }

    /**
     * Returns true if references can be resolved lazily
     */
    public boolean isLazyReferencesAllowed() {
        return lazyReferencesAllowed;
    }

    /**
     * Sets the current ChildPropertySetter.<br>
     * It will assign DefaultChildPropertySetter if null.<br>
     * It accepts a ChildPropertySetter instance or a Closure.
     */
    public void setChildPropertySetter( final Object childPropertySetter ) {
        if( childPropertySetter instanceof ChildPropertySetter ){
            this.childPropertySetter = (ChildPropertySetter) childPropertySetter;
        }else if( childPropertySetter instanceof Closure ){
            final ObjectGraphBuilder self = this;
            this.childPropertySetter = new ChildPropertySetter(){
                public void setChild( Object parent, Object child, String parentName,
                        String propertyName ) {
                    Closure cls = (Closure) childPropertySetter;
                    cls.setDelegate(self);
                    cls.call( new Object[] { parent, child, parentName, propertyName } );
                }
            };
        }else{
            this.childPropertySetter = new DefaultChildPropertySetter();
        }
    }

    /**
     * Sets the classLoader used to load a node's class.
     */
    public void setClassLoader( ClassLoader classLoader ){
        this.classLoader = classLoader;
    }

    /**
     * Sets the current ClassNameResolver.<br>
     * It will assign DefaultClassNameResolver if null.<br>
     * It accepts a ClassNameResolver instance, a String or a Closure.
     */
    public void setClassNameResolver( final Object classNameResolver ) {
        if( classNameResolver instanceof ClassNameResolver ){
            this.classNameResolver = (ClassNameResolver) classNameResolver;
        }else if( classNameResolver instanceof String ){
            this.classNameResolver = new ClassNameResolver(){
                public String resolveClassname( String classname ) {
                    return classNameResolver + "." + classname.substring( 0, 1 )
                            .toUpperCase() + classname.substring( 1 );
                }
            };
        }else if( classNameResolver instanceof Closure ){
            final ObjectGraphBuilder self = this;
            this.classNameResolver = new ClassNameResolver(){
                public String resolveClassname( String classname ) {
                    Closure cls = (Closure) classNameResolver;
                    cls.setDelegate(self);
                    return (String) cls.call( new Object[] { classname } );
                }
            };
        }else{
            this.classNameResolver = new DefaultClassNameResolver();
        }
    }

    /**
     * Sets the current IdentifierResolver.<br>
     * It will assign DefaultIdentifierResolver if null.<br>
     * It accepts a IdentifierResolver instance, a String or a Closure.
     */
    public void setIdentifierResolver( final Object identifierResolver ) {
        if( identifierResolver instanceof IdentifierResolver ){
            this.identifierResolver = (IdentifierResolver) identifierResolver;
        }else if( identifierResolver instanceof String ){
            this.identifierResolver = new IdentifierResolver(){
                public String getIdentifierFor( String nodeName ) {
                    return (String) identifierResolver;
                }
            };
        }else if( identifierResolver instanceof Closure ){
            final ObjectGraphBuilder self = this;
            this.identifierResolver = new IdentifierResolver(){
                public String getIdentifierFor( String nodeName ) {
                    Closure cls = (Closure) identifierResolver;
                    cls.setDelegate(self);
                    return (String) cls.call( new Object[] { nodeName } );
                }
            };
        }else{
            this.identifierResolver = new DefaultIdentifierResolver();
        }
    }

    /**
     * Sets wether references can be resolved lazily or not.
     */
    public void setLazyReferencesAllowed( boolean lazyReferencesAllowed ) {
        this.lazyReferencesAllowed = lazyReferencesAllowed;
    }

    /**
     * Sets the current NewInstanceResolver.<br>
     * It will assign DefaultNewInstanceResolver if null.<br>
     * It accepts a NewInstanceResolver instance or a Closure.
     */
    public void setNewInstanceResolver( final Object newInstanceResolver ) {
        if( newInstanceResolver instanceof NewInstanceResolver ){
            this.newInstanceResolver = (NewInstanceResolver) newInstanceResolver;
        }else if( newInstanceResolver instanceof Closure ){
            final ObjectGraphBuilder self = this;
            this.newInstanceResolver = new NewInstanceResolver(){
                public Object newInstance( Class klass, Map attributes )
                        throws InstantiationException, IllegalAccessException {
                    Closure cls = (Closure) newInstanceResolver;
                    cls.setDelegate(self);
                    return cls.call( new Object[] { klass, attributes } );
                }
            };
        }else{
            this.newInstanceResolver = new DefaultNewInstanceResolver();
        }
    }

    /**
     * Sets the current ReferenceResolver.<br>
     * It will assign DefaultReferenceResolver if null.<br>
     * It accepts a ReferenceResolver instance, a String or a Closure.
     */
    public void setReferenceResolver( final Object referenceResolver ) {
        if( referenceResolver instanceof ReferenceResolver ){
            this.referenceResolver = (ReferenceResolver) referenceResolver;
        }else if( referenceResolver instanceof String ){
            this.referenceResolver = new ReferenceResolver(){
                public String getReferenceFor( String nodeName ) {
                    return (String) referenceResolver;
                }
            };
        }else if( referenceResolver instanceof Closure ){
            final ObjectGraphBuilder self = this;
            this.referenceResolver = new ReferenceResolver(){
                public String getReferenceFor( String nodeName ) {
                    Closure cls = (Closure) referenceResolver;
                    cls.setDelegate(self);
                    return (String) cls.call( new Object[] { nodeName } );
                }
            };
        }else{
            this.referenceResolver = new DefaultReferenceResolver();
        }
    }

    /**
     * Sets the current RelationNameResolver.<br>
     * It will assign DefaultRelationNameResolver if null.
     */
    public void setRelationNameResolver( RelationNameResolver relationNameResolver ) {
        this.relationNameResolver = relationNameResolver != null ? relationNameResolver
                : new DefaultRelationNameResolver();
    }

    protected void postInstantiate( Object name, Map attributes, Object node ) {
        super.postInstantiate( name, attributes, node );
        Map context = getContext();
        String objectId = (String) context.get( OBJECT_ID );
        if( objectId != null && node != null ){
            setVariable( objectId, node );
        }
    }

    protected void preInstantiate( Object name, Map attributes, Object value ) {
        super.preInstantiate( name, attributes, value );
        Map context = getContext();
        context.put( OBJECT_ID,
                attributes.remove( identifierResolver.getIdentifierFor( (String) name ) ) );
    }

    protected Factory resolveFactory( Object name, Map attributes, Object value ) {
        // let custom factories be resolved first
        Factory factory = super.resolveFactory( name, attributes, value );
        if( factory != null ){
            return factory;
        }
        if( attributes.get( referenceResolver.getReferenceFor( (String) name ) ) != null ){
            return objectRefFactory;
        }
        return objectFactory;
    }

    /**
     * Strategy for setting a child node on its parent.<br>
     * Useful for handling Lists/Arrays vs normal properties.
     */
    public interface ChildPropertySetter {
        /**
         * @param parent the parent's node value
         * @param child the child's node value
         * @param parentName the name of the parent node
         * @param propertyName the resolved relation name of the child
         */
        void setChild( Object parent, Object child, String parentName, String propertyName );
    }

    /**
     * Strategy for resolving a classname.
     */
    public interface ClassNameResolver {
        /**
         * @param classname the node name as written on the building code
         */
        String resolveClassname( String classname );
    }

    /**
     * Default impl that calls parent.propertyName = child<br>
     * If parent.propertyName is a Collection it will try to add child to the
     * collection.
     */
    public static class DefaultChildPropertySetter implements ChildPropertySetter {
        public void setChild( Object parent, Object child, String parentName, String propertyName ) {
            try {
                Object property = InvokerHelper.getProperty( parent, propertyName );
                if( property != null && Collection.class.isAssignableFrom( property.getClass() ) ){
                    ((Collection) property).add( child );
                } else {
                    InvokerHelper.setProperty( parent, propertyName, child );
                }
            } catch( MissingPropertyException mpe ) {
                // ignore
            }
        }
    }

    /**
     * Default impl that capitalizes the classname.
     */
    public static class DefaultClassNameResolver implements ClassNameResolver {
        public String resolveClassname( String classname ) {
            if( classname.length() == 1 ){
                return classname.toUpperCase();
            }
            return classname.substring( 0, 1 )
                    .toUpperCase() + classname.substring( 1 );
        }
    }

    /**
     * Default impl, always returns 'id'
     */
    public static class DefaultIdentifierResolver implements IdentifierResolver {
        public String getIdentifierFor( String nodeName ) {
            return "id";
        }
    }

    /**
     * Default impl that calls Class.newInstance()
     */
    public static class DefaultNewInstanceResolver implements NewInstanceResolver {
        public Object newInstance( Class klass, Map attributes ) throws InstantiationException,
                IllegalAccessException {
            return klass.newInstance();
        }
    }

    /**
     * Default impl, always returns 'refId'
     */
    public static class DefaultReferenceResolver implements ReferenceResolver {
        public String getReferenceFor( String nodeName ) {
            return "refId";
        }
    }

    /**
     * Default impl that returns parentName &amp; childName accordingly.
     */
    public static class DefaultRelationNameResolver implements RelationNameResolver {
        /**
         * Follow the most conventional plural in English, add 's' to childName.<br>
         * If the property does not exist then it will return childName
         * unchanged.
         */
        public String resolveChildRelationName( String parentName, Object parent, String childName,
                Object child ) {
            MetaProperty metaProperty = InvokerHelper.getMetaClass( parent )
                    .hasProperty( parent, childName + "s" );
            if( metaProperty != null ){
                return childName + "s";
            }
            return childName;
        }

        /**
         * Follow the most conventional pattern, returns the parentName
         * unchanged.
         */
        public String resolveParentRelationName( String parentName, Object parent,
                String childName, Object child ) {
            return parentName;
        }
    }

    /**
     * Strategy for picking the correct synthetic identifier.
     */
    public interface IdentifierResolver {
        /**
         * Returns the name of the property that will identify the node.<br>
         *
         * @param nodeName the name of the node
         */
        String getIdentifierFor( String nodeName );
    }

    /**
     * Strategy for creating new instances of a class.<br>
     * Useful for plug-in calls to non-default constructors.
     */
    public interface NewInstanceResolver {
        /**
         * Create a new instance of Class klass.
         *
         * @param klass the resolved class name
         * @param attributes the attribute Map available for the node
         */
        Object newInstance( Class klass, Map attributes ) throws InstantiationException,
                IllegalAccessException;
    }

    /**
     * Strategy for picking the correct synthetic reference identifier.
     */
    public interface ReferenceResolver {
        /**
         * Returns the name of the property that references another node.<br>
         *
         * @param nodeName the name of the node
         */
        String getReferenceFor( String nodeName );
    }

    /**
     * Strategy for resolving a relationship property name.
     */
    public interface RelationNameResolver {
        /**
         * Returns the mapping name of child -&gt; parent
         *
         * @param parentName the name of the parent node
         * @param parent the parent node
         * @param childName the name of the child node
         * @param child the child node
         */
        String resolveChildRelationName( String parentName, Object parent, String childName,
                Object child );

        /**
         * Returns the mapping name of parent -&gt; child
         *
         * @param parentName the name of the parent node
         * @param parent the parent node
         * @param childName the name of the child node
         * @param child the child node
         */
        String resolveParentRelationName( String parentName, Object parent, String childName,
                Object child);
    }

    private void resolveLazyReferences() {
        if( !lazyReferencesAllowed ) return;
        for( Iterator refs = lazyReferences.iterator(); refs.hasNext() ; ) {
            NodeReference ref = (NodeReference) refs.next();
            if( ref.parent == null ) continue;

            Object child = null;
            try {
                child = getProperty( ref.refId );
            } catch( MissingPropertyException mpe ) {
                // ignore
            }
            if( child == null ){
                throw new IllegalArgumentException( "There is no valid node for reference "
                    + ref.parentName + "." + ref.childName + "=" + ref.refId );
            }

            // set child first
            childPropertySetter.setChild( ref.parent, child, ref.parentName,
                relationNameResolver.resolveChildRelationName( ref.parentName,
                    ref.parent, ref.childName, child ) );

            // set parent afterwards
            String propertyName = relationNameResolver.resolveParentRelationName( ref.parentName, 
                ref.parent, ref.childName, child );
            MetaProperty metaProperty = InvokerHelper.getMetaClass( child )
                .hasProperty( child, propertyName );
            if( metaProperty != null ){
                metaProperty.setProperty( child, ref.parent );
            }
        }
    }

    private static class ObjectFactory extends AbstractFactory {
        public Object newInstance( FactoryBuilderSupport builder, Object name, Object value,
                Map properties ) throws InstantiationException, IllegalAccessException {
            ObjectGraphBuilder ogbuilder = (ObjectGraphBuilder) builder;
            String classname = ogbuilder.classNameResolver.resolveClassname( (String) name );
            Class klass = (Class) ogbuilder.resolvedClasses.get( classname );
            if( klass == null ){
                klass = loadClass( ogbuilder.classLoader, classname );
                if( klass == null ){
                    klass = loadClass( ogbuilder.getClass().getClassLoader(), classname );
                }
                if( klass == null ){
                    try{
                        klass = Class.forName( classname );
                    }catch( ClassNotFoundException e ){
                        // ignore
                    }
                }
                if( klass == null ){
                    klass = loadClass( Thread.currentThread().getContextClassLoader(), classname );
                }
                if( klass == null ){
                    throw new RuntimeException(new ClassNotFoundException(classname));
                }
                ogbuilder.resolvedClasses.put( classname, klass );
            }

            Map context = ogbuilder.getContext();
            context.put( ObjectGraphBuilder.NODE_NAME, name );
            context.put( ObjectGraphBuilder.NODE_CLASS, klass );

            if( value != null && klass.isAssignableFrom(value.getClass()) ) {
                return value;
            }

            return ogbuilder.newInstanceResolver.newInstance( klass, properties );
        }

        public void setChild( FactoryBuilderSupport builder, Object parent, Object child ) {
            if( child == null ) return;

            ObjectGraphBuilder ogbuilder = (ObjectGraphBuilder) builder;
            if( parent != null ){
                Map context = ogbuilder.getContext();
                Map parentContext = ogbuilder.getParentContext();

                String parentName = null;
                String childName = (String) context.get( NODE_NAME );
                if( parentContext != null ){
                    parentName = (String) parentContext.get( NODE_NAME );
                }

                String propertyName = ogbuilder.relationNameResolver.resolveParentRelationName(
                        parentName, parent, childName, child );
                MetaProperty metaProperty = InvokerHelper.getMetaClass( child )
                        .hasProperty( child, propertyName );
                if( metaProperty != null ){
                    metaProperty.setProperty( child, parent );
                }
            }
        }

        public void setParent( FactoryBuilderSupport builder, Object parent, Object child ) {
            if( child == null ) return;

            ObjectGraphBuilder ogbuilder = (ObjectGraphBuilder) builder;
            if( parent != null ){
                Map context = ogbuilder.getContext();
                Map parentContext = ogbuilder.getParentContext();

                String parentName = null;
                String childName = (String) context.get( NODE_NAME );
                if( parentContext != null ){
                    parentName = (String) parentContext.get( NODE_NAME );
                }

                ogbuilder.childPropertySetter.setChild( parent, child, parentName,
                        ogbuilder.relationNameResolver.resolveChildRelationName( parentName,
                                parent, childName, child ) );
            }
        }

        private Class loadClass( ClassLoader classLoader, String classname ){
            if( classLoader == null || classname == null ){
                return null;
            }
            try{
                return classLoader.loadClass( classname );
            }catch( ClassNotFoundException e ){
                return null;
            }
        }
    }

    private static class ObjectRefFactory extends ObjectFactory {
        public boolean isLeaf() {
            return true;
        }

        public Object newInstance( FactoryBuilderSupport builder, Object name, Object value,
                Map properties ) throws InstantiationException, IllegalAccessException {
            ObjectGraphBuilder ogbuilder = (ObjectGraphBuilder) builder;
            String refProperty = ogbuilder.referenceResolver.getReferenceFor( (String) name );
            Object refId = properties.remove( refProperty );

            Object object = null;
            Boolean lazy = Boolean.FALSE;
            if( refId instanceof String ) {
                try {
                    object = ogbuilder.getProperty( (String) refId );
                } catch( MissingPropertyException mpe ) {
                    // ignore, will try lazy reference
                }
                if( object == null ){
                    if( ogbuilder.isLazyReferencesAllowed() ) {
                        lazy = Boolean.TRUE;
                    } else {
                        throw new IllegalArgumentException( "There is no previous node with "
                                + ogbuilder.identifierResolver.getIdentifierFor( (String) name ) + "="
                                + refId );
                    }
                }
            } else {
                // assume we got a true reference to the object
                object = refId;
            }

            if( !properties.isEmpty() ){
                throw new IllegalArgumentException(
                        "You can not modify the properties of a referenced object." );
            }

            Map context = ogbuilder.getContext();
            context.put( ObjectGraphBuilder.NODE_NAME, name );
            context.put( ObjectGraphBuilder.LAZY_REF, lazy );

            if( lazy.booleanValue() ) {
                Map parentContext = ogbuilder.getParentContext();

                Object parent = null;
                String parentName = null;
                String childName = (String) name;
                if( parentContext != null ){
                    parent = context.get( CURRENT_NODE );
                    parentName = (String) parentContext.get( NODE_NAME );
                }
                ogbuilder.lazyReferences.add( new NodeReference(parent,
                                                                parentName,
                                                                childName,
                                                                (String) refId) );
            } else {
                context.put( ObjectGraphBuilder.NODE_CLASS, object.getClass() );
            }

            return object;
        }

        public void setChild( FactoryBuilderSupport builder, Object parent, Object child ) {
            Boolean lazy = (Boolean) builder.getContext().get( ObjectGraphBuilder.LAZY_REF );
            if( !lazy.booleanValue() ) super.setChild( builder, parent, child );
        }

        public void setParent( FactoryBuilderSupport builder, Object parent, Object child ) {
           Boolean lazy = (Boolean) builder.getContext().get( ObjectGraphBuilder.LAZY_REF );
           if( !lazy.booleanValue() ) super.setParent( builder, parent, child );
        }
    }

    private static class NodeReference {
        private final Object parent;
        private final String parentName;
        private final String childName;
        private final String refId;

        private NodeReference( Object parent, String parentName, String childName, String refId ) {
            this.parent = parent;
            this.parentName = parentName;
            this.childName = childName;
            this.refId = refId;
        }

        public String toString() {
            return new StringBuilder().append("[parentName=").append(parentName)
                .append(", childName=").append(childName)
                .append(", refId=").append(refId)
                .append("]").toString();
        }
    }
}
