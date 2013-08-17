package de.incompleteco.spring.heartbeat;

/**
 * the 'server' side of the heartbeat service (consuming heartbeat messages)
 * typically used by Spring Integration to set the clientId (from the request)
 * and the payload
 * @author wschipp
 *
 */
public interface HeartBeatConsumerService {

	public void registerHeartbeat(String clientId,Object message);
	
}
