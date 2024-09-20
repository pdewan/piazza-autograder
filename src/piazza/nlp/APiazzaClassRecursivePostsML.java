package piazza.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Boolean;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jna.platform.win32.WinBase.SYSTEM_INFO;

import piazza.APiazzaClass;
import piazza.LoginFailedException;
import piazza.NotLoggedInException;

public class APiazzaClassRecursivePostsML extends APiazzaClassRecursivePosts {

	final String piazzaLogic = "https://piazza.com/logic/api";
	final String piazzaMain = "https://piazza.com/logic/api";
	final AGPTClass gptAPI;
	
	final private String LOG_POST_NAME = "Mediated GPT Log";
	
	final private String OH_SIGNUP_NAME = "Signing up for Office Hours";
	final private String OFFICE_HOURS_INSTRUCTIONS = "<md>\r\nHere are the times and guidelines for office hours. \r\n\r\nPlease let us know **at least one hour** before the start of each office hour period if you will join us during the period, otherwise we may not start the Zoom session or terminate the Zoom session before the end of the period. Also let us know when during the period you plan on joining. \r\n\r\nExample incomplete post:\r\n\r\n**_9/1 at 11:15_**\r\n\r\nDoes not give the reason for the visit.\r\n\r\nExample complete post: \r\n\r\n**_11/17 at 2:20pm_**\r\n\r\n**_Help with setting up IndentifierAtom_**\r\n\r\nGives the time and reason\r\n\r\nSchedule a meeting by publicly commenting on the appropriate instructor discussion in this thread.\r\n\r\nIn your comment reference the related Piazza post if it exists or describes the problem publicly if possible and say private question if not possible. **Give as much detail as possible. Saying you have a problem with homework 1 or with localchecks is not specific; give the exact problem.**\r\n\r\nIf your problem requires a screenshot or your stack trace it should be its own post. Please reference that post as part of your help request but do not include screenshots or stack traces in your help request post. Give a text trace when possible and screenshots only for GUIs.\r\n\r\n**Times:**\r\n\r\n(These times will be subject to change while the semester progresses, but will be updated days in advance)\r\n\r\n[INSERT OFFICE HOURS DAYS/TIMES/LOCATIONS]\r\n\r\n#pin</md>";
	
	// TODO: remove (comment out) the ones of these that aren't needed
	final private String PROMPT_POST_NAME = "Mediated GPT Prompt";
	final private String MEDIATED_GPT_PROMPT = "You are a Teaching Assistant for an upper-level Computer Science course. Imagine a student is working on an assignment according to the ASSIGNMENT INSTRUCTIONS below, and the student comes to you with the following STUDENT QUESTION.<br /><br />----------<br /><br />ASSIGNMENT INSTRUCTIONS:<br />[ASSIGNMENT_INSTRUCTIONS]<br /><br />----------<br /><br />STUDENT QUESTION:<br />[STUDENT_QUESTION]<br /><br />----------<br /><br />What should the student be told? You should not give them code in your response. Instead, guide the student to an answer using a step-by-step natural language explanation. You should not give them the full answer all at once, instead reduce the problem for the student by providing a series of smaller tasks for the student to solve that will help them answer their question.";
	//final private String OH_GPT_PROMPT = "You are a Teaching Assistant for an upper-level Computer Science course, and you will be given an office hours request submitted by a student. A complete office hours request should contain a date and time that the student will join office hours (the time does not need to specify AM or PM) and a detailed reason for the visit. However, the actual request you recieve may not contain all of this information, in which case it is not complete. A student saying they have a problem with homework 1 or with localchecks is not specific; the student should give the exact problem. An example of an incomplete request is '9/1 at 11:15' because it does not give the reason for the visit. An example of a complete request is '11/17 at 2:20pm\nHelp with setting up IndentifierAtom', as it gives the time and reason. A student submitted the following office hours request:<br /><br />----------<br /><br />[OFFICE_HOURS_REQUEST]<br /><br />----------<br /><br />Indicate whether the office hours request is complete based on the criteria above. If it is complete, respond with simply the word 'Complete' with no other output. If it is not complete, write a short message to the student indicating why it is not complete and tell them to edit their post with the necessary information.";
	// what aspect of A5
	// what issues are you having with curry
	// what problem are you having with base-case
	
	//final private String OH_GPT_PROMPT = "You are a Teaching Assistant for an upper-level Computer Science course, and you will be given an office hours request submitted by a student. The request must contain two components: 1. The time and date of the visit. 2. The reason for the visit that ideally identifies: a) the specific concept in an assignment with which they are having trouble, b) what kind of problem they are having with it, and c) what they have done to overcome this problem. An office hours request is complete if it covers all of these aspects.<br /><br />----------<br /><br />An example of a complete request is:\r\n- '11/17 at 2:20pm\\nIn A5, need help with the base case of the recursive factorial function, which is not terminating. Have used print statements to trace the arguments to each call and found that the arguments do reduce in each call.': This request is complete as it contains all of the above aspects.<br /><br />----------<br /><br />Examples of incomplete requests are:\r\n- 'Need help with A5': This request is missing 1. and 2. a), b), and c).\r\n- '11/17 at 2:20pm\\nNeed help with A5 factorial': This request is missing 2. b) and c).\r\n- '11/17 at 2:20pm\\nIn A5, need help with the base case of the recursive factorial function, which is not terminating.': This request is missing 2. c).<br /><br />----------<br /><br />Please check if the following request is complete. If it is not complete, please indicate which expected aspects are missing, hy you think they are missing, tell them to edit their post. Student's office hour request:<br /><br />----------<br /><br />[OFFICE_HOURS_REQUEST]";
	
	//final private String OH_GPT_PROMPT = "<p>You are a Teaching Assistant for an upper-level Computer Science course, and you will be given an office hours request submitted by a student. The request must contain two components, <strong>time </strong>and <strong>reason</strong>, which are explained below.</p>\r\n<p></p>\r\n<p><strong>Time</strong></p>\r\n<p>This component contains the time and date of the visit.</p>\r\n<p></p>\r\n<p><strong>Reason</strong></p>\r\n<p>This component has two sub-components:</p>\r\n<ul>\r\n<li><strong>Problem:</strong> the specific problem they are having with the concept</li>\r\n<li><strong>Solution attempt:</strong> what they have done to overcome this problem</li>\r\n</ul>\r\n<p>An office hours request is complete if it covers all aspects of the reason component.</p>\r\n<p></p>\r\n<p></p>\r\n<p>Please check if the following request is complete. If the post is complete, please respond with the word 'Complete' with no other output. If it is not complete, please write a reply to the student that indicates which expected aspects are missing and provides a suggestion for an improved response. Address the student directly in your response. You do not need to be formal or include a salutation.</p>\r\n<p></p>\r\n<p>Student's office hour request:</p>\r\n<p></p>\r\n[OFFICE_HOURS_REQUEST]";

	final private String OH_PROMPT_POST_NAME = "OH Request Checker Prompt";
	final private String OH_GPT_PROMPT = "You are a Teaching Assistant for an upper-level Computer Science course, and you will be given an office hours request submitted by a student. The request must contain two components, **time** and **reason**, which are explained below.\r\n\r\n**Time:** This component contains the time and date of the visit.\r\n\r\n**Reason:** This component contains the specific problem they are having with the concept or assignment.\r\n\r\nAn office hours request is complete if it contains both the time and reason components. These components do not have to be explicitly labeled or demarcated, and they do not need to occur in the same sentence or paragraph. The time and date do not have to be in a particular format, and the reason does not have to be formatted as a complete sentence. Simply listing a time and stating an issue the student is having is enough.\r\n\r\nPlease check if the following request is complete. If the post is complete, or if the post indicates that the issue has already been resolved, please respond with the word ‘Complete’ with no other output. If it is not complete, please write a reply to the student that indicates which expected aspects are missing and provides a suggestion for an improved response. Address the student directly in your response. You do not need to be formal or include a salutation.\r\n\r\n**Student’s office hour request:**\r\n[OFFICE_HOURS_REQUEST]";
	
	// KEEP THE OLD PROMPTS HERE FOR FUTURE DISCUSSION IN A PAPER

	final private String DATAFILE_PATH = "mediatedgpt_data.json";
//	final private String DATAPOST_ID = "2"; // Overwrites post @2, which is "Tips & Tricks for a successful class"
	final private String[] ASSIGNMENT_TAGS = {"hw0", "hw1", "hw2", "hw3", "hw4", "hw5", "hw6", "hw7", "hw8", "hw9", "hw10"};
	final private String[] PRIVATE_TAGS = {"includes_code", "grading_error", "personal_situation", "diary"};
	
