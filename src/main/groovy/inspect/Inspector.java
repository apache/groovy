package groovy.inspect;

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.lang.PropertyValue;

import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.util.*;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

/**
 * The Inspector provides a unified access to an object's
 * information that can be determined by introspection.
 *
 * @author Dierk Koenig
 */
public class Inspector {
    protected Object objectUnderInspection = null;

    // Indexes to retrieve Class Property information
    public static final int CLASS_PACKAGE_IDX       = 0;
    public static final int CLASS_CLASS_IDX         = 1;
    public static final int CLASS_INTERFACE_IDX     = 2;
    public static final int CLASS_SUPERCLASS_IDX    = 3;
    public static final int CLASS_OTHER_IDX         = 4;

    // Indexes to retrieve field and method information
    public static final int MEMBER_ORIGIN_IDX = 0;
    public static final int MEMBER_MODIFIER_IDX = 1;
    public static final int MEMBER_DECLARER_IDX = 2;
    public static final int MEMBER_TYPE_IDX = 3;
    public static final int MEMBER_NAME_IDX = 4;
    public static final int MEMBER_PARAMS_IDX = 5;
    public static final int MEMBER_VALUE_IDX = 5;
    public static final int MEMBER_EXCEPTIONS_IDX = 6;

    public static final String NOT_APPLICABLE = "n/a";
    public static final String GROOVY = "GROOVY";
    public static final String JAVA = "JAVA";

    /**
     * @param objectUnderInspection must not be null
     */
    public Inspector(Object objectUnderInspection) {
        if (null == objectUnderInspection){
            throw new IllegalArgumentException("argument must not be null");
        }
        this.objectUnderInspection = objectUnderInspection;
    }

