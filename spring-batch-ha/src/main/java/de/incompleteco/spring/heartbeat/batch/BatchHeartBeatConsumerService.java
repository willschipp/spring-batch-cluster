package de.incompleteco.spring.heartbeat.batch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.incompleteco.spring.heartbeat.HeartBeatConsumerService;

/**
 * implementation of the service
 * @author wschipp
 *
 */
public class BatchHeartBeatConsumerService implements HeartBeatConsumerService,BatchExecutionState {

	private static final Logger logger = LoggerFactory.getLogger(BatchHeartBeatConsumerService.class);
	
	private Map<String,NodeRegister> nodeRegisters;
	
	private long timeout = 2 * 1000;//default is 2 seconds
	
	public BatchHeartBeatConsumerService() {
		nodeRegisters = new ConcurrentHashMap<String, BatchHeartBeatConsumerService.NodeRegister>();
	}
	
	@Override
	public List<Long> getUnknownJobExecutionIds() {
		List<Long> jobExecutionIds = new ArrayList<Long>();
		//calculate a timeout
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MILLISECOND, new Long(-timeout).intValue());
		//go through the nodes that are "timedout"
		for (NodeRegister register : nodeRegisters.values()) {
			if (register.getTimestamp().before(calendar.getTime())) {
				logger.debug("it's been too long " + register.getTimestamp() + " " + register.getJobExecutionIds());
				//it's been too long
				jobExecutionIds.addAll(register.getJobExecutionIds());
			}//end if
		}//end for
		//if there are any, return
		return jobExecutionIds;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void registerHeartbeat(String clientId,Object message) {
		if (!(message instanceof List)) {
			throw new IllegalArgumentException("payload is incorrect " + message);
		}//end if
		logger.debug("received this: ",clientId,((List<?>)message));
		//init
		NodeRegister register;
		//register
		if (!nodeRegisters.containsKey(clientId)) {
			//create
			register = new NodeRegister(clientId);
			register.setJobExecutionIds((List<Long>)message);
			nodeRegisters.put(clientId,register);
		} else {
			nodeRegisters.get(clientId).setTimestamp(new Date());//refresh the stamp
			nodeRegisters.get(clientId).setJobExecutionIds((List<Long>)message);//update the execution id
		}//end if
	}
	
	
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}


	/**
	 * placeholder class
	 * @author wschipp
	 *
	 */
	class NodeRegister {
		
		private List<Long> jobExecutionIds;
		
		private final String clientId;
		
		private Date timestamp;
		
		public NodeRegister(String clientId) {
			this.jobExecutionIds = Collections.synchronizedList(new ArrayList<Long>());
			this.timestamp = new Date();
			this.clientId = clientId; 
		}

		public List<Long> getJobExecutionIds() {
			return jobExecutionIds;
		}

		public void setJobExecutionIds(List<Long> jobExecutionIds) {
			this.jobExecutionIds = jobExecutionIds;
		}

		public Date getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}

		public String getClientId() {
			return clientId;
		}
		
	}

}
