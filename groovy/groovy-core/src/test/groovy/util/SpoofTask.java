package groovy.util;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class SpoofTask extends Task {

    public SpoofTask() {
        super();
        SpoofTaskContainer.spoof("SpoofTask ctor");
    }

    public void execute() throws BuildException {
        SpoofTaskContainer.spoof("begin SpoofTask execute");
        SpoofTaskContainer.spoof("end SpoofTask execute");
    }

}
