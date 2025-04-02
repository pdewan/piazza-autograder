package piazza.nlp.redux;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public interface DiscussionForum {
	
	// should these be Map<String, Object> instead of JSONObject?
	public Post getPost(String postID);
	public List<Post> getAllPosts();
	public String createPost(JSONObject content);
	public String createInstructorAnswer(String postID, JSONObject content);
	public String createFollowup(String postID, JSONObject content);
	public String createDraftPost(JSONObject content);
	public String createDraftInstructorAnswer(String postID, JSONObject content);
	public String createDraftFollowup(String postID, JSONObject content);
	public String updatePost(String postID, JSONObject content);
	public String updateResponse(String responseID, JSONObject content); // figure out how editing answers/followups works
	public List<ForumUser> getAllUsers(); // figure out what format this should return
	public List<ForumUser> getAdministrators(); // figure out what format this should return
	public List<Post> searchPosts(String query); // TODO: add other parameters here? -- can't natively for Piazza
}

/* Differences from the reference model:
 * createInstructorAnswer() and createFollowup() instead of createResponse(), uses different parameters
*/

// internally store the user as an instance variable, keep the user as a parameters
// create classes for different types
	// in mason B paper, discuss the pros and cons of these decisions

// 	public boolean logInUser(JSONObject credentials); // logs into a Piazza user, so that the other methods may use this user in the API calls. returns a boolean of if it successfully logged in

// role: "sent as instructor" vs "sent as agent"
	// account can take multiple roles
	// write vs read, view vs comment vs edit
	// messages are signed by role

// either make Piazza session instance variable or make the http request instance var
// getter and setter for piazza session