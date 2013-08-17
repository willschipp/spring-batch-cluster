package de.incompleteco.spring.heartbeat;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.incompleteco.spring.heartbeat.batch.BatchHeartBeatClientService;

public class BatchHeartBeatClientServiceTest {

	private BatchHeartBeatClientService clientService;
	
	@Before
	public void before() {
		clientService = new BatchHeartBeatClientService();
	}
	
	@Test
	public void testDeregisterExecution() {
		//register
		clientService.registerExecution(1l);
		//check
		assertTrue(clientService.getIds().size() == 1);
		//deregister
		clientService.deregisterExecution(1l);
		//check
		assertTrue(clientService.getIds().isEmpty());
		//try again
		clientService.deregisterExecution(1l);
		//check
		assertTrue(clientService.getIds().isEmpty());
	}

}
