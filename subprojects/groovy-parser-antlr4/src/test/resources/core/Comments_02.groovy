/**
 * test class Comments
 */
public class Comments {
    /**
     * test Comments.SOME_VAR
     */
    public static final String SOME_VAR = 'SOME_VAR';
    /**
     * test Comments.SOME_VAR2
     */
    public static final String SOME_VAR2 = 'SOME_VAR2';

    public static final String SOME_VAR3 = 'SOME_VAR3';

    /**
     * test Comments.SOME_VAR4
     */
    // no groovydoc for SOME_VAR4
    public static final String SOME_VAR4 = 'SOME_VAR4';


    /**
     * test Comments.constructor1
     */
    public Comments() {

    }

    /**
     * test Comments.m1
     */
    def m1() {
        // executing m1
    }

    /*
     * test Comments.m2
     */
    private m2() {
        // executing m2
    }

    /**
     * test Comments.m3
     */
    public void m3() {
        // executing m3
    }

    /**
     * test class InnerClazz
     */
    static class InnerClazz {
        /**
         * test InnerClazz.SOME_VAR3
         */
        public static final String SOME_VAR3 = 'SOME_VAR3';
        /**
         * test InnerClazz.SOME_VAR4
         */
        public static final String SOME_VAR4 = 'SOME_VAR4';

        /**
         * test Comments.m4
         */
        public void m4() {
            // executing m4
        }

        /**
         * test Comments.m5
         */
        public void m5() {
            // executing m5
        }
    }

    /**
     * test class InnerEnum
     */
    static enum InnerEnum {
        /**
         * InnerEnum.NEW
         */
        NEW,

        /**
         * InnerEnum.OLD
         */
        OLD
    }

    static enum InnerEnum2 {}
}

/**
 * test class Comments2
 */
class Comments2 {
    /*
     * test Comments.SOME_VAR
     */
    public static final String SOME_VAR = 'SOME_VAR';
}

class Comments3 {}

/**
 * test someScriptMethod1
 */
void someScriptMethod1() {}

/*
 * test someScriptMethod2
 */
void someScriptMethod2() {}