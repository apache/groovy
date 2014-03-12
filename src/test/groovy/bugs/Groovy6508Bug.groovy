package groovy.bugs

import java.util.concurrent.Callable

/**
 * LK-TODO
 */
class Groovy6508Bug extends GroovyTestCase {

    void testInnerClassAccessingFieldWithCustomGetter() {
        PropertyGetterOverride x = new PropertyGetterOverride()
        assert x.field == x.getFieldViaInner()
    }

    void testInnerClassAccessingBaseFieldProperty() {
        BaseFieldBearerSub sub = new BaseFieldBearerSub();
        assert sub.baseField == sub.getBaseFieldViaInner()
    }

}

class BaseFieldBearer {
    String baseField = 'baseValue'
}

class BaseFieldBearerSub extends BaseFieldBearer {

    /** Access baseField from our super class by using an inner class instance **/
    String getBaseFieldViaInner() {
        new Callable<String>() {
            String call() {
                // Previous versions of Groovy would fault here, unable to access
                // an *attribute* called baseField, rather than looking for a property
                baseField
            }
        }.call();
    }
}

class PropertyGetterOverride {

    String field = 'fieldAttributeValue'

    /** A property-getter override for field */
    String getField() {
        'fieldPropertyValue'
    }

    /** Access field property from our class by using an inner class */
    String getFieldViaInner() {
        new Callable<String>() {
            String call() {
                // Previous versions of Groovy will access the attribute directly here
                // rather than the property; i.e., the custom getter would not be respected
                field
            }
        }.call()
    }
}

