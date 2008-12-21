package groovy.jmx.builder;

public interface MockSimpleObjectMBean {
    public void setPriority(int prio);

    public int getPriority();

    public void setId(String id);

    public String getId();
}
