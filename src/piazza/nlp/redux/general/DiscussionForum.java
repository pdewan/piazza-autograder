package piazza.nlp.redux.general;

import java.util.List;

import piazza.nlp.redux.general.ForumPost.PostType;
import piazza.nlp.redux.general.ForumPost.PostVisibility;

public interface DiscussionForum {

	public enum EditorType {
		PLAIN_TEXT, RICH_TEXT, MARKDOWN; // TODO: others for other forums?
	}
	
	// NOTE: now that we've changed the parameters to post-creating methods from JSONObjects to individual parameters, there may be some platform-specific functionality lost.
	// 	in these cases, you can make platform-specific overloaded versions of these methods in addition to the ones specified here.
	// 	for example, a Piazza-specific version of createPost() that takes a list of individual recipients instead of a boolean PostVisibility enum.
	
	// TODO: createInstructorAnswer() and createFollowup() are very similar in the Piazza API, if the same is true with Ed, then maybe they should be collapsed into createResponse()
	// 	in that case, the response type can be another enum parameter similar to PostType
	
	// TODO: getClassID()?
	public String getPlatformName();
	public String getCourseName();
	public ForumPost getPost(String postID);
	public List<ForumPost> getAllPosts();
	public String createPost(String subject, String body, PostType type, PostVisibility visibility, List<String> tags, EditorType editor); // returns the ID of the created post
	public String createInstructorAnswer(String postID, String body, EditorType editor);
	public String createFollowup(String postID, String body, EditorType editor, boolean instructorOnly); // TODO: change boolean instructorOnly to enum visibility (public vs instructors-only)?
	public String createDraftPost(String subject, String body, PostType type, PostVisibility visibility, List<String> tags, EditorType editor);
	public String createDraftInstructorAnswer(String postID, String body, EditorType editor);
	public String createDraftFollowup(String postID, String body, EditorType editor, boolean instructorOnly);
	public String updatePost(String postID, String newSubject, String newBody, PostType newType, PostVisibility newVisibility, List<String> newTags, EditorType editor);
	public String updateInstructorAnswer(String responseID, String newBody, EditorType editor); // NOTE: split updateResponse into two separate methods
	public String updateFollowup(String responseID, String newBody, EditorType editor);
	public boolean createFollowupIfDoesNotExist(String postID, String body, EditorType editor, boolean instructorOnly); // returns true if followup is created and false if a followup with the same body already exists
	public ForumUser getUser(String userID);
	public List<ForumUser> getAllUsers(); // figure out what format this should return
	public List<ForumUser> getAdministrators(); // figure out what format this should return
	public List<ForumPost> searchPosts(String query);

}
