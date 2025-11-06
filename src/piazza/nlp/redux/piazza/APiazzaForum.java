package piazza.nlp.redux.piazza;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import piazza.ANewPiazzaSession;
import piazza.PiazzaSession;
import piazza.nlp.redux.exceptions.AnonymousDataAccessException;
import piazza.nlp.redux.exceptions.InvalidCallException;
import piazza.nlp.redux.exceptions.LoginFailedException;
import piazza.nlp.redux.exceptions.NotLoggedInException;
import piazza.nlp.redux.general.ForumPost;
import piazza.nlp.redux.general.ForumPost.PostType;
import piazza.nlp.redux.general.ForumPost.PostVisibility;
import piazza.nlp.redux.general.ForumUser;
import piazza.nlp.redux.general.DiscussionForum.EditorType;

public class APiazzaForum implements PiazzaForum { // MixedInitiativeDiscussionForum
	
	protected String courseName;
	protected String classID;
	protected PiazzaSession currentSession;
	
	// create forum object using a new Piazza API session for the given email/password
	public APiazzaForum(String courseName, String classID, String email, String password) {
		this.courseName = courseName;
		this.classID = classID;
		this.currentSession = new ANewPiazzaSession();
		try {
			this.currentSession.login(email, password);
		} catch (IOException | LoginFailedException e) {
			e.printStackTrace();
		}
	}
	
	// create forum object using an existing Piazza API session
	// assumes the session has already been logged into, otherwise other methods will throw an error
	public APiazzaForum(String courseName, String classID, PiazzaSession initialSession) {
		this.courseName = courseName;
		this.classID = classID;
		this.currentSession = initialSession; 
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + this.getCourseName() + "}";
	}
	
	
	
	/* DiscussionForum METHODS */
	
	@Override
	public String getPlatformName() {
		return "Piazza";
	}

	@Override
	public String getCourseName() {
		return this.courseName;
	}
	
	// get the forum "session" that API calls are delegated to
	@Override
	public PiazzaSession getForumSession() {
		return this.currentSession;
	}
	
	// swap the current forum session with a new session, returning the session that was just swapped out
	@Override
	public PiazzaSession swapForumSession(PiazzaSession newSession) {
		PiazzaSession oldSession = this.getForumSession();
		this.currentSession = newSession;
		return oldSession;
	}
	
	// get post from ID (e.g. m6ie32r77ki5y0)
	@Override
	public ForumPost getPost(String postID) {
		
		//System.out.println("Post ID to be gotten: " + postID);
		
		JSONObject data = new JSONObject()
				.put("cid", postID)
				.put("nid", this.classID)
				.put("student_view", JSONObject.NULL);
		
		Map<String, Object> postData = (Map<String, Object>) makeCallWithBackoff("content.get", data);
				
		return new APiazzaPost(postData, this.classID);
		
	}
	
	// get list of all posts in the class
	// NOTE: makes a call to getPost() for each one; use getFeed() instead if post previews are sufficient
	@Override
	public List<ForumPost> getAllPosts() {
		
		// TODO: add filtering on number, date, and tag like in getAllPostsRecursive()
		// TODO: add progress bar printing
		
		List<APiazzaPostPreview> feed = getFeed();
		
		List<ForumPost> posts = new ArrayList<ForumPost>();
		for (APiazzaPostPreview p : feed) {
			posts.add(getPost((String) p.getPostID()));
		}
		
		return posts;
		
	}
	
	// get user info for the provided user ID
	@Override
	public ForumUser getUser(String userID) {
		return this.getUsers(new String[] {userID}).get(0);
	}
	
	// get a list of all users in the class
	@Override
	public List<ForumUser> getAllUsers() {
		
		JSONObject data = new JSONObject()
				.put("nid", this.classID);
		
		List<Map<String, Object>> resp = (List<Map<String, Object>>) makeCallWithBackoff("network.get_all_users", data);
		
		List<ForumUser> users = new ArrayList<ForumUser>();
		for (Map<String, Object> userInfo : resp) {
			users.add(new APiazzaUser(userInfo));
		}
		
		return users;
		
	}
	
	// get a list of all administrators in the class
	@Override
	public List<ForumUser> getAdministrators() {
		
		List<ForumUser> allUsers = getAllUsers();
		List<ForumUser> admins = new ArrayList<ForumUser>();
		
		for (ForumUser u : allUsers) {
			if (u.getAdmin()) {
				admins.add(u);
			}
		}
		
		return admins;
	
	}
	
