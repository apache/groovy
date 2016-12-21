public trait Person {
    public static final SOME_CONSTANT = 'SOME_CONSTANT';
    private String name = 'Daniel';
    private int age;
    @Test2
    private String country = 'China',
            location = 'Shanghai';

    private String field, field2 = 'field2';
    String someProperty;
    String someProperty2 = 'someProperty2';
    String someProperty3 = 'someProperty3',
            someProperty4 = 'someProperty4';
    String someProperty5, someProperty6 = 'someProperty6';
    final String someProperty7 = 'someProperty7';
    static final String someProperty8 = 'someProperty8';

    @Test3
    static final String someProperty9 = 'someProperty9';

    protected static def protectedStaticDefField;
}

trait xx {
    class yy {
        enum zz {}
    }
}