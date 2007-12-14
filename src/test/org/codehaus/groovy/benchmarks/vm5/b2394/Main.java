package org.codehaus.groovy.benchmarks.vm5.b2394;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

public class Main
{

    public Main(String scriptName, int numIter, int numThreads)
    {
        System.out.println("Running " + scriptName);
        CountDownLatch latch = new CountDownLatch(numThreads);

        Class scriptClass = loadScript(scriptName);

        long start = System.currentTimeMillis();

        // launch threads, each one instantiating the scriptClass and running
        // it numIter times
        for (int i = 0; i < numThreads; i++)
        {
            new ScriptLauncher(scriptClass, numIter, latch).start();
        }

        // wait for the threads to finish
        try
        {
            latch.await();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        double duration = (double)(System.currentTimeMillis() - start) / 1000.0;
        double numberOfOperations = numIter * numThreads;

        System.out.println("Test completed: " + numberOfOperations
            + " scriptExecutions in " + duration + " seconds");
        System.out.println("\t\t\t" + (numberOfOperations / duration)
            + " scriptExecutions/second");
    }

    private Class loadScript(String name)
    {
        Class scriptClass = null;

        name = "src/test/" + getClass().getPackage().getName().replace(".", "/") + "/" + name;
        InputStream is = null;
        try {
            is = new FileInputStream(name);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (is == null)
        {
            throw new RuntimeException("Script file not found: " + name);
        }

        GroovyClassLoader gcl =
            new GroovyClassLoader(this.getClass().getClassLoader());

        try
        {
            scriptClass = gcl.parseClass(is);
        }
        catch (CompilationFailedException e)
        {
            throw new RuntimeException("Script compilation failed: "
                + e.getMessage());
        }
        finally
        {
            try
            {
                if (is != null)
                    is.close();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }

        return scriptClass;
    }

    public static void main(String[] args)
    {
        if (args == null || args.length != 3)
        {
            new Main("script300.groovy", 5000, 20);
        }
        else
        {
            new Main(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        }
    }
}
