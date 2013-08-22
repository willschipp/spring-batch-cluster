package de.incompleteco.spring.batch.performance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/META-INF/spring/batch-resource-context.xml",
	"classpath:/META-INF/spring/job-context.xml",
	"classpath:/META-INF/spring/writebehind-context.xml"})
@ActiveProfiles("local")
public class SimpleJobIT {

	@Autowired
	private Job job;
	
	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	private JobExplorer jobExplorer;
	
	@Autowired
	private DataSource dataSource;
	
	private JdbcTemplate jdbcTemplate;
	
	@Before
	public void before() {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Test
	public void test() throws Exception {
		JobParameters parameters = new JobParametersBuilder().addLong("runtime",System.currentTimeMillis()).toJobParameters();
		JobExecution execution = jobLauncher.run(job, parameters);
		//check
		while (jobExplorer.getJobExecution(execution.getId()).isRunning()) {
			Thread.sleep(1000);
		}//end while
		//load
		execution = jobExplorer.getJobExecution(execution.getId());
		//check
		assertFalse(execution.getStatus().isUnsuccessful());
		//check what's in the database
		assertTrue(jdbcTemplate.queryForObject("select count(*) from batch_job_execution", Integer.class) > 0);
		assertTrue(jdbcTemplate.queryForObject("select count(*) from batch_job_instance", Integer.class) > 0);
		assertTrue(jdbcTemplate.queryForObject("select count(*) from batch_step_execution", Integer.class) > 0);
		//init
		boolean passed = true;
		//verify the integrity of the database to the jobExplorer
		for (String name : jobExplorer.getJobNames()) {
			for (JobInstance instance : jobExplorer.getJobInstances(name, 0, Integer.MAX_VALUE)) {
				for (JobExecution jobExecution : jobExplorer.getJobExecutions(instance)) {
					for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
						int databaseVersion = jdbcTemplate.queryForObject("select version from batch_step_execution where step_execution_id = " + stepExecution.getId(),Integer.class);
						//retrieve components from the database and compare
						assertEquals(jdbcTemplate.queryForObject("select status from batch_step_execution where step_execution_id = " + stepExecution.getId(), String.class).toUpperCase(),stepExecution.getStatus().name().toUpperCase());
						assertTrue(databaseVersion == stepExecution.getVersion());
						//set
						passed = true;
					}//end for
					int databaseVersion = jdbcTemplate.queryForObject("select version from batch_job_execution where job_execution_id = " + jobExecution.getId(),Integer.class);
					//check
					assertEquals(jdbcTemplate.queryForObject("select status from batch_job_execution where job_execution_id = " + jobExecution.getId(), String.class).toUpperCase(),jobExecution.getStatus().name().toUpperCase());
					assertTrue(databaseVersion == jobExecution.getVersion());
					//check the job parameters
					assertEquals(jdbcTemplate.queryForObject("select long_val from BATCH_JOB_EXECUTION_PARAMS where job_execution_id = " + jobExecution.getId(), Long.class),jobExecution.getJobParameters().getLong("runtime"));
					//set
					passed = true;
				}//end for
				assertTrue(jdbcTemplate.queryForObject("select count(*) from batch_job_instance where job_instance_id = " + instance.getId(), Integer.class) == 1);
				//set
				passed = true;
			}//end for
		}//end for
		assertTrue(passed);
	}
	
}
