package de.incompleteco.spring.batch.ha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

public class RemoteJVMEmulator {

	private static final Logger logger = LoggerFactory.getLogger(RemoteJVMEmulator.class);
	
	//bind shared resources to JNDI
	public static void setupJNDI() throws Exception {
		SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		//bind AMQ
		InfrastructureUtils.bindLocalAMQ("ConnectionFactory","request.queue","reply.queue");
		//bind h2
		InfrastructureUtils.bindLocalH2("DataSource");
	}
	
	public static void main(String[] args) throws Exception {
		//setup jndi
		setupJNDI();
		//start the app context
		ApplicationContext context = new ClassPathXmlApplicationContext(args);
		//print a statement saying "it's up"
		logger.info(RemoteJVMEmulator.class.getSimpleName() + " is up " + context.getStartupDate());
	}

}
