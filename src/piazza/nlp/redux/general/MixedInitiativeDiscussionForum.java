package piazza.nlp.redux.general;

import java.util.List;

import piazza.nlp.redux.agents.ForumAgent;

public interface MixedInitiativeDiscussionForum {
	
	public DataStoreDiscussionForum getDataStoreForum(); // NOTE: changed from IS-A to HAS-A between MixedInitiativeDiscussionForum and DataStoreDiscussionForum
	
	public List<AgentAction> getSystemLog();
    public void addToSystemLog(AgentAction action);
    public void resetSystemLog();
    public void registerAgent(ForumAgent agent);
    public List<String> getRegisteredAgentNames();
    public void runAgents(List<String> agentNames, List<ForumPost> posts, DataStoreDiscussionForum dataStoreForum);
    
}