	// get posts corresponding to a search query. use queryPosts() instead if post previews are sufficient, as it makes less API calls
	@Override
	public List<ForumPost> searchPosts(String query) {
		
		List<APiazzaPostPreview> queryResults = queryPosts(query);
		
		List<ForumPost> posts = new ArrayList<ForumPost>();
		for (APiazzaPostPreview p : queryResults) {
			posts.add(getPost((String) p.getAllData().get("id")));
		}
		
		return posts;
		
	}
	
	// create a new Piazza post with the given parameters
	// TODO: make Piazza-specific version with stuff like recipients (string of user IDs) and anonymity (which could be enums)
	// 	original implementation header: public String createPost(String subject, String content, List<String> tags, List<String> recipients, String messageType, String editorType) {
	@Override
	public String createPost(String subject, String body, PostType type, PostVisibility visibility, List<String> tags, EditorType editor) {
	
		String typeString;
		if (type == PostType.QUESTION)
			typeString = "question";
		else if (type == PostType.POLL)
			typeString = "poll";
		else
			typeString = "note";
		
		JSONObject data = new JSONObject()
			.put("nid", this.classID)
			.put("type", typeString)
			.put("subject", subject)
			.put("content", body)
			.put("folders", tags)
			.put("editor", this.convertEditorType(editor))
			.put("anonymous", "no"); // TODO: allow other anonymities
		
		if (visibility == PostVisibility.PRIVATE) {
			String recipients = "instr_" + this.classID; // TODO: allow other individual recipients (should be separated by ',')
			Map<String, String> config = new HashMap();
			config.put("feed_groups", recipients);
			data.put("config", config);
			data.put("status", "private");
		} else {
			data.put("status", "active");
		}
		
		Map<String, Object> resp = (Map<String, Object>) makeCallWithBackoff("content.create", data);	
		
		return (String) resp.get("id");
		
		// TODO: could change the interface to return the post object, if that makes sense with other platforms:
		// APiazzaPost createdPost = new APiazzaPost(resp, this.classID);
		// return createdPost.getPostID(); 
		
	}

	// create an instructor answer for a given question
	@Override
	public String createInstructorAnswer(String postID, String body, EditorType editor) {
		
		JSONObject data = new JSONObject()
				.put("network_id", this.classID)
				.put("cid", postID)
				.put("content", body)
				.put("type", "i_answer")
				.put("editor", this.convertEditorType(editor))
				.put("revision", 0) // Note: if an instructor answer already exists (and the revision number is not incremented to match), the API call will do nothing
				.put("anonymous", "no"); // TODO: allow other anonymyities? (not sure if possible)

		Map<String, Object> resp = (Map<String, Object>) makeCallWithBackoff("content.answer", data);	
		
		return (String) resp.get("id");
		
		// TODO: create answer object? this is the format of the HashMap:
		// {history_size=1, folders=[], data={embed_links=[]}, created=2025-05-09T01:22:32Z, bucket_order=3, tag_endorse=[], bucket_name=Today, history=[{anon=no, uid=lljvnbpqdze3xm, subject=, created=2025-05-09T01:22:32Z, content=Test redux instructor answer}], type=i_answer, tag_endorse_arr=[], children=[], id=mag431foymr41s, config={editor=md}}
		
	}
	
	// create a followup to a given post
	@Override
	public String createFollowup(String postID, String body, EditorType editor, boolean instructorOnly) {
	
		JSONObject data = new JSONObject()
				.put("network_id", this.classID)
				.put("cid", postID)
				.put("subject", body)
				.put("type", "followup")
				.put("editor", this.convertEditorType(editor))
				.put("content", "")
				.put("anonymous", "no"); // TODO: allow other anonymyities? (not sure if possible)

		if (instructorOnly) {
			JSONObject config = new JSONObject()
				.put("ionly", true);
			data.put("config", config);
		}
		
		Map<String, Object> resp = (Map<String, Object>) makeCallWithBackoff("content.create", data);	
		
		return (String) resp.get("id");
		
		// TODO: create followup object? this is the format of the HashMap:
		// {anon=no, folders=[], data=null, no_upvotes=0, subject=Test redux followup, created=2025-05-08T08:58:10Z, bucket_order=3, bucket_name=Today, type=followup, tag_good=[], uid=lljvnbpqdze3xm, children=[], tag_good_arr=[], no_answer=1, id=maf4x51yp792d3, updated=2025-05-08T08:58:10Z, config={}}
		
	}
	
