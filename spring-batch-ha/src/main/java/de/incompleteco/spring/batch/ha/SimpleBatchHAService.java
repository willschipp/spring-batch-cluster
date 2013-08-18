package de.incompleteco.spring.batch.ha;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.incompleteco.spring.heartbeat.batch.BatchExecutionState;

/**
 * simple HA implementation
 * @author wschipp
 *
 */
public class SimpleBatchHAService implements BatchHAService {

	private static final Logger logger = LoggerFactory.getLogger(SimpleBatchHAService.class);
	
	private long timeout = 4 * 1000;//timeout is by default, double what the executionstate is
	
	@Autowired
	private JobExplorer jobExplorer;
	
	@Autowired
	@Qualifier("remoteJobRegistry")//uses the remote one
	private JobLocator jobLocator;
	
	@Autowired
	@Qualifier("remoteJobLauncher")//remote version to launch
	private JobLauncher jobLauncher;
	
	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private BatchExecutionState batchExecutionState;
	
	@Override
	public void execute() {
		//retrieve the list from the execution state
		//these ids represent job executions that were registered on a jvm and the jvm hasn't responded 
		//inside the timeout
		List<Long> executionIds = batchExecutionState.getUnknownJobExecutionIds();
		//loop through the ids
		for (Long executionId : executionIds) {
			//init
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MILLISECOND, new Long(-timeout).intValue());
			//check if it's listed as stopped/not running in the database (state has changed since the heartbeat)
			JobExecution execution = jobExplorer.getJobExecution(executionId);
			if (execution.isRunning()) {
				logger.debug("Execution is still running",executionId);
				//it's still "running" - so we have a potential failure --> check when last updated
				if (execution.getLastUpdated().before(calendar.getTime())) {
					//so now we know it REALLY hasn't been looked at in awhile --> send for 'processing
					processFailedJob(execution);
					//restart
					restart(execution);
				}//end if
			}//end if
		}//end for
	}

	protected void processFailedJob(JobExecution jobExecution) {
		logger.debug("processing a failed job",jobExecution);
		//init
		Date updateTime = new Date();
		//this execution is 'dead' --> need to update it and restart it
		jobExecution.upgradeStatus(BatchStatus.FAILED);
		jobExecution.setExitStatus(ExitStatus.FAILED.addExitDescription("failed through HA"));
		jobExecution.setEndTime(updateTime);
		jobExecution.setEndTime(updateTime);
		//save in the repo
		jobRepository.update(jobExecution);
	}
	
	protected void restart(JobExecution jobExecution) {
		logger.debug("restarting...",jobExecution);
		//retrieve the job
		String jobName = jobExecution.getJobInstance().getJobName();
		Job job;
		try {
			job = jobLocator.getJob(jobName);
			//get the parameters
			JobParameters jobParameters = jobExecution.getJobParameters();
			//launch
			jobLauncher.run(job, jobParameters);
		} catch (JobExecutionAlreadyRunningException
				| NoSuchJobException
				| JobRestartException
				| JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {
			throw new IllegalArgumentException("failure to restart " + jobName,e);
		}
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void setJobExplorer(JobExplorer jobExplorer) {
		this.jobExplorer = jobExplorer;
	}

	public void setJobLocator(JobLocator jobLocator) {
		this.jobLocator = jobLocator;
	}

	public void setJobLauncher(JobLauncher jobLauncher) {
		this.jobLauncher = jobLauncher;
	}

	public void setJobRepository(JobRepository jobRepository) {
		this.jobRepository = jobRepository;
	}

	public void setBatchExecutionState(BatchExecutionState batchExecutionState) {
		this.batchExecutionState = batchExecutionState;
	}
	
}
