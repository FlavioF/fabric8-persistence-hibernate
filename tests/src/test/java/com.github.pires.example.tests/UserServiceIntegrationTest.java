package com.github.pires.example.tests;

import com.github.pires.example.dal.UserService;
import com.github.pires.example.dal.entities.User;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.features.FeaturesService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class UserServiceIntegrationTest
{
    static final Long SERVICE_TIMEOUT = 30000L;
    static final Long COMMAND_TIMEOUT = 10000L;

    private ExecutorService executor = Executors.newCachedThreadPool();

    @Inject
    private CommandProcessor commandProcessor;


    @Inject
    private BundleContext bundleContext;

    @Inject
    private ConfigurationAdmin configurationAdmin;

    @Inject
    protected FeaturesService featuresService;

    /**
     * @param probe
     * @return
     */
    @ProbeBuilder
    public TestProbeBuilder probeConfiguration(TestProbeBuilder probe)
    {
        // makes sure the generated Test-Bundle contains this import!
        //probe.setHeader(Constants.BUNDLE_SYMBOLICNAME, "de.nierbeck.camel.exam.demo.route-control-test");
        probe.setHeader(Constants.BUNDLE_SYMBOLICNAME, "com.github.pires.example.tests");
        //probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "de.nierbeck.camel.exam.demo.control,*,org.apache.felix.service.*;status=provisional");
        probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "com.github.pires.example.tests,*,org.apache.felix.service.*;status=provisional");

        return probe;
    }

    @Configuration
    public Option[] config()
    {
        return new Option[]{
                // Provision and launch a container based on a distribution of Karaf (Apache ServiceMix).
                karafDistributionConfiguration()
                        .frameworkUrl(
                                maven()
                                        .groupId("org.fusesource.fabric")
                                        .artifactId("fuse-fabric")
                                        .version("7.2.0.redhat-024")
                                        .type("zip")
                        )
                        .karafVersion("2.3.0")
                        .name("Fabric Karaf Distro")
                        .unpackDirectory(new File("target/pax"))
                        .useDeployFolder(false),
                // It is really nice if the container sticks around after the test so you can check the contents
                // of the data directory when things go wrong.
                keepRuntimeFolder(),
                // Don't bother with local console output as it just ends up cluttering the logs
                configureConsole().ignoreLocalConsole(),
                // Force the log level to INFO so we have more details during the test.  It defaults to WARN.
                logLevel(LogLevel.INFO),

                //load features from feature module
                features(maven().groupId("com.github.pires.example").artifactId("feature-persistence").type("xml").classifier("features").version("0.1-SNAPSHOT"), "persistence-aries-hibernate"),
                //features(maven().groupId("org.apache.karaf.features").artifactId("standard").type("xml").classifier("features").versionAsInProject(), "http-whiteboard"),
                //features(maven().groupId("org.apache.karaf.features").artifactId("enterprise").type("xml").classifier("features").versionAsInProject(), "transaction", "jpa", "jndi"),
                //features(maven().groupId("org.apache.cxf.karaf").artifactId("apache-cxf").type("xml").classifier("features").versionAsInProject(), "cxf-jaxws", "cxf", "war"),
                //features(maven().groupId("org.apache.activemq").artifactId("activemq-karaf").type("xml").classifier("features").versionAsInProject(), "activemq-blueprint", "activemq-camel"),
                //features(maven().groupId("org.apache.camel.karaf").artifactId("apache-camel").type("xml").classifier("features").versionAsInProject(), "camel-blueprint", "camel-jms","camel-jpa", "camel-mvel", "camel-jdbc", "camel-cxf", "camel-test"),

                //load all necessary bundles
                mavenBundle("com.github.pires.example", "datasource-hsqldb", "0.1-SNAPSHOT"),
                mavenBundle("com.github.pires.example", "dal", "0.1-SNAPSHOT"),
                mavenBundle("com.github.pires.example", "dal-impl", "0.1-SNAPSHOT"),
                //mavenBundle("com.github.pires.example", "rest", "0.1-SNAPSHOT"),
//                mavenBundle().groupId( "org.eclipse.jetty.osgi" ).artifactId( "jetty-osgi-boot" ).versionAsInProject(),
//                mavenBundle().groupId( "org.eclipse.jetty.osgi" ).artifactId( "jetty-osgi-boot-jsp" ).versionAsInProject(),
//
//                mavenBundle().groupId( "org.eclipse.jetty" ).artifactId( "jetty-websocket" ).versionAsInProject(),
//                mavenBundle().groupId( "org.eclipse.jetty" ).artifactId( "jetty-servlets" ).versionAsInProject(),


                // Remember that the test executes in another process.  If you want to debug it, you need
                // to tell Pax Exam to launch that process with debugging enabled.  Launching the test class itself with
                // debugging enabled (for example in Eclipse) will not get you the desired results.
                //debugConfiguration("5000", true),
        };
    }

    @Test
    public void shouldCreateAndFindUser_DAL() throws Exception
    {
        for (Bundle b : bundleContext.getBundles())
        {
            System.out.println("Bundle: " + b.getSymbolicName() + "; ID: " + b.getBundleId() + "; State: " + b.getState());
        }

        //restart hibernate bundle
        Bundle b = getInstalledBundle("org.hibernate.osgi");
        Bundle c = getInstalledBundle("org.apache.aries.jpa.container");

        executeCommand("osgi:restart " + c.getBundleId());

        Thread.sleep(1000);

        executeCommand("osgi:restart " + b.getBundleId());


        System.out.println("--------------> moving on");


        //is  service available ?
        final UserService service = getOsgiService(UserService.class);
        assertNotNull(service);

        User user = new User();
        user.setName("alberto");

        service.create(user);

        List<User> result = service.findAll();

        assertThat(result.size(), Matchers.is(1));
    }

    /*@Test
    public void shouldCreateAndFindUser_REST() throws Exception
    {
        client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        try



        //is  service available ?
        final UserManager service = getOsgiService(UserManager.class);
        assertNotNull(service);



        String content = "content";
        HttpClient client = new HttpClient();
        String url = "http://localhost:8080/cxf/demo/user";
        EntityEnclosingMethod method = new PutMethod(url);
        InputStream inputSteam = new ByteArrayInputStream(content.toString().getBytes());
        method.setRequestEntity(new InputStreamRequestEntity(inputSteam, content.length()));
        try
        {
            int statusCode = client.executeMethod(method);
            assertEquals(201, statusCode);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally
        {
            method.releaseConnection();
        }
    }*/


    protected String executeCommand(final String command)
    {
        return executeCommand(command, COMMAND_TIMEOUT, false);
    }

    protected String executeCommand(final String command, final Long timeout, final Boolean silent)
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

    protected String executeCommands(final String... commands)
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

    protected Bundle getInstalledBundle(String symbolicName)
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

    protected <T> T getOsgiService(Class<T> type, long timeout)
    {
        return getOsgiService(type, null, timeout);
    }

    protected <T> T getOsgiService(Class<T> type)
    {
        return getOsgiService(type, null, SERVICE_TIMEOUT);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected <T> T getOsgiService(Class<T> type, String filter, long timeout)
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
                System.err.println("Test bundle headers: " + TestUtility.explode(dic));

                for (ServiceReference ref : TestUtility.asCollection(bundleContext.getAllServiceReferences(null, null)))
                {
                    System.err.println("ServiceReference: " + ref);
                }

                for (ServiceReference ref : TestUtility.asCollection(bundleContext.getAllServiceReferences(null, flt)))
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
