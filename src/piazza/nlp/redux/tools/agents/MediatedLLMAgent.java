package piazza.nlp.redux.tools.agents;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;

import piazza.nlp.AGPTClass;
import piazza.nlp.redux.actions.APostAgentAction;
import piazza.nlp.redux.actions.AgentAction;
import piazza.nlp.redux.actions.AnEnumAgentAction;
import piazza.nlp.redux.exceptions.AnonymousDataAccessException;
import piazza.nlp.redux.general.DataStoreDiscussionForum;
import piazza.nlp.redux.general.DiscussionForum;
import piazza.nlp.redux.general.DiscussionForum.EditorType;
import piazza.nlp.redux.general.ForumPost;
import piazza.nlp.redux.general.ForumPost.PostType;
import piazza.nlp.redux.general.ForumPost.PostVisibility;
import piazza.nlp.redux.piazza.APiazzaPostPreview;
import piazza.nlp.redux.tools.agents.ImageCheckerAgent.ImageCheckerAction;

public class MediatedLLMAgent extends AnAbstractForumAgent implements ForumAgent {
	
	public enum MediatedLLMAction {
		NOT_APPLICABLE, WAITING_FOR_IMAGE_REMOVAL; // TODO: currently using a non-enum action if a post is actually processed
	}
	
	final protected static String DEFAULT_NAME = "Mediated LLM Agent";
	final protected static String DESCRIPTION = ""; // TODO
	
	final private static String DEFAULT_MEDIATED_LLM_PROMPT = "You are a Teaching Assistant for an upper-level Computer Science course. Imagine a student is working on an assignment according to the ASSIGNMENT INSTRUCTIONS below, and the student comes to you with the following STUDENT QUESTION.<br /><br />----------<br /><br />ASSIGNMENT INSTRUCTIONS:<br />[ASSIGNMENT_INSTRUCTIONS]<br /><br />----------<br /><br />STUDENT QUESTION:<br />[STUDENT_QUESTION]<br /><br />----------<br /><br />What should the student be told? You should not give them code in your response. Instead, guide the student to an answer using a step-by-step natural language explanation. You should not give them the full answer all at once, instead reduce the problem for the student by providing a series of smaller tasks for the student to solve that will help them answer their question.";
	final private static String[] DEFAULT_ASSIGNMENT_TAGS = {"a0", "a1", "a2", "a3", "a4", "a1.1", "a2.1"};

	protected AGPTClass gpt; // TODO: support for other LLMs
	
	public MediatedLLMAgent(String agentName) {
		super(agentName, DESCRIPTION);
		
		String apiKey = System.getenv("OPENAI_API_KEY");
		String defaultModel = System.getenv("DEFAULT_GPT_MODEL");			
		this.gpt = new AGPTClass(apiKey, defaultModel);
	}
	
	public MediatedLLMAgent() {
		this(DEFAULT_NAME);
	}

	
	
	/* ForumAgent METHODS */	
	
	@Override
	public void setUp(DataStoreDiscussionForum dataStoreForum) {
		
		DiscussionForum forum = dataStoreForum.getForum();
		
		List<String> mediatedLLMTags = new ArrayList<String>();
		mediatedLLMTags.add("automated");
		mediatedLLMTags.add("agent_data");
		String mediatedLLMPromptID = forum.createPost("Mediated LLM Prompt", DEFAULT_MEDIATED_LLM_PROMPT, PostType.NOTE, PostVisibility.PRIVATE, mediatedLLMTags, EditorType.MARKDOWN);

		dataStoreForum.registerData("mediatedLLMPromptID", String.class, mediatedLLMPromptID);			
		dataStoreForum.registerData("assignmentTags", DEFAULT_ASSIGNMENT_TAGS.getClass(), DEFAULT_ASSIGNMENT_TAGS);
		
	}
	
