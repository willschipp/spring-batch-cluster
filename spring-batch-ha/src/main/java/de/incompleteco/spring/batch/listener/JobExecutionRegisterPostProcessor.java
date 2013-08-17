package de.incompleteco.spring.batch.listener;

import org.springframework.batch.core.job.AbstractJob;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class JobExecutionRegisterPostProcessor implements BeanPostProcessor, ApplicationContextAware  {

	private ApplicationContext applicationContext;
	
	private JobExecutionRegisterListener listener;
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;//pass through
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		//check
		if (bean instanceof AbstractJob) {
			//check
			if (listener == null) {
				init();
			}//end if
			//attach the listener
			((AbstractJob)bean).registerJobExecutionListener(listener);
		}//end if
		return bean;
	}
	
	protected void init() {
		listener = applicationContext.getBean(JobExecutionRegisterListener.class);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
