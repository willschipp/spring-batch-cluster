package de.incompleteco.spring.batch.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
public class JdbcJobEntityRepository implements JobEntityRepository, InitializingBean  {

	private static final String INSERT_SQL = "insert into batch_job_entity (batch_job_entity_id,version,job_name) values (?,?,?)";
	
	private static final String FIND_ALL_SQL = "select batch_job_entity_id,version,job_name from batch_job_entity order by job_name";
	
	private static final String FIND_BY_NAME_SQL = "select batch_job_entity_id,version,job_name from batch_job_entity where job_name = ? order by version";
	
	private static final String DELETE_BY_NAME_SQL = "delete from batch_job_entity where job_name = ?";
	
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void save(JobEntity jobEntity) {
		jdbcTemplate.update(INSERT_SQL, jobEntity.getId(),jobEntity.getVersion(),jobEntity.getName());
	}

	@Override
	public List<JobEntity> findAll() {
		//return
		return jdbcTemplate.query(FIND_ALL_SQL, new JobEntityRowMapper(),JobEntity.class);
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

	public class JobEntityRowMapper implements RowMapper<JobEntity> {

		@Override
		public JobEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			JobEntity entity = new JobEntity();
			//set
			entity.setId(rs.getLong(1));
			entity.setVersion(rs.getInt(2));
			entity.setName(rs.getString(3));
			//return		
			return entity;
		}

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(jdbcTemplate,"must set the datasource");
	}

	
	
}
