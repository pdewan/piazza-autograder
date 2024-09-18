package piazza.nlp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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

/*
 * Draft
 * {method: "network.save_draft", params: {nid: "k9zvvl9ubao6xa",…}}
method
: 
"network.save_draft"
params
: 
{nid: "k9zvvl9ubao6xa",…}
 */
public class APiazzaClassRecursivePosts extends APiazzaClass implements PiazzaClass {

//	final String piazzaLogic = "https://piazza.com/logic/api";
//
//	protected PiazzaSession mySession = null;
//	protected String cid;
//	private Map<String, String> map = new HashMap<>(); // key: cid value: uid
//	private String outDirectory;
	
	public static final String ALL_POSTS = "AllPosts";
	public static final String BY_AUTHOR_POSTS = "ByAuthorPosts";	
	public static final String AUTHORS = "Authors";
	Date feedDate;
	private Map<String, List<Map<String, Object>>> authorToPosts = new HashMap<>();
	
	


	public APiazzaClassRecursivePosts(String email, 
			String password, 
			String classID, 
			String anOutDirectory)
			throws ClientProtocolException, IOException, LoginFailedException {
		super(email, password, classID);
//		this.mySession = new APiazzaSession();
//		this.mySession.login(email, password);
//		this.cid = classID;
//		outDirectory = anOutDirectory;
	}

