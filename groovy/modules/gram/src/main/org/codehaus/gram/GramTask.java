package org.codehaus.gram;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.xmlbeans.impl.jam.JamService;
import org.apache.xmlbeans.impl.jam.JamServiceFactory;
import org.apache.xmlbeans.impl.jam.JamServiceParams;

import java.io.File;

/**
 * An Ant task for executing Gram scripts, which are Groovy
 * scripts executed on the JAM context.
 *
 * @version $Revision$
 */
public class GramTask extends MatchingTask {

    private Path srcDir = null;
    private Path mToolpath = null;
    private Path mClasspath = null;
    private String mIncludes = "**/*.java";
    private File destDir;
    private File scriptDir;

    /**
     * Sets the directory into which source files should be generated.
     */
    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    public void setScriptDir(File scriptDir) {
        this.scriptDir = scriptDir;
    }

    public void setSrcDir(Path srcDir) {
        this.srcDir = srcDir;
    }

    public void setToolpath(Path path) {
        if (mToolpath == null) {
            mToolpath = path;
        }
        else {
            mToolpath.append(path);
        }
    }

    public void setToolpathRef(Reference r) {
        createToolpath().setRefid(r);
    }


    public Path createToolpath() {
        if (mToolpath == null) {
            mToolpath = new Path(getProject());
        }
        return mToolpath.createPath();
    }

    public void setClasspath(Path path) {
        if (mClasspath == null) {
            mClasspath = path;
        }
        else {
            mClasspath.append(path);
        }
    }

    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }


    public Path createClasspath() {
        if (mClasspath == null) {
            mClasspath = new Path(getProject());
        }
        return mClasspath.createPath();
    }


    public void execute() throws BuildException {
        if (srcDir == null) {
            throw new BuildException("'srcDir' must be specified");
        }
        if (destDir == null) {
            throw new BuildException("'destDir' must be specified");
        }
        if (scriptDir == null) {
            throw new BuildException("'scriptDir' must be specified");
        }
        JamServiceFactory jamServiceFactory = JamServiceFactory.getInstance();
        JamServiceParams serviceParams = jamServiceFactory.createServiceParams();
        if (mToolpath != null) {
            File[] tcp = path2files(mToolpath);
            for (int i = 0; i < tcp.length; i++) {
                serviceParams.addToolClasspath(tcp[i]);
            }
        }
        if (mClasspath != null) {
            File[] cp = path2files(mClasspath);
            for (int i = 0; i < cp.length; i++) {
                serviceParams.addClasspath(cp[i]);
            }
        }
        serviceParams.includeSourcePattern(path2files(srcDir), mIncludes);
        try {
            JamService jam = jamServiceFactory.createService(serviceParams);
            Gram gram = new Gram(jam);

            log("Executing Groovy scripts:");

            DirectoryScanner ds = super.getDirectoryScanner(scriptDir);
            String[] files = ds.getIncludedFiles();

            for (int i = 0; i < files.length; i++) {
                String file = files[i];

                log("Script: " + file);
                gram.execute(new File(scriptDir, file));
            }
            log("...done.");
        }
        catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private File[] path2files(Path path) {
        String[] list = path.list();
        File[] out = new File[list.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = new File(list[i]).getAbsoluteFile();
        }
        return out;
    }
}
