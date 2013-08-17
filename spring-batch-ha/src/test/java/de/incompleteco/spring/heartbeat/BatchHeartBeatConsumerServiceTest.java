package de.incompleteco.spring.heartbeat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import de.incompleteco.spring.heartbeat.batch.BatchHeartBeatConsumerService;

public class BatchHeartBeatConsumerServiceTest {

	BatchHeartBeatConsumerService service;
	
	@Before
	public void before() {
		service = new BatchHeartBeatConsumerService();
	}
	
	@Test
	public void testGetUnknownJobExecutionIds() throws Exception {
		//register
		service.registerHeartbeat("hello", Arrays.asList(System.currentTimeMillis()));
		//set the timeout
		service.setTimeout(500);
		//now wait more than 500 milliseconds (default)
		Thread.sleep(501);
		//get the late ones
		assertFalse(service.getUnknownJobExecutionIds().isEmpty());
	}

	@Test
	public void testRegisterHeartbeat() {
		service.registerHeartbeat("hello", Arrays.asList(System.currentTimeMillis()));
		assertTrue(service.getUnknownJobExecutionIds().isEmpty());
	}

}
