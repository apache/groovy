/**
 * @Groovydoc
 * class AA
 */
class AA {
    /**
     * @Groovydoc
     * field SOME_FIELD
     */
    public static final int SOME_FIELD = 1;

    /**
     * @Groovydoc
     * constructor AA
     */
    public AA() {

    }

    /**
     * @Groovydoc
     * method m
     */
    public void m() {

    }

    /**
     * @Groovydoc
     * class InnerClass
     */
    class InnerClass {

    }


}

/**
 * @Groovydoc
 * annotation BB
 */
@interface BB {

}

assert AA.class.getAnnotation(groovy.lang.Groovydoc).value().contains('class AA')
assert AA.class.getMethod('m', new Class[0]).getAnnotation(groovy.lang.Groovydoc).value().contains('method m')
assert AA.class.getConstructor().getAnnotation(groovy.lang.Groovydoc).value().contains('constructor AA')
assert AA.class.getField('SOME_FIELD').getAnnotation(groovy.lang.Groovydoc).value().contains('field SOME_FIELD')
assert AA.class.getDeclaredClasses().find {it.simpleName.contains('InnerClass')}.getAnnotation(groovy.lang.Groovydoc).value().contains('class InnerClass')
assert BB.class.getAnnotation(groovy.lang.Groovydoc).value().contains('annotation BB')