	final private String SUGGESTED_PUBLIC_POST_NAME = "Suggested Public Visibility Message";
	final private String SUGGESTED_PUBLIC_MESSAGE = "Based on the folder tags associated with your post, it looks like the post visibility can be changed from private to public. In order to help as many students as possible, all posts should be made public unless they include code you’ve written or personal information. If your post meets these private criteria, please add the appropriate folder tags to your post (<code>includes_code</code>, <code>grading_error</code>, <code>personal_situation</code>, etc.) and keep the visibility as private. Otherwise, please edit your post and change the “Post To” setting from “Individual Student(s) / Instructor(s)” to “Entire Class”. Thanks!";
	
	final private String SUGGESTED_PRIVATE_POST_NAME = "Suggested Private Visibility Message";
	final private String SUGGESTED_PRIVATE_MESSAGE = "Your post is tagged as [FOLDER_TAGS] even though its visibility is set to public. If these tags are correct and your post includes code you’ve written or involves a personal situation, please edit your post and change the “Post To” setting from “Entire Class” to “Instructors”. Otherwise, remove the incorrect folder tags from your post. Thanks!";
	
	final private String SUGGESTED_INSTRUCTORS_POST_NAME = "Suggested All Instructors Visibility Message";
	final private String SUGGESTED_ALL_INSTRUCTORS = "It appears that you’ve posted this to individual instructors. Please edit your post and select “Instructors” under the “Individual Student(s) / Instructor(s)” dropdown so that the entire instructional team can view your post. Thanks!";
	
	final private String IMAGE_DETECTED_POST_NAME = "Image Detected Message";;
	final private String IMAGE_MESSAGE = "It looks like you may have included a screenshot in your post. If it is a screenshot of code or a console trace, please replace the image with the actual text itself so we can search for issues easier. If it is another type of image, please include any relevant text contained within the image (errors given in Eclipse pop-up windows, etc.). Thanks!";
	
	final private String AUTOMATED_DISCLAIMER_POST_NAME = "Automated Suggestion Disclaimer Message";
	final private String AUTOMATED_SUGGESTION_DISCLAIMER = "\n\n<hr/>\n\n<em>This message was generated automatically and could be incorrect. If you feel that it does not apply to your post, please disregard the suggestion.</em>";

	
	String lastRun = "";
	
	public APiazzaClassRecursivePostsML(String email, String password, String classID)
			throws ClientProtocolException, IOException, LoginFailedException {
		super(email, password, classID);
		this.gptAPI = null;
		
	}
	
