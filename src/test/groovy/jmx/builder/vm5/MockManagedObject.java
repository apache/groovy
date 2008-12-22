package groovy.jmx.builder.vm5;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MockManagedObject {
    private static Logger logger = Logger.getLogger(MockManagedObject.class.getName());
    private String something;
    private int somethingElse;

    public String getSomething() {
        logger.log(Level.FINEST, "Returning Something " + something);
        return something;
    }

    public void setSomething(String thing) {
        logger.log(Level.INFO, "Setting Something value to " + thing);
        something = thing;
    }

    public int getSomethingElse() {
        logger.log(Level.FINEST, "Returning SomethingElse " + somethingElse);
        return somethingElse;
    }

    public void setSomethingElse(int thing) {
        somethingElse = thing;
    }

    public MockManagedObject() {
    }

    public MockManagedObject(String thing) {
    }

    public MockManagedObject(String thing, int count) {
    }

    public void doSomething() {
        logger.log(Level.FINER, "JMX Invoke - doSomething().. (no param)");
    }

    public void doSomethingElse(int qty, String name) {
    }

    public void doWork(int hour, String what) {

    }

    public void dontDoThis(Object param) {
        logger.log(Level.FINER, "Jmx Invoke - method dontDoThis() with param : " + param);
    }

}
