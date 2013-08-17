package de.incompleteco.spring.batch.step;

import java.util.List;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * dumb testing tasklet
 * @author wschipp
 *
 */
public class WaitingTask implements Tasklet {

	private boolean wait = false;
	
	private long waitTime = 1000;
	
	private boolean fail = false;
	
	private boolean failJVM = false;
	
	@Autowired
	private JobExplorer jobExplorer;
	
	public void invoke() throws Exception {
		if (fail) {
			throw new Exception("forced failure");
		}//end if
		
		if (failJVM) {
			System.exit(0);//exit
		}//end if
		
		if (wait) {
			Thread.sleep(waitTime);
		}//end if
	}

	public void setWait(boolean wait) {
		this.wait = wait;
	}

	public void setWaitTime(long waitTime) {
		this.waitTime = waitTime;
	}

	public void setFail(boolean fail) {
		this.fail = fail;
	}

	public void setFailJVM(boolean failJVM) {
		this.failJVM = failJVM;
	}
	

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		//set attributes based on parameters
		if (chunkContext.getStepContext().getJobParameters().containsKey("fail")) {
			boolean failedBefore = false;
			//check if THIS execution has failed already once before --> use the step execution context?
			Long jobId = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobId();
			Long jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecutionId();
			//check
			JobInstance instance = jobExplorer.getJobInstance(jobId);
			//now get the count of execution
			List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
			for (JobExecution execution : executions) {
				//check
				if (!execution.getId().equals(jobExecutionId)) {
					//check
					if (execution.getStatus().isUnsuccessful()) {
						//failed before
						failedBefore = true;
					}//end if
				}//end if
			}//end for
			//now check failedbefore
			if (!failedBefore) {
				System.exit(1);//kill the jvm
			}//end if
		}//end if
		invoke();
		//return
		return RepeatStatus.FINISHED;
	}
	
}
