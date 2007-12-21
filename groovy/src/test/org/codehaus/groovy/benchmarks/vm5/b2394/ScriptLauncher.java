package org.codehaus.groovy.benchmarks.vm5.b2394;

import groovy.lang.Binding;
import groovy.lang.Script;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;

public class ScriptLauncher extends Thread
{
    final static ThreadMXBean tmxBean = ManagementFactory.getThreadMXBean();

    Class scriptClass;

    Script script;

    int numIter;

    CountDownLatch latch;
    public final long[] tids;

    public ScriptLauncher(Class scriptClass, int numIter, CountDownLatch latch, long[] tids)
    {
        this.tids = tids;
        tmxBean.setThreadContentionMonitoringEnabled (true);
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

            try
            {
                script = (Script)scriptClass.newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            Binding binding = new Binding();
            binding.setVariable("builder", builder);
            script.setBinding(binding);

            script.run();
        }

        final ThreadInfo threadInfo = tmxBean.getThreadInfo(id);
        System.out.println(threadInfo.getBlockedCount()
                + " " + threadInfo.getBlockedTime()
        );

        latch.countDown();
    }
}