	// draft a new Piazza post with the given parameters
	// unlike responses, you can have multiple posts drafted at once
	@Override
	public String createDraftPost(String subject, String body, PostType type, PostVisibility visibility, List<String> tags, EditorType editor) {
	
		Map<String, Integer> recipientsMap = new HashMap();
		boolean individual_members = false;
		boolean entire_group = true;
		
		if (visibility == PostVisibility.PRIVATE) {
			recipientsMap.put("instr_" + this.classID, 1); // TODO: allow other individual recipients, should also have 1 as second param
			individual_members = true;
			entire_group = false;
		}

		JSONObject data = new JSONObject()
				.put("nid", this.classID);		
		
		JSONObject btn = new JSONObject()
				.put("class_live", false)
				.put("entire_group", entire_group)
				.put("class_subgroup", false)
				.put("individual_members", individual_members)
				.put("publish_later", false)
				.put("publish_now", true)
				.put("posting_options_bypass_email", false)
				.put("notify_mobile_update_input", false)
				.put("must_read", 0)
				.put("must_read_manual", true)
				.put("must_read_to_post", false)
				.put("must_read_expire", "null");
				
		if (type == PostType.QUESTION) {
			btn
			 	.put("post_type_note", false)
			 	.put("post_type_poll", false)
			 	.put("post_type_question", true);
		} else if (type == PostType.POLL) {
			btn
			 	.put("post_type_note", false)
			 	.put("post_type_poll", true)
			 	.put("post_type_question", false);
		} else {
			btn
			 	.put("post_type_note", true)
			 	.put("post_type_poll", false)
			 	.put("post_type_question", false);
		}

		JSONObject draft = new JSONObject()
			.put("content", body)
			.put("editorType", this.convertEditorType(editor))
			.put("selectedPrivateUsers", recipientsMap)
			.put("folders", tags)
			.put("btn", btn)
			.put("txt", new JSONObject()
				.put("subgroup_dropdown", "instr_" + this.classID)
				.put("post_summary", subject)
				.put("new_post_anonymity", "no")
			);
		
		data.put("draft", draft);
		
		String resp = (String) makeCallWithBackoff("network.save_draft", data);	

		return resp; // NOTE: this returns a String which looks like a post ID, but will error out if you attempt to retrieve a post with that ID, even after posting the draft using the UI
		
	}
	
	// draft an instructor answer for a given question
	@Override
	public String createDraftInstructorAnswer(String postID, String body, EditorType editor) {
		
		JSONObject data = new JSONObject()
				.put("network_id", this.classID)
				.put("cid", postID)
				.put("body", body)
				.put("type", "i_answer")
				.put("editor", this.convertEditorType(editor))
				.put("revision", 0) // Note: if an instructor answer already exists (and the revision number is not incremented to match), the API call will do nothing
				.put("anonymous", "no"); // TODO: allow other anonymyities? (not sure if possible)

		String resp = (String) makeCallWithBackoff("content.auto_save", data); // should return "OK"
		
		return postID; // TODO: not sure what to return here, currently just returning the ID of the question
		
	}
	
	// draft a followup to a given post
	@Override
	public String createDraftFollowup(String postID, String body, EditorType editor, boolean instructorOnly) {
		
		JSONObject data = new JSONObject()
				.put("network_id", this.classID)
				.put("cid", postID)
				.put("body", body)
				.put("type", "followup")
				.put("editor", this.convertEditorType(editor))
				.put("anonymous", "no"); // TODO: allow other anonymyities? (not sure if possible)

		if (instructorOnly) {
			JSONObject config = new JSONObject()
				.put("ionly", true);
			data.put("config", config);
		}
		
		String resp = (String) makeCallWithBackoff("content.auto_save", data); // should return "OK"
		
		return postID; // TODO: not sure what to return here, currently just returning the ID of the question
		
	}
	