    /**
     * Get the Class Properties of the object under inspection.
     * @return String array to be indexed by the CLASS_xxx_IDX constants
     */
    public String[] getClassProps() {
        String[] result = new String[CLASS_OTHER_IDX+1];
        result[CLASS_PACKAGE_IDX] = "package "+ getClassUnderInspection().getPackage().getName();
        String modifiers = Modifier.toString(getClassUnderInspection().getModifiers());
        String classOrInterface = "class";
        if (getClassUnderInspection().isInterface()){
            classOrInterface = "interface";
        }
        result[CLASS_CLASS_IDX] = modifiers + " "+ classOrInterface+" "+ shortName(getClassUnderInspection());
        result[CLASS_INTERFACE_IDX] = "implements ";
        Class[] interfaces = getClassUnderInspection().getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            result[CLASS_INTERFACE_IDX] += shortName(interfaces[i])+ " ";
        }
        result[CLASS_SUPERCLASS_IDX] = "extends " + shortName(getClassUnderInspection().getSuperclass());
        result[CLASS_OTHER_IDX] = "is Primitive: "+getClassUnderInspection().isPrimitive()
                  +", is Array: "   +getClassUnderInspection().isArray()
                  +", is Groovy: "  + isGroovy();
        return result;
    }

    public boolean isGroovy() {
        return getClassUnderInspection().isAssignableFrom(GroovyObject.class);
    }

    /**
     * Get info about usual Java instance and class Methods as well as Constructors.
     * @return  Array of StringArrays that can be indexed with the MEMBER_xxx_IDX constants
     */
    public Object[] getMethods(){
        Method[] methods = getClassUnderInspection().getMethods();
        Constructor[] ctors = getClassUnderInspection().getConstructors();
        Object[] result = new Object[methods.length + ctors.length];
        int resultIndex = 0;
        for (; resultIndex < methods.length; resultIndex++) {
            Method method = methods[resultIndex];
            result[resultIndex] = methodInfo(method);
        }
        for (int i = 0; i < ctors.length; i++, resultIndex++) {
            Constructor ctor = ctors[i];
            result[resultIndex] = methodInfo(ctor);
        }
        return result;
    }
     /**
     * Get info about instance and class Methods that are dynamically added through Groovy.
     * @return  Array of StringArrays that can be indexed with the MEMBER_xxx_IDX constants
     */
    public Object[] getMetaMethods(){
        MetaClass metaClass = InvokerHelper.getMetaClass(objectUnderInspection);
        List metaMethods = metaClass.getMetaMethods();
        Object[] result = new Object[metaMethods.size()];
        int i=0;
        for (Iterator iter = metaMethods.iterator(); iter.hasNext(); i++) {
            MetaMethod metaMethod = (MetaMethod) iter.next();
            result[i] = methodInfo(metaMethod);
        }
        return result;
    }
    
    /**
     * Get info about usual Java public fields incl. constants.
     * @return  Array of StringArrays that can be indexed with the MEMBER_xxx_IDX constants
     */
    public Object[] getPublicFields(){
        Field[] fields = getClassUnderInspection().getFields();
        Object[] result = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            result[i] = fieldInfo(field);
        }
        return result;
    }
    /**
     * Get info about Properties (Java and Groovy alike).
     * @return  Array of StringArrays that can be indexed with the MEMBER_xxx_IDX constants
     */
    public Object[] getProperties(){
        List props = DefaultGroovyMethods.allProperties(objectUnderInspection);
        Object[] result = new Object[props.size()];
        int i=0;
        for (Iterator iter = props.iterator(); iter.hasNext(); i++) {
            PropertyValue pv = (PropertyValue) iter.next();
            result[i] = fieldInfo(pv);
        }
        return result;
    }

    protected String[] fieldInfo(Field field) {
        String[] result = new String[MEMBER_VALUE_IDX+1];
        result[MEMBER_ORIGIN_IDX] = JAVA;
        result[MEMBER_MODIFIER_IDX] = Modifier.toString(field.getModifiers());
        result[MEMBER_DECLARER_IDX] = shortName(field.getDeclaringClass());
        result[MEMBER_TYPE_IDX] = shortName(field.getType());
        result[MEMBER_NAME_IDX] = field.getName();
        try {
            result[MEMBER_VALUE_IDX] = field.get(objectUnderInspection).toString();
        } catch (IllegalAccessException e) {
            result[MEMBER_VALUE_IDX] = NOT_APPLICABLE;
        }
        return withoutNulls(result);
    }
    protected String[] fieldInfo(PropertyValue pv) {
        String[] result = new String[MEMBER_VALUE_IDX+1];
        result[MEMBER_ORIGIN_IDX] = GROOVY;
        result[MEMBER_MODIFIER_IDX] = "public";
        result[MEMBER_DECLARER_IDX] = NOT_APPLICABLE;
        result[MEMBER_TYPE_IDX] = shortName(pv.getType());
        result[MEMBER_NAME_IDX] = pv.getName();
        try {
            result[MEMBER_VALUE_IDX] = pv.getValue().toString();
        } catch (Exception e) {
            result[MEMBER_VALUE_IDX] = NOT_APPLICABLE;
        }
        return withoutNulls(result);
    }

    protected Class getClassUnderInspection() {
        return objectUnderInspection.getClass();
    }

    public static String shortName(Class clazz){
        if (null == clazz) return NOT_APPLICABLE;
        String className = clazz.getName();
        if (null == clazz.getPackage()) return className;
        String packageName = clazz.getPackage().getName();
        int offset = packageName.length();
        if (offset > 0) offset++;
        className = className.substring(offset);
        return className;
    }

    protected String[] methodInfo(Method method){
        String[] result = new String[MEMBER_EXCEPTIONS_IDX+1];
	    int mod = method.getModifiers();
        result[MEMBER_ORIGIN_IDX] = JAVA;
        result[MEMBER_MODIFIER_IDX] = Modifier.toString(mod);
        result[MEMBER_DECLARER_IDX] = shortName(method.getDeclaringClass());
        result[MEMBER_TYPE_IDX] = shortName(method.getReturnType());
        result[MEMBER_NAME_IDX] = method.getName();
	    Class[] params = method.getParameterTypes();
        StringBuffer sb = new StringBuffer();
	    for (int j = 0; j < params.length; j++) {
		    sb.append(shortName(params[j]));
		    if (j < (params.length - 1)) sb.append(", ");
	    }
        result[MEMBER_PARAMS_IDX] = sb.toString();
	    sb.setLength(0);
	    Class[] exceptions = method.getExceptionTypes();
		for (int k = 0; k < exceptions.length; k++) {
		    sb.append(shortName(exceptions[k]));
		    if (k < (exceptions.length - 1)) sb.append(", ");
	    }
        result[MEMBER_EXCEPTIONS_IDX] = sb.toString();
	    return withoutNulls(result);
    }
    protected String[] methodInfo(Constructor ctor){
        String[] result = new String[MEMBER_EXCEPTIONS_IDX+1];
	    int mod = ctor.getModifiers();
        result[MEMBER_ORIGIN_IDX] = JAVA;
        result[MEMBER_MODIFIER_IDX] = Modifier.toString(mod);
        result[MEMBER_DECLARER_IDX] = shortName(ctor.getDeclaringClass());
        result[MEMBER_TYPE_IDX] = shortName(ctor.getDeclaringClass());
        result[MEMBER_NAME_IDX] = ctor.getName();
	    Class[] params = ctor.getParameterTypes();
        StringBuffer sb = new StringBuffer();
	    for (int j = 0; j < params.length; j++) {
		    sb.append(shortName(params[j]));
		    if (j < (params.length - 1)) sb.append(", ");
	    }
        result[MEMBER_PARAMS_IDX] = sb.toString();
	    sb.setLength(0);
	    Class[] exceptions = ctor.getExceptionTypes();
		for (int k = 0; k < exceptions.length; k++) {
		    sb.append(shortName(exceptions[k]));
		    if (k < (exceptions.length - 1)) sb.append(", ");
	    }
        result[MEMBER_EXCEPTIONS_IDX] = sb.toString();
	    return withoutNulls(result);
    }
    protected String[] methodInfo(MetaMethod method){
        String[] result = new String[MEMBER_EXCEPTIONS_IDX+1];
	    int mod = method.getModifiers();
        result[MEMBER_ORIGIN_IDX] = GROOVY;
        result[MEMBER_MODIFIER_IDX] = Modifier.toString(mod);
        result[MEMBER_DECLARER_IDX] = shortName(method.getDeclaringClass());
        result[MEMBER_TYPE_IDX] = shortName(method.getReturnType());
        result[MEMBER_NAME_IDX] = method.getName();
	    Class[] params = method.getParameterTypes();
        StringBuffer sb = new StringBuffer();
	    for (int j = 0; j < params.length; j++) {
		    sb.append(shortName(params[j]));
		    if (j < (params.length - 1)) sb.append(", ");
	    }
        result[MEMBER_PARAMS_IDX] = sb.toString();
        result[MEMBER_EXCEPTIONS_IDX] = NOT_APPLICABLE; // no exception info for Groovy MetaMethods
        return withoutNulls(result);
    }

    protected String[] withoutNulls(String[] toNormalize){
        for (int i = 0; i < toNormalize.length; i++) {
            String s = toNormalize[i];
            if (null == s) toNormalize[i] = NOT_APPLICABLE;
        }
        return toNormalize;
    }

    public static void print(Object[] memberInfo) {
        for (int i = 0; i < memberInfo.length; i++) {
            String[] metaMethod = (String[]) memberInfo[i];
            System.out.print(i+":\t");
            for (int j = 0; j < metaMethod.length; j++) {
                String s = metaMethod[j];
                System.out.print(s+" ");
            }
            System.out.println("");
        }
    }
    public static Object[] sort(Object[] memberInfo) {
        Arrays.sort(memberInfo, new MemberComparator());
        return memberInfo;
    }

    public static class MemberComparator implements Comparator {
        public int compare(Object a, Object b) {
            String[] aStr = (String[]) a;
            String[] bStr = (String[]) b;
            int result = aStr[Inspector.MEMBER_NAME_IDX].compareTo(bStr[Inspector.MEMBER_NAME_IDX]);
            if (0 != result) return result;
            result = aStr[Inspector.MEMBER_TYPE_IDX].compareTo(bStr[Inspector.MEMBER_TYPE_IDX]);
            if (0 != result) return result;
            result = aStr[Inspector.MEMBER_PARAMS_IDX].compareTo(bStr[Inspector.MEMBER_PARAMS_IDX]);
            if (0 != result) return result;
            result = aStr[Inspector.MEMBER_DECLARER_IDX].compareTo(bStr[Inspector.MEMBER_DECLARER_IDX]);
            if (0 != result) return result;
            result = aStr[Inspector.MEMBER_MODIFIER_IDX].compareTo(bStr[Inspector.MEMBER_MODIFIER_IDX]);
            if (0 != result) return result;
            result = aStr[Inspector.MEMBER_ORIGIN_IDX].compareTo(bStr[Inspector.MEMBER_ORIGIN_IDX]);
            return result;
        }
    }
}