	public APiazzaClassRecursivePosts(String email, String password, String classID)
			throws ClientProtocolException, IOException, LoginFailedException {
		this(email, password, classID, "incompletePostsFile");
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
	public static boolean isDiary(Map<String, Object>  aPostInfo) {
    	List<String> tags = (List<String>) aPostInfo.get("tags");
    	
    	boolean isDiary = tags != null && tags.contains("diary");
    	if (!isDiary) {
    		return false;
    	}
    	
     	
    	
    	String aSubject = ((String) aPostInfo.get("subject")).toLowerCase();
    	boolean aSubjectIsDiary = aSubject.contains("iary"); 
    	
    
    	String aContent = ((String) aPostInfo.get("content")).toLowerCase();
    	boolean aContentIsDiary =  
    			aContent.toLowerCase().contains("q&a");
    	if (!aContentIsDiary) {
    		return false;
    	}
   
    	return true; 
    }
    public static boolean isOfficeHourRoot(Map<String, Object>  aPostInfo) {
    	List<String> tags = (List<String>) aPostInfo.get("tags");
    	boolean isPinned = tags != null && tags.contains("pin");
    	if (!isPinned) {
    		return false;
    	}
    	boolean isInstructorNote = tags.contains("instructor-note");
    	if (!isInstructorNote) {
    		return false;
    	}
     	
    	
    	String aSubject = ((String) aPostInfo.get("subject")).toLowerCase();
    	boolean aSubjectIsOfficeHours = aSubject.contains("office hour"); 
    	if (!aSubjectIsOfficeHours) {
    		return false;
    	}
    
    	String aContent = ((String) aPostInfo.get("content")).toLowerCase();
    	boolean aContentIsOfficeHours =  
    			aContent.toLowerCase().contains("schedule") && 
    			aContent.contains("office hour");
    	
    	if (!aContentIsOfficeHours) {
    		return false;
    	}
   
    	return true; 
    }
    
    public void addAuthorPost(String anAuthor, Map<String, Object> aPost) {
    	List<Map<String, Object>> aPosts = authorToPosts.get(anAuthor);
    	if (aPosts == null) {
    		aPosts = new ArrayList();
    		authorToPosts.put(anAuthor, aPosts);
    	}
    	aPosts.add(aPost);
    }
    
    public static String instructorName (String aContents) {
    	//Respond here for Andrew Wortas' Office Hours (at https://unc.zoom.us/j/99914248661) for assignment problems
    	String aPrefix = "<strong>";
    	int aStartIndexOfName = aContents.indexOf(aPrefix) + aPrefix.length();
    	if (aStartIndexOfName == -1) {
    		return null;
    	}
    	String aSuffix = "</strong>";
    	int anEndIndexOfName = aContents.indexOf(aSuffix);
    	if (anEndIndexOfName == -1) {
    		return null;
    	}
    	return aContents.substring(aStartIndexOfName, anEndIndexOfName).trim();
    	
    }
    public static String getLatestContent(Map<String, Object> item) {	
		Map<String, Object> latestElement = getLatestElement(item);
		return (String) latestElement.get("content");
    }
    public static Map<String, Object> getLatestElement(Map<String, Object> item) {
  		List<Map<String, Object>> historyList = (List<Map<String, Object>>) item.get("history");
  		Map<String, Object> latestElement = historyList.get(0);
  		return latestElement;
      }
	public Map<String, Object> toPostInfo(Map<String, Object> item, boolean isChild, long aStartTime, long anEndTime, String ... aTags ) {
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
//			long aTime = 0;

			if (!isChild) {
//				String aDateString = (String) postInfo.get("modified");
//				Date aDate = getDate(aDateString);
//				aTime = aDate.getTime();
//				
//				if (aTime > anEndTime) {
//					return null;
//				}
				Map<String, Object> post = getPost(id);

//				List<Map<String, Object>> historyList = (List<Map<String, Object>>) post.get("history");
//				Map<String, Object> latestElement = historyList.get(0);
//				content = (String) latestElement.get("content");
				content = getLatestContent(post);
//				postInfo.put("subject", item.get("subject"));
				List<Map<String, Object>> uidList = (List<Map<String, Object>>) post.get("change_log");
				Map<String, Object> uidMap = (Map<String, Object>) uidList.get(0);
				uid = (String) uidMap.get("uid");
				children = (List<Map<String, Object>>) post.get("children");
				postInfo.put("is_office_hour_parent", false);
				postInfo.put("is_office_hour_request", false);
				postInfo.put("is_office_hour_root", false); // will change this below
			} else {
				content = (String) item.get("subject");
				children = (List<Map<String, Object>>) item.get("children");
				uid = (String) item.get("uid");
				String aUTCDate = (String) item.get("created");
				postInfo.put("modified", aUTCDate);
				postInfo.remove("created");
				String aDateString = (String) postInfo.get("modified");
				Date aDate = getDate(aDateString);
				long aTime = aDate.getTime();
				if (aTime > anEndTime) {
					return null;
				}
				postInfo.put("time", aTime);
				postInfo.put("synthesized_time", aTime);


			}
			postInfo.put("content", content);
			if (!isChild) {
				boolean isDiary = isDiary(postInfo);
				postInfo.put("is_diary", isDiary);
				postInfo.put("root_is_diary", isDiary);
				postInfo.put("root_subject", postInfo.get("subject"));
			}

//			postInfo.put("subject", latestElement.get("subject"));
//			postInfo.put("updated", item.get("updated"));
//			postInfo.put("uid", uid);
			String anEmail = getUserEmail(uid);
			String aUserName = getUserName(uid);
			if (aUserName == null) {
				System.out.println("Null user name for uid " + uid);
				aUserName = "Instructor";
			}
			if (aUserName.equals("Piazza Team") && anEmail == null) {
				return null;
			}
			if (anEmail == null) {
				anEmail = "instructor@piazza.com";
			}
//			postInfo.put("userName", aUserName);
//			postInfo.put("email", anEmail);
			String anAuthor = aUserName + "(" + anEmail + ")";
			postInfo.put("author",anAuthor);
			addAuthorPost(anAuthor, postInfo);
			
//			String aDateString = (String) postInfo.get("modified");
//			Date aDate = getDate(aDateString);
//			long aTime = aDate.getTime();
//			if (aTime > anEnd)
//			postInfo.put("time", aTime);
//			postInfo.put("sythesized_time", aTime);

			

//			postInfo.put("tags", item.get("tags"));
//			postInfo.put("timeCreated", post.get("modified"));
//			postInfo.put("no_answer_followup", item.get("no_answer_followup"));
			if (children != null && !children.isEmpty()) {
				boolean isOfficeHourRoot = isOfficeHourRoot(postInfo);
				postInfo.put("is_office_hour_root", isOfficeHourRoot);
				 

				
				Boolean  isOfficeHourParent = (Boolean) postInfo.get("is_office_hour_parent");
				

				
				List<Map<String, Object>> aChildren = new ArrayList();
				for (Map<String, Object> child : children) {
//					String aRootSubject = (String)  postInfo.get("root_subject"); 
//					if (aRootSubject == null) {
//						aRootSubject = (String)  item.get("subject");						
//					}
					
					child.put("root_subject", postInfo.get("root_subject"));	
					child.put("root_is_diary", postInfo.get("root_is_diary"));
					child.put("is_office_hour_parent", isOfficeHourRoot);
					child.put("is_office_hour_request", isOfficeHourParent);
					if (isOfficeHourRoot) {
						String anInstructorName = 						
						instructorName(((String) child.get("subject")).toLowerCase());
						if (anInstructorName != null) {
							child.put("instructor_name", anInstructorName);
						}
					}
					if (isOfficeHourParent) {
//						child.put("isOfficeHourParent", false);
						String aParentInstructorName = (String) postInfo.get("instructor_name");
						if (aParentInstructorName != null) {
							child.put("parent_instructor_name", aParentInstructorName);
						}
					}				
					child.put("tags", postInfo.get("tags"))	;
					Map<String, Object> aChildPost = toPostInfo(child, true, aStartTime, anEndTime);
						if (aChildPost == null) {
							continue;
						}
					aChildren.add(aChildPost);
					long aCurrentParentTime = (long) postInfo.get("synthesized_time");
					long aChildTime = (long) aChildPost.get("synthesized_time");
					
					long aMaxTime = Math.max(aCurrentParentTime, aChildTime);
					
					if (aMaxTime != aCurrentParentTime) {
						postInfo.put("synthesized_time", aMaxTime);
					}
					
					
				}
				long aFinalParentTime = (long) postInfo.get("synthesized_time");
				if (aFinalParentTime < aStartTime) {
					return null;
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
	public void writeAuthorPosts(String anOutDirectory, String aDateString) throws IOException {
		JSONObject aJsonObject = new JSONObject();
		for (String aKey:authorToPosts.keySet()) {
			aJsonObject.put(aKey, authorToPosts.get(aKey));
		}
		String aString = aJsonObject.toString();
		String aFileName = anOutDirectory+"/"+cid + "_" + BY_AUTHOR_POSTS+"_" + aDateString + ".json";
		File aFile = new File(aFileName);
		aFile.createNewFile();
		writeToFile(aFile, aString);
		
	}

	public void writeAuthors(String anOutDirectory, String aDateString) throws IOException {
		
		String aString = allAuthors();
		String aFileName = anOutDirectory+"/"+cid + "_" +AUTHORS + "_" + aDateString + ".txt";
		File aFile = new File(aFileName);
		aFile.createNewFile();
		writeToFile(aFile, aString);
		
	}
	public void writeToFile(File aFile, String aString)   {
		try {
			
			
//			FileWriter fileWriter = new FileWriter("C:\\Users\\gubow\\COMP 691H\\Find Incomplete Post\\COMP 301 Summer 2021.json");
			FileWriter fileWriter = new FileWriter(aFile);

//			fileWriter.write(result.toJSONString());
			fileWriter.write(aString);

			fileWriter.flush();
			System.out.println("File " + aFile + " created successfully");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String dateToFileExtension(Date aDate) {
		return aDate.toString().replaceAll(":", "_").replaceAll(" ", "-");	
		
	}
	public  String allAuthors() {
		StringBuilder retVal = new StringBuilder();
		for (String anAuthor:authorToPosts.keySet()) {
			retVal.append(anAuthor + "\n");
		}
		return retVal.toString();
	}
	public void writeAllPosts(String anOutDirectory, int aStartCid, int anEndCid, long aStartTime, long anEndTime, String ... aTags ) throws ClientProtocolException, NotLoggedInException, IOException {
		JSONObject allPosts = getAllPostsRecursive(aStartCid, anEndCid, aStartTime, anEndTime, aTags);
		try {
		String aDateString = dateToFileExtension(feedDate);
			String aFileName = anOutDirectory+"/"+ cid + "_" + ALL_POSTS+"_" + aDateString + ".json";
			File aFile = new File(aFileName);
			aFile.getParentFile().mkdirs();
			aFile.createNewFile();
			String aString = allPosts.toString();
			writeToFile(aFile, aString);
			
			writeAuthorPosts(anOutDirectory, aDateString);
			writeAuthors(anOutDirectory, aDateString);
			
//			FileWriter fileWriter = new FileWriter("C:\\Users\\gubow\\COMP 691H\\Find Incomplete Post\\COMP 301 Summer 2021.json");
//			FileWriter fileWriter = new FileWriter(aFile);
//
////			fileWriter.write(result.toJSONString());
//			String aString = allPosts.toString();
//			fileWriter.write(aString);
//
//			fileWriter.flush();
//			System.out.println("JSON file " + aFileName + " created successfully");

		} catch (Exception e) {
			e.printStackTrace();
		}
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
	public static boolean intersectsLC (List<String> aList1, List<String> aList2) {
		for (String anElement1:aList1) {
			String anElement1LowerCase = anElement1.toLowerCase();
			for (String anElement2: aList2) {
				if (anElement1LowerCase.equals(anElement2.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}
	public JSONObject getAllPostsRecursive(int aStartCid, int anEndCid, long aStartTime, long anEndTime, String ...aTags) throws ClientProtocolException, NotLoggedInException, IOException {
		long startTime = System.currentTimeMillis();
		feedDate = new Date(startTime);
		List<Map<String, Object>> feed = this.getFeed(999999, 0);
//		List<Map<String, Object>> feed2 = this.getFeed(999999, 7);
//		List<Map<String, Object>> posts = new ArrayList<Map<String, Object>>();

		// The following helps to display a visual of the percentage of completion due
		// to having to wait with Thread.sleep
		double maxSize = feed.size();
		int numItemsProcessed = 0;
//		org.json.simple.JSONObject result = new org.json.simple.JSONObject();
		JSONObject result = new JSONObject();
		

		// Time how long it takes to access the feed given that we are waiting one
		// second between each request

		for (Map<String, Object> item : feed) {
			
//			System.out.println(item);
			
			int aCid =  (int) item.get("nr");
			if (aCid < aStartCid || aCid > anEndCid) {
				continue;
			}
			String aDateString = (String) item.get("modified");
			Date aDate = getDate(aDateString);
			long aTime = aDate.getTime();
			
			if (aTime > anEndTime) {
				return null;
			}
			item.put("time", aTime);
			item.put("synthesized_time", aTime);
			if (aTags.length > 0) {
				
				List<String> aTagsList = Arrays.asList(aTags);
				List<String> anActualTags = (List<String>) item.get("tags");
				if (!intersectsLC(anActualTags, aTagsList)) {
					continue;
				}
			}
//			String id = (String) item.get("id");
			Map<String, Object> postInfo = toPostInfo(item, false, aStartTime, anEndTime);
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


