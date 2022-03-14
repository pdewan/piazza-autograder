 package piazza.bowen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import piazza.PiazzaClass;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class APiazzaClassBowen implements PiazzaClass {

	final String piazzaLogic = "https://piazza.com/logic/api";

	protected PiazzaSession mySession = null;
	protected String cid;
	private Map<String, String> map = new HashMap<>();    // key: cid   value: uid

	public APiazzaClassBowen(String email, String password, String classID)
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
		////System.out.println(data.toString());
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
//		//System.out.println("UserId: " + resp.toString());
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
		int numItemsProcessed = 0;
		org.json.simple.JSONObject result = new org.json.simple.JSONObject();
		// Time how long it takes to access the feed given that we are waiting one second between each request
		long startTime = System.currentTimeMillis();
		
		for (Map<String, Object> item : feed) {
			numItemsProcessed++;		
			HashMap<String, Object> postInfo = new HashMap();
			postInfo.put("id", item.get("id"));
			postInfo.put("type", item.get("type"));
			postInfo.put("title", item.get("subject"));
			double percentage = numItemsProcessed / maxSize;
			System.out.println(Math.round(percentage * 100) + "% complete");
			String id = (String) item.get("id");
			//result.put("id", item.get("id"));
			//result.put("type", item.get("type"));
			//result.put("title", item.get("subject"));
			ArrayList<Map<String, Object>> contentArray = (ArrayList<Map<String, Object>>)this.getPost(id).get("history");
			Map<String, Object> contentMap = (Map<String, Object>) contentArray.toArray()[0];
			String content = (String) contentMap.get("content");
			ArrayList<Map<String, Object>> uidArray = (ArrayList<Map<String, Object>>)item.get("log");
			Map<String, Object> uidMap = (Map<String, Object>) uidArray.toArray()[0];
			String uid = (String) uidMap.get("u");
			//content = content.replaceAll("\n", "");
			//content = content.replaceAll("<p>", "");
			//ontent = content.replaceAll("<\\/li>", "");
			//content = content.replaceAll("<\\/strong>", "");
			//content = content.replaceAll("<li style=\\\"margin:0;padding:0\\\">", "");
			//content = content.replaceAll("<\\/ol>", "");
			//content = content.replaceAll("<\\/md>", "");
			//content = content.replaceAll("#pin", "");
			//content = content.replaceAll("<" + StringUtils.substringBetween(content, "<", ">") + ">", "");
			//content = content.replaceAll("<" + StringUtils.substringBetween(content, "<img", ">") + ">", "");
			//content = content.replaceAll("<\\/p>", "");
			postInfo.put("cleanedContent", content);
			postInfo.put("uid", uid);
			postInfo.put("tags", item.get("tags"));
			postInfo.put("timeCreated", item.get("modified"));
			postInfo.put("totalFollowUpPosts", item.get("no_answer_followup"));
			//result.put("cleanedContent", content);
			//result.put("tags", item.get("tags"));
			//result.put("timeCreated", item.get("modified"));
			//result.put("totalFollowUpPosts", item.get("no_answer_followup"));
			//System.out.println("id: " + id);
			result.put("Post" + numItemsProcessed, postInfo);
			posts.add(this.getPost(id));
			//System.out.println("Post content: " + this.getPost(id));
			
			
			// Sleep as to not overwhelm the Piazza backend with requests and get thrown errors for it
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("Accessing feed took " + (endTime - startTime) / 1000 + " seconds");
		
		
		try {
			FileWriter fileWriter = new FileWriter("C:\\Users\\gubow\\COMP 691H\\Find Incomplete Post\\COMP 301 Summer 2021.json");
			fileWriter.write(result.toJSONString());
			fileWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("JSON file created successfully");
		
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
//		//System.out.println(resp.toString());
		return resp != null? true:false;
	}


	public boolean createPost(String postToReply, String content, boolean anonymous) {
		// TODO fill-in
		return true;
	}

	private Object getResults(Map<String, Object> resp) {
		if (resp.get("error") != null) {
			//System.out.println("Error in resp: " + resp.get("error"));
			return null;
		}
		return resp.get("result");
	}
}
