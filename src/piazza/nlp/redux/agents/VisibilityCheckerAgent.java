package piazza.nlp.redux.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import piazza.nlp.redux.actions.AgentAction;
import piazza.nlp.redux.actions.AnEnumAgentAction;
import piazza.nlp.redux.general.DataStoreDiscussionForum;
import piazza.nlp.redux.general.DiscussionForum;
import piazza.nlp.redux.general.ForumPost;
import piazza.nlp.redux.general.DiscussionForum.EditorType;
import piazza.nlp.redux.general.ForumPost.PostType;
import piazza.nlp.redux.general.ForumPost.PostVisibility;
import piazza.nlp.redux.piazza.APiazzaPostPreview;
import piazza.nlp.redux.piazza.PiazzaPost;

public class VisibilityCheckerAgent extends AnAbstractForumAgent implements ForumAgent {

	public enum VisibilityCheckerAction {
		VISIBILITY_CORRECT, SUGGESTED_PUBLIC, SUGGESTED_PRIVATE, SUGGESTED_ALL_INSTRUCTORS, SUGGESTED_PUBLIC_OR_ALL_INSTRUCTORS, FOLLOWUP_EXISTS;
	}
	
	final protected static String DEFAULT_NAME = "Visibility Checker Agent";
	final protected static String DESCRIPTION = "Determines whether the visibility of a post is appropriate, given its tags. If not, creates a followup instructing the post author to change either the visibility of their post or the post tags.";	

	final private static String DEFAULT_SUGGESTED_PUBLIC_MESSAGE = "Based on the folder tags associated with your post, it looks like the post visibility can be changed from private to public. In order to help as many students as possible, all posts should be made public unless they include code you�ve written or personal information. If your post meets these private criteria, please add the appropriate folder tags to your post (<code>includes_code</code>, <code>grading_error</code>, <code>personal_situation</code>, etc.) and keep the visibility as private. Otherwise, please edit your post and change the `Post To` setting from `Individual Student(s) / Instructor(s)` to `Entire Class`. Thanks!";
	final private static String DEFAULT_SUGGESTED_PRIVATE_MESSAGE = "Your post is tagged as [FOLDER_TAGS] even though its visibility is set to public. If these tags are correct and your post includes code you've written or involves a personal situation, please edit your post and change the `Post To` setting from `Entire Class` to `Instructors`. (Note that stack traces and error messages are fine to include in a public post!) Otherwise, please remove the incorrect folder tags from your post. Thanks!";
	final private static String DEFAULT_SUGGESTED_ALL_INSTRUCTORS_MESSAGE = "It appears that you've posted this to individual instructors. Please edit your post and select `Instructors` under the `Individual Student(s) / Instructor(s)` dropdown so that the entire instructional team can view your post. Thanks!";
	final private static String[] DEFAULT_PRIVATE_TAGS = {"includes_code", "office_hours", "grading_error", "personal_situation", "diary"};
	
	public VisibilityCheckerAgent(String agentName) {
		super(agentName, DESCRIPTION);
	}
	
	public VisibilityCheckerAgent() {
		this(DEFAULT_NAME);
	}
	
	
	
	/* ForumAgent METHODS */	
	
	@Override
	public void setUp(DataStoreDiscussionForum dataStoreForum) {
		
		DiscussionForum forum = dataStoreForum.getForum();

		List<String> suggestedMessageTags = new ArrayList<String>();
		suggestedMessageTags.add("automated");
		
		String suggestedPublicMessageID = forum.createPost("Suggested Public Message", DEFAULT_SUGGESTED_PUBLIC_MESSAGE, PostType.NOTE, PostVisibility.PRIVATE, suggestedMessageTags, EditorType.MARKDOWN);
		String suggestedPrivateMessageID = forum.createPost("Suggested Private Message", DEFAULT_SUGGESTED_PRIVATE_MESSAGE, PostType.NOTE, PostVisibility.PRIVATE, suggestedMessageTags, EditorType.MARKDOWN);
		String suggestedAllInstructorsMessageID = forum.createPost("Suggested All Instructors Message", DEFAULT_SUGGESTED_ALL_INSTRUCTORS_MESSAGE, PostType.NOTE, PostVisibility.PRIVATE, suggestedMessageTags, EditorType.MARKDOWN);
		
		dataStoreForum.registerData("suggestedPublicMessageID", String.class, suggestedPublicMessageID);	
		dataStoreForum.registerData("suggestedPrivateMessageID", String.class, suggestedPrivateMessageID);	
		dataStoreForum.registerData("suggestedAllInstructorsMessageID", String.class, suggestedAllInstructorsMessageID);	
		dataStoreForum.registerData("privateTags", DEFAULT_PRIVATE_TAGS.getClass(), DEFAULT_PRIVATE_TAGS);
		
	}

