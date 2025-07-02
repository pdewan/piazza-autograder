package piazza.nlp.redux.agents;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

public class MediatedLLMAgent extends AnAbstractForumAgent implements ForumAgent {
	
	public enum MediatedLLMAction {
		NOT_APPLICABLE; // TODO: currently using a non-enum action if a post is actually processed
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
		String mediatedLLMPromptID = forum.createPost("Mediated LLM Prompt", DEFAULT_MEDIATED_LLM_PROMPT, PostType.NOTE, PostVisibility.PRIVATE, mediatedLLMTags, EditorType.MARKDOWN);

		dataStoreForum.registerData("mediatedLLMPromptID", String.class, mediatedLLMPromptID);			
		dataStoreForum.registerData("assignmentTags", DEFAULT_ASSIGNMENT_TAGS.getClass(), DEFAULT_ASSIGNMENT_TAGS);
		
	}
	
	@Override
	public AgentAction processPost(DataStoreDiscussionForum dataStoreForum, ForumPost post, List<? extends AgentAction> pastActions) {
				
		
		// TODO: support re-processing of posts once edited
		// does revision number need to be an element of AgentAction then?
		//   currently we have the post and can get it from that, but it depends on how it gets processed using JSON
		
		
		// TODO: don't run until it passes ImageChecker
		
		
		// if the agent has already processed the post, don't process it again
		for (AgentAction a : pastActions) {
			if (a.getAgentName().equals(this.getAgentName())) {
				return null;
			}
		}	
		
		String[] assignmentTags = (String[]) dataStoreForum.getDataValue("assignmentTags");
		boolean helpNeeded = (post.getType() == PostType.QUESTION) && (post.getTags().stream().anyMatch(Arrays.asList(assignmentTags)::contains));
		
		if (!helpNeeded) {
			return new AnEnumAgentAction(this, post, new Date(), MediatedLLMAction.NOT_APPLICABLE);
		}
		
		if (post instanceof APiazzaPostPreview) {
			post = dataStoreForum.getForum().getPost(post.getPostID());
		}
		
		DiscussionForum forum = dataStoreForum.getForum();
		String studentQuestion = post.getSubject();
		
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
		
		// set up instructor reference thread
		String author = "Anonymous Student";
		try {
			author = forum.getUser(post.getAuthorID()).getName();
		} catch (AnonymousDataAccessException e) {
			e.printStackTrace();
		}
		String postTime = (new SimpleDateFormat("MMMM d 'at' K:ma")).format(post.getDateCreated());
		
		String refPostSubject = "Instructor reference thread for \"" + post.getSubject() + "\"";				
		String refPostContent = "The following question was submitted by " + author + " on " + postTime + " (@" + post.getPostNumber() + "):\n\n" + studentQuestion;

		List<String> refPostTags = new ArrayList<String>();
		refPostTags.add("automated");
		refPostTags.add("instructor_reference");
		if (assignmentTag != null)
			refPostTags.add(assignmentTag);

		String refPostID = forum.createPost(refPostSubject, refPostContent, PostType.NOTE, PostVisibility.PRIVATE, refPostTags, EditorType.MARKDOWN);
		String refPostFollowup = "The following response was generated by GPT:\\n\\n" + gptResponse; // TODO: change text if more general LLM
		forum.createFollowup(refPostID, refPostFollowup, EditorType.MARKDOWN);
		
		// draft the LLM answer under the original question
		int refPostNumber = forum.getPost(refPostID).getPostNumber();
		String draftAnswer = gptResponse + "\n\n---\n\n_This post was drafted automatically using GPT. The private instructor-only reference thread for this discussion is @" + refPostNumber + "_";
		forum.createDraftInstructorAnswer(post.getPostID(), draftAnswer, EditorType.MARKDOWN);
		
		return new APostAgentAction(this, post, new Date(), refPostNumber);
		
	}
	
	
	
	/* HELPER METHODS */

	protected String fetchAssignmentInstructions(DataStoreDiscussionForum dataStoreForum, String assignmentTag) {
		
		List<String> postTags;
		List<ForumPost> searchResults = dataStoreForum.getForum().searchPosts(assignmentTag + " Instructions");
		
		for (ForumPost p : searchResults) {
			postTags = p.getTags();
			
			if (postTags.contains(assignmentTag) && postTags.contains("assignment_instructions")) {
				return p.getPostID();
			}
			
		}
		
		return null;
		
	}
	
	
}
