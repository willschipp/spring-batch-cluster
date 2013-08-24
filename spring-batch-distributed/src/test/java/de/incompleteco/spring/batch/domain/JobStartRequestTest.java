package de.incompleteco.spring.batch.domain;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class JobStartRequestTest {

	@Test
	public void test() throws Exception {
		JobStartRequest request = new JobStartRequest();
		//show as json
		System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(request));
	}

}
