package groovy.util;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.UnknownElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SpoofTaskContainer extends Task implements TaskContainer {
    private List tasks = new ArrayList();
    static StringBuffer spoof = new StringBuffer();

    public SpoofTaskContainer() {
        super();
        spoof("SpoofTaskContainer ctor");
    }

    static StringBuffer getSpoof() {
        return spoof;
    }

    static void resetSpoof() {
        spoof = new StringBuffer();
    }

    static void spoof(String message) {
        spoof.append(message);
        spoof.append("\n");
    }

    public void addTask(Task task) {
        // to work with ant 1.6
        spoof("in addTask");
        if (task instanceof UnknownElement) {
            spoof("configuring UnknownElement");
            task.maybeConfigure();
            task = ((UnknownElement) task).getTask();
        }
        tasks.add(task);
    }

    public void execute() throws BuildException {
        spoof("begin SpoofTaskContainer execute");
        for (Iterator iter = tasks.iterator(); iter.hasNext();) {
            Task task = (Task) iter.next();
            task.perform();
        }
        spoof("end SpoofTaskContainer execute");
    }

}
