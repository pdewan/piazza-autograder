package piazza.nlp.redux.general;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import piazza.nlp.redux.actions.AgentAction;
import piazza.nlp.redux.agents.ForumAgent;

public interface MixedInitiativeDiscussionForum {
	
	public DataStoreDiscussionForum getDataStoreForum(); // NOTE: changed from IS-A to HAS-A between MixedInitiativeDiscussionForum and DataStoreDiscussionForum
	
	// system log format: map indexed by post ID, value is a map containing the post_number, rev_number, and actions_taken
	
	public void setUp();
	public String createNewSystemLog(boolean replacePreviousLog); // returns the ID of the new system log post
	public JSONObject getSystemLog(); // TODO: make type Map<Integer, List<AgentAction>>? or this just extra work for no reason?
//    public void addToSystemLog(ForumPost post, AgentAction action);
    public void addToSystemLog(ForumPost post, List<AgentAction> actions);
	public void resetSystemLog();
    public void registerAgent(ForumAgent agent); // TODO: add parameter so you can insert rather than append (because agents run in order)? for now, we just register in the order we want to run them in
    public ForumAgent getRegisteredAgent(String agentName);
    public List<String> getRegisteredAgentNames();
    public void setUpAgents(List<String> agentNames);
    public void runAgents(List<String> agentNames, List<? extends ForumPost> posts); // will fetch dataStoreForum and pastActions using other methods and send them to each agent's processPost()
    public void runAllAgents(List<? extends ForumPost> posts); // will fetch dataStoreForum and pastActions using other methods and send them to each agent's processPost()

    
    // TODO: have a seperate post from the log that stores all of the errors/exceptions encountered by the agents?
    //  or is this just stored in the log by virtue of adding an AgentAction with this same info?

    /*
     * - allow for different prompts for different assigments tags (such as constrained assignments), not just different agents
  - can do this by storing JSON in prompt post? or multiple posts?
- store all exceptions/errors raised in a Piazza post so instructors can view it
  - we still need to do this
- store time of last process in log post (this will be part of agentAction)
  - also revision number
     * 
     */
    
}
