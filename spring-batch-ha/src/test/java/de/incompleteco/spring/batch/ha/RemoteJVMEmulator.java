package de.incompleteco.spring.batch.ha;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

public class RemoteJVMEmulator {

	//bind shared resources to JNDI
	public static void setupJNDI() throws Exception {
		SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		//bind AMQ
		InfrastructureUtils.bindLocalAMQ();
		//bind h2
		InfrastructureUtils.bindLocalH2();
	}
	
	public static void main(String[] args) throws Exception {
		//setup jndi
		setupJNDI();
		//start the app context
		ApplicationContext context = new ClassPathXmlApplicationContext(args);
		//print a statement saying "it's up"
		System.out.println(RemoteJVMEmulator.class.getSimpleName() + " is up " + context.getStartupDate());
	}

}
