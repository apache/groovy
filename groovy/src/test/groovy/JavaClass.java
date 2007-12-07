package groovy;

public class JavaClass {

    public static class StaticInner {
        int getResult () {
            return 239;
        }

         static long getIt () {
            return 30;
         }

        public static class Inner2 {}
    }

    class NonStaticInner {

    }

    static final StaticInner CONST = new StaticInner();
}
