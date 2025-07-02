package piazza.nlp.redux.general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import piazza.nlp.redux.actions.AgentAction;
import piazza.nlp.redux.agents.ForumAgent;
import piazza.nlp.redux.general.DiscussionForum.EditorType;
import piazza.nlp.redux.general.ForumPost.PostType;
import piazza.nlp.redux.general.ForumPost.PostVisibility;
import piazza.nlp.redux.piazza.PiazzaPost;

public class AMixedInitiativeDiscussionForum implements MixedInitiativeDiscussionForum {

	final private String DEFAULT_AUTOMATED_SUGGESTION_DISCLAIMER = "\n\n<hr/>\n\n<em>This message was generated automatically and could be incorrect. If you feel that it does not apply to your post, please disregard the suggestion.</em>";
	
	protected DataStoreDiscussionForum datastore;
	protected String logPostID;
	protected List<ForumAgent> registeredAgents;
	
	public AMixedInitiativeDiscussionForum(DataStoreDiscussionForum datastoreForum) {
		this.datastore = datastoreForum;
		this.registeredAgents = new ArrayList<ForumAgent>();
		this.logPostID = (String) datastore.getDataValue("logPostID");
	}
	
	
	
	/* MixedInitiativeDiscussionForum METHODS */
	
	@Override
	public void setUp() {
		
		List<String> automatedSuggestionTags = new ArrayList<String>();
		automatedSuggestionTags.add("automated");
		String automatedSuggestionDisclaimerID = this.getDataStoreForum().getForum().createPost("Automated Suggestion Disclaimer", DEFAULT_AUTOMATED_SUGGESTION_DISCLAIMER, PostType.NOTE, PostVisibility.PRIVATE, automatedSuggestionTags, EditorType.MARKDOWN);
		
		this.getDataStoreForum().registerData("automatedSuggestionDisclaimerID", String.class, automatedSuggestionDisclaimerID);	
						
	}
	
	@Override
	public DataStoreDiscussionForum getDataStoreForum() {
		return this.datastore;
	}

	@Override
	public String createNewSystemLog() {
		
		String systemLogSubject = "Mixed-Initiative System Log";
		String[] systemLogTags = {"automated"};
		List<String> systemLogTagList = new ArrayList<>(Arrays.asList(systemLogTags)); // TODO: what tags?
		String systemLogInitialBody = "{}"; // format: map indexed by post number, value is a map containing the post_id, rev_number, and actions_taken
		
		String postID = this.datastore.getForum().createPost(systemLogSubject, systemLogInitialBody, PostType.NOTE, PostVisibility.PRIVATE, systemLogTagList, EditorType.PLAIN_TEXT);
		this.datastore.registerData("logPostID", String.class, postID);
		this.logPostID = postID;
		return postID;
		
	}
	
	@Override
	public JSONObject getSystemLog() {
		
		// TODO: make type Map<Integer, List<AgentAction>>? or this just extra work for no reason?
		
		String jsonBody = this.datastore.getForum().getPost(logPostID).getBody();
		String formattedJSON = StringEscapeUtils.unescapeHtml4(jsonBody);
		JSONObject log = new JSONObject(formattedJSON);
		return log;
	
	}

	@Override
	public void addToSystemLog(ForumPost post, AgentAction action) {
		
		ForumPost logPost = this.datastore.getForum().getPost(this.logPostID);
		JSONObject log = this.getSystemLog();
		
		String postID = logPost.getPostID();
		JSONObject entry;
		JSONArray pastActions;
		int postNumber = post.getPostNumber();
		int revNumber = post.getRevisionNumber();
		
		if (log.has(postID)) {
			JSONObject postInfo = log.getJSONObject(postID);
			pastActions = postInfo.getJSONArray("actions_taken");
			log.remove(postID);
		} else {
			pastActions = new JSONArray();
		}
		
		pastActions.put(action);
		
		entry = new JSONObject()
				.put("post_id", postNumber)
				.put("rev_number", revNumber)
				.put("actions_taken", pastActions);
		
		log.put(postID, entry);
		
		this.datastore.getForum().updatePost(this.logPostID, logPost.getSubject(), log.toString(), logPost.getType(), logPost.getVisibility(), logPost.getTags(), EditorType.PLAIN_TEXT);
		
	}

	@Override
	public void resetSystemLog() {
			
		String systemLogSubject = "Mixed-Initiative System Log";
		String[] systemLogTags = {"automated"};
		List<String> systemLogTagList = new ArrayList<>(Arrays.asList(systemLogTags)); // TODO: what tags?
		String systemLogInitialBody = "{}"; // format: map indexed by post number, value is a list of actions taken on that post
		
		this.datastore.getForum().updatePost(this.logPostID, systemLogSubject, systemLogInitialBody, PostType.NOTE, PostVisibility.PRIVATE, systemLogTagList, EditorType.PLAIN_TEXT);
		
	}

	@Override
	public void registerAgent(ForumAgent agent) {
		
		registeredAgents.add(agent);
		
		// TODO: should we store these in the data post? is that useful?
		// TODO: add parameter so you can insert rather than append (because agents run in order)? for now, we just register in the order we want to run them in
	    
	}

	@Override
	public List<String> getRegisteredAgentNames() {
		
		ArrayList<String> registeredAgentNames = new ArrayList<String>();
		
		for (ForumAgent a : this.registeredAgents)
			registeredAgentNames.add(a.getAgentName());
		
		return registeredAgentNames;
		
	}
	
	@Override
	public void setUpAgents(List<String> agentNames) {
		
		for (ForumAgent a : this.registeredAgents) {
			if (agentNames.contains(a.getAgentName())) {
				a.setUp(this.getDataStoreForum());
			}
		}
		
	}
	
	// TODO: mode code reuse between runAgents() and runAllAgents()

	@Override
	public void runAgents(List<String> agentNames, List<? extends ForumPost> posts) {
	
		DataStoreDiscussionForum dataStoreForum = this.getDataStoreForum();
		for (ForumPost p : posts) {
			
			List<AgentAction> pastActions = getPastActions(p);
			for (ForumAgent a : this.registeredAgents) {
				
				if (agentNames.contains(a.getAgentName())) {
					
					AgentAction action = a.processPost(dataStoreForum, p, pastActions);
					
					if (action != null) {
						this.addToSystemLog(p, action);
					}
										
				}
					
			}
			
		}
		
	}

	@Override
	public void runAllAgents(List<? extends ForumPost> posts) {
	
		DataStoreDiscussionForum dataStoreForum = this.getDataStoreForum();
		for (ForumPost p : posts) {
			
			List<AgentAction> pastActions = getPastActions(p);
			for (ForumAgent a : this.registeredAgents) {

				AgentAction action = a.processPost(dataStoreForum, p, pastActions);
				
				if (action != null) {
					this.addToSystemLog(p, action);
				}
			
			}
			
		}
		
	}
	
	
	
	/* HELPER METHODS */
	
	protected List<AgentAction> getPastActions(ForumPost post) {
		
		JSONObject log = this.getSystemLog();
		String postID = post.getPostID();
		JSONArray pastActions = new JSONArray();
		
		if (log.has(postID)) {
			JSONObject postInfo = log.getJSONObject(postID);
			pastActions = postInfo.getJSONArray("actions_taken");
		}
		
		List<AgentAction> actionList = new ArrayList<AgentAction>();
		List<Object> objList = pastActions.toList();
		for (Object a : objList) { // ERROR: a IS A STRING HERE??
			actionList.add((AgentAction) a);
		}
		
		return actionList;
		
	}
	

}
