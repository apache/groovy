package org.codehaus.groovy.ast.decompiled;

import org.codehaus.groovy.ast.ClassNode;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Anno(
        stringAttr = "s",
        enumAttr = SomeEnum.BAR,
        intArrayAttr = {4, 2},
        clsAttr = String.class,
        classArrayAttr = {AsmDecompilerTestData.class},
        annoArrayAttr = {@InnerAnno, @InnerAnno(booleanAttr = false)}
)
public class AsmDecompilerTestData<T extends List<? super T>, V> extends SuperClass implements Intf<Map<T, String>> {
    public AsmDecompilerTestData(boolean b) {
    }

    @Anno
    public ClassNode objectMethod() {
        return null;
    }

    public void withParametersThrowing(@Anno int a, AsmDecompilerTestData[] b) throws IOException {
    }

    public int[][] primitiveArrayMethod() {
        return null;
    }

    public <A, B extends IOException> List<?> genericMethod(A a, int[] array) throws B {
        return null;
    }

    @Anno
    protected Object aField;
}

@SuppressWarnings("unused")
interface Intf<S> { }

enum SomeEnum { FOO, BAR }

class SuperClass { }

@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@interface Anno {
    String stringAttr() default "";
    SomeEnum enumAttr() default SomeEnum.FOO;
    Class clsAttr() default Object.class;
    boolean booleanAttr() default true;
    int[] intArrayAttr() default {};
    Class[] classArrayAttr() default {};
    InnerAnno[] annoArrayAttr() default {};
}

@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@interface InnerAnno {
    boolean booleanAttr() default true;
}