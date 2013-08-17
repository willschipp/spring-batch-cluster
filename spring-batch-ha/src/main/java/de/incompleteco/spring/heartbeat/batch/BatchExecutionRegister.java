package de.incompleteco.spring.heartbeat.batch;

/**
 * register of executions in the local JVM
 * @author wschipp
 *
 */
public interface BatchExecutionRegister {

	public void registerExecution(Long executionId);
	
	public void deregisterExecution(Long executionId);
	
}
