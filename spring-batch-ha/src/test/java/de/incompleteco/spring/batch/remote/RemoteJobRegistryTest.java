package de.incompleteco.spring.batch.remote;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.NoSuchJobException;

import de.incompleteco.spring.batch.domain.JobEntity;
import de.incompleteco.spring.batch.domain.JobEntityRepository;

public class RemoteJobRegistryTest {

	private RemoteJobRegistry remoteJobRegistry;
	
	JobRegistry jobRegistry;
	
	JobEntityRepository jobEntityRepository;
	
	@Before
	public void before() {
		jobRegistry = mock(JobRegistry.class);
		jobEntityRepository = mock(JobEntityRepository.class);
		
		remoteJobRegistry = new RemoteJobRegistry(jobRegistry);
		remoteJobRegistry.setJobEntityRepository(jobEntityRepository);
	}
	
	@Test
	public void testGetJobNames() {
		assertNotNull(remoteJobRegistry.getJobNames());
	}

	@Test
	public void testGetJob() throws Exception {
		assertNull(remoteJobRegistry.getJob("test_job"));
	}
	
	@Test(expected=NoSuchJobException.class)
	public void testGetJobNotLocal() throws Exception {
		//behavior
		when(jobRegistry.getJob(anyString())).thenThrow(NoSuchJobException.class);
		//execute
		assertNull(remoteJobRegistry.getJob("test_job"));
	}
	
	@Test
	public void testGetJobNotLocalHasRemote() throws Exception {
		//mock
		JobEntity jobEntity = mock(JobEntity.class);
		//behavior
		when(jobRegistry.getJob(anyString())).thenThrow(NoSuchJobException.class);
		when(jobEntityRepository.findByName(anyString())).thenReturn(jobEntity);
		//execute
		assertNotNull(remoteJobRegistry.getJob("test_job"));
	}	

	@Test
	public void testRegister() throws Exception {
		//mock
		JobFactory jobFactory = mock(JobFactory.class);
		Job job = mock(Job.class);
		//behavior
		when(jobFactory.getJobName()).thenReturn("test_job");
		when(jobFactory.createJob()).thenReturn(job);
		//execute
		remoteJobRegistry.register(jobFactory);
		//verify
		verify(jobEntityRepository,atLeastOnce()).save(any(JobEntity.class));
	}

	@Test
	public void testUnregister() {
		remoteJobRegistry.unregister("test_job");
		//verify
		verify(jobEntityRepository,atLeastOnce()).delete("test_job");		
	}

}
