package piazza.nlp.redux.actions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import piazza.nlp.redux.agents.ForumAgent;
import piazza.nlp.redux.general.ForumPost;

public class AnEnumAgentAction implements AgentAction {

	protected ForumAgent agent;
	protected ForumPost post;
	protected Date time;
	protected Enum actionTaken;

	// action info should include the post number, revision number, timestamp, and what it did

	public AnEnumAgentAction(ForumAgent agent, ForumPost post, Date time, Enum actionTaken) {
		this.agent = agent;
		this.post = post;
		this.time = time;
		this.actionTaken = actionTaken;
	}
	
	
	
	/* AgentAction METHODS */
	
	@Override
	public String getAgentName() {
		return this.agent.getAgentName();
	}

	@Override
	public Map<String, Object> getActionInfo() {
		
		HashMap<String, Object> actionInfo = new HashMap<String, Object>();
		
		actionInfo.put("agent", this.getAgentName());
		actionInfo.put("rev", String.valueOf(this.post.getRevisionNumber()));
		actionInfo.put("time", this.time.toString());
		actionInfo.put("action", actionTaken.toString());
		
		return actionInfo;
	
	}

}
