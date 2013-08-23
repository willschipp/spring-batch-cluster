package de.incompleteco.spring.batch.map;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.batch.core.DefaultJobKeyGenerator;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobKeyGenerator;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.MapJobExecutionDao;
import org.springframework.batch.core.repository.dao.MapJobInstanceDao;
import org.springframework.batch.core.repository.dao.MapStepExecutionDao;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.integration.annotation.ServiceActivator;

public class MapJobRepositoryLoader {
	
	private MapJobRepositoryFactoryBean jobRepositoryFactoryBean;
	
	private JobKeyGenerator<JobParameters> jobKeyGenerator = new DefaultJobKeyGenerator();
	
	@ServiceActivator
	public void loadExecution(JobExecution jobExecution) throws Exception {
		//load up the following daos
		//jobInstanceDao
		loadJobInstance(jobExecution);
		//jobExecutionDao
		loadJobExecution(jobExecution);
		//stepExecutionDao
		for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
			loadStepExecutionDao(stepExecution);
		}//end for
		//executionContextDao 
	}
	
	protected void loadStepExecutionDao(StepExecution stepExecution) throws Exception {
		//get
		MapStepExecutionDao stepExecutionDao = (MapStepExecutionDao) jobRepositoryFactoryBean.getStepExecutionDao();
		//init
		Map<Long, Map<Long, StepExecution>> executionsByJobExecutionId = null;
		//get the fields
		Field field = stepExecutionDao.getClass().getDeclaredField("executionsByJobExecutionId");
		field.setAccessible(true);
		//init
		Map<Long, StepExecution> executionsMap;
		//retrieve the executions
		Object executions = ((Map<Long, Map<Long,StepExecution>>)field.get(stepExecutionDao)).get(stepExecution.getJobExecutionId());
		if (executions == null) {
			executionsMap = new ConcurrentHashMap<Long, StepExecution>();
		} else {
			executionsMap = (Map<Long, StepExecution>) executions;
		}//end if
		//retrieve a value
		//add jobexecutionid mapping
		((Map<Long, Map<Long,StepExecution>>)field.get(stepExecutionDao)).put(stepExecution.getJobExecutionId(), executionsMap);
		//set two maps
		field = stepExecutionDao.getClass().getDeclaredField("executionsByStepExecutionId");
		field.setAccessible(true);
		//put
		((Map<Long, StepExecution>)field.get(stepExecutionDao)).put(stepExecution.getId(), stepExecution);
	}
	
	protected void loadJobExecution(JobExecution jobExecution) throws Exception {
		//get
		MapJobExecutionDao jobExecutionDao = (MapJobExecutionDao) jobRepositoryFactoryBean.getJobExecutionDao();
		//add the execution to the map
		Field field = jobExecutionDao.getClass().getDeclaredField("executionsById");
		field.setAccessible(true);
		((Map<Long,JobExecution>)field.get(jobExecutionDao)).put(jobExecution.getId(), jobExecution);
		//need to update the id
		field = jobExecutionDao.getClass().getDeclaredField("currentId");
		field.setAccessible(true);
		((AtomicLong)field.get(jobExecutionDao)).set(jobExecution.getId());
	}
	
	protected void loadJobInstance(JobExecution jobExecution) throws Exception {
		//get the jobInstance from the execution
		JobInstance instance = jobExecution.getJobInstance();
		JobParameters parameters = jobExecution.getJobParameters();
		//now build the key
		String key = instance.getJobName() + jobKeyGenerator.generateKey(parameters);
		//retrieve the map
		MapJobInstanceDao jobInstanceDao = (MapJobInstanceDao) jobRepositoryFactoryBean.getJobInstanceDao();
		//add the instance to it's map
		Field field = jobInstanceDao.getClass().getDeclaredField("jobInstances");
		field.setAccessible(true);
		((Map<String,JobInstance>)field.get(jobInstanceDao)).put(key, instance);
	}

	public void setJobRepositoryFactoryBean(MapJobRepositoryFactoryBean jobRepositoryFactoryBean) {
		this.jobRepositoryFactoryBean = jobRepositoryFactoryBean;
	}
	
	
}
