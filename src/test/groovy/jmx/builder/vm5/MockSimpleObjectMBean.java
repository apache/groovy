package groovy.jmx.builder.vm5;

public interface MockSimpleObjectMBean {
    void setPriority(int prio);

    int getPriority();

    void setId(String id);

    String getId();
}
