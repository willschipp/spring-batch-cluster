package de.incompleteco.spring.heartbeat.batch;

import java.util.List;

/**
 * retrieve unknown status execution ids
 * @author wschipp
 *
 */
public interface BatchExecutionState {

	public List<Long> getUnknownJobExecutionIds();
}
