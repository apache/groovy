package groovy;

public class TestInterruptor implements Runnable {
    private Thread caller;

    public TestInterruptor(Thread caller) {
        this.caller = caller;
    }

    public void run() {
        try {
            Thread.currentThread().sleep(100); // enforce yield, so we have something to interrupt
        } catch (InterruptedException e) {
            // ignore
        }
        caller.interrupt();
    }
}
