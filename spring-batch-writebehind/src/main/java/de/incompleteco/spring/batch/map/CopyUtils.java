package de.incompleteco.spring.batch.map;

import java.lang.reflect.Field;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.util.ReflectionUtils;

public class CopyUtils {

	public Object copyExecution(final Object object) {
		//check the type
		if (object instanceof StepExecution) {
			return copyStepExecution((StepExecution)object);
		} else {
			return copyJobExecution((JobExecution)object);
		}//end if
	}
	
	public StepExecution copyStepExecution(final StepExecution stepExecution) {
		//create the 'target' step execution
		final StepExecution targetExecution = new StepExecution(stepExecution.getStepName(), stepExecution.getJobExecution());
		
		ReflectionUtils.doWithFields(StepExecution.class, new ReflectionUtils.FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				field.setAccessible(true);
				field.set(targetExecution, field.get(stepExecution));
			}
		});		
		//return
		return targetExecution;
	}

	public JobExecution copyJobExecution(final JobExecution jobExecution) {
		//create the 'target' step execution
		final JobExecution targetExecution = new JobExecution(jobExecution.getJobInstance(), jobExecution.getId(), jobExecution.getJobParameters());
		
		ReflectionUtils.doWithFields(JobExecution.class, new ReflectionUtils.FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				field.setAccessible(true);
				field.set(targetExecution, field.get(jobExecution));
			}
		});		
		//return
		return targetExecution;
	}	
	
}
