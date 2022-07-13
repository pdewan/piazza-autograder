package piazza.nlp;

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

import piazza.APiazzaClass;
import piazza.APiazzaSession;
import piazza.LoginFailedException;
import piazza.NotLoggedInException;
import piazza.PiazzaClass;
import piazza.PiazzaSession;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class APiazzaClassRecursivePosts extends APiazzaClass implements PiazzaClass {

//	final String piazzaLogic = "https://piazza.com/logic/api";
//
//	protected PiazzaSession mySession = null;
//	protected String cid;
//	private Map<String, String> map = new HashMap<>(); // key: cid value: uid
	private String incompletePostsFile;

	public APiazzaClassRecursivePosts(String email, String password, String classID, String anIncompletePostsFile)
			throws ClientProtocolException, IOException, LoginFailedException {
		super(email, password, classID);
//		this.mySession = new APiazzaSession();
//		this.mySession.login(email, password);
//		this.cid = classID;
		incompletePostsFile = anIncompletePostsFile;
	}

	public APiazzaClassRecursivePosts(String email, String password, String classID)
			throws ClientProtocolException, IOException, LoginFailedException {
		this(email, password, classID, "incompletePostsFile");
	}

	public void setIncompletePostsFile(String anIncompletePostsFile) {
		incompletePostsFile = anIncompletePostsFile;

	}

//	// get feed from
//	public List<Map<String, Object>> getFeed(int limit, int offset)
//			throws ClientProtocolException, NotLoggedInException, IOException {
//		JSONObject data = new JSONObject().put("limit", limit).put("offset", offset).put("sort", "updated").put("nid",
//				this.cid);
//		//// System.out.println(data.toString());
//		Map<String, Object> resp = this.mySession.piazzaAPICall("network.get_my_feed", data, piazzaLogic);
//		@SuppressWarnings("unchecked")
//		List<Map<String, Object>> feed = (List<Map<String, Object>>) ((Map<String, Object>) this.getResults(resp))
//				.get("feed");
////		BufferedWriter br = new BufferedWriter(new FileWriter("/Users/jedidiah/desktop/401.txt"));
////		for(Map<String,Object> item: feed) {
////			br.write(item.get("id").toString());
////			br.write("\n\n\n");
////		}
////		br.close();
//		return feed;
//	}

//	// get users provided user id
//	public Map<String, Object> getUser(String uid) throws ClientProtocolException, NotLoggedInException, IOException {
//		JSONObject data = new JSONObject().put("ids", new String[] { uid }).put("nid", this.cid);
//		Map<String, Object> resp = this.mySession.piazzaAPICall("network.get_users", data, piazzaLogic);
////		//System.out.println("UserId: " + resp.toString());
//		if (((List<Map<String, Object>>) this.getResults(resp)).size() == 0)
//			return null;
//		@SuppressWarnings("unchecked")
//		Map<String, Object> user = ((List<Map<String, Object>>) this.getResults(resp)).get(0);
//		return user;
//	}

//	// get post provided content id
//	public Map<String, Object> getPost(String cid) throws ClientProtocolException, NotLoggedInException, IOException {
//		JSONObject data = new JSONObject().put("cid", cid);
//		Map<String, Object> resp = this.mySession.piazzaAPICall("content.get", data, piazzaLogic);
//		@SuppressWarnings("unchecked")
//		Map<String, Object> post = (Map<String, Object>) this.getResults(resp);
//
//		// System.out.println(post);
//		@SuppressWarnings("unchecked")
//		Map<String, String> change_log = ((List<Map<String, String>>) post.get("change_log")).get(0);
//		if (change_log.get("type").equals("create"))
//			map.put(cid, change_log.get("uid"));
//
//		return post;
//	}

//	public Map<String, String> getMap() {
//		return map;
//	}

	public Map<String, Object> toPostInfo(Map<String, Object> item, boolean isChild) {
		HashMap<String, Object> postInfo = new HashMap();
		try {
			for (String aKey:item.keySet()) {
				postInfo.put(aKey, item.get(aKey));
			}
			String id = (String) item.get("id");
//			postInfo.put("id", id);
//			postInfo.put("type", item.get("type"));
			String uid = null;
			String content = null;
			List<Map<String, Object>> children = null;

			if (!isChild) {
				Map<String, Object> post = getPost(id);

				List<Map<String, Object>> historyList = (List<Map<String, Object>>) post.get("history");
				Map<String, Object> latestElement = historyList.get(0);
				content = (String) latestElement.get("content");
//				postInfo.put("subject", item.get("subject"));
				List<Map<String, Object>> uidList = (List<Map<String, Object>>) post.get("change_log");
				Map<String, Object> uidMap = (Map<String, Object>) uidList.get(0);
				uid = (String) uidMap.get("uid");
				children = (List<Map<String, Object>>) post.get("children");
			} else {
				content = (String) item.get("subject");
				children = (List<Map<String, Object>>) item.get("children");
				uid = (String) item.get("uid");

			}
			postInfo.put("content", content);

//			postInfo.put("subject", latestElement.get("subject"));
//			postInfo.put("updated", item.get("updated"));
//			postInfo.put("uid", uid);
			String anEmail = getUserEmail(uid);
			String aUserName = getUserName(uid);
			if (aUserName.equals("Piazza Team") && anEmail == null) {
				return null;
			}
			if (anEmail == null) {
				anEmail = "noreply@piazza.com";
			}
			postInfo.put("userName", aUserName);
			postInfo.put("email", anEmail);
//			postInfo.put("tags", item.get("tags"));
//			postInfo.put("timeCreated", post.get("modified"));
//			postInfo.put("no_answer_followup", item.get("no_answer_followup"));
			if (children != null && !children.isEmpty()) {
				List<Map<String, Object>> aChildren = new ArrayList();
				for (Map<String, Object> child : children) {
					child.put("parentSubject", item.get("subject"));
					Map<String, Object> aChildPost = toPostInfo(child, true);
					aChildren.add(aChildPost);
				}
				postInfo.put("children", aChildren);
			}
			Thread.sleep(3000);

			// System.out.println("Post content: " + this.getPost(id));
		} catch (Exception e) {
			e.printStackTrace();

		}
		return postInfo;

	}
	public void writeAllPosts() throws ClientProtocolException, NotLoggedInException, IOException {
		JSONObject allPosts = getAllPostsRecursive();
		try {
//			FileWriter fileWriter = new FileWriter("C:\\Users\\gubow\\COMP 691H\\Find Incomplete Post\\COMP 301 Summer 2021.json");
			FileWriter fileWriter = new FileWriter(incompletePostsFile);

//			fileWriter.write(result.toJSONString());
			String aString = allPosts.toString();
			fileWriter.write(aString);

			fileWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("JSON file created successfully");
	}

//	public List<Map<String, Object>> getAllPosts() throws ClientProtocolException, NotLoggedInException, IOException {
//		List<Map<String, Object>> feed = this.getFeed(999999, 0);
//		List<Map<String, Object>> posts = new ArrayList<Map<String, Object>>();
//
//		// The following helps to display a visual of the percentage of completion due
//		// to having to wait with Thread.sleep
//		double maxSize = feed.size();
//		int numItemsProcessed = 0;
////		org.json.simple.JSONObject result = new org.json.simple.JSONObject();
//		JSONObject result = new JSONObject();
//
//		// Time how long it takes to access the feed given that we are waiting one
//		// second between each request
//		long startTime = System.currentTimeMillis();
//
//		for (Map<String, Object> item : feed) {
////			String id = (String) item.get("id");
//			Map<String, Object> postInfo = toPostInfo(item, false);
//			if (postInfo == null) {
//				continue;
//			}
//			result.put("Post" + numItemsProcessed, postInfo);
//			numItemsProcessed++;
//			double percentage = numItemsProcessed / maxSize;
//			System.out.println(Math.round(percentage * 100) + "% complete");
//		}
//
//		long endTime = System.currentTimeMillis();
//		System.out.println("Accessing feed took " + (endTime - startTime) / 1000 + " seconds");
//
////		try {
//////			FileWriter fileWriter = new FileWriter("C:\\Users\\gubow\\COMP 691H\\Find Incomplete Post\\COMP 301 Summer 2021.json");
////			FileWriter fileWriter = new FileWriter(incompletePostsFile);
////
//////			fileWriter.write(result.toJSONString());
////			String aString = result.toString();
////			fileWriter.write(aString);
////
////			fileWriter.flush();
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
////		System.out.println("JSON file created successfully");
//
//		return result;
//	}
	public JSONObject getAllPostsRecursive() throws ClientProtocolException, NotLoggedInException, IOException {
		List<Map<String, Object>> feed = this.getFeed(999999, 0);
		List<Map<String, Object>> posts = new ArrayList<Map<String, Object>>();

		// The following helps to display a visual of the percentage of completion due
		// to having to wait with Thread.sleep
		double maxSize = feed.size();
		int numItemsProcessed = 0;
//		org.json.simple.JSONObject result = new org.json.simple.JSONObject();
		JSONObject result = new JSONObject();

		// Time how long it takes to access the feed given that we are waiting one
		// second between each request
		long startTime = System.currentTimeMillis();

		for (Map<String, Object> item : feed) {
//			String id = (String) item.get("id");
			Map<String, Object> postInfo = toPostInfo(item, false);
			if (postInfo == null) {
				continue;
			}
			result.put("Post" + numItemsProcessed, postInfo);
			numItemsProcessed++;
			double percentage = numItemsProcessed / maxSize;
			System.out.println(Math.round(percentage * 100) + "% complete");
		}

		long endTime = System.currentTimeMillis();
		System.out.println("Accessing feed took " + (endTime - startTime) / 1000 + " seconds");

//		try {
////			FileWriter fileWriter = new FileWriter("C:\\Users\\gubow\\COMP 691H\\Find Incomplete Post\\COMP 301 Summer 2021.json");
//			FileWriter fileWriter = new FileWriter(incompletePostsFile);
//
////			fileWriter.write(result.toJSONString());
//			String aString = result.toString();
//			fileWriter.write(aString);
//
//			fileWriter.flush();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println("JSON file created successfully");

		return result;
	}

//	// get author id of the post
//	public String getAuthorId(Map<String, Object> post) {
//		@SuppressWarnings("unchecked")
//		List<Map<String, Object>> hist = (List<Map<String, Object>>) post.get("change_log");
//		String authorId = "";
//		for (Map<String, Object> update : hist) {
//			if (update.get("type").equals("create")) {
//				authorId = (String) update.get("uid");
//				break;
//			}
//		}
//		return authorId;
//	}
//
//	public String getUserName(String uid) throws ClientProtocolException, NotLoggedInException, IOException {
//		return (String) this.getUser(uid).get("name");
//	}

//	public String getUserEmail(String uid) throws ClientProtocolException, NotLoggedInException, IOException {
//		return (String) this.getUser(uid).get("email");
//	}
//
//	public boolean createFollowup(String cid, String post)
//			throws ClientProtocolException, NotLoggedInException, IOException {
//		JSONObject data = new JSONObject().put("cid", cid).put("subject", post).put("type", "followup")
//				.put("content", "").put("anonymous", "no");
//		Map<String, Object> resp = this.mySession.piazzaAPICall("content.create", data, piazzaLogic);
////		//System.out.println(resp.toString());
//		return resp != null ? true : false;
//	}
//
//	public boolean createPost(String postToReply, String content, boolean anonymous) {
//		// TODO fill-in
//		return true;
//	}
//
//	private Object getResults(Map<String, Object> resp) {
//		if (resp.get("error") != null) {
//			// System.out.println("Error in resp: " + resp.get("error"));
//			return null;
//		}
//		return resp.get("result");
//	}
}
