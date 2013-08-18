package de.incompleteco.spring.batch.remote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import de.incompleteco.spring.batch.domain.JobEntity;
import de.incompleteco.spring.batch.domain.JobEntityRepository;

/**
 * implementation to support remote jobs
 * @author wschipp
 *
 */
public class RemoteJobRegistry implements JobRegistry, InitializingBean  {
	
	private static final Logger logger = LoggerFactory.getLogger(RemoteJobRegistry.class);

	@Autowired
	private JobEntityRepository jobEntityRepository;
	
	private JobRegistry localJobRegistry;
	
	public RemoteJobRegistry(JobRegistry localJobRegistry) {
		this.localJobRegistry = localJobRegistry;
	}
	
	
	@Override
	public Collection<String> getJobNames() {
		logger.debug("retrieving jobnames");
		Collection<String> names = new ArrayList<String>();
		List<JobEntity> entities = jobEntityRepository.findAll();
		for (JobEntity entity : entities) {
			names.add(entity.getName());
		}//end for
		return names;
	}


	@Override
	public Job getJob(String name) throws NoSuchJobException {
		//check if exists locally
		try {
			Job job = localJobRegistry.getJob(name);
			logger.debug("retrieved from local JobRegistry");
			return job;
		} 
		catch (NoSuchJobException e) {
			//check if the name exists
			JobEntity entity = jobEntityRepository.findByName(name);
			if (entity == null) {
				throw new NoSuchJobException("job doesn't exist " + name);
			}//end if
			//build a 'fake' job
			SimpleJob job = new SimpleJob(entity.getName());
			job.setJobParametersIncrementer(entity.getIncrementer());
			logger.debug("retrieved from remote JobRegistry");
			//return
			return job;			
		}
	}


	@Override
	public void register(JobFactory jobFactory) throws DuplicateJobException {
		//build an entity
		JobEntity entity = new JobEntity();
		entity.setName(jobFactory.getJobName());
		//get the incrementer
		entity.setIncrementer(jobFactory.createJob().getJobParametersIncrementer());
		//save
		jobEntityRepository.save(entity);
		//register it 'locally'
		localJobRegistry.register(jobFactory);
	}


	@Override
	public void unregister(String jobName) {
		jobEntityRepository.delete(jobName);
		//remove locally
		localJobRegistry.unregister(jobName);
	}


	public void setJobEntityRepository(JobEntityRepository jobEntityRepository) {
		this.jobEntityRepository = jobEntityRepository;
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(jobEntityRepository,"JobEntityRepository must be set");
	}

	
}
