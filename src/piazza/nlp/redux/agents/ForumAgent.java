package piazza.nlp.redux.agents;

import java.util.List;

import piazza.nlp.redux.general.AgentAction;
import piazza.nlp.redux.general.DataStoreDiscussionForum;
import piazza.nlp.redux.general.ForumPost;

public interface ForumAgent {
	
	public String getAgentName();
	public void setAgentName(String newName);
	public String getAgentDescription();
	public AgentAction processPost(DataStoreDiscussionForum dataStoreForum, ForumPost post, List<AgentAction> pastActions); // runs the agent on the given post
	
	// NOTE: just added list of past actions to processPost, to handle chained order of agents

}
