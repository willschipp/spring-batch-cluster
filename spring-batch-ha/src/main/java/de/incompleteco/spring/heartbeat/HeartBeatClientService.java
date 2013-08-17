package de.incompleteco.spring.heartbeat;

import java.util.List;

/**
 * client service to retrieve ids
 * typcially used by a polling event (i.e. from Spring Integration) to then
 * push the resulting List somewhere
 * @author wschipp
 *
 */
public interface HeartBeatClientService {

	public List<Long> getIds();
	
}
