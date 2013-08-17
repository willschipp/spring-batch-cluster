package de.incompleteco.spring.heartbeat;

public interface HeartBeatConsumerService {

	public void registerHeartbeat(String clientId,Object message);
	
}
