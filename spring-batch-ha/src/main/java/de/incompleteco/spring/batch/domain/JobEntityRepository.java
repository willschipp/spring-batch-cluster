package de.incompleteco.spring.batch.domain;

import java.util.List;

/**
 * repository interface to interact with JobEntity
 * @author wschipp
 *
 */
public interface JobEntityRepository {

	public void save(JobEntity jobEntity);
	
	public List<JobEntity> findAll();
	
	public JobEntity findByName(String name);
	
	public void delete(String jobName);
	
}
