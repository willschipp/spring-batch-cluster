package de.incompleteco.spring.batch.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.annotate.JsonIgnore;

public class JobStartRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobName;
	
	private Map<String,String> parameters;
	
	public JobStartRequest() {
		parameters = new HashMap<String,String>();
	}
	
	public JobStartRequest(String jobName) { 
		this.jobName = jobName;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	@JsonIgnore
	public String getKeyValuePair() {
		StringBuilder builder = new StringBuilder();
		for (Entry<String,String> entry : parameters.entrySet()) {
			if (builder.length() > 0) {
				builder.append(",");
			}//end if
			builder.append(entry.getKey());
			builder.append("=");
			builder.append(entry.getValue());
		}//end for
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return "JobStartRequest [jobName=" + jobName + ", parameters="
				+ parameters + "]";
	}
	
}
