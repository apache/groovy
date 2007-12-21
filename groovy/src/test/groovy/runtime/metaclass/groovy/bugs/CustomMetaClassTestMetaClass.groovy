package groovy.runtime.metaclass.groovy.bugs

class CustomMetaClassTestMetaClass extends groovy.lang.DelegatingMetaClass {
  CustomMetaClassTestMetaClass( final MetaClass metaClazz ){
     super( metaClazz )
  }
}