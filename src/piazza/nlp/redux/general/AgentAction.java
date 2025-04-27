package piazza.nlp.redux.general;

import java.util.Map;

public interface AgentAction {

	public String getAgentName(); // get the name of the agent that took this action
	public Map<String, Object> getActionInfo(); // get information related to the action that was taken
	
	// TODO: add more methods depending on what we want to do with the logs?
	
}
