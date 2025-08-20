package piazza.nlp.redux.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;

import piazza.nlp.redux.actions.AgentAction;
import piazza.nlp.redux.actions.AnEnumAgentAction;
import piazza.nlp.redux.general.DataStoreDiscussionForum;
import piazza.nlp.redux.general.DiscussionForum;
import piazza.nlp.redux.general.DiscussionForum.EditorType;
import piazza.nlp.redux.general.ForumPost;
import piazza.nlp.redux.general.ForumPost.PostType;
import piazza.nlp.redux.general.ForumPost.PostVisibility;
import piazza.nlp.redux.piazza.APiazzaPostPreview;

public class ImageCheckerAgent extends AnAbstractForumAgent implements ForumAgent {

	public enum ImageCheckerAction {
		NO_IMAGE, NEW_FOLLOWUP, FOLLOWUP_EXISTS, IMAGE_REMOVED, NOT_APPLICABLE;
	}
	
	final protected static String DEFAULT_NAME = "Image Checker Agent";
	final protected static String DESCRIPTION = "Determines whether a post contains an image. If so, creates a followup instructing the post author to replace the image with the relevant text it contains.";

	final private static String DEFAULT_CONTAINS_IMAGE_MESSAGE = "It looks like you may have included a screenshot in your post. If it is a screenshot of code or a console trace, please replace the image with the actual text itself so we can search for issues easier. If it is a screenshot of a Gradescope score displayed in the right tab, please paste the trace text shown in the tab on the left. If it is another type of image, please include any relevant text contained within the image (errors given in Eclipse pop-up windows, etc.). Thanks!";
	
	public ImageCheckerAgent(String agentName) {
		super(agentName, DESCRIPTION);		
	}
	
	public ImageCheckerAgent() {
		this(DEFAULT_NAME);
	}

	
	
	/* ForumAgent METHODS */	
	
	@Override
	public void setUp(DataStoreDiscussionForum dataStoreForum) {

		DiscussionForum forum = dataStoreForum.getForum();
		
		List<String> containsImageTags = new ArrayList<String>();
		containsImageTags.add("automated");
		containsImageTags.add("agent_data");
		String containsImageMessageID = forum.createPost("Contains Image Message", StringEscapeUtils.unescapeHtml4(DEFAULT_CONTAINS_IMAGE_MESSAGE), PostType.NOTE, PostVisibility.PRIVATE, containsImageTags, EditorType.MARKDOWN);
		
		dataStoreForum.registerData("containsImageMessageID", String.class, containsImageMessageID);	
		
	}
	
	@Override
	public AgentAction processPost(DataStoreDiscussionForum dataStoreForum, ForumPost post, List<? extends AgentAction> pastActions) {

		// TODO: In future, check to see if the post still violates the guidelines, and if not, delete the previously-created followups
		
		Map<Integer, ImageCheckerAction> imageCheckerActions = new HashMap<Integer, ImageCheckerAction>();
		for (AgentAction a : pastActions) {
			if (a.getAgentName().equals(this.getAgentName())) {				
				imageCheckerActions.put(Integer.valueOf(a.getRevNumber()), (ImageCheckerAction) ((AnEnumAgentAction)a).getActionTaken());
			}
		}
		
		// if post has not been updated since the last time this agent has been run, do not reprocess
		if (imageCheckerActions.containsKey(Integer.valueOf(post.getRevisionNumber())))
			return null;
		// if the post passed the no-image check, do not reprocess
		else if (imageCheckerActions.containsValue(ImageCheckerAction.NO_IMAGE) || imageCheckerActions.containsValue(ImageCheckerAction.IMAGE_REMOVED) || imageCheckerActions.containsValue(ImageCheckerAction.NOT_APPLICABLE))
			return null;
		
		String[] assignmentTags = (String[]) dataStoreForum.getDataValue("assignmentTags");
		boolean helpNeeded = (post.getType() == PostType.QUESTION) && (post.getTags().stream().anyMatch(Arrays.asList(assignmentTags)::contains)) && !(post.getTags().contains("personal_situation"));
		if (!helpNeeded)
			return new AnEnumAgentAction(this.getAgentName(), post.getPostNumber(), post.getRevisionNumber(), new Date(), ImageCheckerAction.NOT_APPLICABLE);
				
		boolean containedImageBefore = imageCheckerActions.containsValue(ImageCheckerAction.NEW_FOLLOWUP);
		
		if (post instanceof APiazzaPostPreview) {
			post = dataStoreForum.getForum().getPost(post.getPostID());
		}
		
		// TODO: these markers may depend on the forum platform?
		String[] imageMarkers = {"<img src=", "!["};
		
		String postBody = post.getBody();
		String postID = post.getPostID();
		ImageCheckerAction actionTaken = containedImageBefore ? ImageCheckerAction.IMAGE_REMOVED : ImageCheckerAction.NO_IMAGE;
		
		for (String marker : imageMarkers) {
			if (postBody.contains(marker)) {
							
				String containsImageMessageID = (String) dataStoreForum.getDataValue("containsImageMessageID");
				String containsImageMessage = dataStoreForum.getForum().getPost(containsImageMessageID).getBody();
			
				String automatedSuggestionDisclaimerID = (String) dataStoreForum.getDataValue("automatedSuggestionDisclaimerID");
				String automatedSuggestionDisclaimer = dataStoreForum.getForum().getPost(automatedSuggestionDisclaimerID).getBody();
				
				boolean createdFollowup = dataStoreForum.getForum().createFollowupIfDoesNotExist(postID, containsImageMessage + automatedSuggestionDisclaimer, EditorType.MARKDOWN, false);
				actionTaken = createdFollowup ? ImageCheckerAction.NEW_FOLLOWUP : ImageCheckerAction.FOLLOWUP_EXISTS;
				
			}			
		}
		
		return new AnEnumAgentAction(this.getAgentName(), post.getPostNumber(), post.getRevisionNumber(), new Date(), actionTaken);
		
	}
	
}
