package de.incompleteco.spring.batch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.sql.DataSource;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
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
	"classpath:/META-INF/spring/job-context.xml"})
@ActiveProfiles("local")
public class SimpleWaitJobIT {

	@Autowired
	private Job job;
	
	@Autowired
	private JobExplorer jobExplorer;
	
	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	private DataSource dataSource;
	
	@Test
	public void test() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder().addLong("runtime",System.currentTimeMillis()).toJobParameters();
		JobExecution execution = jobLauncher.run(job,jobParameters);
		//monitor
		while (jobExplorer.getJobExecution(execution.getId()).isRunning()) {
			Thread.sleep(100);
		}//end while
		//load
		execution = jobExplorer.getJobExecution(execution.getId());
		//check
		assertFalse(execution.getStatus().isUnsuccessful());
		//check that the 'remote' job has been registered
		assertTrue(new JdbcTemplate(dataSource).queryForObject("select count(*) from batch_job_entity",Integer.class) > 0);
	}
	
	@Ignore//will screw up JUNIT tests so run 'manually' and not in CI
	@Test
	public void testWithKill() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder().addLong("runtime",System.currentTimeMillis())
				.addString("fail","true").toJobParameters();
		JobExecution execution = jobLauncher.run(job,jobParameters);
		//monitor
		while (jobExplorer.getJobExecution(execution.getId()).isRunning()) {
			Thread.sleep(100);
		}//end while
		//load
		execution = jobExplorer.getJobExecution(execution.getId());
		//check
		assertFalse(execution.getStatus().isUnsuccessful());
	}	

}
