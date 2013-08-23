package de.incompleteco.spring.batch.performance;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.sql.DataSource;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;


//shouldn't be run in a CI environment
public class TestFullStartupShutdown {
	

	private String[] locations = {"classpath:/META-INF/spring/batch-resource-context.xml",
			"classpath:/META-INF/spring/job-context.xml",
			"classpath:/META-INF/spring/writebehind-context.xml",
			"classpath:/META-INF/spring/loader-context.xml"};
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		//start a builder
		SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		//setup the services (h2 and amq)
		InfrastructureUtils.startH2();
		//setup the database
		DataSource dataSource = InfrastructureUtils.bindLocalH2("DataSource");
		setupH2Data(dataSource);
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		//shutdown the services
		InfrastructureUtils.stopH2();
	}	
	
	
	@Test
	public void test() throws Exception {
		//going to test a 'full' startup/shutdown scenario
		//- start up the app context w/ a db behind it
		//- run a job (using mapJobRepo --> write behind to the db)
		//- shutdown the app context "losing" the mapJobRepo
		//- verify data still exists for the execution it the db
		//- startup an app context again
		//- check that the mapJobRepo is populated
		
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(locations);
		//now that it's up, extract and 'run' a job
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);
		JobExplorer jobExplorer = context.getBean("jobExplorer",JobExplorer.class);
		Job job = context.getBean(Job.class);
		//build some parameters
		JobParameters jobParameters = new JobParametersBuilder().addLong("runtime",System.currentTimeMillis()).toJobParameters();
		//execute
		JobExecution jobExecution = jobLauncher.run(job,jobParameters);
		//monitor
		while (jobExplorer.getJobExecution(jobExecution.getId()).isRunning()) {
			Thread.sleep(100);
		}//end while
		//reload
		jobExecution = jobExplorer.getJobExecution(jobExecution.getId());
		//check
		assertFalse(jobExecution.getStatus().isUnsuccessful());
		//now kill off the context to make sure the map repo is destroyed
		context.stop();
		assertFalse(context.isRunning());
		context = null;//kill the context?
		//lets kill off some beans
		jobLauncher = null;
		jobExplorer = null;
		job = null;
		//now lets test 'restarting' it
		context = new ClassPathXmlApplicationContext(locations);
		//lets check that the executions have reloaded
		jobExplorer = context.getBean("jobExplorer",JobExplorer.class);
		//check
		assertTrue(jobExplorer.getJobNames().size() == 1);
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
		//create statements
		statements = InfrastructureUtils.convertSqlFile("/org/springframework/batch/core/schema-h2.sql");
		for (String statement : statements) {
			template.execute(statement);
		}//end for		
	}
		

}