	// updates a given Piazza post with the given parameters
	// null for new subject = use existing subject
	@Override
	public String updatePost(String postID, String newSubject, String newBody, PostType newType, PostVisibility newVisibility, List<String> newTags, EditorType editor) {
				
		PiazzaPost oldPost = (PiazzaPost) this.getPost(postID);
		
		int newRevisionNumber = oldPost.getRevisionNumber() + 1;
		String authorID = "";
		
		try {
			authorID = "," + oldPost.getAuthorID();
		} catch (AnonymousDataAccessException e) {
			e.printStackTrace();
		}

		String typeString;
		if (newType == PostType.QUESTION)
			typeString = "question";
		else if (newType == PostType.POLL)
			typeString = "poll";
		else
			typeString = "note";
		
		// TODO: support individual students
		String visibilityString;
		if (newVisibility == PostVisibility.PRIVATE)
			visibilityString = "instr_" + this.classID + authorID;
		else
			visibilityString = "all";
		
		JSONObject data = new JSONObject()
			.put("cid", postID)
			.put("type", typeString)
			.put("subject", newSubject)
			.put("content", newBody)
			.put("folders", newTags)
			.put("editor", this.convertEditorType(editor))
			.put("visibility", visibilityString)
			.put("revision", newRevisionNumber)
			.put("config", new HashMap());
					
		Map<String, Object> resp = (Map<String, Object>) makeCallWithBackoff("content.update", data);	
		
		String response = (String) resp.get("id");
		
//		System.out.println("RESP: " + resp); // if the action could not be completed, resp has a key with "bad"
		
		return response;
		
		// TODO: could change the interface to return the post object, if that makes sense with other platforms:
		// APiazzaPost createdPost = new APiazzaPost(resp, this.classID);
		// return createdPost.getPostID(); 
		
	}

	// TODO: need to finish
	@Override
	public String updateInstructorAnswer(String postID, String newBody, EditorType editor) {
		
		//PiazzaPost parentPost = (PiazzaPost) this.getPost(postID);
	
		// TODO: need to get the proper revision number somehow
		
//		JSONObject data = new JSONObject()
//				.put("cid", postID)
//				.put("content", newBody)
//				.put("type", "i_answer")
//				.put("editor", "md");
//				//.put("revision", 0); // Note: if an instructor answer already exists (and the revision number is not incremented to match), the API call will do nothing
//				//.put("anonymous", "no"); // TODO: allow other anonymyities? (not sure if possible)
//
//		Map<String, Object> resp = (Map<String, Object>) makeCallWithBackoff("content.answer", data);	
//		
//		return (String) resp.get("id");
		
		
		/* {
		  "anonymous": "no",
		  "cid": "maf3s5j94gzyr",
		  "content": "Editing instructor answer!",
		  "revision": 2,
		  "type": "i_answer",
		  "editor": "rte"
		} */

		return null;
		
	}
	
	// TODO: need to finish
	@Override
	public String updateFollowup(String responseID, String newBody, EditorType editor) {
		
		// TODO: need to test getPost() on a followup first (which may error out), since we need to be able to get the response ID at some point
		/* {
  			"cid": "maf4x51yp792d3", // this is the followup ID
			"subject": "Editing followup!",
			"editor": "rte"
		} */
		
		// currently doesn't seem like you are able to change whether an existing followup is instructor-only or not
		
		return null;
		
	}
	
	// returns true if followup is created and false if a followup with the same body already exists
	public boolean createFollowupIfDoesNotExist(String postID, String body, EditorType editor, boolean instructorOnly) {
		
		PiazzaPost parentPost = (PiazzaPost) this.getPost(postID);
		List<Map<String, Object>> children = (List<Map<String, Object>>) parentPost.getAllData().get("children");
		
		for (Map<String, Object> c : children) {
			String type = (String) c.get("type");
			if (type.equals("followup")) {
				String followupContent = (String) c.get("subject");
				if (followupContent.equals(body)) {
					return false;
				}
			}
		}
		
		this.createFollowup(postID, body, editor, instructorOnly);
		return true;
		
		// TODO: need to test
		
	}
	
	// deletes post/followup/answer with the given ID, returns whether the post was successfully deleted 
	public boolean deletePost(String postID) {
		
		JSONObject data = new JSONObject()
				.put("network_id", this.classID)
				.put("cid", postID);
		
		String resp = (String) makeCallWithBackoff("content.delete", data);	
				
		return resp.equals("OK");
	
	}



	
	/* PiazzaForum METHODS */
	
	// get user info for the provided user IDs
	@Override
	public List<ForumUser> getUsers(String[] userIDs) {
		
		JSONObject data = new JSONObject()
				.put("ids", userIDs)
				.put("nid", this.classID);
		
		List<Map<String, Object>> resp = (List<Map<String, Object>>) makeCallWithBackoff("network.get_users", data);
		
		List<ForumUser> users = new ArrayList<ForumUser>();
		for (Map<String, Object> userInfo : resp) {
			users.add(new APiazzaUser(userInfo));
		}
		
		return users;
		
	}
	
