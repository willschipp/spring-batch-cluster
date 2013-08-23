package de.incompleteco.spring.batch.performance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

public class InfrastructureUtils {
	
	private static Server server;
	
	public static void startH2() throws Exception {
		if (server == null) {
			server = Server.createTcpServer("-tcpAllowOthers");
			server.start();
		}//end if
	}	
	
	public static void stopH2() throws Exception {
		if (server != null) {
			server.stop();
		}//end if
	}	
	
	
	public static DataSource bindLocalH2(String dataSourceName) throws Exception {
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL("jdbc:h2:tcp://localhost/~/test");
		//build a pool and bind
		JdbcConnectionPool pool = JdbcConnectionPool.create(dataSource);
		SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.getCurrentContextBuilder();
		builder.bind("jdbc/" + dataSourceName, pool);
		//return it in case there's other uses
		return dataSource;
	}	
	
	public static String[] convertSqlFile(String location) throws Exception {
		List<String> sqlStatements = new ArrayList<String>();
		//load up the file
		BufferedReader reader = new BufferedReader(new InputStreamReader(InfrastructureUtils.class.getResourceAsStream(location)));
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
