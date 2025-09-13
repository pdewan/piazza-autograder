package piazza.nlp.redux.general;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import piazza.nlp.redux.actions.APostAgentAction;

//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;

import piazza.nlp.redux.actions.AgentAction;
import piazza.nlp.redux.actions.AnEnumAgentAction;
import piazza.nlp.redux.general.DiscussionForum.EditorType;
import piazza.nlp.redux.general.ForumPost.PostType;
import piazza.nlp.redux.general.ForumPost.PostVisibility;
import piazza.nlp.redux.piazza.PiazzaForum;
import piazza.nlp.redux.tools.agents.ForumAgent;
import piazza.nlp.redux.tools.agents.ImageCheckerAgent.ImageCheckerAction;
import piazza.nlp.redux.tools.agents.MediatedLLMAgent.MediatedLLMAction;
import piazza.nlp.redux.tools.agents.VisibilityCheckerAgent.VisibilityCheckerAction;

public class AMixedInitiativeDiscussionForum implements MixedInitiativeDiscussionForum {

	final private String DEFAULT_AUTOMATED_SUGGESTION_DISCLAIMER = "\n\n<hr/>\n\n<em>This message was generated automatically and could be incorrect. If you feel that it does not apply to your post, please disregard the suggestion.</em>";
	
	protected DataStoreDiscussionForum datastore;
	protected String logPostID;
	protected List<ForumAgent> registeredAgents;
//	protected Gson gson;
	
