package de.incompleteco.spring.batch.launcher;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.integration.Message;
import org.springframework.integration.core.MessagingOperations;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.support.MessageBuilder;

public class MessageChannelJobLauncher implements JobLauncher {

	private MessagingOperations gateway;
	
	private PollableChannel replyChannel;
	
	private long timeout = 5 * 1000;//default timeout
	
	@Override
	public JobExecution run(Job job, JobParameters jobParameters) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		//extract the jobname
		//build
		Message<?> request = MessageBuilder.withPayload(jobParameters).setHeader("jobName", job.getName()).build();
		//send
		gateway.send(request);
		//await response
		Message<?> reply = replyChannel.receive(timeout);
		//convert
		if (reply.getPayload() instanceof JobExecution) {
			return (JobExecution) reply.getPayload();
		} else if (reply.getPayload() instanceof JobExecutionException) {
			//check, cast and throw
			if (reply.getPayload() instanceof JobExecutionAlreadyRunningException) {
				throw (JobExecutionAlreadyRunningException) reply.getPayload();
			} else if (reply.getPayload() instanceof JobRestartException) {
				throw (JobRestartException) reply.getPayload();
			} else if (reply.getPayload() instanceof JobInstanceAlreadyCompleteException) {
				throw (JobInstanceAlreadyCompleteException) reply.getPayload();
			} else if (reply.getPayload() instanceof JobParametersInvalidException) {
				throw (JobParametersInvalidException) reply.getPayload();
			} else {
				throw new IllegalArgumentException((JobExecutionException) reply.getPayload());
			}//end if
		} else {
			throw new IllegalArgumentException(reply.getPayload().toString());
		}//end if
	}

	public void setGateway(MessagingOperations gateway) {
		this.gateway = gateway;
	}

	public void setReplyChannel(PollableChannel replyChannel) {
		this.replyChannel = replyChannel;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
