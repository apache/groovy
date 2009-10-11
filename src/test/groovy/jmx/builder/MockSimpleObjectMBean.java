package groovy.jmx.builder;

public interface MockSimpleObjectMBean {
    void setPriority(int prio);

    int getPriority();

    void setId(String id);

    String getId();
}
