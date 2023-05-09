 package piazza;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

public class APiazzaClass implements PiazzaClass {

	final String piazzaLogic = "https://piazza.com/logic/api";
	final String piazzaMain = "https://piazza.com/logic/api";


	protected PiazzaSession mySession = null;
	protected String cid;
	private Map<String, String> map = new HashMap<>();    // key: cid   value: uid

	public APiazzaClass(String email, String password, String classID)
			throws ClientProtocolException, IOException, LoginFailedException {
		this.mySession = new APiazzaSession();
		this.mySession.login(email, password);
		this.cid = classID;
	}
	static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssz");

	public static Date getDate(String aPiazzaTime) {
		if (aPiazzaTime == null) {
			return null;
		}
		try {
			return df.parse(aPiazzaTime.replace('T', ' ').replace("Z", "UTC"));
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
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
	
	Map<String, Object> emptyMap = new HashMap<>();
	

	// get users provided user id
	public Map<String, Object> getUser(String uid) throws ClientProtocolException, NotLoggedInException, IOException {
//		System.out.println("uid" + uid);
		if (uid == null) {
			System.out.println("null uid");
			return emptyMap;
		}
		JSONObject data = new JSONObject().put("ids", new String[] { uid }).put("nid", this.cid);
		Map<String, Object> resp = this.mySession.piazzaAPICall("network.get_users", data, piazzaLogic);
		if (resp == null) {
			System.out.println("null get_users for uid " + uid);
			return emptyMap;
		}
//		System.out.println("UserId: " + resp.toString());
		if (((List<Map<String, Object>>) this.getResults(resp)).size() == 0) return null;
		@SuppressWarnings("unchecked")
		Map<String, Object> user = ((List<Map<String, Object>>) this.getResults(resp)).get(0);
//		System.out.println("user" + user);

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
		if (uid == null) return null;
		
		return (String) this.getUser(uid).get("name");
	}

	public String getUserEmail(String uid) throws ClientProtocolException, NotLoggedInException, IOException {
		if (uid == null) return null;
		return (String) this.getUser(uid).get("email");
	}

	public boolean createFollowup(String cid, String post) throws ClientProtocolException, NotLoggedInException, IOException {
		JSONObject data = new JSONObject().put("cid", cid).put("subject", post)
				.put("type", "followup").put("content", "").put("anonymous", "no");
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.create", data, piazzaLogic);
//		System.out.println(resp.toString());
		return resp != null? true:false;
	}
	
//	public boolean updatePost(Map<String, Object> postInfo) throws ClientProtocolException, NotLoggedInException, IOException {
//		JSONObject data = new JSONObject(postInfo);
//		Map<String, Object> resp = this.mySession.piazzaAPICall("content.update", data, piazzaLogic);
////		System.out.println(resp.toString());
//		return resp != null? true:false;
//	}
	public boolean createReply(String cid, String post) throws ClientProtocolException, NotLoggedInException, IOException {
		JSONObject data = new JSONObject().put("cid", cid).put("subject", post)
				.put("type", "feedback").put("content", "").put("anonymous", "no");
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.create", data, piazzaLogic);
//		System.out.println(resp.toString());
		return resp != null? true:false;
	}
	public boolean createInstructorAnswer(String cid, String post) throws ClientProtocolException, NotLoggedInException, IOException {
		JSONObject data = new JSONObject().put("cid", cid).put("subject", post)
				.put("type", "feedback").put("content", "").put("anonymous", "no");
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.create", data, piazzaLogic);
//		System.out.println(resp.toString());
		return resp != null? true:false;
	}
	public String toCSVString(Object[] anArray) {
		String aString = Arrays.toString(anArray);
		return aString.replace("[","").replace("]", "");
	}
	public boolean createPost(String aSubject, String aContent, List<String> aTags, String aRecipients ) throws ClientProtocolException, NotLoggedInException, IOException {
		JSONObject data = new JSONObject().
				put("nid", this.cid).
				put("subject", aSubject).
				put("type", "note").
				put("content", aContent).
//				put("anonymous", "no").
//				put("feed_groups", aRecipients).
				put("folders", aTags);
//				put("tags", aTags);
//				put("status", "private");
				;
		if (aRecipients != null) {
			Map<String, String> aMap = new HashMap();
			aMap.put ("feed_groups", aRecipients);
			data.put("config", aMap);
		}
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.create", data, piazzaLogic);
//		System.out.println(resp.toString());
		return resp != null? true:false;
	}
	public boolean markPostAsDuplicate(String newPostID, String duplicatedID) throws ClientProtocolException, NotLoggedInException, IOException {
		JSONObject data = new JSONObject().
				put("nid", this.cid)
//				.
//				put("cid", duplicatedID)
//				.
//				put("duplicate", newPostID)
				;
		Map<String, Object> resp1 = this.mySession.piazzaAPICall("content.get", data, piazzaLogic);

		Map<String, Object> resp = this.mySession.piazzaAPICall("content.duplicate", data, piazzaLogic);

//		System.out.println(resp.toString());
		return resp != null? true:false;
	}
	public boolean updatePost(String postID, String newSubject, String newContent, int revision) throws ClientProtocolException, NotLoggedInException, IOException {
		JSONObject data = new JSONObject().
				put("nid", this.cid).
				put("cid", postID).
				put("subject", newSubject).
				put("content",  newContent).
				put("revision", revision);
				;
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.update", data, piazzaLogic);
//		System.out.println(resp.toString());
		return resp != null? true:false;
	}
	public boolean updateFollowup(String postID, String newContent) throws ClientProtocolException, NotLoggedInException, IOException {
		JSONObject data = new JSONObject().
				put("nid", this.cid).
				put("cid", postID).
				put("subject", newContent);
				;
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.update", data, piazzaLogic);
//		System.out.println(resp.toString());
		return resp != null? true:false;
	}
//	public boolean createFollowp(String aSubject, String aContent, List<String> aTags, String aRecipients ) throws ClientProtocolException, NotLoggedInException, IOException {
//		JSONObject data = new JSONObject().
//				put("nid", this.cid).
//				put("subject", aSubject).
//				put("type", "note").
//				put("content", aContent).
////				put("anonymous", "no").
////				put("feed_groups", aRecipients).
//				put("folders", aTags);
////				put("tags", aTags);
////				put("status", "private");
//				;
//		if (aRecipients != null) {
//			Map<String, String> aMap = new HashMap();
//			aMap.put ("feed_groups", aRecipients);
//			data.put("config", aMap);
//		}
//		Map<String, Object> resp = this.mySession.piazzaAPICall("content.create", data, piazzaLogic);
////		System.out.println(resp.toString());
//		return resp != null? true:false;
//	}

//	public boolean createPost(String postToReply, String content, boolean anonymous) {
//		// TODO fill-in
//		return true;
//	}

	private Object getResults(Map<String, Object> resp) {
		if (resp == null) {
			System.out.println("Null response");
			return null;
		}
		if (resp.get("error") != null) {
			System.out.println("Error in resp: " + resp.get("error"));
			return null;
		}
		return resp.get("result");
	}
}
/*
 {method: "network.find_similar", params: {query: "Tentat", nid: "k9zvvl9ubao6xa"}}
 
 {method: "network.find_similar", params: {query: "Tentat", nid: "k9zvvl9ubao6xa"}}


 {method: "network.find_similar",…}
method: "network.find_similar"
params: {query: "Tentative question", old_query: "tent questi", nid: "k9zvvl9ubao6xa"}
params: {query: "Tentat", nid: "k9zvvl9ubao6xa"}
nid: "k9zvvl9ubao6xa"
query: "Tentat"
{"result":{"query":"tentat","list":[]},"error":null,"aid":"l64lkghrupq73a"}
{method: "network.find_similar", params: {query: "Tentat", nid: "k9zvvl9ubao6xa"}}

{method: "network.find_similar", params: {query: "Tentat", nid: "k9zvvl9ubao6xa"}}

{method: "network.find_similar", params: {query: "Tentat", nid: "k9zvvl9ubao6xa"}}

{method: "network.find_similar", params: {query: "Tentat", nid: "k9zvvl9ubao6xa"}}
nid: "k9zvvl9ubao6xa"
old_query: "tent questi"
query: "Tentative question"

{"result":{"query":"tent question discov duplic detect","list":[{"highlight":"Prasun Dewan: ___bold_start___Duplicate___bold_end___ dummy activehomework... ___bold_start___question___bold_end___\nI have a null pointer exception...","score":8.329855,"nr":12,"subject":"Duplicate dummy activehomework question","id":"l5nxlawp2qd3ek"},{"highlight":"Prasun Dewan: Auto Active Post ___bold_start___Detection___bold_end___...","score":7.2903333,"nr":15,"subject":"Auto Active Post Detection","id":"l5qnut4no5p243"},{"highlight":"Prasun Dewan: Constructive ___bold_start___question___bold_end___\nI get... a null pointer exception when I run ___bold_start___test___bold_end___...","score":6.6432858,"nr":13,"subject":"Constructive question","id":"l5qffw1p6wm52s"},{"highlight":"Prasun Dewan: Dummy active homework ___bold_start___question___bold_end___... exception\n\nPrasun Dewan: I have the same ___bold_start___question___bold_end___...","score":5.3561397,"nr":10,"subject":"Dummy active homework question","id":"l5mo2o1e38x3ry"},{"highlight":"problem publicly if possible and say private ___bold_start___question___bold_end___... Prasun Dewan: I will come by at 5pm to ask ___bold_start___questions___bold_end___... to visit Prasun dewan at 4pm to ask also ___bold_start___questions___bold_end___...","score":1.564445,"nr":9,"subject":"Office Hours","id":"l5j67zroibi1s1"},{"highlight":"problem publicly if possible and say private ___bold_start___question___bold_end___...","score":1.4205946,"nr":14,"subject":"Andrew Office Hours Signup","id":"l5qfqpiowe113c"},{"highlight":"Prasun Dewan: ___bold_start___testing___bold_end___ message to sam\nignore...","score":0.0,"nr":11,"subject":"testing message to sam","id":"l5mpiwpuld117o"},{"highlight":"cloud-based data sciene\n\nPrasun Dewan: ___bold_start___Test___bold_end___...","score":0.0,"nr":7,"subject":"Could-based Data Sciene","id":"l5gqx2voq3d3lg"}]},"error":null,"aid":"l64llyhngy249y"}

*
method: "content.duplicate"
params: {cid_dupe: "l5nxlawp2qd3ek", cid_to: "l5qffw1p6wm52s", msg: "experimenring with duplicate"}
cid_dupe: "l5nxlawp2qd3ek"
cid_to: "l5qffw1p6wm52s"
msg: "experimenring with duplicate"

{"result":"OK","error":null,"aid":"l6505rkyclq1in"}


method: "network.search"
params: {nid: "k9zvvl9ubao6xa", query: "exper"}
{"result":[],"error":null,"aid":"l650g8sazzt5r8"}

method: "content.duplicate"
params: {cid_dupe: "l650icekfx86je", cid_to: "l5qffw1p6wm52s", msg: "will this also dsappear"}
{"result":"OK","error":null,"aid":"l650lgdnsc08i"}

method: "network.get_online_users"
params: {nid: "k9zvvl9ubao6xa", uid: "h68jepo6q4z3bk"}
{"result":{"users":1},"error":null,"aid":"l650k7yy2y278e"}

method: "network.get_all_users"
params: {nid: "k9zvvl9ubao6xa"}
nid: "k9zvvl9ubao6xa"
{"result":[{"role":"ta","name":"Cong Lu","endorser":{},"admin":true,"photo":null,"id":"k6hd7s9zstt65n","photo_url":null,"published":true,"email":"conglu@live.unc.edu","us":false,"admin_permission":5,"facebook_id":null},{"role":"ta","name":"Duy Nguyen","endorser":{},"admin":true,"photo":null,"id":"idjrrmtnu7y4b8","photo_url":null,"published":true,"email":"duyn@email.unc.edu, duyn@cs.unc.edu","us":false,"admin_permission":5,"facebook_id":null},{"role":"professor","name":"Prasun Dewan","endorser":{},"admin":true,"photo":null,"id":"h68jepo6q4z3bk","photo_url":null,"email":"dewan@cs.unc.edu","us":false,"admin_permission":15,"facebook_id":null},{"role":"ta","name":"Sam George","endorser":{"id7hnxcun154a8":1},"admin":true,"photo":null,"id":"i588uvywlmn3b5","photo_url":null,"published":true,"email":"sdgeorge@med.unc.edu, samuel_george@med.unc.edu, sdgeorge@cs.unc.edu, sdgeorge@icloud.com, sdgeorge27518@gmail.com","us":false,"admin_permission":5,"facebook_id":null}],"error":null,"aid":"l650h5icb6z628"}

method: "content.update"
params: {cid: "l5qnut4no5p243", subject: "Auto Active Post Detection",…}
anonymous: "no"
cid: "l5qnut4no5p243"
content: "<p>Followups to this post will indicate posts that have been automatically labeled as active. Active posts are those that describe a problem but no attempt to solve it. An upvite indicates you agree with the labeling.</p>"
editor: null
revision: 1
subject: "Auto Active Post Detection"
type: "question"

{method: "content.update", params: {cid: "l5qnut4no5p243", subject: "Auto Active Post Detection",…}}
method: "content.update"
params: {cid: "l5qnut4no5p243", subject: "Auto Active Post Detection",…}
anonymous: "no"
cid: "l5qnut4no5p243"
content: "<p>Followups to this post will indicate posts that have been automatically labeled as active. Active posts are those that describe a problem but no attempt to solve it. An upvite indicates you agree with the labeling.</p>"
editor: null
revision: 2
subject: "Auto Active Post Detection"
type: "note"

*
*/
