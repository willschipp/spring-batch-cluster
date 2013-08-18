package de.incompleteco.spring.batch.listener;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.job.AbstractJob;
import org.springframework.context.ApplicationContext;

public class JobExecutionRegisterPostProcessorTest {

	private JobExecutionRegisterPostProcessor processor;
	
	ApplicationContext context;
	
	@Before
	public void before() {
		processor = new JobExecutionRegisterPostProcessor();
		context = mock(ApplicationContext.class);
		processor.setApplicationContext(context);
	}
	
	@Test
	public void testPostProcessBeforeInitialization() {
		Object bean = processor.postProcessBeforeInitialization("hello", null);
		assertEquals(bean,"hello");
	}

	@Test
	public void testPostProcessAfterInitialization() {
		//mock
		AbstractJob job = mock(AbstractJob.class);
		JobExecutionRegisterListener listener = mock(JobExecutionRegisterListener.class);
		//behavior
		when(context.getBean(JobExecutionListener.class)).thenReturn(listener);
		
		//execute
		processor.postProcessAfterInitialization(job,null);
		//verify
		verify(job,atLeastOnce()).registerJobExecutionListener(any(JobExecutionListener.class));
	}

}
