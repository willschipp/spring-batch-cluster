package de.incompleteco.spring.batch.map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.XStreamExecutionContextStringSerializer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.Assert;

public class SerializationService implements InitializingBean {

	private ExecutionContextSerializer serializer;
	
	private JdbcTemplate jdbcTemplate;
	
	private LobHandler lobHandler = new DefaultLobHandler();
	
	public String serialize(ExecutionContext context) {
		//convert to map
		Map<String, Object> map = new HashMap<String, Object>();
		for (Entry<String, Object> entry : context.entrySet()) {
			map.put(entry.getKey(), entry.getValue());
		}		
		//string serialize
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String results = "";

		try {
			serializer.serialize(map, out);
			results = new String(out.toByteArray(), "ISO-8859-1");
		}
		catch (IOException ioe) {
			throw new IllegalArgumentException("Could not serialize the execution context", ioe);
		}
		return results;
	}
	
	public Map<?,?> deserialize(String serializedContext) {
		Map<?,?> map = null;
		ByteArrayInputStream in;
		try {
			in = new ByteArrayInputStream(serializedContext.getBytes("ISO-8859-1"));
			map = (Map<String, Object>) serializer.deserialize(in);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		
		
		return map;
		
	}
	
	public void persistBlob(final String context,final Long id,String sql) throws Exception {
		Assert.notNull(context,"Null context");
		jdbcTemplate.update(sql,new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				//check the context size
				if (context.length() < 2400) {
					//put it all (and only) in the context
					ps.setString(1, context);
					ps.setNull(2, Types.BLOB);
				} else {
					//set 'some' in the short and rest in the long
					ps.setString(1,context.substring(0,2400));
					lobHandler.getLobCreator().setBlobAsBytes(ps, 2, context.getBytes());
				}//end if
				ps.setLong(3,id);
			} });
	}
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(jdbcTemplate,"Must set a DataSource");
		serializer = new XStreamExecutionContextStringSerializer();
		((XStreamExecutionContextStringSerializer) serializer).init();
	}
	
}
