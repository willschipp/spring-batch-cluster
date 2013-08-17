package de.incompleteco.spring.batch.launcher;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/META-INF/spring/client/*-context.xml",
	"classpath:/META-INF/spring/server/*-context.xml",
	"classpath:/META-INF/spring/batch-resource-context.xml",
	"classpath:/META-INF/spring/jms-resource-context.xml",
	"classpath:/META-INF/spring/job-context.xml"})
@ActiveProfiles("local")
public class MessageChannelJobLauncherIT {

	@Autowired
	private Job job;
	
	@Autowired
	@Qualifier("remoteJobLauncher")
	private JobLauncher jobLauncher;
	
	@Autowired
	private JobExplorer jobExplorer;
	
	@Test
	public void testRun() throws Exception {
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
