package de.incompleteco.spring.batch.domain;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/META-INF/spring/batch-resource-context.xml")
@ActiveProfiles("local")
public class JdbcJobEntityRepositoryIT {

	@Autowired
	private JobEntityRepository jobEntityRepository;
	
	@Autowired
	private DataSource dataSource;
	
	private JdbcTemplate jdbcTemplate;
	
	@Before
	public void before() {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@After
	public void after() {
		//clean out the table
		jdbcTemplate.execute("delete from batch_job_entity");
	}
	
	@Test
	public void testSave() {
		JobEntity jobEntity = new JobEntity();
		jobEntity.setName("test_job");
		//set the incrementer
		jobEntity.setIncrementer(new RunIdIncrementer());
		jobEntityRepository.save(jobEntity);
		//check the count
		assertTrue(jdbcTemplate.queryForObject("select count(*) from batch_job_entity",Integer.class) == 1); 
	}

	@Test
	public void testFindAll() {
		//create an entity
		testSave();
		//now get 'all'
		assertTrue(jobEntityRepository.findAll().size() == 1);
	}

	@Test
	public void testFindByName() {
		//create an entity
		testSave();
		//not get
		assertNotNull(jobEntityRepository.findByName("test_job"));
	}

	@Test
	public void testDelete() {
		//create an entity
		testSave();
		//now delete
		jobEntityRepository.delete("test_job");
		//confirm
		assertTrue(jdbcTemplate.queryForObject("select count(*) from batch_job_entity",Integer.class) == 0);
	}

}
