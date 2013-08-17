package de.incompleteco.spring.heartbeat;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.incompleteco.spring.batch.step.WaitingTask;

@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/META-INF/spring/client/*-context.xml",
	"classpath:/META-INF/spring/server/*-context.xml",
	"classpath:/META-INF/spring/batch-resource-context.xml",
	"classpath:/META-INF/spring/jms-resource-context.xml",
	"classpath:/META-INF/spring/job-context.xml"})
@ActiveProfiles("local")
public class HeartBeatClientServiceIT {

	@Autowired
	private Job job;
	
	@Autowired
	private JobExplorer jobExplorer;
	
	@Autowired
	private JobLauncher jobLauncher;	
	
	@Autowired
	private WaitingTask waitTask;
	
	@Test
	public void testGetJobExecutionIds() throws Exception {
		//set a delay to the wait task
		waitTask.setWait(true);
		waitTask.setWaitTime(2 * 1000);
		//now run
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
	}

}
