package piazza.nlp.redux.actions;

import java.util.Map;

public interface AgentAction {

	public String getAgentName(); // get the name of the agent that took this action
	public Map<String, Object> getActionInfo(); // get information related to the action that was taken
		// action info should include the post number, revision number, timestamp, and what it did
	
	// TODO: add more methods depending on what we want to do with the logs?
	
}