	public AMixedInitiativeDiscussionForum(DataStoreDiscussionForum datastoreForum) {
		
		this.datastore = datastoreForum;
		this.registeredAgents = new ArrayList<ForumAgent>();
		this.logPostID = (String) datastore.getDataValue("logPostID");
		
//        GsonBuilder builder = new GsonBuilder();
//        builder.registerTypeAdapter(AgentAction.class, new AgentActionTypeAdapter());
//        builder.setPrettyPrinting(); // Makes the output JSON readable
//        this.gson = builder.create();
		
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
	public String createNewSystemLog(boolean replacePreviousLog) {
		
		String systemLogSubject = replacePreviousLog ? "Mixed-Initiative System Log" : "Mixed-Initiative System Log (continued)";
		String[] systemLogTags = {"automated"};
		List<String> systemLogTagList = new ArrayList<>(Arrays.asList(systemLogTags)); // TODO: what tags?
		String systemLogInitialBody = "{}"; // format: map indexed by post number, value is a map containing the post_id, rev_number, and actions_taken
		
		String postID = this.datastore.getForum().createPost(systemLogSubject, systemLogInitialBody, PostType.NOTE, PostVisibility.PRIVATE, systemLogTagList, EditorType.PLAIN_TEXT);
		
		if (replacePreviousLog) {
			boolean alreadyRegistered = !this.datastore.registerData("logPostID", String.class, postID);
			if (alreadyRegistered)
				this.datastore.updateData("logPostID", postID);
			this.logPostID = postID;
		}
		
		return postID;
		
	}
	
	@Override
	public JSONObject getSystemLog() {
		
		// TODO: make type Map<Integer, List<AgentAction>>? or this just extra work for no reason?

		String jsonBody = this.datastore.getForum().getPost(logPostID).getBody();
		return DataStoreDiscussionForum.formatJSONData(jsonBody);
				
//		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
//		System.out.println("STRING LENGTH IN getSystemLog: " + jsonBody.length());
//		System.out.println(jsonBody.substring(201000));
//		System.out.println("STRING LENGTH AFTER FORMATTING: " + formattedJSON.length());		
//		System.out.println(cleanedJson);			
		
	}

//	@Override
//	public void addToSystemLog(ForumPost post, AgentAction action) {
//		
//		ForumPost logPost = this.datastore.getForum().getPost(this.logPostID);
//		JSONObject log = this.getSystemLog();
//			
//		String postID = post.getPostID();
//		JSONObject entry;
//		JSONArray pastActions;
//		int postNumber = post.getPostNumber();
//		int revNumber = post.getRevisionNumber();
//		
//		if (log.has(postID)) {
//			JSONObject postInfo = log.getJSONObject(postID);
//			pastActions = postInfo.getJSONArray("actions_taken");
//			log.remove(postID);
//		} else {
//			pastActions = new JSONArray();
//		}
//		
//		pastActions.put(action.toJSONObject());
//		
//		entry = new JSONObject()
//				.put("post_id", postNumber)
//				.put("rev_number", revNumber)
//				.put("actions_taken", pastActions);
//		
//		log.put(postID, entry);
//		
//		this.datastore.getForum().updatePost(this.logPostID, logPost.getSubject(), log.toString(1), logPost.getType(), logPost.getVisibility(), logPost.getTags(), EditorType.PLAIN_TEXT);
//		
//	}
	
	
	@Override
	public void addToSystemLog(ForumPost post, List<AgentAction> totalActions) {
		
		// create an entry for the current actions
		JSONObject entry;
		String postID = post.getPostID();
		int postNumber = post.getPostNumber();
		int revNumber = post.getRevisionNumber();
		
		JSONArray actions = new JSONArray();
		for (AgentAction a : totalActions)
			actions.put(a.toJSONObject());
		
		entry = new JSONObject()
				.put("postID", postNumber)
				.put("revNumber", revNumber)
				.put("actionsTaken", actions);
		
		// if the log is extended across multiple posts, find the current post
		String logPostID = this.logPostID;
		JSONObject log = this.getSystemLog();
		boolean logExtensionExists = log.has("logExtension");
		while (logExtensionExists) {
			logPostID = log.getJSONObject("logExtension").getString("newPostID");
			String logBody = this.getDataStoreForum().getForum().getPost(logPostID).getBody();
			log = DataStoreDiscussionForum.formatJSONData(logBody);
			logExtensionExists = log.has("logExtension");
		}
		ForumPost logPost = this.datastore.getForum().getPost(logPostID);
		
		// add the entry to the log
		if (log.has(postID))
			log.remove(postID);
		log.put(postID, entry);
		
		// if updating the current log would make future edits impossible (because the API response would be too large),
		//   create another log and add its ID to the original log (essentially treat it like a linked list)
		// this is needed because Piazza keeps a history of all previous edits, making size increase exponentially with number of edits
		if (this.getDataStoreForum().getForum() instanceof PiazzaForum) {
		
			// chose factor of 16 here instead of 4, so a couple of manual edits can be made in the future
			int currentLogLength = logPost.getAllData().toString().length();
			if (currentLogLength > PiazzaForum.MAX_POST_SIZE / 16) {
							
				// create an additional log post
				String newLogID = createNewSystemLog(false);
				int newLogNumber = this.getDataStoreForum().getForum().getPost(newLogID).getPostNumber();
				System.out.println("NOTE: System log at @" + logPost.getPostNumber() + " has reached its maximum size; creating an extension at @" + newLogNumber + ".");
				
				// add a reference to the new log post
				JSONObject extensionEntry = new JSONObject()
						.put("newPostID", newLogID)
						.put("newPostNumber", newLogNumber);
				
				log.put("logExtension", extensionEntry);
				
			}
			
		}
		
		// otherwise, update the post with the new log
		this.datastore.getForum().updatePost(logPostID, logPost.getSubject(), log.toString(4), logPost.getType(), logPost.getVisibility(), logPost.getTags(), EditorType.PLAIN_TEXT);
		
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
	public ForumAgent getRegisteredAgent(String agentName) {
		for (ForumAgent a : this.registeredAgents) {
			if (a.getName().equals(agentName)) {
				return a;
			}
		}
		return null;
	}
	
	@Override
	public List<String> getRegisteredAgentNames() {
		
		ArrayList<String> registeredAgentNames = new ArrayList<String>();
		
		for (ForumAgent a : this.registeredAgents)
			registeredAgentNames.add(a.getName());
		
		return registeredAgentNames;
		
	}
	
	@Override
	public void setUpAgents(List<String> agentNames) {
		
		for (ForumAgent a : this.registeredAgents) {
			if (agentNames.contains(a.getName())) {
				a.setUp(this.getDataStoreForum());
			}
		}
		
	}
	
	// TODO: mode code reuse between runAgents() and runAllAgents()

	@Override
	public void runAgents(List<String> agentNames, List<? extends ForumPost> posts) {
	
		// obtain the current system log
		JSONObject log = this.getSystemLog();
		List<JSONObject> logs = new ArrayList<JSONObject>();
		logs.add(log);

		// if the log is extended across multiple posts, traverse the posts like a linked list		
		while (log.has("logExtension")) {
			String logExtensionID = log.getJSONObject("logExtension").getString("newPostID");
			String logBody = this.getDataStoreForum().getForum().getPost(logExtensionID).getBody();
			log = DataStoreDiscussionForum.formatJSONData(logBody);
			logs.add(log);
		}
		
		DataStoreDiscussionForum dataStoreForum = this.getDataStoreForum();
		for (ForumPost p : posts) {
			
			boolean newActions = false;
			List<AgentAction> pastActions = getPastActions(p, logs);
			for (ForumAgent a : this.registeredAgents) {
				
				if (agentNames.contains(a.getName())) {
					
					AgentAction action = a.processPost(dataStoreForum, p, pastActions);
					if (action != null) {
						pastActions.add(action);
						newActions = true;
					}
										
				}
					
			}
			
			if (newActions)
				this.addToSystemLog(p, pastActions);
			
		}
		
	}

	@Override
	public void runAllAgents(List<? extends ForumPost> posts) {
	
		this.runAgents(this.getRegisteredAgentNames(), posts);
		
//		DataStoreDiscussionForum dataStoreForum = this.getDataStoreForum();
//		for (ForumPost p : posts) {
//			
//			List<AgentAction> pastActions = getPastActions(p);
//			for (ForumAgent a : this.registeredAgents) {
//
//				AgentAction action = a.processPost(dataStoreForum, p, pastActions);
//				
//				if (action != null) {
//					this.addToSystemLog(p, action);
//				}
//			
//			}
////			System.out.println();
//			
//			
//		}
		
	}
	

	// TODO: currently experiencing a problem where the log post can't be updated anymore bc it exceeds the max MongoDB size
	// 	this is because every time the log post is edited, it adds an entire snapshot of the current post body (which is very long) to the edit history
	// 	best solution I can think of is to keep track of the log state as a parameter and only actually update the post one at the end of each processPosts()
	
	
	
	/* HELPER METHODS */
	
	protected List<AgentAction> getPastActions(ForumPost post, List<JSONObject> logs) {
		
		List<AgentAction> actionList = new ArrayList<AgentAction>();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
		String postID = post.getPostID();
        
		// if the log is extended across multiple posts, traverse the posts like a linked list		
		for (JSONObject log : logs) {
			
			JSONArray pastActions = new JSONArray();
			
			if (log.has(postID)) {
				JSONObject postInfo = log.getJSONObject(postID);
				pastActions = postInfo.getJSONArray("actionsTaken");
			}
			
			for (int i = 0; i < pastActions.length(); i++) {
				JSONObject a = pastActions.getJSONObject(i);
				
				Date timestamp = new Date();
				try {
					timestamp = formatter.parse(a.getString("timestamp"));
				} catch (JSONException | ParseException e) {
					System.out.println("WARNING: Could not parse date '" + a.getString("timestamp") + "', using the current datetime.");
					e.printStackTrace();
				}

				String actionType = a.getString("actionType");
				if (actionType.equals("AnEnumAgentAction")) {
					
					// TODO: find a way to do this that's not hard coded
					Enum actionTaken = null;
					String agentName = a.getString("agentName");
					if (agentName.equals("Visibility Checker Agent"))
						actionTaken = VisibilityCheckerAction.valueOf(a.getString("actionTaken"));
					else if (agentName.equals("Image Checker Agent"))
						actionTaken = ImageCheckerAction.valueOf(a.getString("actionTaken"));
					else if (agentName.equals("Mediated LLM Agent"))
						actionTaken = MediatedLLMAction.valueOf(a.getString("actionTaken"));

					actionList.add(new AnEnumAgentAction(a.getString("agentName"), a.getInt("postNumber"), a.getInt("revNumber"), timestamp, actionTaken));
				
				} else if (actionType.equals("APostAgentAction")) {
					actionList.add(new APostAgentAction(a.getString("agentName"), a.getInt("postNumber"), a.getInt("revNumber"), timestamp, a.getInt("createdPostNumber")));
				} else {
					System.out.println("WARNING: Could not identify AgentAction type corresponding to '" + actionType + "'");	
				}
				
			}
			
		}	

		return actionList;
		
	}
	

}
