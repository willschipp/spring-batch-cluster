package de.incompleteco.spring.batch.domain;

import java.util.List;

public interface JobEntityRepository {

	public void save(JobEntity jobEntity);
	
	public List<JobEntity> findAll();
	
	public JobEntity findByName(String name);
	
	public void delete(String jobName);
	
}
