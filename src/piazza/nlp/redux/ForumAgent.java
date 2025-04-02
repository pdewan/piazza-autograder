package piazza.nlp.redux;

import org.json.JSONObject;

public interface ForumAgent {
	
	public JSONObject runAgent(JSONObject post); // runs the agent on the given post, returns an action for use in adding to system log?
	// TODO
	
}
