package groovy.jmx.builder.vm5;

public class MockSimpleObject implements MockSimpleObjectMBean {
    private int priority;
    private String id;

    public void setPriority(int prio) {
        priority = prio;
    }

    public int getPriority() {
        return priority;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
