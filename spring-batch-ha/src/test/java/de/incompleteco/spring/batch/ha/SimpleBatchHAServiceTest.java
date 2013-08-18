package de.incompleteco.spring.batch.ha;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;

import de.incompleteco.spring.heartbeat.batch.BatchExecutionState;

public class SimpleBatchHAServiceTest {

	private SimpleBatchHAService service;
	
	BatchExecutionState batchExecutionState;
	
	JobExplorer jobExplorer;
	
	JobRepository jobRepository;
	
	JobLocator jobLocator;
	
	JobLauncher jobLauncher;
	
	@Before
	public void before() {
		service = new SimpleBatchHAService();
		
		batchExecutionState = mock(BatchExecutionState.class);
		jobExplorer = mock(JobExplorer.class);
		jobRepository = mock(JobRepository.class);
		jobLocator = mock(JobLocator.class);
		jobLauncher = mock(JobLauncher.class);
		
		//set
		service.setBatchExecutionState(batchExecutionState);
		service.setJobExplorer(jobExplorer);
		service.setJobRepository(jobRepository);
		service.setJobLocator(jobLocator);
		service.setJobLauncher(jobLauncher);
	}
	
	@Test
	public void testExecute() throws Exception {		
		//mock
		JobExecution execution = mock(JobExecution.class);
		JobInstance instance = mock(JobInstance.class);
		Job job = mock(Job.class);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MILLISECOND, -1000);
		//behaviour
		when(batchExecutionState.getUnknownJobExecutionIds()).thenReturn(Arrays.asList(System.currentTimeMillis()));
		when(jobExplorer.getJobExecution(anyLong())).thenReturn(execution);
		when(execution.isRunning()).thenReturn(Boolean.TRUE);
		when(execution.getLastUpdated()).thenReturn(calendar.getTime());
		when(execution.getJobInstance()).thenReturn(instance);
		when(instance.getJobName()).thenReturn("test_job");
		when(jobLocator.getJob("test_job")).thenReturn(job);
		//reset the timeout to 500 ms
		service.setTimeout(500);
		//execute
		service.execute();
		//verify
		verify(batchExecutionState,atLeastOnce()).getUnknownJobExecutionIds();
		verify(jobExplorer,atLeastOnce()).getJobExecution(anyLong());
		verify(jobRepository,atLeastOnce()).update(any(JobExecution.class));
		verify(jobLauncher,atLeastOnce()).run(any(Job.class), any(JobParameters.class));
	}
	
	@Test
	public void testExecuteNotRunning() throws Exception {		
		//mock
		JobExecution execution = mock(JobExecution.class);
		JobInstance instance = mock(JobInstance.class);
		Job job = mock(Job.class);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MILLISECOND, -1000);
		//behaviour
		when(batchExecutionState.getUnknownJobExecutionIds()).thenReturn(Arrays.asList(System.currentTimeMillis()));
		when(jobExplorer.getJobExecution(anyLong())).thenReturn(execution);
		when(execution.isRunning()).thenReturn(Boolean.FALSE);
		when(execution.getLastUpdated()).thenReturn(calendar.getTime());
		when(execution.getJobInstance()).thenReturn(instance);
		when(instance.getJobName()).thenReturn("test_job");
		when(jobLocator.getJob("test_job")).thenReturn(job);
		//reset the timeout to 500 ms
		service.setTimeout(500);
		//execute
		service.execute();
		//verify
		verify(batchExecutionState,atLeastOnce()).getUnknownJobExecutionIds();
		verify(jobExplorer,atLeastOnce()).getJobExecution(anyLong());
		verify(jobRepository,never()).update(any(JobExecution.class));
		verify(jobLauncher,never()).run(any(Job.class), any(JobParameters.class));
	}	

	@Test
	public void testExecuteTimeout() throws Exception {		
		//mock
		JobExecution execution = mock(JobExecution.class);
		JobInstance instance = mock(JobInstance.class);
		Job job = mock(Job.class);
		//behaviour
		when(batchExecutionState.getUnknownJobExecutionIds()).thenReturn(Arrays.asList(System.currentTimeMillis()));
		when(jobExplorer.getJobExecution(anyLong())).thenReturn(execution);
		when(execution.isRunning()).thenReturn(Boolean.FALSE);
		when(execution.getLastUpdated()).thenReturn(new Date());
		when(execution.getJobInstance()).thenReturn(instance);
		when(instance.getJobName()).thenReturn("test_job");
		when(jobLocator.getJob("test_job")).thenReturn(job);
		//reset the timeout to 1000 ms
		service.setTimeout(1000);
		//execute
		service.execute();
		//verify
		verify(batchExecutionState,atLeastOnce()).getUnknownJobExecutionIds();
		verify(jobExplorer,atLeastOnce()).getJobExecution(anyLong());
		verify(jobRepository,never()).update(any(JobExecution.class));
		verify(jobLauncher,never()).run(any(Job.class), any(JobParameters.class));
	}	
	
}
