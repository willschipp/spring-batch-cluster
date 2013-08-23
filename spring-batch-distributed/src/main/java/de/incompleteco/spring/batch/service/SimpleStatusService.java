package de.incompleteco.spring.batch.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;

public class SimpleStatusService implements StatusService, ApplicationContextAware {

	private AbstractApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (AbstractApplicationContext) applicationContext;
	}

	@Override
	public boolean started() {
		return applicationContext.isActive();
	}

}
