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
		BufferedWriter br = new BufferedWriter(new FileWriter("/Users/Yifan/desktop/401.txt"));
		for(Map<String,Object> item: feed) {
			br.write(item.get("id").toString());
			br.write("\n\n\n");
		}
		br.close();
		return feed;
	}

	// get users provided user id
	public Map<String, Object> getUser(String uid) throws ClientProtocolException, NotLoggedInException, IOException {
		JSONObject data = new JSONObject().put("ids", new String[] { uid }).put("nid", this.cid);
		Map<String, Object> resp = this.mySession.piazzaAPICall("network.get_users", data, piazzaLogic);
		// System.out.println(resp.toString());
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
		for (Map<String, Object> item : feed) {
			String id = (String) item.get("id");
			if(id.equals("jlo17ck1vlu6pv")) {
				System.out.println("stop");
			}
			posts.add(this.getPost((String) item.get("id")));
		}
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
		String date = LocalDate.now().toString();
		String post1 = "<p>This is another auto post test<br>Your diary grade up to " + date +" is:  " + 100 + "<br>" + "If you have any questions on your grading, please talk to TAs/Dewan</p>";
		JSONObject data = new JSONObject().put("cid", "jnudo16g3qe5d0").put("subject", post1).put("nid", "jkws0l0gvcr7it")
				.put("type", "followup").put("content", "").put("anonymous", "no");
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.create", data, piazzaLogic);
		System.out.println(resp.toString());
		return resp != null? true:false;
	}


	public boolean createPost(String postToReply, String content, boolean anonymous) {
		// TODO fill-in
		return true;
	}

	private Object getResults(Map<String, Object> resp) {
		if (resp.get("error") != null) {
			return null;
		}
		return resp.get("result");
	}
}
