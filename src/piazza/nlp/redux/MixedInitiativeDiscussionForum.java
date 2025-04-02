package piazza.nlp.redux;

import java.util.List;
import org.json.JSONObject;

public interface MixedInitiativeDiscussionForum {
	
	public JSONObject getSystemLog();
    public void addToSystemLog(JSONObject action);
    public void resetSystemLog();
    public void registerAgent(String agentName, ForumAgent agent);
    public List<String> getRegisteredAgentNames();
    public void runAgents(List<String> agentNames, List<JSONObject> posts);
    
}

