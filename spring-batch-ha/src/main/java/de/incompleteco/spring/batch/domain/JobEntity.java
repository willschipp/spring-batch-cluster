package de.incompleteco.spring.batch.domain;

import java.io.Serializable;

import org.springframework.batch.core.JobParametersIncrementer;

/**
 * simple object to record a job name with a version
 * 
 * @author wschipp
 *
 */
public class JobEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	
	private Integer version = 0;
	
	private String name;
	
	private JobParametersIncrementer incrementer;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public JobParametersIncrementer getIncrementer() {
		return incrementer;
	}

	public void setIncrementer(JobParametersIncrementer incrementer) {
		this.incrementer = incrementer;
	}

	@Override
	public String toString() {
		return "JobEntity [id=" + id + ", version=" + version + ", name="
				+ name + "]";
	}
	
	
	
}
