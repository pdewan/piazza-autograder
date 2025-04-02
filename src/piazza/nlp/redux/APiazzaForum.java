package piazza.nlp.redux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import piazza.ANewPiazzaSession;
import piazza.InvalidCallException;
import piazza.LoginFailedException;
import piazza.NotLoggedInException;
import piazza.PiazzaSession;

public class APiazzaForum implements PiazzaForum { // MixedInitiativeDiscussionForum

	protected String classID;
	protected PiazzaSession currentSession;
	
	// create forum object using a new Piazza API session for the given email/password
	public APiazzaForum(String classID, String email, String password) {
		
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
	public APiazzaForum(String classID, PiazzaSession initialSession) {
	
		this.classID = classID;
		this.currentSession = initialSession; 
				
	}
	
	
	/* DiscussionForum METHODS */
	
	// get post from ID (e.g. m6ie32r77ki5y0)
	@Override
	public Post getPost(String postID) {
		
		JSONObject data = new JSONObject()
				.put("cid", postID);
		
		Map<String, Object> postData = (Map<String, Object>) makeCallWithBackoff("content.get", data);
		return new APiazzaPost(postData);
		
	}
	
	// get list of all posts in the class
	// NOTE: makes a call to getPost() for each one; use getFeed() instead if post previews are sufficient
	@Override
	public List<Post> getAllPosts() {
		
		// TODO: add filtering on number, date, and tag like in getAllPostsRecursive()
		// TODO: add progress bar printing
		
		List<APiazzaPostPreview> feed = getFeed();
		
		List<Post> posts = new ArrayList<Post>();
		for (APiazzaPostPreview p : feed) {
			posts.add(getPost((String) p.getFullData().get("id")));
		}
		
		return posts;
		
	}
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
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
	public List<Post> searchPosts(String query) {
		
		List<APiazzaPostPreview> queryResults = queryPosts(query);
		
		List<Post> posts = new ArrayList<Post>();
		for (APiazzaPostPreview p : queryResults) {
			posts.add(getPost((String) p.getFullData().get("id")));
		}
		
		return posts;
		
	}
	
	

	
	
	
	
	
	
	
	
	
	/* ADDITIONAL METHODS */
	
	// get post from number (e.g. @6)
	public Post getPost(int postNumber) {
		
		JSONObject data = new JSONObject()
				.put("cid", postNumber)
				.put("nid", this.classID);
		
		Map<String, Object> postData = (Map<String, Object>) makeCallWithBackoff("content.get", data);
		return new APiazzaPost(postData);
		
	}
	
	// get post headers from the feed. limit = # posts to return, offset = # of posts from the top to ignore
	public List<APiazzaPostPreview> getFeed(int limit, int offset) {
		
		JSONObject data = new JSONObject().
				put("limit", limit)
				.put("offset", offset)
				.put("sort", "updated")
				.put("nid", this.classID);

		Map<String, Object> resp = (Map<String, Object>) makeCallWithBackoff("network.get_my_feed", data);
		List<Map<String, Object>> feed = (List<Map<String, Object>>) resp.get("feed");
		
		System.out.println(feed);
		
		/* stuff for log
		 * nr=281,
		 * id=m72prlqjhxgj5,
		 * version=1
		 * */
		
		List<APiazzaPostPreview> previews = new ArrayList<APiazzaPostPreview>();
		for (Map<String, Object> p : feed) {
			previews.add(new APiazzaPostPreview(p));
		}
		
		return previews;
		
	}
	
	public List<APiazzaPostPreview> getFeed() {
		
		return getFeed(999999, 0);
		
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/* HELPER METHODS */
	
	// attempt the API call with exponential backoff
	protected Object makeCallWithBackoff(String method, JSONObject params, int waitTime, int maxTime, double increaseRate) throws InterruptedException, ClientProtocolException, NotLoggedInException, IOException, InvalidCallException {
		
		String piazzaAPIEndpoint = "https://piazza.com/logic/api";
		String errorString;
		
		while (waitTime < maxTime) {
			
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
				if (!errorMessage.equals("test error")) { // TODO:
					errorString = "Error in response for call to " + method + " with parameters " + params.toString() + ". Error message: " + resp.get("error");
					throw new InvalidCallException(errorString);
				}
				
				// otherwise, wait for the allotted time and try again
				Thread.sleep(waitTime);
				waitTime = (int) (waitTime * increaseRate);
				
			}

			// if no errors in the response, return the result
			return resp.get("result");
			
		}
		
		// if no valid response is returned before the maxTime backoff has been reached, raise an exception
		errorString = "Maximum backoff time of " + maxTime + "ms has been reached without receiving a valid response.";
		throw new InvalidCallException(errorString);
	
	}
	
	// overloaded version with default backoff values
	protected Object makeCallWithBackoff(String method, JSONObject params) {
		
		int waitTime = 500;
		int maxTime = 60000;
		double increaseRate = 2.0;
		
		try {
			
			return makeCallWithBackoff(method, params, waitTime, maxTime, increaseRate);
			
		} catch (InterruptedException | NotLoggedInException | IOException | InvalidCallException e) {
			
			e.printStackTrace();
			System.out.println("Piazza API call failed (see above exception), returning null response");
			return null;
			
		}
		
	}

		
		
		
		
		


	
	
	
	
	
	
	
	
	
	
	



	
	
	


	@Override
	public String createPost(JSONObject content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createInstructorAnswer(String postID, JSONObject content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createFollowup(String postID, JSONObject content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createDraftPost(JSONObject content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createDraftInstructorAnswer(String postID, JSONObject content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createDraftFollowup(String postID, JSONObject content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String updatePost(String postID, JSONObject content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String updateResponse(String responseID, JSONObject content) {
		// TODO Auto-generated method stub
		return null;
	}



}