	public APiazzaClassRecursivePostsML(String email, String password, String classID, String apiKey, String defaultGPTModel, String lastRun)
			throws ClientProtocolException, IOException, LoginFailedException {
		super(email, password, classID);
		this.gptAPI = new AGPTClass(apiKey, defaultGPTModel);
		this.lastRun = lastRun;
	}
	
	
	// get all users enrolled in the class (including instructors)
	public List<Map<String, Object>> getAllUsers(
		) throws ClientProtocolException, NotLoggedInException, IOException {

		JSONObject data = new JSONObject().
			put("nid", this.cid);
	
		Map<String, Object> resp = this.mySession.piazzaAPICall("network.get_all_users", data, piazzaLogic);	
		return (List<Map<String, Object>>) resp.get("result");
	}
	
	
	// get all instructors enrolled in the class
	public List<Map<String, Object>> getInstructors(
		) throws ClientProtocolException, NotLoggedInException, IOException {

		List<Map<String, Object>> allUsers = getAllUsers();
		List<Map<String, Object>> instructors = new ArrayList();
		
		for (Map<String, Object> user : allUsers) {
			if ((boolean) user.get("admin")) {
				instructors.add(user);
			}
		}
		
		return instructors;
	}
	
	
	public List<String> getInstructorIDs() throws ClientProtocolException, NotLoggedInException, IOException {
		
		List<Map<String, Object>> instructors = getInstructors();
		List<String> instructorIDs = new ArrayList();
		
		for (Map<String, Object> instructor : instructors) {
			instructorIDs.add(String.valueOf(instructor.get("id")));
		}
		
		return instructorIDs;
	}

	
	// TODO: make messageType an enum, or just check if recipients is empty?
	// TODO: feed groups? other config?
	// TODO: also do "type" -- currently accepts markdown, could expand to plain text or rich text
	public String createPost(
			String aSubject,
			String aContent,
			List<String> aTags,
			List<String> aRecipients,
			String messageType
		) throws ClientProtocolException, NotLoggedInException, IOException {
		
		String recipients = "";
		if (messageType.equals("individual")) {
			
			for (String r : aRecipients) {
				recipients += r + ",";
			}
			// NOTE: remove this if you don't want to post to all instructors
			recipients += "instr_" + this.cid;
		}
		
		JSONObject data = new JSONObject().
			put("nid", this.cid).
			put("type", "note").
			put("subject", aSubject).
			put("content", aContent).
			put("folders", aTags).
			put("editor", "md").
			put("anonymous", "no").
			put("status", "active");
		
		if (aRecipients != null) {
			Map<String, String> config = new HashMap();
			config.put("feed_groups", recipients);
			data.put("config", config);
		}
		
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.create", data, piazzaLogic);
		Map<String, Object> res = (Map<String, Object>) resp.get("result");
		System.out.println("RESP");
		System.out.println(resp);
		System.out.println("RES");
		System.out.println(res);
		
		return (String) (res.get("id"));
		//return (String) resp.get("aid");
		//return resp != null? true:false;
		
	}
	
	
	public boolean createDraftNote(
			String aSubject,
			String aContent,
			List<String> aTags,
			List<String> aRecipients,
			String messageType
		) throws ClientProtocolException, NotLoggedInException, IOException {
		
		Map<String, Integer> recipientsMap = new HashMap();
		boolean individual_members = false;
		boolean entire_group = true;
		
		if (messageType.equals("individual")) {
			// NOTE: remove this if you don't want to post to all instructors
			recipientsMap.put("instr_" + this.cid, 1);
			for (String r : aRecipients) {
				recipientsMap.put(r, 1);
			}
			individual_members = true;
			entire_group = false;
		}
		
		JSONObject data = new JSONObject().
				put("nid", this.cid).
				put("draft", new JSONObject().
					put("content", aContent).
					put("editorType", "md").
					put("selectedPrivateUsers", recipientsMap).
					put("folders", aTags).
					put("btn", new JSONObject().
						put("post_type_note", true).
						put("post_type_poll", false).
						put("post_type_question", false).
						put("class_live", false).
						put("entire_group", entire_group).
						put("class_subgroup", false).
						put("individual_members", individual_members).
						put("publish_later", false).
						put("publish_now", true).
						put("posting_options_bypass_email", false).
						put("notify_mobile_update_input", false).
						put("must_read", 0).
						put("must_read_manual", true).
						put("must_read_to_post", false).
						put("must_read_expire", "null")
					).
					put("txt", new JSONObject().
						put("subgroup_dropdown", "instr_" + this.cid).
						put("post_summary", aSubject).
						put("new_post_anonymity", "no")
					)
				);
	
//		System.out.println(data.toString());
		
		Map<String, Object> resp = this.mySession.piazzaAPICall("network.save_draft", data, piazzaLogic);
//		System.out.println(resp.toString());
		return resp != null? true:false;
		
	}
	
	
	public boolean createDraftReply(
			String postID,
			String aContent
		) throws ClientProtocolException, NotLoggedInException, IOException {
		
		String type;
		Map<String, Object> originalPost = this.getPost(postID);
		
		//System.out.println(originalPost);
//		for (String key : originalPost.keySet()) {
//		    System.out.println(key + " = " + originalPost.get(key));
//		}
		//System.out.println(originalPost.get("type"));
		
		
		if (((String) originalPost.get("type")).equals("question")) {
			type = "i_answer";
		} else {
			type = "followup";
		}
		
		JSONObject data = new JSONObject().
				put("network_id", this.cid).
				put("body", aContent).
				put("cid", postID).
				put("editor", "md").
				put("revision", 0).
				put("type", type); // followup for post replies, feedback for nested (comment) replies
	
		//System.out.println(data.toString());
		
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.auto_save", data, piazzaLogic);
		return resp != null? true:false;
		
	}
	
	
	public boolean createDraftAnswer(
			String postID,
			String aContent
		) throws ClientProtocolException, NotLoggedInException, IOException {
	
		Map<String, Object> originalPost = this.getPost(postID);
		if (!((String) originalPost.get("type")).equals("question")) {
			throw new IOException("ERROR: trying to answer a Piazza post that is not a question.");
		}
		
		JSONObject data = new JSONObject().
				put("network_id", this.cid).
				put("body", aContent).
				put("cid", postID).
				put("editor", "md").
				put("revision", 0).
				put("type", "i_answer"); // followup for post replies, feedback for nested (comment) replies
	
		System.out.println("ABOUT TO DRAFT THE FOLLOWING ANSWER");
		System.out.println(data.toString());
		
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.auto_save", data, piazzaLogic);
		System.out.println("RESP");
		System.out.println(resp);
		return resp != null? true:false;
		
	}
	
	
	public boolean createReply(
			String postID,
			String aContent
		) throws ClientProtocolException, NotLoggedInException, IOException {
		
		String type;
		Map<String, Object> originalPost = this.getPost(postID);
		
		for (String key : originalPost.keySet()) {
		    System.out.println(key + " = " + originalPost.get(key));
		}

		// TODO: split into 2 separate methods, make this check in processPosts()followup
		if (((String) originalPost.get("type")).equals("question")) {
			type = "i_answer";
		} else {
			type = "followup";
		}
		
		JSONObject data = new JSONObject().
				put("network_id", this.cid).
				put("subject", aContent).
				put("cid", postID).
				put("editor", "md").
				put("revision", 0).
				put("type", type); // followup for post replies, feedback for nested (comment) replies
	
		//System.out.println(data.toString());
		
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.create", data, piazzaLogic);
		Map<String, Object> res = (Map<String, Object>) resp.get("result");
		
		System.out.println("RESP");
		System.out.println(resp);
		System.out.println("RES");
		System.out.println(res);
		
		
		//System.out.println(resp);
		return resp != null? true:false;

		/*JSONObject data = new JSONObject().put("cid", cid).put("subject", post)
				.put("type", "feedback").put("content", "").put("anonymous", "no");
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.create", data, piazzaLogic);
		return resp != null? true:false;*/
	
	}
	
	
	public boolean createFollowup(
			String postID,
			String aContent
		) throws ClientProtocolException, NotLoggedInException, IOException {
		
		JSONObject data = new JSONObject().
				put("network_id", this.cid).
				put("subject", aContent).
				put("cid", postID).
				put("editor", "md").
				put("revision", 0).
				put("type", "followup");
		
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.create", data, piazzaLogic);
		System.out.println("RESP");
		System.out.println(resp);
		Map<String, Object> res = (Map<String, Object>) resp.get("result");
		System.out.println("RES");
		System.out.println(res);
		
		return resp != null? true:false;
	
	}
	
	
//	
//	// look into anonymous message
//	public boolean createTaggedCommentReply(
//			String commentID,
//			String aContent
//		) throws ClientProtocolException, NotLoggedInException, IOException {
//	
//		String authorID = this.getAuthorId(this.getPost(commentID));
//		String username = this.getUserName(authorID);
//		String taggedContent = "<strong attention=\"" + authorID + "\">@" + username + "</strong> " + aContent;
//		
//		return this.createReply(commentID, taggedContent);
//		
//	}
//	
//	
//	// TODO: return the Map<String, Object> instead?
//	// creates a private thread with a student and the instructors
//	public String createPrivateInstructorThread(
//			String userID
//		) throws ClientProtocolException, NotLoggedInException, IOException {
//		
//		String username = this.getUserName(userID);
//		String subject = "Private Instructor thread with " + username;
//		String threadBody = "This thread can be used as a private channel for communication between you and course instructors.";
//		// TODO: make specialized tag?
//		String[] tags = {"other"};
//		String[] recipients = {userID};
//		
//		return this.createPost(subject, threadBody, Arrays.asList(tags), Arrays.asList(recipients), "individual");
//	}
//	
//	
//	// finds all students in the class and creates a private instructor thread for each
//	public List<String> createPrivateInstructorThreadsForAllUsers(
//		) throws ClientProtocolException, NotLoggedInException, IOException {
//		
//		List<Map<String, Object>> users = this.getAllUsers();
//		List<String> privateInstructorThreads = new ArrayList<String>();
//		for (Map<String, Object> user : users) {
//			// don't create private threads for instructors
//			if (user.get("admin") == Boolean.FALSE) {
//				String userID = (String) user.get("id");
//				privateInstructorThreads.add(this.createPrivateInstructorThread(userID));
//			}
//		} 
//		
//		// returns the ID of each post
//		return privateInstructorThreads;
//	
//	}
//	
//	
//	// TODO: return the Map<String, Object> instead?
//	// given a user ID, return the correct private instructor thread
//	// if no such thread exists, create one and return it
//	// works even if there are multiple students with the same name
//	public String getPrivateInstructorThread(
//			String userID
//		) throws ClientProtocolException, NotLoggedInException, IOException {
//
//		String username = this.getUserName(userID);
//		String query = "Private Instructor thread with " + username;
//		JSONObject data = new JSONObject().
//				put("nid", this.cid).
//				put("query", query);
//		Map<String, Object> resp = this.mySession.piazzaAPICall("network.search", data, piazzaLogic);
//		
//		List<Map<String, Object>> posts = (List<Map<String, Object>>) resp.get("result");
//		for (Map<String, Object> post : posts) {
//			if (post.get("feed_groups") != null && ((String)post.get("feed_groups")).contains(userID)) {
//				return (String) post.get("id");
//			}
//		}
//		
//		return this.createPrivateInstructorThread(userID);
//	}
//	
//	
//	public String getOfficeHoursRootID() throws ClientProtocolException, NotLoggedInException, IOException {
//		
//		JSONObject posts = getAllPostsRecursive(0, Integer.MAX_VALUE, 0, Long.MAX_VALUE);
//		Iterator keyIter = posts.keys();
//		
//		while (keyIter.hasNext()) {
//			String key = (String) keyIter.next();
//			JSONObject val = posts.getJSONObject(key);
//			if (val.getBoolean("is_office_hour_root")) {
//				
//				//root = (String) val.get("id");
//				JSONArray roots = val.getJSONArray("children");
//				if (roots.getJSONObject(0).getString("content").contains("assignment")) {
//					return roots.getJSONObject(0).getString("id");
//				}
//				return roots.getJSONObject(1).getString("id");
//			}
//		}
//		
//		return "No root found";
//	}
//	
//	
//	public Date getOfficeHoursReqests(Date lastChecked) throws ClientProtocolException, NotLoggedInException, IOException {
//		
//		//String rootID = getOfficeHoursRootID();
//		String rootID = "l5j68x15lig3ue";
//		ArrayList<Map<String, Object>> requests = (ArrayList) getPost(rootID).get("children");
//		
//		// TODO: for some reason, getPost returns the parent post even when given the child ID, so this is a workaround
//		requests = (ArrayList<Map<String, Object>>) requests.get(0).get("children");
//		Date checkedTime = new Date();
//		
//		for (Map<String, Object> request : requests) {
//			
//			Date requestDate = getDate((String) request.get("created"));
//			if (requestDate.after(lastChecked)) {
//			
//				//System.out.println(request);
//				
//				String requestText = (String) request.get("subject");
//				String requestUserID = (String) request.get("uid");
//				
//				//System.out.println(requestDate);
//				//System.out.println(requestText);
//				//System.out.println(requestUserID);
//				
//				// TODO
//				String privateThreadID = getPrivateInstructorThread(requestUserID);
//				//System.out.println(privateThreadID);
//				
//				String message = "<p>Hi!</p>"
//						+ "<p></p>"
//						+ "<p>You've submitted an assignment-related office hour request with the following text:</p>"
//						+ "<p></p><blockquote>"
//						+ requestText
//						+ "</blockquote>"
//						+ gptHints(requestText)
//						+ "<p>Also, please reply to this followup with a brief description of the issue you are running into, and the relevant code for the part of the assignment you're working on.</p>"
//						+ "\r\n<p></p>"
//						+ "<p>Then, after coming to office hours, please create another reply that includes both the fixed code and a brief description of what the issue was.</p>"
//						+ "\r\n<p></p>"
//						+ "<p>Thanks!</p>"
//						+ "<p></p>"
//						+ "<p><em>This is an automated message, but the instructors will be notified of any replies you make.</em></p>";
//				
//				//System.out.println(message);
//				//createReply(privateThreadID, message);	
//				
//			}
//			
//		}
//		
//		// save last checked time in txt file?
//		return checkedTime;
//	}
//	
//	
//	// TODO
//	public String manualHints(
//			String requestText
//		) throws ClientProtocolException, NotLoggedInException, IOException {
//		
//		// convert html to plaintext
//		// then try to infer which assignment they're asking about
//		
//		return "<p>HINT TEXT WILL GO HERE</p>"
//			+ "\r\n<p></p>";
//		
//	}
//	
//	
//	public String gptHints(
//			String requestText
//		) throws ClientProtocolException, NotLoggedInException, IOException {
//	
//		String prompt = "You are a Teaching Assistant for an upper-level Computer Science course. Please explain the possible sources of this error:\n"
//				+ requestText;
//		
//		
//		// TODO: convert markdown to plaintext (ask chatgpt)
//		
//		
//		//System.out.println(prompt);
//		return this.gptAPI.makeCall(prompt);
//	}
//	
//	public boolean getNewOfficeHoursReqests(String timeLogPath) throws ClientProtocolException, NotLoggedInException, IOException {
//		
//		// save as json, look in built in files
//		// try to insert instead of append?
//		
//		
//		// if any were found, write to file
//		
//		
//		return true;
//	}

	
	/* Stuff for IUI tool below: ----------------------------------------------------------- */
	
