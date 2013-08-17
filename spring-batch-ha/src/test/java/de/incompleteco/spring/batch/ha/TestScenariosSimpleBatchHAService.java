package de.incompleteco.spring.batch.ha;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.PrintStream;
import java.util.List;

import javax.sql.DataSource;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

//don't run this test in CI
public class TestScenariosSimpleBatchHAService {

	
	@BeforeClass
	public static void beforeClass() throws Exception {
		//start a builder
		SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		//setup the services (h2 and amq)
		InfrastructureUtils.startH2();
		//setup the database
		DataSource dataSource = InfrastructureUtils.bindLocalH2();
		setupH2Data(dataSource);
		//start amq
		InfrastructureUtils.startAMQ();
		//bind
		InfrastructureUtils.bindLocalAMQ();
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		//shutdown the services
		InfrastructureUtils.stopH2();
		InfrastructureUtils.stopAMQ();
	}
	
	private static void setupH2Data(DataSource dataSource) throws Exception {
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
	public void testExecute() throws Exception {
		//start a thread to run the remote
		new Thread(new RemoteJVMRunner()).start();
		//start another 'node' in the cluster
		new Thread(new RemoteJVMRunner()).start();
		//now start the 'server'
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/META-INF/spring/server-context.xml");
		//now that it's started, run the job
		Job job = context.getBean(Job.class);
		JobParameters parameters = new JobParametersBuilder().addLong("runtime",System.currentTimeMillis())
				.addString("fail", "true").toJobParameters();
		JobLauncher launcher = context.getBean("remoteJobLauncher", JobLauncher.class);
		JobExecution execution = launcher.run(job,parameters);
		JobExplorer explorer = context.getBean(JobExplorer.class);
		//monitor
		while (explorer.getJobExecution(execution.getId()).isRunning()) {
			Thread.sleep(500);
		}//end while
		//reload the execution
		execution = explorer.getJobExecution(execution.getId());
		//check
		assertTrue(execution.getStatus().isUnsuccessful());
		//give 1 second to see if the other has been executed
		Thread.sleep(1000);
		//now lets see if there's any other version of the job running...
		List<JobExecution> executions = explorer.getJobExecutions(execution.getJobInstance());
		assertNotNull(executions);
		assertTrue(executions.size() == 2);//should be 2 attempts
		Long executionId = null;
		//get the second and monitor
		for (JobExecution jobExecution : executions) {
			if (jobExecution.getId() != execution.getId()) {
				//this is it
				executionId = jobExecution.getId();
				break;
			}//end if
		}//end for
		//now retrieve
		while (explorer.getJobExecution(executionId).isRunning()) {
			Thread.sleep(500);
		}//end while
		execution = explorer.getJobExecution(executionId);
		//check
		assertFalse(execution.getStatus().isUnsuccessful());
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
			java.setTaskName("run-remote-jvm - " + Thread.currentThread().getId());
			java.setFork(true);
			java.setFailonerror(true);
			//set the classname
			java.setClassname(RemoteJVMEmulator.class.getName());
			java.setClasspath(new Path(project,System.getProperty("java.class.path")));
			//create arguments
			java.createArg().setValue("classpath:/META-INF/spring/client-context.xml");
			//init
			java.init();
			//execute
			java.executeJava();
		}
	}
}