	@Override
	public AgentAction processPost(DataStoreDiscussionForum dataStoreForum, ForumPost post, List<? extends AgentAction> pastActions) {
		
		// TODO: In future, check to see if the post still violates the guidelines, and if not, delete the previously-created followups
		
		for (AgentAction a : pastActions) {
			if (a.getAgentName().equals(this.getAgentName())) {
				return null;
			}
		}
		
		if (post instanceof APiazzaPostPreview) {
			post = dataStoreForum.getForum().getPost(post.getPostID());
		}

		String postID = post.getPostID();
		VisibilityCheckerAction actionTaken = VisibilityCheckerAction.VISIBILITY_CORRECT;
				
		if (!post.getTags().contains("instructor-note")) {
		
			PostVisibility visibility = post.getVisibility();
			String[] privateTags =  (String[]) dataStoreForum.getDataValue("privateTags");
			boolean containsPrivateTags = post.getTags().stream().anyMatch(Arrays.asList(privateTags)::contains);
			
			boolean suggestPrivate = false;
			boolean suggestPublic = false;
			boolean suggestAllInstructors = false;
			
			// determine what followups should be made
			if (visibility == PostVisibility.PRIVATE) {
				
				if (post instanceof PiazzaPost) {
					//System.out.print(post.getAllData());
					Map<String, Object> config = (Map<String, Object>) post.getAllData().get("config");
					String feedGroups = (String) config.get("feed_groups");
					//System.out.println(feedGroups);
					List<String> postedTo = Arrays.asList(feedGroups.split(","));
					//System.out.println(postedTo);

					suggestAllInstructors = !(postedTo.contains("instr_" + post.getCourseID()) || postedTo.containsAll(dataStoreForum.getForum().getAdministrators()));					
				}
				
				suggestPublic = !containsPrivateTags;
			
			} else {
				
				suggestPrivate = containsPrivateTags;
			
			}
					
			// make the appropriate followups
			if (suggestPrivate || suggestPublic || suggestAllInstructors) {
			
				boolean createdFollowup;
				
				String automatedSuggestionDisclaimerID = (String) dataStoreForum.getDataValue("automatedSuggestionDisclaimerID");
				//System.out.println("automatedSuggestionDisclaimerID: " + automatedSuggestionDisclaimerID);
				String automatedSuggestionDisclaimer = dataStoreForum.getForum().getPost(automatedSuggestionDisclaimerID).getBody();

				if (suggestPrivate) {
					
					String tagString = "<code>" + String.join(", ", post.getTags()) + "</code>";
					
					String suggestedPrivateMessageID = (String) dataStoreForum.getDataValue("suggestedPrivateMessageID");
					String suggestedPrivateMessage = dataStoreForum.getForum().getPost(suggestedPrivateMessageID).getBody();
					suggestedPrivateMessage = suggestedPrivateMessage.replace("[FOLDER_TAGS]", tagString);
					
					createdFollowup = dataStoreForum.getForum().createFollowupIfDoesNotExist(postID, suggestedPrivateMessage + automatedSuggestionDisclaimer, EditorType.MARKDOWN);
					actionTaken = VisibilityCheckerAction.SUGGESTED_PRIVATE;
					
				}
				
				else {
					
					if (suggestPublic) {
						
						String suggestedPublicMessageID = (String) dataStoreForum.getDataValue("suggestedPublicMessageID");
						String suggestedPublicMessage = dataStoreForum.getForum().getPost(suggestedPublicMessageID).getBody();
						
						createdFollowup = dataStoreForum.getForum().createFollowupIfDoesNotExist(postID, suggestedPublicMessage + automatedSuggestionDisclaimer, EditorType.MARKDOWN);
						actionTaken = VisibilityCheckerAction.SUGGESTED_PUBLIC;			
						
					}
					
					if (suggestAllInstructors) {
						
						String suggestedAllInstructorsMessageID = (String) dataStoreForum.getDataValue("suggestedAllInstructorsMessageID");
						String suggestedAllInstructorsMessage = dataStoreForum.getForum().getPost(suggestedAllInstructorsMessageID).getBody();
						
						createdFollowup = dataStoreForum.getForum().createFollowupIfDoesNotExist(postID, suggestedAllInstructorsMessage + automatedSuggestionDisclaimer, EditorType.MARKDOWN);
						
						if (actionTaken == VisibilityCheckerAction.SUGGESTED_PUBLIC)
							actionTaken = VisibilityCheckerAction.SUGGESTED_PUBLIC_OR_ALL_INSTRUCTORS;
						else
							actionTaken = VisibilityCheckerAction.SUGGESTED_ALL_INSTRUCTORS;
	
					}
			
				}

				if (createdFollowup = false)
					actionTaken = VisibilityCheckerAction.FOLLOWUP_EXISTS;
				
			}
	
		}
		
		return new AnEnumAgentAction(this, post, new Date(), actionTaken);
		
	}
	
}