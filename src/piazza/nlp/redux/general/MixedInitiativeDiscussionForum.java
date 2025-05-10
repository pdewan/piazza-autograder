package piazza.nlp.redux.general;

import java.util.List;

import piazza.nlp.redux.agents.ForumAgent;

public interface MixedInitiativeDiscussionForum {
	
	public DataStoreDiscussionForum getDataStoreForum(); // NOTE: changed from IS-A to HAS-A between MixedInitiativeDiscussionForum and DataStoreDiscussionForum
	
	public List<AgentAction> getSystemLog();
    public void addToSystemLog(AgentAction action); // TODO: system log should include the revision number in addition to post number and post ID
    public void resetSystemLog();
    public void registerAgent(ForumAgent agent);
    public List<String> getRegisteredAgentNames();
    public void runAgents(List<String> agentNames, List<ForumPost> posts, DataStoreDiscussionForum dataStoreForum);
    
    // TODO: have a seperate post from the log that stores all of the errors/exceptions encountered by the agents?
    //  or is this just stored in the log by virtue of adding an AgentAction with this same info?
    
}
