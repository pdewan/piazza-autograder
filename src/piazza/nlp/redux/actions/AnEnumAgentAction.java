package piazza.nlp.redux.actions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import piazza.nlp.redux.general.ForumPost;
import piazza.nlp.redux.tools.agents.ForumAgent;

public class AnEnumAgentAction extends AnAbstractAgentAction implements AgentAction {

	protected Enum actionTaken;
	
	public AnEnumAgentAction(String agentName, int postNumber, int revNumber, Date timestamp, Enum actionTaken) {
		super(agentName, postNumber, revNumber, timestamp);
		this.actionTaken = actionTaken;
	}
	
	public Enum getActionTaken() {
		return this.actionTaken;
	}

	public JSONObject toJSONObject() {
		JSONObject out = new JSONObject()
			.put("agentName", this.agentName)
			.put("actionType", this.getClass().getSimpleName())
			.put("postNumber", this.postNumber)
			.put("revNumber", this.revNumber)
			.put("timestamp", this.timestamp)
			.put("actionTaken", this.actionTaken);
		return out;
	}
	
}
