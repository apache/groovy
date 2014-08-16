package org.codehaus.groovy.benchmarks.vm5.b2394;

import groovy.lang.Binding;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.concurrent.CountDownLatch;

public class ScriptLauncher extends Thread
{
    Class scriptClass;

    Script script;

    int numIter;

    CountDownLatch latch;
    public final long[] tids;

    public ScriptLauncher(Class scriptClass, int numIter, CountDownLatch latch, long[] tids)
    {
        this.tids = tids;
        this.scriptClass = scriptClass;
        this.numIter = numIter;
        this.latch = latch;

    }

    public void run()
    {
        final long id = Thread.currentThread().getId();

        // run the script numIter times
        for (int i = 0; i < numIter; i++)
        {
            Builder builder = new Builder();

            Binding binding = new Binding();
            binding.setVariable("builder", builder);

            script = InvokerHelper.createScript(scriptClass, binding);

            script.run();
        }

        latch.countDown();
    }
}