	// TODO: remove support for nested tags; they don't fully work
	
	// old version if using config.json file, new version based on environment variables below
	public AGPTClass getGPTFromFile() throws IOException {
		BufferedReader configReader = new BufferedReader(new FileReader("config.json"));
		
		String text = "";
		String line = configReader.readLine();
		while (line != null) {
			text = text + line;
			line = configReader.readLine();
		}
		configReader.close();
		
		JSONObject config = new JSONObject(text);
		String apiKey = config.getString("openai_api_key");
		String defaultModel = config.getString("default_gpt_model");
		
		AGPTClass gptTest = new AGPTClass(apiKey, defaultModel);
		
		return gptTest;
	}

	
	public AGPTClass getGPT() throws IOException {
;
		String apiKey = System.getenv("OPENAI_API_KEY");
		String defaultModel = System.getenv("DEFAULT_GPT_MODEL");
		
		AGPTClass gptTest = new AGPTClass(apiKey, defaultModel);
		
		return gptTest;
	}
	
	
	// --------------------------
	
	
	// TODO: when revamping, parameterize updatePost to accept editor as a parameter and then use that here
	// NOTE: you cannot edit any of the posts automatically made by Piazza
	// if you provide null for newSubject, it will use the existing subject
	public boolean writeJSONPost(String postNumber, JSONObject obj, String newSubject) throws ClientProtocolException, NotLoggedInException, IOException {
		
		Map<String, Object> post = getPostFromNumber(postNumber);
		Map<String, Object> currPost = getLatestElement(post);
		String subject = newSubject != null ? newSubject : (String) currPost.get("subject");
		int revision = (int) post.get("history_size");
		String postID = (String) post.get("id");

		JSONObject data = new JSONObject().
				put("nid", this.cid).
				put("cid", postID).
				put("editor", "plain").
				put("subject", subject).
				put("content",  obj.toString()).
				put("revision", revision);
				
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.update", data, piazzaLogic);
		//System.out.println(resp);
		return resp != null? true:false;
		
	}
	
	
	public JSONObject readJSONPost(String postNumber) throws IOException, NotLoggedInException {
		
		Map<String, Object> post = getPostFromNumber(postNumber);
		Map<String, Object> currPost = getLatestElement(post);
		String content = (String) currPost.get("content");
		JSONObject data = new JSONObject(content);
		
		return data;
		
	}
	
	
	// TODO: switch things off of JSONFile versions
	
	
	
	

	
	public void writeJSONFile(String filename, JSONObject obj) throws IOException {
		
		FileWriter file = new FileWriter(filename);
		file.write(obj.toString());
		file.close();
		
	}
	
	
	public JSONObject readJSONFile(String filename) throws IOException {
		
		JSONObject data;
		
		// if the named file exists, read it
		try {
			
			BufferedReader dataReader = new BufferedReader(new FileReader(filename));
			String text = "";
			String line = dataReader.readLine();
			while (line != null) {
				text = text + line;
				line = dataReader.readLine();
			}
			dataReader.close();
			
			data = new JSONObject(text);
			
		}
		
		// otherwise, create a new JSON file with that name
		catch (IOException e) {
			
			data = new JSONObject();
			writeJSONFile(filename, data);
			
		}
		
		return data;
		
	}
	
	
	public String createLogPost() throws ClientProtocolException, NotLoggedInException, IOException {
		
		String newContent = "{\"ProcessedPosts\":[]}";
		List<String> newTags = new ArrayList();
		newTags.add("mediated_gpt");
		newTags.add("automated_system");
		List<String> newRecipients = new ArrayList(); // createPost will always include instructors as recipients
		
		return createPost(LOG_POST_NAME, newContent, newTags, newRecipients, "individual");
		
	}
	
	
	public String searchForLogPostID() throws ClientProtocolException, NotLoggedInException, IOException {
		
		String query = LOG_POST_NAME;
		JSONObject data = new JSONObject().
				put("nid", this.cid).
				put("query", query);
		
		Map<String, Object> resp = this.mySession.piazzaAPICall("network.search", data, piazzaLogic);
		List<Map<String, Object>> posts = (List<Map<String, Object>>) resp.get("result");
		
		for (Map<String, Object> post : posts) {
			
			String tags = String.join("|", (List<String>) post.get("folders"));
			if (post.get("subject").equals(query) && tags.contains("mediated_gpt") && tags.contains("automated_system")) {
				return (String) post.get("id");
			}

		}

		// if no log exists, create one
		return createLogPost();
		
	}
	
	
	public String getLogID() throws ClientProtocolException, NotLoggedInException, IOException {
		
		JSONObject dataObj = readJSONFile(DATAFILE_PATH);
		String logID;
		
		// if the key is present in the file, read it from there
		try {
			logID = dataObj.getString("logID");
		}
		
		// otherwise, search through piazza for the ID and write it to the log
		catch (JSONException e) {
			logID = searchForLogPostID();
			dataObj.put("logID", logID);
			writeJSONFile(DATAFILE_PATH, dataObj);
		}
	
		return logID;
		
	}
	
	
	public JSONArray getReadPosts() throws ClientProtocolException, NotLoggedInException, IOException {
		
		Map<String, Object> logPost = getPost(getLogID());
		// TODO: find a better way to split this markdown
		String logContent = getLatestContent(logPost).replace("&#34;", "\"");//.split(">")[1].split("<")[0];
		
		JSONObject logObj = new JSONObject(logContent);
		JSONArray logArr = logObj.getJSONArray("ProcessedPosts");
		
		return logArr;

	}
	
	
//	// DEPRECATED - INSTEAD, USE getAutomaticallyCreatedPost("mediatedGPTPromptID", PROMPT_POST_NAME, MEDIATED_GPT_PROMPT, "mediated_gpt")
//	public String getGptPrompt() throws ClientProtocolException, NotLoggedInException, IOException {
//		
//		JSONObject dataObj = readJSONFile(DATAFILE_PATH);
//		String mediatedGPTPromptID;
//		
//		// if the key is present in the file, read it from there
//		try {
//			mediatedGPTPromptID = dataObj.getString("mediatedGPTPromptID");
//			return getLatestContent(getPost(mediatedGPTPromptID));
//		}
//		
//		// otherwise, search through piazza for the ID and write it to the log
//		catch (JSONException e) {
//			
//			String query = PROMPT_POST_NAME;
//			JSONObject data = new JSONObject().
//					put("nid", this.cid).
//					put("query", query);
//			
//			Map<String, Object> resp = this.mySession.piazzaAPICall("network.search", data, piazzaLogic);
//			List<Map<String, Object>> posts = (List<Map<String, Object>>) resp.get("result");
//			
//			for (Map<String, Object> post : posts) {
//				
//				String tags = String.join("|", (List<String>) post.get("folders"));
//				if (post.get("subject").equals(query) && tags.contains("mediated_gpt") && tags.contains("automated_system")) {
//					mediatedGPTPromptID = (String) post.get("id");
//					dataObj.put("mediatedGPTPromptID", mediatedGPTPromptID);
//					writeJSONFile(DATAFILE_PATH, dataObj);
//					return getLatestContent(getPost(mediatedGPTPromptID));				
//				}
//
//			}
//			
//			// if no prompt post exists, create one		
//			List<String> newTags = new ArrayList();
//			newTags.add("mediated_gpt");
//			newTags.add("automated_system");
//			List<String> newRecipients = new ArrayList(); // createPost will always include instructors as recipients
//			mediatedGPTPromptID = createPost(PROMPT_POST_NAME, MEDIATED_GPT_PROMPT, newTags, newRecipients, "individual");
//			dataObj.put("mediatedGPTPromptID", mediatedGPTPromptID);
//			writeJSONFile(DATAFILE_PATH, dataObj);
//			return getLatestContent(getPost(mediatedGPTPromptID));	
//			
//		}
//		
//	}
//	
//	
//	// DEPRECATED - INSTEAD, USE getAutomaticallyCreatedPost("ohCheckerPromptID", OH_PROMPT_POST_NAME, OH_GPT_PROMPT, "office_hours")
//	public String getOHPrompt() throws ClientProtocolException, NotLoggedInException, IOException {
//		
//		JSONObject dataObj = readJSONFile(DATAFILE_PATH);
//		String ohCheckerPromptID;
//		
//		// if the key is present in the file, read it from there
//		try {
//			ohCheckerPromptID = dataObj.getString("ohCheckerPromptID");
//			return getLatestContent(getPost(ohCheckerPromptID));
//		}
//		
//		// otherwise, search through piazza for the ID and write it to the log
//		catch (JSONException e) {
//			
//			String query = OH_PROMPT_POST_NAME;
//			JSONObject data = new JSONObject().
//					put("nid", this.cid).
//					put("query", query);
//			
//			Map<String, Object> resp = this.mySession.piazzaAPICall("network.search", data, piazzaLogic);
//			List<Map<String, Object>> posts = (List<Map<String, Object>>) resp.get("result");
//			
//			for (Map<String, Object> post : posts) {
//				
//				String tags = String.join("|", (List<String>) post.get("folders"));
//				if (post.get("subject").equals(query) && tags.contains("office_hours") && tags.contains("automated_system")) {
//					ohCheckerPromptID = (String) post.get("id");
//					dataObj.put("ohCheckerPromptID", ohCheckerPromptID);
//					writeJSONFile(DATAFILE_PATH, dataObj);
//					return getLatestContent(getPost(ohCheckerPromptID));				
//				}
//
//			}
//			
//			// if no prompt post exists, create one
//			System.out.println("CREATED NEW OH PROMPT POST");
//			List<String> newTags = new ArrayList();
//			newTags.add("office_hours");
//			newTags.add("automated_system");
//			List<String> newRecipients = new ArrayList(); // createPost will always include instructors as recipients
//			ohCheckerPromptID = createPost(OH_PROMPT_POST_NAME, OH_GPT_PROMPT, newTags, newRecipients, "individual");
//			dataObj.put("ohCheckerPromptID", ohCheckerPromptID);
//			writeJSONFile(DATAFILE_PATH, dataObj);
//			return getLatestContent(getPost(ohCheckerPromptID));	
//			
//		}
//		
//	}
	
	
	public String getAutomaticallyCreatedPost(String dataObjKey, String postName, String defaultText, String categoryTag) throws ClientProtocolException, NotLoggedInException, IOException {
		
		JSONObject dataObj = readJSONFile(DATAFILE_PATH);
		String promptID;
		
		// if the key is present in the file, read it from there
		try {
			promptID = dataObj.getString(dataObjKey);
			System.out.println(promptID);
			System.out.println(getPost(promptID));
			return getLatestContent(getPost(promptID));
		}
		
		// otherwise, search through piazza for the ID and write it to the log
		catch (JSONException e) {
			
			System.out.println("IN CATCH");
			
			String query = postName;
			JSONObject data = new JSONObject().
					put("nid", this.cid).
					put("query", query);
			
			Map<String, Object> resp = this.mySession.piazzaAPICall("network.search", data, piazzaLogic);
			List<Map<String, Object>> posts = (List<Map<String, Object>>) resp.get("result");
			
			for (Map<String, Object> post : posts) {
				
				System.out.println(post);
				
				String tags = String.join("|", (List<String>) post.get("folders"));
				if (post.get("subject").equals(query) && tags.contains(categoryTag) && tags.contains("automated_system")) {
					promptID = (String) post.get("id");
					dataObj.put(dataObjKey, promptID);
					writeJSONFile(DATAFILE_PATH, dataObj);
					return getLatestContent(getPost(promptID));				
				}

			}
			
			// if no prompt post exists, create one
			System.out.println("AUTOMATICALLY CREATED POST: " + postName);
			List<String> newTags = new ArrayList();
			newTags.add(categoryTag);
			newTags.add("automated_system");
			List<String> newRecipients = new ArrayList(); // createPost will always include instructors as recipients
			promptID = createPost(postName, defaultText, newTags, newRecipients, "individual");
			dataObj.put(dataObjKey, promptID);
			writeJSONFile(DATAFILE_PATH, dataObj);
			return getLatestContent(getPost(promptID));	
			
		}
		
	}
	
	
	public String fetchAssignmentWriteupFromTag(String assignmentTag) throws ClientProtocolException, NotLoggedInException, IOException {
		
		JSONObject dataObj = readJSONFile(DATAFILE_PATH);
		String assignmentIDKey = assignmentTag + "ID";
		String assignmentIDVal;
		
		// if the key is present in the file, read it from there
		try {
			assignmentIDVal = dataObj.getString(assignmentIDKey);
			return assignmentIDVal;
		}
		
		// otherwise, search through piazza for the ID and write it to the log
		catch (JSONException e) {
			
			String query = assignmentTag;
			JSONObject data = new JSONObject().
					put("nid", this.cid).
					put("query", query);
			
			Map<String, Object> resp = this.mySession.piazzaAPICall("network.search", data, piazzaLogic);
			List<Map<String, Object>> posts = (List<Map<String, Object>>) resp.get("result");
			
			System.out.println("assignment writeup search posts:");
			System.out.println(posts);
			System.out.println();
			
			for (Map<String, Object> post : posts) {
				
				String tags = String.join("|", (List<String>) post.get("folders"));
				if (tags.contains(assignmentTag) && tags.contains("assignment_instructions")) {
					assignmentIDVal = (String) post.get("id");
					dataObj.put(assignmentIDKey, assignmentIDVal);
					writeJSONFile(DATAFILE_PATH, dataObj);
					return assignmentIDVal;
				}

			}
		
			// TODO: actually throw an error here
			System.out.println("Unable to find assignment writeup for this assignment tag.");
			return null;
		
		}
		
	}
	
	
	public String getAssignmentWriteup(Map<String, Object> studentPost) throws ClientProtocolException, NotLoggedInException, IOException {
		
		// TODO: integrate scraping from google docs?
		
		String tags = String.join("|", (List<String>) studentPost.get("folders"));
		
		System.out.println("TAGS IN getAssignmentWriteup");
		
		for (String assignmentTag : ASSIGNMENT_TAGS) {
			
			
			System.out.println(assignmentTag);
			
			
			if (tags.contains(assignmentTag)) {
				
				String postID = fetchAssignmentWriteupFromTag(assignmentTag);
				Map<String, Object> assignmentPost = getPost(postID);
				return getLatestContent(assignmentPost);
				
			}
		}

		// TODO: actually throw an error here
		System.out.println("Post does not contain a valid assignment tag.");
		return null;

	}
	
	
	public boolean resetLog() throws ClientProtocolException, NotLoggedInException, IOException {
		
		String logID = getLogID();
		Map<String, Object> log = getPost(logID);
		Map<String, Object> currLog = getLatestElement(log);
		String subject = (String) currLog.get("subject");
		
		String newContent = "{\"ProcessedPosts\":[]}";
		int revision = (int) log.get("history_size");
		
		return updatePost(logID, subject, newContent, revision);
		
	}
	
	
	// NOTE: currently this returns the number of posts that MediatedGPT was run on this iteration
	//		 even though this code also now does private/public checking, that is ignored for this return number
	public int processNewPosts(boolean resetLog) throws IOException, NotLoggedInException {
		
		if (resetLog) {
			resetLog();
		}
		
		int numNewPosts = 0;
		//boolean successFlag = true;
		AGPTClass gpt = getGPT();

		// retrieve list of posts that have been processed already
		JSONArray readPosts = getReadPosts();

		// go through all posts to find unread ones		
		JSONObject allPosts = getAllPostsRecursive(0, Integer.MAX_VALUE, 0, Long.MAX_VALUE);
		Iterator keyIter = allPosts.keys();
		
		while (keyIter.hasNext()) {
			
			String key = (String) keyIter.next();
			JSONObject val = allPosts.getJSONObject(key);

			//boolean helpNeeded = val.getString("fol").contains("mediated_gpt") && Arrays.stream(ASSIGNMENT_TAGS).anyMatch(val.getString("fol")::contains);
			boolean helpNeeded = Arrays.stream(ASSIGNMENT_TAGS).anyMatch(val.getString("fol")::contains) && val.getString("type").equals("question");
			
//			System.out.println("VAL");
//			System.out.println(val);		
			
			// TODO: make this more efficient
			boolean unread = true;
			for (int i = 0; i < readPosts.length(); i++) {
				JSONObject rp = readPosts.getJSONObject(i);
				if ((rp.get("post_number").equals("@" + val.get("nr"))) && (rp.get("post_id").equals(val.getString("id")))) {
					unread = false;
					break;
				}
			}
			
			// if we have not processed this post before, do so
			if (unread) {
			
				System.out.println("IF UNREAD");
				
				// perform private/public checking
				Map<String, Object> studentPost = getPost(val.getString("id"));
				String studentQuestion = getLatestContent(studentPost); // TODO: I should probably also be including the title in analysis (still not done)
				String studentQuestionTitle = (String) getLatestElement(studentPost).get("subject");
				String studentPostID = (String) studentPost.get("id");
				ArrayList changes = (ArrayList) studentPost.get("change_log");
				//\Map<String, Object> lastChange = (Map<String, Object>) changes.get(changes.size() - 1);
				Map<String, Object> lastChange = (Map<String, Object>) changes.get(0);
				boolean containsPrivateTags = Arrays.stream(PRIVATE_TAGS).anyMatch(val.getString("fol")::contains);
				
				ArrayList tags = (ArrayList) studentPost.get("tags");
//				System.out.println("TAGS");
//				System.out.println(tags);
//				System.out.println(tags.get(1));
//				System.out.println(tags.contains("instructor-note"));
//				
//				System.out.println();
				System.out.println();
//				System.out.println("studentPost");
//				//System.out.println(studentPost);
				System.out.println("studentQuestionTitle and studentQuestion");
				System.out.println(studentQuestionTitle);
				System.out.println(studentQuestion);
				System.out.println("HELP NEEDED? " + helpNeeded);
//				System.out.println(studentQuestionTitle.contains("Your office hour request made on"));
				System.out.println();
//				System.out.println();
				
				System.out.println("BEFORE automatedSuggestionDisclaimer");
				String automatedSuggestionDisclaimer = getAutomaticallyCreatedPost("automatedSuggestionDisclaimerID", AUTOMATED_DISCLAIMER_POST_NAME, AUTOMATED_SUGGESTION_DISCLAIMER, "other_tools");
				System.out.println("AFTER automatedSuggestionDisclaimer");
				
				// if post was made by a student, run visibility checker
				// TODO: should MediatedGPT also use this check?
				if (!tags.contains("instructor-note")) {
					
					
					if (lastChange.get("v").equals("private")) { // && !studentQuestionTitle.contains("Your office hour request made on")) {
						
						System.out.println("IF PRIVATE");
						
						// if post is only visible to some instructors, create a response
						List<String> postedTo = Arrays.asList(((String) val.get("feed_groups")).split(","));
						boolean includesAllInstructors = postedTo.contains("instr_" + this.cid) || postedTo.containsAll(getInstructorIDs());
						
						
						System.out.println(includesAllInstructors);
						System.out.println(containsPrivateTags);
						
						if (!includesAllInstructors) {
							String suggestedAllInstructors = getAutomaticallyCreatedPost("suggestedAllInstructorsID", SUGGESTED_INSTRUCTORS_POST_NAME, SUGGESTED_ALL_INSTRUCTORS, "other_tools");
							createFollowup(studentPostID, suggestedAllInstructors + automatedSuggestionDisclaimer);
						}
						
						// if post is private and should be public, create a response
						if (!containsPrivateTags) {
							System.out.println("we're here: " + studentPost.get("nr"));
							String suggestedPublic = getAutomaticallyCreatedPost("suggestedPublicMessageID", SUGGESTED_PUBLIC_POST_NAME, SUGGESTED_PUBLIC_MESSAGE, "other_tools");
							createFollowup(studentPostID, suggestedPublic + automatedSuggestionDisclaimer);
						}
						
					}
					
					// if post is public and should be private, create a response
					else if (lastChange.get("v").equals("all") && containsPrivateTags) {
						String tagString = "<code>" + val.getString("fol").replace("|", ", ") + "</code>";
						String suggestedPrivate = getAutomaticallyCreatedPost("suggestedPrivateMessageID", SUGGESTED_PRIVATE_POST_NAME, SUGGESTED_PRIVATE_MESSAGE, "other_tools");
						String completePrivateMessage = suggestedPrivate.replace("[FOLDER_TAGS]", tagString);
						createFollowup(studentPostID, completePrivateMessage + automatedSuggestionDisclaimer);
					}
					
				}
				
				// if post is tagged needing help, run MediatedGPT
				if (helpNeeded && !tags.contains("instructor-note")) {
					
					System.out.println("IF HELP NEEDED");
					System.out.println("STUDENT QUESTION:");
					System.out.println(studentQuestion);
					
					numNewPosts++;

					// if student has uploaded a screenshot of their code, tell them to put it in text instead
					if (studentQuestion.contains("<img src=") || studentQuestion.contains("![")) {
						
//						System.out.println("\nstudentPost:");
//						System.out.println(studentPost);
						//System.out.println(studentPost.get("children"));
						List<Map<String, Object>> children = (List<Map<String, Object>>) studentPost.get("children");
						
						// TODO: if student responds / marks the followup as resolved, maybe send it to GPT anyways?
						String imageMessage = getAutomaticallyCreatedPost("screenshottedCodeMessageID", IMAGE_DETECTED_POST_NAME, IMAGE_MESSAGE, "other_tools");
						
						boolean alreadyReplied = false;
						for (Map<String, Object> child : children) {
							System.out.println("\nChild:");
							System.out.println(child);
							// if content of latest item from studentPost history contains imageMessage
							//if (((String) child.get("content")).contains(imageMessage)) {
							String childType = (String) child.get("type");
							if (childType.equals("i_answer") && getLatestContent(child).contains(imageMessage)) {
								System.out.println("Contains!");
								alreadyReplied = true;
							} else if (childType.equals("followup")) {
								String content = (String) child.get("content");
								if (content == null && ((String) child.get("subject")).contains(imageMessage)) {
									alreadyReplied = true;
									System.out.println("Contains!");
								}
								else if (content != null && content.contains(imageMessage)) {
									alreadyReplied = true;
									System.out.println("Contains!");
								}

							}				
						}
						
						if (!alreadyReplied) {
							createFollowup(studentPostID, imageMessage + automatedSuggestionDisclaimer);
						}
						
					}
					
//					else {
					
					
					
					
					
					// CHECK HERE, RESP of 57 instead of 55??
					// make check inb help_needed that it's a non-instructor question
					// is hitting "unable to find assignment writeup based on tag" (check line 920)
					
					
					
					
					
					
					
					
					
					// get prompt and assignment writeup
					String prompt = getAutomaticallyCreatedPost("mediatedGPTPromptID", PROMPT_POST_NAME, MEDIATED_GPT_PROMPT, "mediated_gpt");
					String assignmentInstructions = getAssignmentWriteup(studentPost);
				
					// send prompt to GPT
					String fullPrompt = prompt.replace("[ASSIGNMENT_INSTRUCTIONS]", assignmentInstructions).replace("[STUDENT_QUESTION]", studentQuestion);
					System.out.println("\nFULL PROMPT:");
					System.out.println(fullPrompt + "\n");
					String gptResponse = gpt.makeCall(fullPrompt);
					
					// TODO: change the way this works?
					gptResponse = gptResponse.replaceAll("\\\\n", "\n");
					
					// make private post
					String newSubject = "Instructor Thread: " + val.getString("subject");				
					String newContent = "The following question was submitted by " + getUserName(getAuthorId(studentPost)) + " (@" + studentPost.get("nr") + "):\n\n" + studentQuestion;
					List<String> newTags = new ArrayList();
					newTags.add("mediated_gpt");
					newTags.add("instructor_reference");
					// TODO: add assignment tags?
	//				String[] folderArr = val.getString("fol").split("\\|");
	//				for (String f : folderArr) {
	//					if (!f.contains("mediated_gpt")) {
	//						newTags.add(f.replace("?", "&#8725;")); // this slash is actually a UTF-8 char, I'll figure it out later
	//					}
	//				}
					
	//				System.out.println("studentQuestion:");
	//				System.out.println(studentQuestion);
	//				System.out.println();
	//				System.out.println("newContent:");
	//				System.out.println(newContent);
	//				System.out.println();
					
					
					List<String> newRecipients = new ArrayList();
					String newPostID = createPost(newSubject, newContent, newTags, newRecipients, "individual");
	//				System.out.println(newPostID);
					String newGptResponse = "The following response was generated by GPT:\n\n" + gptResponse;
					createReply(newPostID, newGptResponse);
	
					// make draft reply
					String draftReply = gptResponse + "\n\n---\n\n_This post was drafted automatically using GPT. The private instructor-only reference thread for this discussion is @" + getPost(newPostID).get("nr") + "_";
					//if (createDraftReply(studentPostID, draftReply) == false) {
					//	successFlag = false;
					//}
					System.out.println("ABOUT TO DRAFT A REPLY");
					createDraftAnswer(studentPostID, draftReply);

//					}
					
				}
				
				// add to log
				JSONObject newLog = new JSONObject();
				newLog.put("post_number", "@" + studentPost.get("nr"));
				newLog.put("post_id", studentPostID);
				readPosts.put(newLog);
				JSONObject logObj = new JSONObject();
				logObj.put("ProcessedPosts", readPosts);
				int revNumber = (int) getPost(getLogID()).get("history_size");
				//if (updatePost(getLogID(), "Mediated GPT Log", logObj.toString(), revNumber) == false) {
				//	successFlag = false;
				//}
				System.out.println("BEFORE UPDATE LOG");
				updatePost(getLogID(), LOG_POST_NAME, logObj.toString(), revNumber);
				System.out.println("AFTER UPDATE LOG");

				
			}
			
		}
		
		System.out.println("Done Processing!");
		//return successFlag;
		return numNewPosts;
	}
	
	
	public String fetchAssignmentInstructionsFromGoogleDoc(String docID) {
		
		// TODO
		
		return null;
	}
		
	
	
		
	/* Stuff for processing office hours: ----------------------------------------------------------- */
	
//	public String getIDFromNumber(String nr) throws ClientProtocolException, NotLoggedInException, IOException {
//		
//		JSONObject data = new JSONObject()
//				.put("cid", nr)
//				.put("nid", this.cid);
//		Map<String, Object> resp = this.mySession.piazzaAPICall("content.get", data, piazzaLogic);
//		@SuppressWarnings("unchecked")
//		Map<String, Object> post = (Map<String, Object>) this.getResults(resp);
//
//		return (String) post.get("id");
//	}
	
	
	public Map<String, Object> getPostFromNumber(String nr) throws ClientProtocolException, NotLoggedInException, IOException {
		
		JSONObject data = new JSONObject()
				.put("cid", nr)
				.put("nid", this.cid);
		Map<String, Object> resp = this.mySession.piazzaAPICall("content.get", data, piazzaLogic);
		@SuppressWarnings("unchecked")
		Map<String, Object> post = (Map<String, Object>) this.getResults(resp);

		@SuppressWarnings("unchecked")
		Map<String, String> change_log = ((List<Map<String, String>>) post.get("change_log")).get(0);
		if (change_log.get("type").equals("create"))
			map.put((String) post.get("cid"), change_log.get("uid"));
		
		return post;
		
	}
	
	
	public String createOfficeHoursRoot() throws ClientProtocolException, NotLoggedInException, IOException {
		
		String subject = "Signing up for Office Hours";
		String content = OFFICE_HOURS_INSTRUCTIONS;
		String[] tagsArr = {"logistics", "office_hours"};
		List<String> tags = Arrays.asList(tagsArr);
		
		return createPost(subject, content, tags, null, "");
		
	}
	
	
	public String getOfficeHoursRootID() throws ClientProtocolException, NotLoggedInException, IOException {
	
		String OHRoot;
		JSONObject dataObj = readJSONFile(DATAFILE_PATH);
		
		// if the key is present in the file, read it from there
		try {
			OHRoot = dataObj.getString("OHRoot");
			return OHRoot;
		}
		
		// otherwise, search through piazza for the ID and wTrite it to the log
		catch (JSONException e) {
			
//			// TODO: change from getAllPosts to a search
//			// maybe can't do bc of is_office_hour_root
//			String query = OH_SIGNUP_NAME;
//			JSONObject data = new JSONObject().
//					put("nid", this.cid).
//					put("query", query);
//			
//			Map<String, Object> resp = this.mySession.piazzaAPICall("network.search", data, piazzaLogic);
//			List<Map<String, Object>> posts = (List<Map<String, Object>>) resp.get("result");
//			
//			for (Map<String, Object> post : posts) {
//				
//			}
			
			
			JSONObject posts = getAllPostsRecursive(0, Integer.MAX_VALUE, 0, Long.MAX_VALUE);
			Iterator keyIter = posts.keys();
			
			while (keyIter.hasNext()) {
				String key = (String) keyIter.next();
				JSONObject val = posts.getJSONObject(key);
				if (val.getBoolean("is_office_hour_root")) {
					
					JSONArray roots = val.getJSONArray("children");
					if (roots.getJSONObject(1).getString("content").contains("assignment")) {
						OHRoot = roots.getJSONObject(1).getString("id");
						dataObj.put("OHRoot", OHRoot);
						writeJSONFile(DATAFILE_PATH, dataObj);
						return OHRoot;
					}
					OHRoot = roots.getJSONObject(0).getString("id");
					dataObj.put("OHRoot", OHRoot);
					writeJSONFile(DATAFILE_PATH, dataObj);
					return OHRoot;
				}
			}
			
			System.out.println("\nError: No office hours root found.");
			return null;
			
		}
	
	}
	
	
	// TODO: have a date that you're checking new ones since? or other criteria
	public ArrayList<Map<String, Object>> getOfficeHoursReqests() throws ClientProtocolException, NotLoggedInException, IOException {
	
		String rootID = getOfficeHoursRootID();
		ArrayList<Map<String, Object>> requests = (ArrayList) getPost(rootID).get("children");
		
		// TODO: for some reason, getPost returns the parent post even when given the child ID, so this is a workaround
		requests = (ArrayList<Map<String, Object>>) requests.get(0).get("children");
		
		return requests;
	
	}

	
//	public static void displayDirectory(File dir)
//    {
// 
//        try {
//            File[] files = dir.listFiles();
// 
//            // For-each loop for iteration
//            for (File file : files) {
// 
//                // Checking of file inside directory
//                if (file.isDirectory()) {
// 
//                    // Display directories inside directory
//                    System.out.println(
//                        "directory:"
//                        + file.getCanonicalPath());
//                    displayDirectory(file);
//                }
// 
//              // Simply get the path
//                else {
//                    System.out.println(
//                        "     file:"
//                        + file.getCanonicalPath());
//                }
//            }
//        }
// 
//        // if any exceptions occurs printStackTrace
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
	
	
	public int checkOfficeHourRequests() throws IOException, NotLoggedInException, ParseException {
		
//		File currentDir = new File(".");
//		displayDirectory(currentDir);
		
		
		ArrayList<Map<String, Object>> requests = getOfficeHoursReqests();
		int numNewRequests = 0;
		
		for (Map<String, Object> request : requests) {
			
			String requestText = (String) request.get("subject");
			String fullRequestText = requestText;
			String requestUserID = (String) request.get("uid");
			
//			System.out.println(fullRequestText);
			
			String incompleteMarkerBegin = "<sub>[incomplete, see @";
			String incompleteMarkerEnd = "]</sub>";
			String completeMarker = "<sub>[complete]</sub>";
			if (requestText.contains(incompleteMarkerBegin) || requestText.contains(completeMarker)) {
				continue;
			}
			numNewRequests++;
			
			// if the OH request references another post, use that post's text instead
			Pattern referencePattern = Pattern.compile("@\\d+");
			Matcher referenceMatcher = referencePattern.matcher(requestText);
			boolean referencesOtherPost = referenceMatcher.find();
			String feedbackNumber = null;
			String referencedID = null;
			String referencedVisibility = null;
			
			if (referencesOtherPost) {
				
				String referencedPostNumber = requestText.substring(referenceMatcher.start()+1, referenceMatcher.end());
				feedbackNumber = referencedPostNumber;
				Map<String, Object> referencedPost = getPostFromNumber(referencedPostNumber);
				Map<String, Object> referencedPostElement = getLatestElement(referencedPost);
				
				String referencedTitle = (String) referencedPostElement.get("subject");
				String referencedBody = (String) referencedPostElement.get("content");
				referencedID = (String) referencedPost.get("id");
				referencedVisibility = (String) referencedPost.get("status");

				System.out.println("\n\nREFERENCED POST\n\n");
				System.out.println(referencedPost);
				System.out.println("\n\nREFERENCED ELEMENT\n\n");
				System.out.println(referencedPostElement);
				System.out.println("\n\nEND REFERENCED POST\n\n");

				fullRequestText = requestText + "\n\n<em>The above office hours request references the following post:</em>\n\n<b>" + referencedTitle + "</b>\n" + referencedBody;
				
			}

			AGPTClass gpt = getGPT();
			String prompt = getAutomaticallyCreatedPost("ohCheckerPromptID", OH_PROMPT_POST_NAME, OH_GPT_PROMPT, "office_hours");
			String fullPrompt = prompt.replace("[OFFICE_HOURS_REQUEST]", fullRequestText);
			
			System.out.println("\nFULL PROMPT:");
			System.out.println(fullPrompt);
			
			String gptResponse = gpt.makeCall(fullPrompt);
			
			// TODO: change the way this works?
			gptResponse = gptResponse.replaceAll("\\\\n", "\n");
			
			//System.out.println("\nOH Request:");
			//System.out.println(fullRequestText);
			//System.out.println("\nGPT Response:");
			//System.out.println(gptResponse);
			
			// if the OH request is marked as sufficient by GPT, mark it as such
			if (gptResponse.equals("Complete")) {

				String requestID = (String) request.get("id");
				String markedText = "<p>" + requestText + "\n\n" + completeMarker + "<p>";
				updateFollowup(requestID, markedText);

			}
			
			// if the OH request is not marked as sufficient by GPT, mark it as such and create a private post with the student to give feedback
			else {
				
				String oldDateTimeFormat = "yyyy-MM-dd H:m";
				SimpleDateFormat sdf = new SimpleDateFormat(oldDateTimeFormat);
				String oldDateTime = ((String) request.get("created")).substring(0, 10) + " " + ((String) request.get("created")).substring(11, 16);
				
				String newDateTimeFormat = "MMMM d 'at' K:ma";
				Date newDateTimeObj = sdf.parse(oldDateTime);
				sdf.applyPattern(newDateTimeFormat);
				String newDateTime = sdf.format(newDateTimeObj);
				// TODO: when doing phase 2, make sure they keep the linked post number when changing to complete
				String newContent = "Hi " + getUserName(requestUserID) + ",\n\nYou posted the following office hours request on " + newDateTime + ":\n\n<blockquote>" + fullRequestText + "</blockquote>\n\nOur system marked this request as incomplete, and gave the following suggestion to fix it:\n\n<b>" + gptResponse + "</b>\n\nPlease edit your original request and add the missing information. When you make the edit, also remove the incomplete marker (which looks like " + incompleteMarkerBegin + "XX" + incompleteMarkerEnd + ") so that our system can reprocess the request.\n\nThis post is private, so feel free to reply with any code or further information that will be helpful for the office hours session. Thanks!\n\n<hr/>\n\n<em>This response was generated automatically using AI, and could be wrong. If you believe your request is actually complete and our system made an incorrect inference, please reply to this post with what it got wrong and change the incomplete marker to say " + completeMarker + " to mark your request as complete manually. Apologies for any inconveniences!</em>";

				// if request references another (private) post, give feedback in a followup
				if (referencesOtherPost && referencedVisibility.equals("private")) {
					
					System.out.println(referencedID);
					createReply(referencedID, newContent);
					
				}
				
				// otherwise, create a new private post to give feedback
				else {

					String newSubject = "Your office hour request made on " + newDateTime;
					List<String> newTags = new ArrayList();
					newTags.add("office_hours");
					List<String> newRecipients = new ArrayList();
					newRecipients.add(requestUserID);
					
					String newPostID = createPost(newSubject, newContent, newTags, newRecipients, "individual");
					feedbackNumber = String.valueOf(getPost(newPostID).get("nr")); // NOTE: if ever changes what createPost returns, don't need to make another call here					
					
				}
				
				// mark the OH request as incomplete
				String requestID = (String) request.get("id");
				String markedText = "<p>" + requestText + "\n\n" + incompleteMarkerBegin + feedbackNumber + incompleteMarkerEnd + "<p>";
				updateFollowup(requestID, markedText);
				
			}
			
		}
		
		System.out.println(numNewRequests);
		
		return numNewRequests;
	
	}
	
	
	
