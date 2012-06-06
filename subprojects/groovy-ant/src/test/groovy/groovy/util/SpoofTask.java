package groovy.util;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.codehaus.groovy.runtime.InvokerHelper;

public class SpoofTask extends Task {
    private int foo;

    public SpoofTask() {
        super();
        SpoofTaskContainer.spoof("SpoofTask ctor");
    }

    public void setFoo(final int i) {
        foo = i;
    }


    public void execute() throws BuildException {
        SpoofTaskContainer.spoof("begin SpoofTask execute");
        SpoofTaskContainer.spoof("tag name from wrapper: " + getWrapper().getElementTag());
        // don't rely on Map.toString(), behaviour is not documented
        SpoofTaskContainer.spoof("attributes map from wrapper: "
                + InvokerHelper.toMapString(getWrapper().getAttributeMap()));
        SpoofTaskContainer.spoof("param foo: " + foo);

        SpoofTaskContainer.spoof("end SpoofTask execute");
    }

}
