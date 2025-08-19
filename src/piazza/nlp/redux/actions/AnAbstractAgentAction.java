package piazza.nlp.redux.actions;

import java.util.Date;

import org.json.JSONObject;

public abstract class AnAbstractAgentAction implements AgentAction {

	protected String agentName;
	protected int postNumber;
	protected int revNumber;
	protected Date timestamp;
	
	public AnAbstractAgentAction(String agentName, int postNumber, int revNumber, Date timestamp) {
		this.agentName = agentName;
		this.postNumber = postNumber;
		this.revNumber = revNumber;
		this.timestamp = timestamp;
	}
	
	@Override
	public String getAgentName() {
		return this.agentName;
	}
	
	@Override
	public int getPostNumber() {
		return this.postNumber;
	}
	@Override
	public int getRevNumber() {
		return this.revNumber;
	}
	@Override
	public Date getTimestamp() {
		return this.timestamp;
	}
	
}
