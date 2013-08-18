package de.incompleteco.spring.heartbeat.batch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class BatchHeartBeatClientServiceTest {

	private BatchHeartBeatClientService service;
	
	@Before
	public void before() {
		service = new BatchHeartBeatClientService();
	}
	
	@Test
	public void testGetIds() {
		assertNotNull(service.getIds());
	}

	@Test
	public void testRegisterExecution() {
		service.registerExecution(1l);
		//get the id
		List<Long> ids = service.getIds();
		assertTrue(ids.contains(1l));
	}

	@Test
	public void testDeregisterExecution() {
		testRegisterExecution();
		//remove it
		service.deregisterExecution(1l);
		//check again
		List<Long> ids = service.getIds();
		assertFalse(ids.contains(1l));		
	}

}
