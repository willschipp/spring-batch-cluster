package de.incompleteco.spring.batch.listener;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.JobExecution;

import de.incompleteco.spring.heartbeat.batch.BatchExecutionRegister;

public class JobExecutionRegisterListenerTest {

	private JobExecutionRegisterListener listener;
	
	BatchExecutionRegister register;
	
	@Before
	public void before() {
		listener = new JobExecutionRegisterListener();
		register = mock(BatchExecutionRegister.class);
		listener.setRegister(register);
	}
	
	@Test
	public void testBeforeJob() {
		//mock
		JobExecution execution = mock(JobExecution.class);
		//behavior
		when(execution.getId()).thenReturn(1l);
		//execute
		listener.beforeJob(execution);
		//verify
		verify(register,atLeastOnce()).registerExecution(anyLong());
	}

	@Test
	public void testAfterJob() {
		//mock
		JobExecution execution = mock(JobExecution.class);
		//behavior
		when(execution.getId()).thenReturn(1l);
		//execute
		listener.afterJob(execution);
		//verify
		verify(register,atLeastOnce()).deregisterExecution(anyLong());		
	}

}
