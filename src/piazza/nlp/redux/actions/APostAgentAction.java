package piazza.nlp.redux.actions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import piazza.nlp.redux.general.ForumPost;
import piazza.nlp.redux.tools.agents.ForumAgent;

public class APostAgentAction extends AnAbstractAgentAction implements AgentAction {

	protected int createdPostNumber;

	public APostAgentAction(String agentName, int postNumber, int revNumber, Date timestamp, int createdPostNumber) {
		super(agentName, postNumber, revNumber, timestamp);
		this.createdPostNumber = createdPostNumber;
	}
	
	public int getCreatedPostNumber() {
		return this.createdPostNumber;
	}
	
	public JSONObject toJSONObject() {
		JSONObject out = new JSONObject()
			.put("agentName", this.agentName)
			.put("actionType", this.getClass().getSimpleName())
			.put("postNumber", this.postNumber)
			.put("revNumber", this.revNumber)
			.put("timestamp", this.timestamp)
			.put("createdPostNumber", this.createdPostNumber);
		return out;
	}
	
}
