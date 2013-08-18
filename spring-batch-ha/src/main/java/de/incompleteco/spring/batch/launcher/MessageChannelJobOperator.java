package de.incompleteco.spring.batch.launcher;

import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobParametersNotFoundException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.support.SimpleJobOperator;

public class MessageChannelJobOperator extends SimpleJobOperator {

	private JobLauncher jobLauncher;

	@Override
	public Long startNextInstance(String jobName) 
			throws NoSuchJobException, JobParametersNotFoundException,
			UnexpectedJobExecutionException, JobParametersInvalidException{
		return null;
	}


}
