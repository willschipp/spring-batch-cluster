package de.incompleteco.spring.batch.launcher;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.integration.Message;
import org.springframework.integration.core.MessagingOperations;
import org.springframework.integration.core.PollableChannel;

public class MessageChannelJobLauncherTest {

	private MessageChannelJobLauncher launcher;
	
	MessagingOperations operations;
	
	PollableChannel replyChannel;
	
	@Before
	public void before() {
		launcher = new MessageChannelJobLauncher();
		
		operations = mock(MessagingOperations.class);
		replyChannel = mock(PollableChannel.class);
		
		launcher.setReplyChannel(replyChannel);
		launcher.setGateway(operations);
	}
	
	@Test
	public void testRun() throws Exception {
		//mocks
		Job job = mock(Job.class);
		JobParameters parameters = mock(JobParameters.class);
		Message message = mock(Message.class);
		JobExecution jobExecution = mock(JobExecution.class);
		//behaviors
		when(job.getName()).thenReturn("test_job");
		when(replyChannel.receive(anyLong())).thenReturn(message);
		when(message.getPayload()).thenReturn(jobExecution);
		//executions
		JobExecution execution = launcher.run(job, parameters);
		assertNotNull(execution);
		//check touches
		verify(operations,atLeastOnce()).send(any(Message.class));
	}
	
	@Test(expected=RuntimeException.class)
	public void testRunTimeout() throws Exception {
		//mocks
		Job job = mock(Job.class);
		JobParameters parameters = mock(JobParameters.class);
		Message message = mock(Message.class);
		JobExecution jobExecution = mock(JobExecution.class);
		//behaviors
		when(job.getName()).thenReturn("test_job");
		when(replyChannel.receive(anyLong())).thenReturn(message);
//		when(message.getPayload()).thenReturn(jobExecution);
		//executions
		launcher.run(job, parameters);
		fail("expected an exception");
	}	
	
	@Test
	public void testRunJobExecutionAlreadyRunningException() throws Exception {
		//mocks
		Job job = mock(Job.class);
		JobParameters parameters = mock(JobParameters.class);
		Message message = mock(Message.class);
		JobExecution jobExecution = mock(JobExecution.class);
		JobExecutionAlreadyRunningException exception = mock(JobExecutionAlreadyRunningException.class);
		//behaviors
		when(job.getName()).thenReturn("test_job");
		when(replyChannel.receive(anyLong())).thenReturn(message);
		when(message.getPayload()).thenReturn(exception);
		//executions
		try {
			launcher.run(job, parameters);
			fail("exception should've been thrown");
		}
		catch (Exception e) { }
		//check touches
		verify(operations,atLeastOnce()).send(any(Message.class));		
	}

	@Test
	public void testRunJobRestartException() throws Exception {
		//mocks
		Job job = mock(Job.class);
		JobParameters parameters = mock(JobParameters.class);
		Message message = mock(Message.class);
		JobExecution jobExecution = mock(JobExecution.class);
		JobRestartException exception = mock(JobRestartException.class);
		//behaviors
		when(job.getName()).thenReturn("test_job");
		when(replyChannel.receive(anyLong())).thenReturn(message);
		when(message.getPayload()).thenReturn(exception);
		//executions
		try {
			launcher.run(job, parameters);
			fail("exception should've been thrown");
		}
		catch (Exception e) { }
		//check touches
		verify(operations,atLeastOnce()).send(any(Message.class));		
	}	
	
	@Test
	public void testRunJobInstanceAlreadyCompleteException() throws Exception {
		//mocks
		Job job = mock(Job.class);
		JobParameters parameters = mock(JobParameters.class);
		Message message = mock(Message.class);
		JobExecution jobExecution = mock(JobExecution.class);
		JobInstanceAlreadyCompleteException exception = mock(JobInstanceAlreadyCompleteException.class);
		//behaviors
		when(job.getName()).thenReturn("test_job");
		when(replyChannel.receive(anyLong())).thenReturn(message);
		when(message.getPayload()).thenReturn(exception);
		//executions
		try {
			launcher.run(job, parameters);
			fail("exception should've been thrown");
		}
		catch (Exception e) { }
		//check touches
		verify(operations,atLeastOnce()).send(any(Message.class));		
	}		
	
	@Test
	public void testRunJobParametersInvalidException() throws Exception {
		//mocks
		Job job = mock(Job.class);
		JobParameters parameters = mock(JobParameters.class);
		Message message = mock(Message.class);
		JobExecution jobExecution = mock(JobExecution.class);
		JobParametersInvalidException exception = mock(JobParametersInvalidException.class);
		//behaviors
		when(job.getName()).thenReturn("test_job");
		when(replyChannel.receive(anyLong())).thenReturn(message);
		when(message.getPayload()).thenReturn(exception);
		//executions
		try {
			launcher.run(job, parameters);
			fail("exception should've been thrown");
		}
		catch (Exception e) { }
		//check touches
		verify(operations,atLeastOnce()).send(any(Message.class));		
	}	
	
	@Test
	public void testRunJobExecutionException() throws Exception {
		//mocks
		Job job = mock(Job.class);
		JobParameters parameters = mock(JobParameters.class);
		Message message = mock(Message.class);
		JobExecution jobExecution = mock(JobExecution.class);
		JobExecutionException exception = mock(JobExecutionException.class);
		//behaviors
		when(job.getName()).thenReturn("test_job");
		when(replyChannel.receive(anyLong())).thenReturn(message);
		when(message.getPayload()).thenReturn(exception);
		//executions
		try {
			launcher.run(job, parameters);
			fail("exception should've been thrown");
		}
		catch (Exception e) { }
		//check touches
		verify(operations,atLeastOnce()).send(any(Message.class));		
	}		
	
}
