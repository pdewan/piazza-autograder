package piazza.nlp.redux.actions;

import java.util.Date;

import org.json.JSONObject;

public interface AgentAction {

	public String getAgentName();
	public int getPostNumber();
	public int getRevNumber();
	public Date getTimestamp();
	public JSONObject toJSONObject();
	
}