	@Override
	public AgentAction processPost(DataStoreDiscussionForum dataStoreForum, ForumPost post, List<? extends AgentAction> pastActions) {
				
		// TODO: if can't find assignment writeup, don't ask GPT? and process once it's been added?
		
		// TODO: support re-processing of posts once edited
			// how would this work with reference posts?
		
		Map<Integer, ImageCheckerAction> imageCheckerActions = new HashMap<Integer, ImageCheckerAction>();

		// if the agent has already processed the post, don't process it again
		boolean waitingForImageRemoval = false;
		for (AgentAction a : pastActions) {
		
			if (a instanceof APostAgentAction)
				return null;
			
			if (a instanceof AnEnumAgentAction) {
				AnEnumAgentAction act = (AnEnumAgentAction) a;
				
				if (act.getActionTaken() == MediatedLLMAction.NOT_APPLICABLE)
					return null;
				
				if (act.getActionTaken() == MediatedLLMAction.WAITING_FOR_IMAGE_REMOVAL)
					waitingForImageRemoval = true;
				
				if (a.getAgentName().contains("Image Checker Agent"))			
					imageCheckerActions.put(Integer.valueOf(a.getRevNumber()), (ImageCheckerAction) ((AnEnumAgentAction)a).getActionTaken());
				
			}
		
		}
		
		// if ImageCheckerAgent detected an image that has not been fixed, wait until it is fixed to process
		if ((imageCheckerActions.containsValue(ImageCheckerAction.NEW_FOLLOWUP) || imageCheckerActions.containsValue(ImageCheckerAction.FOLLOWUP_EXISTS)) && !imageCheckerActions.containsValue(ImageCheckerAction.IMAGE_REMOVED)) {
		
			if (waitingForImageRemoval)
				return null;
			else
				return new AnEnumAgentAction(this.getName(), post.getPostNumber(), post.getRevisionNumber(), new Date(), MediatedLLMAction.WAITING_FOR_IMAGE_REMOVAL);
		
		}
			
		String[] assignmentTags = (String[]) dataStoreForum.getDataValue("assignmentTags");
		boolean helpNeeded = (post.getType() == PostType.QUESTION) && (post.getTags().stream().anyMatch(Arrays.asList(assignmentTags)::contains)) && !(post.getTags().contains("personal_situation"));
		
		if (!helpNeeded) {
			return new AnEnumAgentAction(this.getName(), post.getPostNumber(), post.getRevisionNumber(), new Date(), MediatedLLMAction.NOT_APPLICABLE);
		}
		
		if (post instanceof APiazzaPostPreview) {
			post = dataStoreForum.getForum().getPost(post.getPostID());
		}
		
		DiscussionForum forum = dataStoreForum.getForum();
		String studentQuestion = post.getBody();
		
		String mediatedLLMPromptID = (String) dataStoreForum.getDataValue("mediatedLLMPromptID");
		String mediatedLLMPrompt = forum.getPost(mediatedLLMPromptID).getBody();
		
		String assignmentInstructions = "(No assignment instructions found)";
		String assignmentTag = null;
		for (String t : post.getTags()) {
			if (Arrays.asList(assignmentTags).contains(t)) {
				
				assignmentTag = t;
				String instructionsPostID = fetchAssignmentInstructions(dataStoreForum, t);
			
				if (instructionsPostID != null) {	
					assignmentInstructions = forum.getPost(instructionsPostID).getBody();
					break;
				}
				
				else {
					System.out.println("WARNING: Could not find assignment instructions for assignment " + t);
				}
				
			}
		}		
		
		String filledPrompt = mediatedLLMPrompt.replace("[ASSIGNMENT_INSTRUCTIONS]", assignmentInstructions).replace("[STUDENT_QUESTION]", studentQuestion);

		String gptResponse = null;
		try {
			gptResponse = gpt.makeCallWithBackoff(filledPrompt);
		} catch (IOException e) {
			e.printStackTrace();
		}
		gptResponse = StringEscapeUtils.unescapeHtml4(gptResponse); // TODO: does this work?
		//gptResponse = gptResponse.replaceAll("\\\\n", "\n"); // TODO: change the way this works?
		
		if (gptResponse.equals("Error: OpenAI API response timed out after backoff.")) {
			forum.createDraftInstructorAnswer(post.getPostID(), gptResponse, EditorType.MARKDOWN);
			return null;	
		}
		
		// LEGACY INSTRUCTOR REFERENCE POST
		// TODO: figure out if we need to use this old version for other forum platforms
		// set up instructor reference thread
		/* 
		String author = "Anonymous Student";
		try {
			author = forum.getUser(post.getAuthorID()).getName();
		} catch (AnonymousDataAccessException e) {
			System.out.println("WARNING: " + e.getMessage());
		}
		String postTime = (new SimpleDateFormat("MMMM d 'at' K:mma")).format(post.getDateCreated());
		
		String refPostSubject = "Instructor reference thread for \"" + post.getSubject() + "\"";				
		String refPostContent = "The following question was submitted by " + author + " on " + postTime + " (@" + post.getPostNumber() + "):\n\n" + studentQuestion;

		List<String> refPostTags = new ArrayList<String>();
		refPostTags.add("automated");
		refPostTags.add("instructor_reference");
		if (assignmentTag != null)
			refPostTags.add(assignmentTag);

		String refPostID = forum.createPost(refPostSubject, refPostContent, PostType.NOTE, PostVisibility.PRIVATE, refPostTags, EditorType.MARKDOWN);
		String refPostFollowup = "The following response was generated by GPT:\n\n" + gptResponse; // TODO: change text if more general LLM
		forum.createFollowup(refPostID, refPostFollowup, EditorType.MARKDOWN);
		
		// draft the LLM response as an instructor answer
		int refPostNumber = forum.getPost(refPostID).getPostNumber();
		String draftAnswer = gptResponse + "\n\n---\n\n_This post was drafted automatically using GPT. The private instructor-only reference thread for this discussion is @" + refPostNumber + "_";
		forum.createDraftInstructorAnswer(post.getPostID(), draftAnswer, EditorType.MARKDOWN);
		*/
		
		/* set up instructor reference followup */
		String postTime = (new SimpleDateFormat("K:mma 'on' MMMM d")).format(post.getDateUpdated());	
		String refPostText = "The following was generated by an LLM in response to the above post (as of " + postTime + "):\n\n" + gptResponse;
		String refPostID = forum.createFollowup(post.getPostID(), refPostText, EditorType.MARKDOWN, true);
		
		// draft the LLM response as an instructor answer
		int refPostNumber = forum.getPost(refPostID).getPostNumber();
		String draftAnswer = gptResponse + "\n\n---\n\n_This post was drafted automatically using an LLM._";
		forum.createDraftInstructorAnswer(post.getPostID(), draftAnswer, EditorType.MARKDOWN);
		
		return new APostAgentAction(this.getName(), post.getPostNumber(), post.getRevisionNumber(), new Date(), refPostNumber);
		
	}
	
	
	
	/* HELPER METHODS */

	protected String fetchAssignmentInstructions(DataStoreDiscussionForum dataStoreForum, String assignmentTag) {
		
		List<String> postTags;
		List<ForumPost> searchResults = dataStoreForum.getForum().searchPosts(assignmentTag + " Instructions");
		
		for (ForumPost p : searchResults) {
			postTags = p.getTags();
			
			if (postTags.contains(assignmentTag) && postTags.contains("agent_data")) {
				return p.getPostID();
			}
			
		}
		
		return null;
		
	}
	
	
}
