package de.incompleteco.spring.heartbeat.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class BatchHeartBeatConsumerServiceTest {
	
	private BatchHeartBeatConsumerService service;
	
	@Before
	public void before() {
		service = new BatchHeartBeatConsumerService();
	}

	@Test
	public void testGetUnknownJobExecutionIds() {
		assertNotNull(service.getUnknownJobExecutionIds());
	}

	@Test
	public void testRegisterHeartbeat() {
		//check if started is false
		assertFalse(service.started());
		service.registerHeartbeat("test", Collections.emptyList());
		assertTrue(service.started());
	}
	
	@Test(expected=RuntimeException.class)
	public void testRegisterHeartbeatException() {
		service.registerHeartbeat("test", "test");
	}

	@Test
	public void testRegisterHeartbeatDouble() {
		//check if started is false
		assertFalse(service.started());
		service.registerHeartbeat("test", Collections.emptyList());
		assertTrue(service.started());
		//set it again
		service.registerHeartbeat("test", Collections.emptyList());
		assertTrue(service.started());
	}
	
	@Test
	public void testGetUnknownJobExecutionIdsTimeout() throws Exception {
		//register
		List<Long> ids = Arrays.asList(System.currentTimeMillis());
		service.registerHeartbeat("test", ids);
		//set the timeout to 500 ms
		service.setTimeout(500);
		//wait 600 ms
		Thread.sleep(600);
		//retrieve
		List<Long> unknownIds = service.getUnknownJobExecutionIds();
		assertEquals(ids,unknownIds);
	}
}