	// NOTE: before running, make sure to add the needed folders to Piazza
	// ONLY USE fullReset=true IF YOU'RE OKAY WITH IT OVERWRITING ALL OF THE LOGS AND TEMPLATE POSTS ON SUBSEQUENT RUNS
	public void setUpTool(String dataPostNumber, boolean fullReset) throws IOException, NotLoggedInException {
		
		Map<String, Object> dataPost = getLatestElement(getPostFromNumber(dataPostNumber));
		String dataPostID = (String) dataPost.get("id");
		String dataPostSubject = "Data for MediatedGPT Tool";
				
		// if we're not doing a full reset and it appears that the tool has already been set up, don't redo anything
		if (!fullReset && dataPostSubject.equals(dataPost.get("subject"))) {
			System.out.println("NOT RUNNING FULL SETUP AGAIN");
			return;
		}
		
		System.out.println("FULLRESET? " + fullReset);
		System.out.println(getLatestElement(dataPost));
		System.out.println(dataPostSubject);
		System.out.println("RUNNING FULL SETUP AGAIN");
		
		// set up tags and recipients for template posts
//		String[] automatedSystemTagsArr = {"automated_system"};
		List<String> automatedSystemTags;
		List<String> privatePostRecipients = new ArrayList();
		String privatePostType = "individual"; // if type == "individual", then it will add all instructors to the recipients list automatically
		
		// create template posts for MediatedGPT
		String mediatedGPTLogID = createLogPost();
		automatedSystemTags = Arrays.asList("automated_system", "mediated_gpt");
		String mediatedGPTPromptID = createPost(PROMPT_POST_NAME, MEDIATED_GPT_PROMPT, automatedSystemTags, privatePostRecipients, privatePostType);
		
		// create template posts for OHRequestChecker
		String officeHoursRootID = createOfficeHoursRoot();
		automatedSystemTags = Arrays.asList("automated_system", "office_hours");
		String officeHoursGPTPromptID = createPost(OH_PROMPT_POST_NAME, OH_GPT_PROMPT, automatedSystemTags, privatePostRecipients, privatePostType);
		
		// create template posts for private/public message checking
		automatedSystemTags = Arrays.asList("automated_system", "other_tools");
		String suggestedPublicMessageID = createPost(SUGGESTED_PUBLIC_POST_NAME, SUGGESTED_PUBLIC_MESSAGE, automatedSystemTags, privatePostRecipients, privatePostType);
		String suggestedPrivateMessageID = createPost(SUGGESTED_PRIVATE_POST_NAME, SUGGESTED_PRIVATE_MESSAGE, automatedSystemTags, privatePostRecipients, privatePostType);
		String suggestedAllInstructorsID = createPost(SUGGESTED_INSTRUCTORS_POST_NAME, SUGGESTED_ALL_INSTRUCTORS, automatedSystemTags, privatePostRecipients, privatePostType);
		String automatedSuggestionDisclaimerID = createPost(AUTOMATED_DISCLAIMER_POST_NAME, AUTOMATED_SUGGESTION_DISCLAIMER, automatedSystemTags, privatePostRecipients, privatePostType);
		String screenshottedCodeMessageID = createPost(IMAGE_DETECTED_POST_NAME, IMAGE_MESSAGE, automatedSystemTags, privatePostRecipients, privatePostType);

		
		// TODO: add lastRun
		
		
		// save the post IDs for all of the posts we just created
		JSONObject data = new JSONObject()
				.put("mediatedGPTLogID", mediatedGPTLogID)
				.put("mediatedGPTPromptID", mediatedGPTPromptID)
				.put("officeHoursRootID", officeHoursRootID)
				.put("officeHoursGPTPromptID", officeHoursGPTPromptID)
				.put("assignmentTags", ASSIGNMENT_TAGS)
				.put("privateTags", PRIVATE_TAGS)
				.put("suggestedPublicMessageID", suggestedPublicMessageID)
				.put("suggestedPrivateMessageID", suggestedPrivateMessageID)
				.put("suggestedAllInstructorsID", suggestedAllInstructorsID)
				.put("automaticSuggestionDisclaimerID", automatedSuggestionDisclaimerID)
				.put("screenshottedCodeMessageID", screenshottedCodeMessageID)
				.put("lastRun", "");

		
		// set post @dataPostNumber to a JSON file holding all the data for the tool
		writeJSONPost(dataPostNumber, data, dataPostSubject);
		return;
		
	}
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/* Old stuff: ----------------------------------------------------------- */
	
	// edit this post telling them to check private thread
	// demarkate instructor added text
	
	
	/*
	
	{
	  "method": "content.update",
	  "params": {
	    "cid": "lnm64j3kb2v236",
	    "type": "note",
	    "subject": "Private Instructor thread with Mason Laney",
	    "content": "<p>This thread can be used as a private channel for communication between you and course instructors.</p>\n<p></p>\n<p>---</p>\n<p></p>\n<p>Adding some text through an edit.</p>",
	    "anonymous": "no",
	    "editor": "rte",
	    "revision": 1,
	    "must_read": false,
	    "config": {},
	    "folders": [
	      "other"
	    ],
	    "visibility": "lljvnbpqdze3xm,instr_k9zvvl9ubao6xa"
	  }
	}
	
	 */
	
	
}

/*
	{
	  "method": "content.mark_resolved",
	  "params": {
	    "cid": "ln3n9af88yl4h6",
	    "resolved": false
	  }
	}
 */

