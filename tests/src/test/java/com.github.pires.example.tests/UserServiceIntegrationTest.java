package com.github.pires.example.tests;

import com.github.pires.example.dal.UserService;
import com.github.pires.example.dal.entities.User;
import io.fabric8.api.Container;
import io.fabric8.itests.paxexam.support.ContainerBuilder;
import io.fabric8.itests.paxexam.support.FabricTestSupport;
import io.fabric8.itests.paxexam.support.SshContainerBuilder;
import org.apache.karaf.tooling.exam.options.DoNotModifyLogOption;
import org.fusesource.tooling.testing.pax.exam.karaf.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.junit.ProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.EagerSingleStagedReactorFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import java.io.File;
import java.util.Set;

import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.OptionUtils.combine;


@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(EagerSingleStagedReactorFactory.class)
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
        //System.err.println(executeCommand("features:install fabric-cxf/1.0.0.redhat-340", 600000, false));

        System.err.println(executeCommand("features:install persistence-aries-hibernate", 600000, false));


        System.err.println(executeCommand("osgi:install -s mvn:com.github.pires.example/datasource-hsqldb/0.1-SNAPSHOT"));
        System.err.println(executeCommand("osgi:install -s mvn:com.github.pires.example/dal/0.1-SNAPSHOT"));
        System.err.println(executeCommand("osgi:install -s mvn:com.github.pires.example/dal-impl/0.1-SNAPSHOT"));
        //System.err.println(executeCommand("osgi:install -s mvn:com.github.pires.example/rest/0.1-SNAPSHOT"));

        System.err.println("setUp all done");
    }

    @After
    public void tearDown() throws InterruptedException
    {
        //ContainerBuilder.destroy();
    }

    @Configuration
    public Option[] config()
    {
       return combine(
                /*new Option[]{
                        karafDistributionConfiguration().frameworkUrl(
                                maven().groupId(GROUP_ID).artifactId(ARTIFACT_ID).version("1.0.0.redhat-346").type("zip")
                        )
                                .karafVersion(getKarafVersion()).name("Fabric Karaf Distro").unpackDirectory(new File("target/paxexam/unpack/")),
                        useOwnExamBundlesStartLevel(50),
                        envAsSystemProperty(ContainerBuilder.CONTAINER_TYPE_PROPERTY, "child"),
                        envAsSystemProperty(ContainerBuilder.CONTAINER_NUMBER_PROPERTY, "1"),
                        envAsSystemProperty(SshContainerBuilder.SSH_HOSTS_PROPERTY),
                        envAsSystemProperty(SshContainerBuilder.SSH_USERS_PROPERTY),
                        envAsSystemProperty(SshContainerBuilder.SSH_PASSWORD_PROPERTY),
                        envAsSystemProperty(SshContainerBuilder.SSH_RESOLVER_PROPERTY),

                        editConfigurationFilePut("etc/config.properties", "karaf.startlevel.bundle", "50"),
                        editConfigurationFilePut("etc/config.properties", "karaf.startup.message", "Loading Fabric from: ${karaf.home}"),
                        editConfigurationFilePut("etc/users.properties", "admin", "admin,admin"),
                        mavenBundle("io.fabric8.itests", "fabric-itests-common", MavenUtils.getArtifactVersion("io.fabric8.itests", "fabric-itests-common")),
                        mavenBundle("org.fusesource.tooling.testing", "pax-exam-karaf", MavenUtils.getArtifactVersion("org.fusesource.tooling.testing", "pax-exam-karaf")),
                        new DoNotModifyLogOption(),
                        keepRuntimeFolder()
                }*/
               fabricDistributionConfiguration()
        );
    }

    @Test
    public void shouldCreateUserWith_UserService() throws Exception
    {
        Set<Container> containers = ContainerBuilder.create().withName("cnt1").withProfiles("karaf").assertProvisioningResult().build();

        Thread.sleep(2000);
        Bundle b = getInstalledBundle("org.hibernate.osgi");
        System.err.println(executeCommand("osgi:restart " + b.getBundleId()));

        assertTrue("We should have one container.", containers.size() == 1);
        System.err.println("created the cxf-server container.");

        Thread.sleep(2000);
        System.err.println(executeCommand("fabric:cluster-list"));

        try
        {
            Thread.sleep(2000);
            UserService proxy = ServiceLocator.getOsgiService(UserService.class);
            assertNotNull(proxy);

            User user = new User();
            user.setName("alberto");
            proxy.create(user);
        }
        finally
        {
            //ContainerBuilder.destroy();
        }


        //Thread.sleep(600000);
    }

    //@Test
    public void shouldCreateUserWith_UserManager() throws Exception
    {
        Set<Container> containers = ContainerBuilder.create().withName("cnt2").withProfiles("example-quickstarts-rest").assertProvisioningResult().build();

        Thread.sleep(2000);
        Bundle b = getInstalledBundle("org.hibernate.osgi");
        System.err.println(executeCommand("osgi:restart " + b.getBundleId()));

        assertTrue("We should have one container.", containers.size() == 1);
        System.err.println("created the cxf-server container.");

        Thread.sleep(2000);
        System.err.println(executeCommand("fabric:cluster-list"));

        try
        {
            Thread.sleep(2000);
//            UserService proxy = ServiceLocator.getOsgiService(UserService.class);
//            assertNotNull(proxy);
//
//            User user = new User();
//            user.setName("alberto");
//            proxy.create(user);
        }
        finally
        {
            //ContainerBuilder.destroy();
        }
    }


}
