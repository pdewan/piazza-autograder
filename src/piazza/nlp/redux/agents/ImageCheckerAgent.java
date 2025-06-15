package piazza.nlp.redux.agents;

import java.util.List;

import piazza.nlp.redux.general.AgentAction;
import piazza.nlp.redux.general.DataStoreDiscussionForum;
import piazza.nlp.redux.general.ForumPost;

public class ImageCheckerAgent extends AnAbstractForumAgent implements ForumAgent {

	//final private String IMAGE_MESSAGE = "It looks like you may have included a screenshot in your post. If it is a screenshot of code or a console trace, please replace the image with the actual text itself so we can search for issues easier. If it is a screenshot of a Gradescope score displayed in the right tab, please paste the trace text shown in the tab on the left. If it is another type of image, please include any relevant text contained within the image (errors given in Eclipse pop-up windows, etc.). Thanks!";
	//final private String AUTOMATED_SUGGESTION_DISCLAIMER = "\n\n<hr/>\n\n<em>This message was generated automatically and could be incorrect. If you feel that it does not apply to your post, please disregard the suggestion.</em>";

	final protected static String DEFAULT_NAME = "Image Checker Agent";
	final protected static String DESCRIPTION = "Determines whether a post contains an image. If so, creates a followup instructing the post author to replace the image with the relevant text it contains.";
	
	public ImageCheckerAgent(String agentName) {
		super(agentName, DESCRIPTION);		
	}
	
	public ImageCheckerAgent() {
		this(DEFAULT_NAME);
	}
	
	@Override
	public AgentAction processPost(DataStoreDiscussionForum dataStoreForum, ForumPost post, List<AgentAction> pastActions) {

		// TODO: this may depend on the forum platform?
		String[] imageMarkers = {"<img src=", "!["};
		
		String postBody = post.getBody();
		String postID = post.getPostID();
		
		for (String marker : imageMarkers) {
			if (postBody.contains(marker)) {
			
				String automatedSuggestionDisclaimer;
				String containsImageMessage;
				
				//String automatedSuggestionDisclaimer = getAutomaticallyCreatedPost("automatedSuggestionDisclaimerID", AUTOMATED_DISCLAIMER_POST_NAME, AUTOMATED_SUGGESTION_DISCLAIMER, "other_tools");
				//String imageMessage = getAutomaticallyCreatedPost("screenshottedCodeMessageID", IMAGE_DETECTED_POST_NAME, IMAGE_MESSAGE, "other_tools");
				
				// TODO: createFollowupIfDoesNotExist? where should that method be added?
				// TODO: change this depending on the parameter types of createFollowup()
				
				// maybe we don't need createFollowupIfDoesNotExist now, because either we can check pastActions, or the dispatcher won't even call processPost() here
				
				//dataStoreForum.getForum().createFollowup(postID, containsImageMessage + automatedSuggestionDisclaimer);
				
				// TODO: finish
				
				break;
			}			
		}

		// TODO: how to log this? include the agent name and whether an image was present?
		return null;
		
	}
	
}