	// get post from number (e.g. @6)
	public ForumPost getPost(int postNumber) {
		
		JSONObject data = new JSONObject()
				.put("cid", postNumber)
				.put("nid", this.classID);
		
		Map<String, Object> postData = (Map<String, Object>) makeCallWithBackoff("content.get", data);
		return new APiazzaPost(postData, this.classID);
		
	}
	
	// get post headers from the feed. limit = # posts to return, offset = # of posts from the top to ignore
	public List<APiazzaPostPreview> getFeed(int limit, int offset) {
		
		JSONObject data = new JSONObject()
				.put("limit", limit)
				.put("offset", offset)
				.put("sort", "updated")
				.put("nid", this.classID);

		Map<String, Object> resp = (Map<String, Object>) makeCallWithBackoff("network.get_my_feed", data);
		List<Map<String, Object>> feed = (List<Map<String, Object>>) resp.get("feed");
		
		List<APiazzaPostPreview> previews = new ArrayList<APiazzaPostPreview>();
		for (Map<String, Object> p : feed) {
			previews.add(new APiazzaPostPreview(p)); // TODO
		}
		
		return previews;
		
	}
	
	// get all post headers from the feed
	public List<APiazzaPostPreview> getFeed() {
		
		return getFeed(999999, 0);
		
	}
	
	// create a reply to a given followup on a post
	@Override
	public String createFollowupReply(String followupID, String body, EditorType editor) {
	
		JSONObject data = new JSONObject()
				.put("network_id", this.classID)
				.put("cid", followupID)
				.put("subject", body)
				.put("type", "feedback")
				.put("content", "")
				.put("anonymous", "no"); // TODO: allow other anonymyities? (not sure if possible)

		Map<String, Object> resp = (Map<String, Object>) makeCallWithBackoff("content.create", data);	

		return (String) resp.get("id");
		
		// TODO: create reply object? this is the format of the HashMap:
		// {anon=no, folders=[], data=null, subject=Test redux followup reply, created=2025-05-08T09:07:24Z, bucket_order=3, bucket_name=Today, type=feedback, tag_good=[], uid=lljvnbpqdze3xm, children=[], tag_good_arr=[], id=maf5909e7aw5uc, updated=2025-05-08T09:07:24Z, config={}}
		
	}
	
	// get drafts saved under the current user's account
	public Map<String, Object> getDrafts() {
		
		JSONObject data = new JSONObject().
				put("limit", 0)
				.put("offset", 0)
				.put("sort", "updated")
				.put("nid", this.classID);

		Map<String, Object> resp = (Map<String, Object>) makeCallWithBackoff("network.get_my_feed", data);
		Map<String, Object> drafts = (Map<String, Object>) resp.get("drafts");
		
		// TODO: format the response further?
		return drafts;

	}

	// get post previews corresponding to a search query. use searchPosts() instead if you need full posts instead of previews
	public List<APiazzaPostPreview> queryPosts(String query) {
		
		JSONObject data = new JSONObject()
				.put("nid", this.classID)
				.put("query", query);
		
		List<Map<String, Object>> resp = (List<Map<String, Object>>) makeCallWithBackoff("network.search", data);
		
		List<APiazzaPostPreview> previews = new ArrayList<APiazzaPostPreview>();
		for (Map<String, Object> p : resp) {
			previews.add(new APiazzaPostPreview(p));
		}

		return previews;
		
	}
	
	public String createIndividualPost(String subject, String body, PostType type, String individualID, List<String> tags, EditorType editor) {
		
		String typeString;
		if (type == PostType.QUESTION)
			typeString = "question";
		else if (type == PostType.POLL)
			typeString = "poll";
		else
			typeString = "note";
		
		JSONObject data = new JSONObject()
			.put("nid", this.classID)
			.put("type", typeString)
			.put("subject", subject)
			.put("content", body)
			.put("folders", tags)
			.put("editor", this.convertEditorType(editor))
			.put("anonymous", "no"); // TODO: allow other anonymities
		
		String recipients = "instr_" + this.classID + "," + individualID; // TODO: allow other individual recipients (should be separated by ',')
		Map<String, String> config = new HashMap();
		config.put("feed_groups", recipients);
		data.put("config", config);
		data.put("status", "private");
		
		Map<String, Object> resp = (Map<String, Object>) makeCallWithBackoff("content.create", data);	
		
		return (String) resp.get("id");
		
		// TODO: could change the interface to return the post object, if that makes sense with other platforms:
		// APiazzaPost createdPost = new APiazzaPost(resp, this.classID);
		// return createdPost.getPostID();
		
	}


	
	
