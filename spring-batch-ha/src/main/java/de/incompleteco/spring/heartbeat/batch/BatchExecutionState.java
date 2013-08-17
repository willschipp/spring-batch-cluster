package de.incompleteco.spring.heartbeat.batch;

import java.util.List;

public interface BatchExecutionState {

	public List<Long> getUnknownJobExecutionIds();
}
