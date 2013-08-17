package de.incompleteco.spring.heartbeat.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.incompleteco.spring.heartbeat.HeartBeatClientService;

/**
 * client service to that both registers and publishes 
 * this JVM's jobExecutionId list (for 'running' jobs)
 * @author wschipp
 *
 */
public class BatchHeartBeatClientService implements HeartBeatClientService, BatchExecutionRegister {

	private List<Long> jobExecutionIds;
	
	public BatchHeartBeatClientService() {
		jobExecutionIds = Collections.synchronizedList(new ArrayList<Long>());
	}
	
	@Override
	public List<Long> getIds() {
		return jobExecutionIds;
	}
	

	@Override
	public void registerExecution(Long executionId) {	
		jobExecutionIds.add(executionId);
	}

	@Override
	public void deregisterExecution(Long executionId) {
		jobExecutionIds.remove(executionId);
	}

}
