package core

import java.sql.SQLException

public interface AA1 {
        int a;
        long b;
        double c;
        char d;
        short e;
        byte f;
        float g;
        boolean h;
        String i;

        public static final NAME = "AA1"

        @Test3
        public static final NAME2 = "AA1"

        void sayHello();
        abstract void sayHello2();
        public void sayHello3();
        public abstract void sayHello4();
        @Test2
        public abstract void sayHello5();

        @Test2
        public abstract void sayHello6() throws IOException, SQLException;

        @Test2
        @Test3
        public abstract <T> T sayHello7() throws IOException, SQLException;

        @Test2
        @Test3
        public abstract <T extends A> T sayHello8() throws IOException, SQLException;

        @Test2
        @Test3
        public abstract <T extends A & B> T sayHello9() throws IOException, SQLException;
}
