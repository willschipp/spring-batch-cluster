package de.incompleteco.spring.batch;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.startup.Tomcat;
import org.junit.Test;
import org.springframework.web.servlet.DispatcherServlet;

public class EmbeddedTomcatIT {

	@Test 
	public void test() throws Exception {
		//start up an embedded tomcat container
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(8080);
		//context
		Context context = tomcat.addContext("/", new File(".").getAbsolutePath());
		ApplicationParameter activeProfileParameter = new ApplicationParameter();
		activeProfileParameter.setName("spring.profiles.active");
		activeProfileParameter.setValue("local");
		context.addApplicationParameter(activeProfileParameter);
		//add a servlet
		Wrapper wrapper = Tomcat.addServlet(context, "dispatcher", new DispatcherServlet());
		wrapper.addInitParameter("contextConfigLocation", "classpath:/META-INF/spring/app-context.xml");
		wrapper.setLoadOnStartup(1);;
		//set values
		context.addServletMapping("/*", "dispatcher");
		//start
		tomcat.start();
		tomcat.getServer().await();//wait
	}
	
}
