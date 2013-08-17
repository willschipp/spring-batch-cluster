package de.incompleteco.spring.heartbeat.batch;

public interface BatchExecutionRegister {

	public void registerExecution(Long executionId);
	
	public void deregisterExecution(Long executionId);
	
}
