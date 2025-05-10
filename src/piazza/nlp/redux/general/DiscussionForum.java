package piazza.nlp.redux.general;

import java.util.List;

import piazza.nlp.redux.general.ForumPost.PostType;
import piazza.nlp.redux.general.ForumPost.PostVisibility;

public interface DiscussionForum {

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
	public String createPost(String subject, String body, PostType type, PostVisibility visibility, List<String> tags); // returns the ID of the created post
	public String createInstructorAnswer(String postID, String body);
	public String createFollowup(String postID, String body); // TODO: visibility (public vs instructors-only)?
	public String createDraftPost(String subject, String body, PostType type, PostVisibility visibility, List<String> tags);
	public String createDraftInstructorAnswer(String postID, String body);
	public String createDraftFollowup(String postID, String body);
	public String updatePost(String postID, String newSubject, String newBody, PostType newType, PostVisibility newVisibility, List<String> newTags);
	public String updateInstructorAnswer(String responseID, String newBody); // NOTE: split updateResponse into two separate methods
	public String updateFollowup(String responseID, String newBody);
	public ForumUser getUser(String userID);
	public List<ForumUser> getAllUsers(); // figure out what format this should return
	public List<ForumUser> getAdministrators(); // figure out what format this should return
	public List<ForumPost> searchPosts(String query);

}
