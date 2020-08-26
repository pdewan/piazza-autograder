 package piazza;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

public class APiazzaClass implements PiazzaClass {

	final String piazzaLogic = "https://piazza.com/logic/api";

	protected PiazzaSession mySession = null;
	protected String cid;
	private Map<String, String> map = new HashMap<>();    // key: cid   value: uid

	public APiazzaClass(String email, String password, String classID)
			throws ClientProtocolException, IOException, LoginFailedException {
		this.mySession = new APiazzaSession();
		this.mySession.login(email, password);
		this.cid = classID;
	}

	
	// get feed from 
	public List<Map<String, Object>> getFeed(int limit, int offset)
			throws ClientProtocolException, NotLoggedInException, IOException {
		JSONObject data = new JSONObject().put("limit", limit).put("offset", offset).put("sort", "updated").put("nid",
				this.cid);
		System.out.println(data.toString());
		Map<String, Object> resp = this.mySession.piazzaAPICall("network.get_my_feed", data, piazzaLogic);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> feed = (List<Map<String, Object>>) ((Map<String, Object>) this.getResults(resp))
				.get("feed");
//		BufferedWriter br = new BufferedWriter(new FileWriter("/Users/jedidiah/desktop/401.txt"));
//		for(Map<String,Object> item: feed) {
//			br.write(item.get("id").toString());
//			br.write("\n\n\n");
//		}
//		br.close();
		return feed;
	}

	// get users provided user id
	public Map<String, Object> getUser(String uid) throws ClientProtocolException, NotLoggedInException, IOException {
		JSONObject data = new JSONObject().put("ids", new String[] { uid }).put("nid", this.cid);
		Map<String, Object> resp = this.mySession.piazzaAPICall("network.get_users", data, piazzaLogic);
//		System.out.println("UserId: " + resp.toString());
		if (((List<Map<String, Object>>) this.getResults(resp)).size() == 0) return null;
		@SuppressWarnings("unchecked")
		Map<String, Object> user = ((List<Map<String, Object>>) this.getResults(resp)).get(0);
		return user;
	}
	
	
    // get post provided content id
	public Map<String, Object> getPost(String cid) throws ClientProtocolException, NotLoggedInException, IOException {
		JSONObject data = new JSONObject().put("cid", cid);
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.get", data, piazzaLogic);
		@SuppressWarnings("unchecked")
		Map<String, Object> post = (Map<String, Object>) this.getResults(resp);
		
		//System.out.println(post);
		@SuppressWarnings("unchecked")
		Map<String, String> change_log = ((List<Map<String, String>>) post.get("change_log")).get(0);
		if(change_log.get("type").equals("create")) map.put(cid, change_log.get("uid"));
		
		return post;
	}
	
	public Map<String, String> getMap(){
		return map;
	}
	
	public List<Map<String, Object>> getAllPosts() throws ClientProtocolException, NotLoggedInException, IOException {
		List<Map<String, Object>> feed = this.getFeed(999999, 0);
		List<Map<String, Object>> posts = new ArrayList<Map<String, Object>>();
		
		// The following helps to display a visual of the percentage of completion due to having to wait with Thread.sleep
		double maxSize = feed.size();
		double numItemsProcessed = 0;
		
		// Time how long it takes to access the feed given that we are waiting one second between each request
		long startTime = System.currentTimeMillis();
		
		for (Map<String, Object> item : feed) {
			numItemsProcessed++;
			double percentage = numItemsProcessed / maxSize;
			System.out.println(Math.round(percentage * 100) + "% complete");
			
			String id = (String) item.get("id");
//			System.out.println("id: " + id);
			posts.add(this.getPost(id));
			
			// Sleep as to not overwhelm the Piazza backend with requests and get thrown errors for it
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("Accessing feed took " + (endTime - startTime) / 1000 + " seconds");
		
		return posts;
	}
	
	// get author id of the post
	public String getAuthorId(Map<String, Object> post) { 
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> hist = (List<Map<String, Object>>) post.get("change_log");
		String authorId = "";
		for (Map<String, Object> update : hist) {
			if (update.get("type").equals("create")) {
				authorId = (String) update.get("uid");
				break;
			}
		}
		return authorId;
	}


	public String getUserName(String uid) throws ClientProtocolException, NotLoggedInException, IOException {
		return (String) this.getUser(uid).get("name");
	}

	public String getUserEmail(String uid) throws ClientProtocolException, NotLoggedInException, IOException {
		return (String) this.getUser(uid).get("email");
	}

	public boolean createFollowup(String cid, String post) throws ClientProtocolException, NotLoggedInException, IOException {
		JSONObject data = new JSONObject().put("cid", cid).put("subject", post)
				.put("type", "followup").put("content", "").put("anonymous", "no");
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.create", data, piazzaLogic);
//		System.out.println(resp.toString());
		return resp != null? true:false;
	}


	public boolean createPost(String postToReply, String content, boolean anonymous) {
		// TODO fill-in
		return true;
	}

	private Object getResults(Map<String, Object> resp) {
		if (resp.get("error") != null) {
			System.out.println("Error in resp: " + resp.get("error"));
			return null;
		}
		return resp.get("result");
	}
}
