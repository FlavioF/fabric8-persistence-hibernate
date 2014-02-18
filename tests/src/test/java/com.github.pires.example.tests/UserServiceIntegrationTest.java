package com.github.pires.example.tests;

import com.github.pires.example.dal.UserService;
import com.github.pires.example.dal.entities.User;
import io.fabric8.api.Container;
import io.fabric8.api.ServiceLocator;
import io.fabric8.itests.paxexam.support.ContainerBuilder;
import io.fabric8.itests.paxexam.support.FabricTestSupport;
import io.fabric8.itests.paxexam.support.Provision;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.junit.ProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.framework.Constants;

import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;


@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class UserServiceIntegrationTest extends FabricTestSupport
{

    /**
     * @param probe
     * @return
     */
    @ProbeBuilder
    public TestProbeBuilder probeConfiguration(TestProbeBuilder probe)
    {
        // makes sure the generated CustomTest-Bundle contains this import!
        //probe.setHeader(Constants.BUNDLE_SYMBOLICNAME, "de.nierbeck.camel.exam.demo.route-control-test");
        probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "com.github.pires.example.tests,*,org.apache.felix.service.*;status=provisional");
        //probe.setHeader(Constants.IMPORT_PACKAGE, "io.fabric8.demo.cxf,org.fusesource.tooling.testing.pax.exam.karaf,*");
        probe.setHeader(Constants.IMPORT_PACKAGE, "org.fusesource.tooling.testing.pax.exam.karaf,*");
        probe.setHeader(Constants.BUNDLE_SYMBOLICNAME, "com.github.pires.example.tests");
        return probe;
    }

    @Before
    public void setUp() throws Exception
    {
        System.err.println(executeCommand("fabric:create -n"));


        //System.err.println(executeCommand("fabric:create --clean --wait-for-provisioning"));

        waitForFabricCommands();

        System.err.println(executeCommand("features:addUrl mvn:com.github.pires.example/feature-persistence/0.1-SNAPSHOT/xml/features", 10000, false));
        System.err.println(executeCommand("features:install swagger", 600000, false));
        System.err.println(executeCommand("features:install fabric-cxf", 600000, false));
        System.err.println(executeCommand("features:install persistence-aries-hibernate", 600000, false));

        System.err.println(executeCommand("osgi:install -s mvn:com.github.pires.example/datasource-hsqldb/0.1-SNAPSHOT"));
        System.err.println(executeCommand("osgi:install -s mvn:com.github.pires.example/dal/0.1-SNAPSHOT"));
        System.err.println(executeCommand("osgi:install -s mvn:com.github.pires.example/dal-impl/0.1-SNAPSHOT"));
        System.err.println(executeCommand("osgi:install -s mvn:com.github.pires.example/rest/0.1-SNAPSHOT"));


//        System.err.println(executeCommand("fabric:profile-create --parents example-quickstarts-rest persistence-example"));
//        System.err.println(executeCommand("fabric:profile-edit --repositories mvn:com.github.pires.example/feature-persistence/0.1-SNAPSHOT/xml/features persistence-example"));
//        System.err.println(executeCommand("fabric:profile-edit --features persistence-aries-hibernate persistence-example"));
//        System.err.println(executeCommand("fabric:profile-edit --bundles mvn:com.github.pires.example/datasource-hsqldb/0.1-SNAPSHOT persistence-example"));
//        System.err.println(executeCommand("fabric:profile-edit --bundles mvn:com.github.pires.example/dal/0.1-SNAPSHOT persistence-example"));
//        System.err.println(executeCommand("fabric:profile-edit --bundles mvn:com.github.pires.example/dal-impl/0.1-SNAPSHOT persistence-example"));
//        System.err.println(executeCommand("fabric:profile-edit --bundles mvn:com.github.pires.example/rest/0.1-SNAPSHOT persistence-example"));
    }

    @Configuration
    public Option[] config()
    {
//        return new Option[]{
//                // Provision and launch a container based on a distribution of Karaf (Apache ServiceMix).
//                karafDistributionConfiguration()
//                       /* .frameworkUrl(
//                                maven()
//                                        .groupId("org.fusesource.fabric")
//                                        .artifactId("fuse-fabric")
//                                        .version("7.2.0.redhat-024")
//                                        .type("zip")
//                        )*/
//                        .frameworkUrl(
//                                maven()
//                                        .groupId("fabric8")
//                                        .artifactId("fabric8-karaf")
//                                        .version("1.0.0.redhat-340")
//                                        .type("zip")
//                        )
//                        .karafVersion("2.2.6")
//                        .name("Fabric Karaf Distro")
//                        .unpackDirectory(new File("target/pax"))
//                        .useDeployFolder(false),
//                // It is really nice if the container sticks around after the test so you can check the contents
//                // of the data directory when things go wrong.
//                keepRuntimeFolder(),
//                // Don't bother with local console output as it just ends up cluttering the logs
//                configureConsole().ignoreLocalConsole(),
//                // Force the log level to INFO so we have more details during the test.  It defaults to WARN.
//                logLevel(LogLevel.INFO),
//
//                //load features from feature module
//                features(maven().groupId("com.github.pires.example").artifactId("feature-persistence").type("xml").classifier("features").version("0.1-SNAPSHOT"), "persistence-aries-hibernate"),
//                //features(maven().groupId("org.apache.karaf.features").artifactId("standard").type("xml").classifier("features").versionAsInProject(), "http-whiteboard"),
//                //features(maven().groupId("org.apache.karaf.features").artifactId("enterprise").type("xml").classifier("features").versionAsInProject(), "transaction", "jpa", "jndi"),
//                //features(maven().groupId("org.apache.cxf.karaf").artifactId("apache-cxf").type("xml").classifier("features").versionAsInProject(), "cxf-jaxws", "cxf", "war"),
//                //features(maven().groupId("org.apache.activemq").artifactId("activemq-karaf").type("xml").classifier("features").versionAsInProject(), "activemq-blueprint", "activemq-camel"),
//                //features(maven().groupId("org.apache.camel.karaf").artifactId("apache-camel").type("xml").classifier("features").versionAsInProject(), "camel-blueprint", "camel-jms","camel-jpa", "camel-mvel", "camel-jdbc", "camel-cxf", "camel-test"),
//
//                //load all necessary bundles
//                mavenBundle("com.github.pires.example", "datasource-hsqldb", "0.1-SNAPSHOT"),
//                mavenBundle("com.github.pires.example", "dal", "0.1-SNAPSHOT"),
//                mavenBundle("com.github.pires.example", "dal-impl", "0.1-SNAPSHOT"),
//                //mavenBundle("com.github.pires.example", "rest", "0.1-SNAPSHOT"),
////                mavenBundle().groupId( "org.eclipse.jetty.osgi" ).artifactId( "jetty-osgi-boot" ).versionAsInProject(),
////                mavenBundle().groupId( "org.eclipse.jetty.osgi" ).artifactId( "jetty-osgi-boot-jsp" ).versionAsInProject(),
////
////                mavenBundle().groupId( "org.eclipse.jetty" ).artifactId( "jetty-websocket" ).versionAsInProject(),
////                mavenBundle().groupId( "org.eclipse.jetty" ).artifactId( "jetty-servlets" ).versionAsInProject(),
//
//
//                // Remember that the test executes in another process.  If you want to debug it, you need
//                // to tell Pax Exam to launch that process with debugging enabled.  Launching the test class itself with
//                // debugging enabled (for example in Eclipse) will not get you the desired results.
//                //debugConfiguration("5000", true),
//        };

//        return new Option[]{
//                new DefaultCompositeOption(fabricDistributionConfiguration()),
//                mavenBundle("commons-io", "commons-io").versionAsInProject(),
//                //systemProperty("fabricitest.version").value(System.getProperty("fabricitest.version"))
//        };

        return combine(
                fabricDistributionConfiguration()
                //mavenBundle("org.fusesource.examples", "fabric-cxf-demo-common"),
                // Passing the system property to the test container
                //systemProperty("fabricitest.version").value(System.getProperty("fabricitest.version"))
        );
    }

    @Test
    public void shouldCreateAndFindUser_DAL() throws Exception
    {
//        for (Bundle b : bundleContext.getBundles())
//        {
//            System.out.println("Bundle: " + b.getSymbolicName() + "; ID: " + b.getBundleId() + "; State: " + b.getState());
//        }
//
//        //restart hibernate bundle
//        Bundle hibernateService = getInstalledBundle("org.hibernate.osgi");
//        Bundle airesService = getInstalledBundle("org.apache.aries.jpa.container");
//
//        /*executeCommand("osgi:restart " + airesService.getBundleId());
//        Thread.sleep(1000);*/
//
//        executeCommand("osgi:restart " + hibernateService.getBundleId());
//        Thread.sleep(1000);
//
//
//        System.out.println("--------------> moving on");


        //is  service available ?
//        final UserService service = ServiceLocator.getOsgiService(UserService.class);
//        assertNotNull(service);
//
//        User user = new User();
//        user.setName("alberto");
//
//        service.create(user);
//
//        List<User> result = service.findAll();
//
//        Assert.assertThat(result.size(), Matchers.is(1));

        assertTrue(Provision.profileAvailable("persistence-example", "1.0", DEFAULT_TIMEOUT));
        Set<Container> containers = ContainerBuilder.create().withName("cnt1").withProfiles("default").assertProvisioningResult().build();
        try
        {
            assertTrue("We should have one container.", containers.size() == 1);
            System.err.println("created the cxf-server container.");
            // install bundle of CXF
            Thread.sleep(2000);
            System.err.println(executeCommand("fabric:cluster-list"));
            // install bundle of CXF
            Thread.sleep(2000);
            // calling the client here

            UserService proxy = ServiceLocator.awaitService(UserService.class);
            //assertNotNull(proxy);

            User user = new User();
            user.setName("alberto");
            proxy.create(user);

            //assertNotSame("We should get the two different result", result1, result2);
        }
        finally
        {
            ContainerBuilder.destroy();
        }
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


}
