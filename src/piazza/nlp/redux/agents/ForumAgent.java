package piazza.nlp.redux.agents;

import piazza.nlp.redux.general.AgentAction;
import piazza.nlp.redux.general.DataStoreDiscussionForum;
import piazza.nlp.redux.general.ForumPost;

public interface ForumAgent {
	
	public String getAgentName();
	public void setAgentName(String newName);
	public String getAgentDescription();
	public AgentAction processPost(DataStoreDiscussionForum dataStoreForum, ForumPost post); // runs the agent on the given post

}
