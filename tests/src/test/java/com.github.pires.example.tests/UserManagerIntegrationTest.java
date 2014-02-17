package com.github.pires.example.tests;

import com.github.pires.example.dal.UserService;
import org.apache.karaf.features.FeaturesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

import javax.inject.Inject;
import java.io.File;
import java.util.Dictionary;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

@RunWith(PaxExam.class)
public class UserManagerIntegrationTest
{
    static final Long SERVICE_TIMEOUT = 30000L;

    @Inject
    private BundleContext bundleContext;

    @Inject
    private ConfigurationAdmin configurationAdmin;

    @Inject
    protected FeaturesService featuresService;

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

                //load all necessary bundles
                mavenBundle("com.github.pires.example", "datasource-hsqldb", "0.1-SNAPSHOT"),
                mavenBundle("com.github.pires.example", "dal", "0.1-SNAPSHOT"),
                mavenBundle("com.github.pires.example", "dal-impl", "0.1-SNAPSHOT"),
                //mavenBundle("com.github.pires.example", "rest", "0.1-SNAPSHOT")

                // Remember that the test executes in another process.  If you want to debug it, you need
                // to tell Pax Exam to launch that process with debugging enabled.  Launching the test class itself with
                // debugging enabled (for example in Eclipse) will not get you the desired results.
                //debugConfiguration("5000", true),
        };
    }

    @Test
    public void test() throws Exception
    {

        // Since PAX Exam doesn't provide any sort of proxying to the service reference injected using
        // @Inject, we do it ourselves here as the reconfiguration may result in a change to the service.
        // When using Blueprint w/ the configuration management service integration from Aries, this will
        // happen.


        assertTrue(featuresService.isInstalled(featuresService.getFeature("persistence-aries-hibernate")));

        final UserService service = getOsgiService(UserService.class);
        assertNotNull(service);



        /*ServiceReference serviceReference = bundleContext.getServiceReference(UserManager.class.getName());
        assertNotNull(serviceReference);

        UserManager service = (UserManager) bundleContext.getService(serviceReference);
        assertNotNull(service);*/

        /*try
        {
            assertEquals("Hello Bob.", service.sayHello("Bob"));
        }
        finally
        {
            bundleContext.ungetService(serviceReference);
        }

        org.osgi.service.cm.Configuration configuration = configurationAdmin.getConfiguration(
                "valeri.blog.example.pax-exam-servicemix", null
        );

        assertNull(configuration.getProperties());

        Dictionary<String, Object> dict = new Hashtable<String, Object>();
        dict.put("hello", "Hola");

        configuration.update(dict);

        // Wait a little because the configuration event is asynchronous.
        Thread.sleep(2000l);


        serviceReference = bundleContext.getServiceReference(Service.class.getName());
        assertNotNull(serviceReference);

        service = (Service) bundleContext.getService(serviceReference);
        assertNotNull(service);
        try
        {
            assertEquals("Hola Bob.", service.sayHello("Bob"));
        }
        finally
        {
            bundleContext.ungetService(serviceReference);
        }*/
    }

    protected <T> T getOsgiService(Class<T> type, long timeout)
    {
        return getOsgiService(type, null, timeout);
    }

    protected <T> T getOsgiService(Class<T> type)
    {
        return getOsgiService(type, null, SERVICE_TIMEOUT);
    }

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