	/* HELPER METHODS */
	
	// TODO: should these be in PiazzaForum interface? or no because they should only be used internally
	
	// attempt the API call with exponential backoff
	protected Object makeCallWithBackoff(String method, JSONObject params, int waitTime, int maxTime, double increaseRate) throws InterruptedException, ClientProtocolException, NotLoggedInException, IOException, InvalidCallException {
		
		String piazzaAPIEndpoint = "https://piazza.com/logic/api";
		String errorString;
		
		while (waitTime <= maxTime) {
			
			// get the response from the Piazza API
			Map<String, Object> resp = this.currentSession.piazzaAPICall(method, params, piazzaAPIEndpoint);
			
			// if the response is null, this API call won't work so we raise an exception
			if (resp == null) {
				errorString = "Null response for call to " + method + " with parameters " + params.toString();
				throw new InvalidCallException(errorString);
			}
			
			if (resp.get("error") != null) {
				
				String errorMessage = (String) resp.get("error");
				
				// if error is not caused by rate limit, raise an exception and don't try again
				if (!(errorMessage.equals("Sorry, too fast -- please wait a second and try again.")
						|| errorMessage.equals("Sorry! It looks like you are posting too quickly -- please wait a few seconds and try again."))) {
					errorString = "Error in response for call to " + method + " with parameters " + params.toString() + ". Error message: " + errorMessage;
					throw new InvalidCallException(errorString);
				}
				
				// otherwise, wait for the allotted time and try again
//				System.out.println("Waiting for" + waitTime + "ms");
				Thread.sleep(waitTime);
				waitTime = (int) (waitTime * increaseRate);
				
			}

			else {
				// if no errors in the response, return the result
//				System.out.println("RESULT: " + resp.get("result"));
				
				return resp.get("result");
			}
		}
		
		// if no valid response is returned before the maxTime backoff has been reached, raise an exception
		errorString = "Maximum backoff time of " + maxTime + "ms has been reached without receiving a valid response.";
		throw new InvalidCallException(errorString);
	
	}
	
	// overloaded version with default backoff values
	protected Object makeCallWithBackoff(String method, JSONObject params) {
		
		int waitTime = 500;
		int maxTime = 128000;
		double increaseRate = 2.0;
		
		try {
			return makeCallWithBackoff(method, params, waitTime, maxTime, increaseRate);
		} catch (InterruptedException | NotLoggedInException | IOException | InvalidCallException e) {
			e.printStackTrace();
			System.out.println("Piazza API call failed (see above exception), returning null response");
			return null;
		}
	}
	
	// convert DiscussionForum.EditorType enum into Piazza-compatible string
	protected String convertEditorType(EditorType editor) {
		
		String editorString;
		if (editor == EditorType.PLAIN_TEXT)
			editorString = "plain";
		else if (editor == EditorType.RICH_TEXT)
			editorString = "rte";
		else
			editorString = "md";
		
		return editorString;
		
	}

}



/* NOTES */

/* instructor-only followups:
{
	  "method": "content.create",
	  "params": {
	    "nid": "mcic7dhsju035c",
	    "cid": "mcz8abde7oe2ql",
	    "type": "followup",
	    "subject": "Instructor only followup test!",
	    "content": "",
	    "anonymous": "no",
	    "editor": "rte",
	    "config": {
	      "ionly": true
	    }
	  }
	}
 */

/* can filter feed based on various properties using the gear in the ui:
{
	"method": "network.filter_feed",
	"params": {
			"nid": "m522b50mg435bd",
			"instructors": 1 // filtering based on instructor posts
		}
} */

/* separate from network.search, there's a network.find_similar:
{
	"query": "Hint for interpolated methods <p>I am trying to refactor my interpolation methods so that I don&#39;t get the nestedIfDepth complaints from the checkstyle, any tips? Everything I have tried/can think of still involves too many if statements. Thanks!</p>\n<p></p>\n<p>THIS HAS BEEN UPDATED</p>",
	"old_query": null,
	"nid": "m0mymncloco2ty",
	"to_nr": 69
} */

/* other known functionalities (some in comments in APiazzaClass):
 	get online users
 	mark followup as resolved
 	mark post as duplicate
 	create a student answer (see how this works, not sure if we can do so from an instructor account)
 	draft a followup reply (should be straightforward)
 */
