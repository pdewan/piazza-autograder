package piazza.nlp.redux.agents;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import piazza.nlp.redux.general.AgentAction;
import piazza.nlp.redux.general.DataStoreDiscussionForum;
import piazza.nlp.redux.general.ForumPost;

public class VisibilityCheckerAgent extends AnAbstractForumAgent implements ForumAgent {

	//final private String SUGGESTED_PUBLIC_MESSAGE = "Based on the folder tags associated with your post, it looks like the post visibility can be changed from private to public. In order to help as many students as possible, all posts should be made public unless they include code you’ve written or personal information. If your post meets these private criteria, please add the appropriate folder tags to your post (<code>includes_code</code>, <code>grading_error</code>, <code>personal_situation</code>, etc.) and keep the visibility as private. Otherwise, please edit your post and change the “Post To” setting from “Individual Student(s) / Instructor(s)” to “Entire Class”. Thanks!";
	//final private String SUGGESTED_PRIVATE_MESSAGE = "Your post is tagged as [FOLDER_TAGS] even though its visibility is set to public. If these tags are correct and your post includes code you’ve written or involves a personal situation, please edit your post and change the “Post To” setting from “Entire Class” to “Instructors”. (Note that stack traces and error messages are fine to include in a public post!) Otherwise, please remove the incorrect folder tags from your post. Thanks!";
	//final private String SUGGESTED_ALL_INSTRUCTORS = "It appears that you’ve posted this to individual instructors. Please edit your post and select “Instructors” under the “Individual Student(s) / Instructor(s)” dropdown so that the entire instructional team can view your post. Thanks!";
	//final private String AUTOMATED_SUGGESTION_DISCLAIMER = "\n\n<hr/>\n\n<em>This message was generated automatically and could be incorrect. If you feel that it does not apply to your post, please disregard the suggestion.</em>";

	final protected static String DEFAULT_NAME = "Visibility Checker Agent";
	final protected static String DESCRIPTION = "Determines whether the visibility of a post is appropriate, given its tags. If not, creates a followup instructing the post author to change either the visibility of their post or the post tags.";	
	
	public VisibilityCheckerAgent(String agentName) {
		super(agentName, DESCRIPTION);
	}
	
	public VisibilityCheckerAgent() {
		this(DEFAULT_NAME);
	}

	@Override
	public AgentAction processPost(DataStoreDiscussionForum dataStoreForum, ForumPost post, List<AgentAction> pastActions) {

		// TODO: check if not made by instructors
			// method for this? or get the author and then their admin?
		
		
		
		
		
		return null;
		
	}
	
}