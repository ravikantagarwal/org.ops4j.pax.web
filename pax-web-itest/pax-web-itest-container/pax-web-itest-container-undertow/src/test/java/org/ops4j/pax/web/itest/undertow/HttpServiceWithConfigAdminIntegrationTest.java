package org.ops4j.pax.web.itest.undertow;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.web.itest.base.VersionUtil;
import org.ops4j.pax.web.service.WebContainerConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.service.cm.ConfigurationAdmin;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;


/**
 * @author Toni Menzel (tonit)
 * @since Mar 3, 2009
 */
@RunWith(PaxExam.class)
public class HttpServiceWithConfigAdminIntegrationTest extends ITestBase {

	private Bundle installWarBundle;
	
	@Inject
	private ConfigurationAdmin caService;
	

	@Configuration
	public static Option[] configure() {
		return combine(configureUndertow(),
	            mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.4.0"));
	}

	@Before
	public void setUp() throws BundleException, InterruptedException, IOException {

		org.osgi.service.cm.Configuration config = caService.getConfiguration(WebContainerConstants.PID);

		Dictionary<String,Object> props = new Hashtable<String,Object>();

        props.put(WebContainerConstants.PROPERTY_LISTENING_ADDRESSES,"127.0.0.1");
		props.put(WebContainerConstants.PROPERTY_HTTP_PORT,"8181");
        
		config.setBundleLocation(null);
        config.update(props);
		
		String bundlePath = "mvn:org.ops4j.pax.web.samples/helloworld-hs/" + VersionUtil.getProjectVersion();
		installWarBundle = installAndStartBundle(bundlePath);
		
		waitForServer("http://127.0.0.1:8181/");
	}

	@After
	public void tearDown() throws BundleException {
		if (installWarBundle != null) {
			installWarBundle.stop();
			installWarBundle.uninstall();
		}
	}

	/**
	 * You will get a list of bundles installed by default plus your testcase,
	 * wrapped into a bundle called pax-exam-probe
	 */
	@Test
	public void listBundles() {
		for (Bundle b : bundleContext.getBundles()) {
			System.out.println("Bundle " + b.getBundleId() + " : "
					+ b.getSymbolicName());
		}

	}

	@Test
	public void testSubPath() throws Exception {

		testClient.testWebPath("http://127.0.0.1:8181/helloworld/hs", "Hello World");
		
		//test to retrive Image
		testClient.testWebPath("http://127.0.0.1:8181/images/logo.png", "", 200, false);
		
	}

	@Test
	public void testRootPath() throws Exception {

		testClient.testWebPath("http://127.0.0.1:8181/", "");

	}
	
	@Test
	public void testServletPath() throws Exception {

		testClient.testWebPath("http://127.0.0.1:8181/lall/blubb", "Servlet Path: ");
		testClient.testWebPath("http://127.0.0.1:8181/lall/blubb", "Path Info: /lall/blubb");

	}
	
	@Test
	public void testServletDeRegistration() throws Exception {
		
		if (installWarBundle != null) {
			installWarBundle.stop();
		}
	}
	
	@Test
	public void testReconfiguration() throws Exception {
		
		testClient.testWebPath("http://127.0.0.1:8181/lall/blubb",
				"Servlet Path: ");
		testClient.testWebPath("http://127.0.0.1:8181/lall/blubb",
				"Path Info: /lall/blubb");

		org.osgi.service.cm.Configuration config = caService.getConfiguration(WebContainerConstants.PID);

		Dictionary<String,Object> props = new Hashtable<String,Object>();

        props.put(WebContainerConstants.PROPERTY_LISTENING_ADDRESSES,"127.0.0.1");
		props.put(WebContainerConstants.PROPERTY_HTTP_PORT,"9191");
        
		config.setBundleLocation(null);
        config.update(props);

		waitForServer("http://127.0.0.1:9191/");

		testClient.testWebPath("http://127.0.0.1:9191/lall/blubb", "Servlet Path: ");
		testClient.testWebPath("http://127.0.0.1:9191/lall/blubb", "Path Info: /lall/blubb");

	}

}
