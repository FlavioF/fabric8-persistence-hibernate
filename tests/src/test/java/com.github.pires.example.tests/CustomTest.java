package com.github.pires.example.tests;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.features.FeaturesService;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by amarcos on 2/18/14.
 */
public class CustomTest
{

    final Long SERVICE_TIMEOUT = 30000L;
    final Long COMMAND_TIMEOUT = 10000L;
    final ExecutorService executor = Executors.newCachedThreadPool();

    @Inject
    protected CommandProcessor commandProcessor;

    @Inject
    protected BundleContext bundleContext;

    @Inject
    protected ConfigurationAdmin configurationAdmin;

    @Inject
    protected FeaturesService featuresService;

    public CustomTest()
    {

    }

    /**
     * Explodes the dictionary into a ,-delimited list of key=value pairs
     */
    public String explode(Dictionary dictionary)
    {
        Enumeration keys = dictionary.keys();
        StringBuffer result = new StringBuffer();
        while (keys.hasMoreElements())
        {
            Object key = keys.nextElement();
            result.append(String.format("%s=%s", key, dictionary.get(key)));
            if (keys.hasMoreElements())
            {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /*
     * Provides an iterable collection of references, even if the original array
     * is null
     */
    public Collection<ServiceReference> asCollection(ServiceReference[] references)
    {
        return references != null ? Arrays.asList(references) : Collections.<ServiceReference>emptyList();
    }


    public String executeCommand(final String command)
    {
        return executeCommand(command, COMMAND_TIMEOUT, false);
    }

    public String executeCommand(final String command, final Long timeout, final Boolean silent)
    {
        String response;
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArrayOutputStream);
        final CommandProcessor commandProcessor = getOsgiService(CommandProcessor.class);
        final CommandSession commandSession = commandProcessor.createSession(System.in, printStream, System.err);
        FutureTask<String> commandFuture = new FutureTask<String>(
                new Callable<String>()
                {
                    public String call()
                    {
                        try
                        {
                            if (!silent)
                            {
                                System.err.println(command);
                            }
                            commandSession.execute(command);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace(System.err);
                        }
                        printStream.flush();
                        return byteArrayOutputStream.toString();
                    }
                }
        );

        try
        {
            executor.submit(commandFuture);
            response = commandFuture.get(timeout, TimeUnit.MILLISECONDS);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            response = "SHELL COMMAND TIMED OUT: ";
        }

        return response;
    }

    public String executeCommands(final String... commands)
    {
        String response;
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArrayOutputStream);
        final CommandProcessor commandProcessor = getOsgiService(CommandProcessor.class);
        final CommandSession commandSession = commandProcessor.createSession(System.in, printStream, System.err);
        FutureTask<String> commandFuture = new FutureTask<String>(
                new Callable<String>()
                {
                    public String call()
                    {
                        try
                        {
                            for (String command : commands)
                            {
                                System.err.println(command);
                                commandSession.execute(command);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace(System.err);
                        }
                        return byteArrayOutputStream.toString();
                    }
                }
        );

        try
        {
            executor.submit(commandFuture);
            response = commandFuture.get(COMMAND_TIMEOUT, TimeUnit.MILLISECONDS);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            response = "SHELL COMMAND TIMED OUT: ";
        }

        return response;
    }

    public Bundle getInstalledBundle(String symbolicName)
    {
        for (Bundle b : bundleContext.getBundles())
        {
            if (b.getSymbolicName().equals(symbolicName))
            {
                return b;
            }
        }
        for (Bundle b : bundleContext.getBundles())
        {
            System.err.println("Bundle: " + b.getSymbolicName());
        }
        throw new RuntimeException("Bundle " + symbolicName + " does not exist");
    }

    public <T> T getOsgiService(Class<T> type, long timeout)
    {
        return getOsgiService(type, null, timeout);
    }

    public <T> T getOsgiService(Class<T> type)
    {
        return getOsgiService(type, null, SERVICE_TIMEOUT);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> T getOsgiService(Class<T> type, String filter, long timeout)
    {
        ServiceTracker tracker = null;
        try
        {
            String flt;
            if (filter != null)
            {
                if (filter.startsWith("("))
                {
                    flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName() + ")" + filter + ")";
                }
                else
                {
                    flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName() + ")(" + filter + "))";
                }
            }
            else
            {
                flt = "(" + Constants.OBJECTCLASS + "=" + type.getName() + ")";
            }
            Filter osgiFilter = FrameworkUtil.createFilter(flt);
            tracker = new ServiceTracker(bundleContext, osgiFilter, null);
            tracker.open(true);
            // Note that the tracker is not closed to keep the reference
            // This is buggy, as the service reference may change i think
            Object svc = type.cast(tracker.waitForService(timeout));
            if (svc == null)
            {
                Dictionary dic = bundleContext.getBundle().getHeaders();
                System.err.println("CustomTest bundle headers: " + explode(dic));

                for (ServiceReference ref : asCollection(bundleContext.getAllServiceReferences(null, null)))
                {
                    System.err.println("ServiceReference: " + ref);
                }

                for (ServiceReference ref : asCollection(bundleContext.getAllServiceReferences(null, flt)))
                {
                    System.err.println("Filtered ServiceReference: " + ref);
                }

                throw new RuntimeException("Gave up waiting for service " + flt);
            }
            return type.cast(svc);
        }
        catch (InvalidSyntaxException e)
        {
            throw new IllegalArgumentException("Invalid filter", e);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
