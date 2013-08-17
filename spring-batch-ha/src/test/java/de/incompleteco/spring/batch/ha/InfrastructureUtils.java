package de.incompleteco.spring.batch.ha;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

public class InfrastructureUtils {

	private static Server server;
	
	private static BrokerService broker;
	
	public static void startH2() throws Exception {
		if (server == null) {
			server = Server.createTcpServer("-tcpAllowOthers");
			server.start();
			System.out.println(server.getStatus());
		}//end if
	}	
	
	public static void stopH2() throws Exception {
		if (server != null) {
			server.stop();
		}//end if
	}	
	
	public static void startAMQ() throws Exception {
		if (broker == null) {
			broker = new BrokerService();
			broker.addConnector("tcp://localhost:61616");
			broker.setUseJmx(false);
			broker.setUseShutdownHook(true);
			broker.start();
			broker.deleteAllMessages();//clean up
		}//end if
	}
	
	public static void stopAMQ() throws Exception {
		if (broker != null) {
			broker.stop();
		}//end if
	}	
	
	public static void bindLocalAMQ() {
		//get jndi
		SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.getCurrentContextBuilder();
		//get the connection
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setBrokerURL("tcp://localhost:61616");
		//bind
		builder.bind("jms/ConnectionFactory",connectionFactory);
		//setup the queues
		ActiveMQQueue requestQueue = new ActiveMQQueue("batch.request.queue");
		//bind
		builder.bind("jms/batch.request.queue",requestQueue);
		//next
		ActiveMQQueue replyQueue = new ActiveMQQueue("batch.reply.queue");
		//bind
		builder.bind("jms/batch.reply.queue",replyQueue);		
	}	
	
	public static DataSource bindLocalH2() throws Exception {
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL("jdbc:h2:tcp://localhost/~/test");
		//build a pool and bind
		JdbcConnectionPool pool = JdbcConnectionPool.create(dataSource);
		SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.getCurrentContextBuilder();
		builder.bind("jdbc/DataSource", pool);
		//return it in case there's other uses
		return dataSource;
	}
	
	public static String[] convertSqlFile(String location) throws Exception {
		List<String> sqlStatements = new ArrayList<String>();
		//load up the file
		BufferedReader reader = new BufferedReader(new InputStreamReader(TestSimpleBatchHAService.class.getResourceAsStream(location)));
		String line;
		StringBuilder statement = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			if (line.contains(";") && !line.contains("--")) {
				statement.append(line);
				sqlStatements.add(statement.toString().replace(';',' '));
				statement = new StringBuilder();//reset the string
			} else if (line.contains("--")) {
				//ignore
			} else {
				statement.append(line);
			}//end if
		}//end while
		reader.close();
		//return
		return sqlStatements.toArray(new String[sqlStatements.size()]);
	}	
}
