package de.incompleteco.spring.batch;

import java.io.File;
import java.io.PrintStream;

import javax.sql.DataSource;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.web.servlet.DispatcherServlet;



public class EmbeddedTomcatIT {

	//bootstrap the jndi services
	@BeforeClass
	public static void beforeClass() throws Exception {
		//start a builder
		SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		//setup the services (h2 and amq)
		InfrastructureUtils.startH2();
		//setup the database
		DataSource dataSource = InfrastructureUtils.bindLocalH2("DataSource");
		setupH2Data(dataSource);
		//start amq
		InfrastructureUtils.startAMQ();
		//bind
		InfrastructureUtils.bindLocalAMQ("ConnectionFactory","batch.request.queue","batch.reply.queue");
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		//shutdown the services
		InfrastructureUtils.stopH2();
		InfrastructureUtils.stopAMQ();
	}
	
	private static void setupH2Data(DataSource dataSource) throws Exception {
		//execute the statements
		JdbcTemplate template = new JdbcTemplate(dataSource);
		//drop statements
		String[] statements = InfrastructureUtils.convertSqlFile("/org/springframework/batch/core/schema-drop-h2.sql");
		try {
			for (String statement : statements) {
				template.execute(statement);
			}//end for
		}
		catch (Exception e) { }
		statements = InfrastructureUtils.convertSqlFile("/META-INF/sql/schema-ext-drop-h2.sql");
		try {
			for (String statement : statements) {
				template.execute(statement);
			}//end for
		}
		catch (Exception e) { }		
		//create statements
		statements = InfrastructureUtils.convertSqlFile("/org/springframework/batch/core/schema-h2.sql");
		for (String statement : statements) {
			template.execute(statement);
		}//end for		
		//create statements
		statements = InfrastructureUtils.convertSqlFile("/META-INF/sql/schema-ext-h2.sql");
		for (String statement : statements) {
			template.execute(statement);
		}//end for				
	}	
	
	@Test 
	public void test() throws Exception {
		//start up the 'remote' node
		new Thread(new RemoteJVMRunner()).start();		
		//start up an embedded tomcat container
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(8080);
		//context
		Context context = tomcat.addContext("/", new File(".").getAbsolutePath());
//		ApplicationParameter activeProfileParameter = new ApplicationParameter();
//		activeProfileParameter.setName("spring.profiles.active");
//		activeProfileParameter.setValue("local");
//		context.addApplicationParameter(activeProfileParameter);
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
	
	
	class RemoteJVMRunner implements Runnable {
		
		@Override
		public void run() {
			Project project = new Project();
			project.setName("remote-jvm");
			project.init();
			//setup the logger
			DefaultLogger logger = new DefaultLogger();
			project.addBuildListener(logger);
			logger.setOutputPrintStream(System.out);
			logger.setErrorPrintStream(System.err);
			logger.setMessageOutputLevel(Project.MSG_INFO);
			System.setOut(new PrintStream(new DemuxOutputStream(project,false)));
			System.setErr(new PrintStream(new DemuxOutputStream(project,true)));
			//start the project
			project.fireBuildStarted();
			
			Java java = new Java();
			java.setProject(project);;
			java.setTaskName("run-remote-jvm");
			java.setFork(true);
			java.setFailonerror(true);
			//set the classname
			java.setClassname(RemoteJVMEmulator.class.getName());
			java.setClasspath(new Path(project,System.getProperty("java.class.path")));
			//create arguments
			java.createArg().setValue("classpath:/META-INF/spring/client/client-context.xml");
			//init
			java.init();
			//execute
			java.executeJava();
		}
	}	
}
