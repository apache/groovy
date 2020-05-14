import org.codehaus.groovy.runtime.NullObject;

public class TestObjectExtension {
    public static Class func(TestObject self, NullObject arg) {
        return NullObject.class;
    }
    public static Class func(TestObject self, Integer arg) {
        return Integer.class;
    }
}
