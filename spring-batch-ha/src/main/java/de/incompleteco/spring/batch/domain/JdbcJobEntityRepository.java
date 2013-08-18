package de.incompleteco.spring.batch.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.serializer.DefaultDeserializer;
import org.springframework.core.serializer.Deserializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.thoughtworks.xstream.XStream;

/**
 * JDBC implementation of the JobEntityRepository
 * @author wschipp
 *
 */
@Repository
public class JdbcJobEntityRepository implements JobEntityRepository, InitializingBean  {
	
	private static final Logger logger = LoggerFactory.getLogger(JdbcJobEntityRepository.class);

	private static final String INSERT_SQL = "insert into batch_job_entity (batch_job_entity_id,version,job_name,job_incrementer) values (?,?,?,?)";
	
	private static final String FIND_ALL_SQL = "select batch_job_entity_id,version,job_name,job_incrementer from batch_job_entity order by job_name";
	
	private static final String FIND_BY_NAME_SQL = "select batch_job_entity_id,version,job_name,job_incrementer from batch_job_entity where job_name = ? order by version";
	
	private static final String DELETE_BY_NAME_SQL = "delete from batch_job_entity where job_name = ?";
	
	private JdbcTemplate jdbcTemplate;

	private LobHandler lobHandler = new DefaultLobHandler();
	
	private XStream streamer = new XStream();
	
	@Override
	public void save(final JobEntity jobEntity) {
		jdbcTemplate.update(INSERT_SQL, new PreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
//				ps.setLong(1, jobEntity.getId());
				if (jobEntity.getId() != null) {
					ps.setLong(1, jobEntity.getId());
				} else {
					ps.setNull(1, Types.BIGINT);
				}//end if
				ps.setInt(2, jobEntity.getVersion());
				ps.setString(3,jobEntity.getName());
				if (jobEntity.getIncrementer() != null) {
					//serialize
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					streamer.toXML(jobEntity.getIncrementer(), bos);
					lobHandler.getLobCreator().setBlobAsBytes(ps, 4, bos.toByteArray());
				} else {
					ps.setNull(4, Types.BLOB);
				}//end if
			} });
	}

	@Override
	public List<JobEntity> findAll() {
		//return
		return jdbcTemplate.query(FIND_ALL_SQL, new JobEntityRowMapper());
	}

	@Override
	public JobEntity findByName(String name) {
		List<JobEntity> entities = jdbcTemplate.query(FIND_BY_NAME_SQL, new Object[]{name}, new JobEntityRowMapper());
		if (entities != null && !entities.isEmpty()) {
			return entities.get(0);
		}//end if
		return null; 
	}

	@Override
	public void delete(String jobName) {
		jdbcTemplate.update(DELETE_BY_NAME_SQL,jobName);
	}	
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(jdbcTemplate,"must set the datasource");
	}
	
	
	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	public void setStreamer(XStream streamer) {
		this.streamer = streamer;
	}

	public class JobEntityRowMapper implements RowMapper<JobEntity> {

		@Override
		public JobEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			JobEntity entity = new JobEntity();
			//set
			entity.setId(rs.getLong(1));
			entity.setVersion(rs.getInt(2));
			entity.setName(rs.getString(3));
			//check if the value exists
			byte[] serialized = lobHandler.getBlobAsBytes(rs, 4);
			if (serialized != null) {
				entity.setIncrementer((JobParametersIncrementer) streamer.fromXML(new ByteArrayInputStream(serialized)));
			}//end if
			//return		
			return entity;
		}

	}


	
}
