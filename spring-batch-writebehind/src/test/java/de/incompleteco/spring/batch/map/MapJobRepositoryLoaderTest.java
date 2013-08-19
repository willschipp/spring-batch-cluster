package de.incompleteco.spring.batch.map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;

public class MapJobRepositoryLoaderTest {

	private MapJobRepositoryLoader loader;
	
	@Before
	public void before() {
		 loader = new MapJobRepositoryLoader();
	}
	
	@Test
	public void testLoadExecution() throws Exception {
		//mock
		JobInstance jobInstance = new JobInstance(1l,"test_job");
		MapJobRepositoryFactoryBean jobRepositoryFactoryBean = new MapJobRepositoryFactoryBean();
		JobParameters jobParameters = new JobParametersBuilder().addLong("runtime",System.currentTimeMillis()).toJobParameters();
		JobExecution jobExecution = new JobExecution(1l,jobParameters);
		//init
		jobRepositoryFactoryBean.afterPropertiesSet();
		//behavior
		jobExecution.setJobInstance(jobInstance);
		//setup
		loader.setJobRepositoryFactoryBean(jobRepositoryFactoryBean);
		//execute
		loader.loadExecution(jobExecution);
		//check
		assertNotNull(jobRepositoryFactoryBean.getJobInstanceDao().getJobNames());
		assertTrue(jobRepositoryFactoryBean.getJobInstanceDao().getJobNames().size() > 0);
		//check if in the repo for jobExecution
		assertNotNull(jobRepositoryFactoryBean.getJobExecutionDao().getJobExecution(1l));
	}

}
