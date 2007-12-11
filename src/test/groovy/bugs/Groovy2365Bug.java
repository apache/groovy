package groovy.bugs;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

public class Groovy2365Bug extends Groovy2365Base {

    public void testDeadlock () {
        String path = createData();

        System.out.println("Test started");
        for (int i = 0; i != 100; ++i ) {
                System.out.println("Iter " + i);
                final GroovyClassLoader groovyLoader = new GroovyClassLoader ();
                groovyLoader.addClasspath(path);

                Class _script1Class = null;
                try {
                    _script1Class = groovyLoader.loadClass("Script1", true, true);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                final Class script1Class = _script1Class;

                // setup two threads to try a deadlock

                // thread one: newInstance script foo
                final boolean completed [] = new boolean[2] ;
                Thread thread1 = new Thread() {
                    public void run() {
                        try {
                            Script script = (Script) script1Class.newInstance();
                            script.run();
                            completed [0] = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                Thread thread2 = new Thread() {
                    public void run() {
                        try {
                            Class cls = groovyLoader.loadClass("Script2", true, true);
                            Script script = (Script) cls.newInstance();
                            script.run();
                            completed [1] = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                // let's see if we get a deadlock
                thread2.start();
                thread1.start();

            try {
                thread1.join(5000);
                thread2.join(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            assertTrue("Potentially deadlock", completed[0] && completed[1]);
        }
    }
}
