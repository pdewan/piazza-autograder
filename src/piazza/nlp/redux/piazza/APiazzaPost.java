package piazza.nlp.redux.piazza;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class APiazzaPost implements PiazzaPost {

	protected Map<String, Object> postData;
	
	public APiazzaPost(Map<String, Object> postData) {
		this.postData = postData;
	}
	
	
	/
	/* ForumPost METHODS */
	
	@Override
	public Map<String, Object> getAllData() {
		return this.postData;
	}
	
	@Override
	public String getPostID() {
		return (String) this.getAllData().get("id");
	}
	
	@Override
	public String getCourseID() {
				
		// TODO
		return null;
	
	}
	
	@Override
	public String getSubject() {
		
		// TODO
		//return this.postData.get("");
		return null;
		
	}
	
	@Override
	public String getBody() {
		
		// TODO
		return null;
		
	}
	
	@Override
	public String getType() {
		
		// TODO
		return null;
		
	}
	
	@Override
	public Date getDateCreated() {
		
		// TODO
		return null;
		
	}
	
	@Override
	public Date getDateUpdated() {
		
		// TODO
		return null;
		
	}
	
	// TODO: "folders" will give just poster-assigned tags without tags like "instructor-note" and "pin"
	@Override
	public List<String> getTags() {
	
		return (List<String>) this.postData.get("tags");
	
	}
	
	@Override
	public String getURL() {
		return "https://piazza.com/class/" + this.getCourseID() + "/post/" + this.getPostNumber();
	}
	
	@Override
	public String getAuthorID() {
	
		// TODO
		return null;
		
	}


	@Override
	public String getVisibility() {

		// TODO
		return null;
		
	}
	
	
	
	/* PiazzaPost METHODS */
	
	@Override
	public int getPostNumber() {
		
		// TODO
		return null;
		
	}
	
	@Override
	public int getRevisionNumber() {
		
		// TODO: is this the same as history size? or does that factor in replies and such
		return null;
		
	}



	
	
	
	/*
    protected String getLatestContent(Map<String, Object> item) {
    	
    	Map<String, Object> latestElement = getLatestElement(item);
    	return (String) latestElement.get("content");
	
    }
    
    protected Map<String, Object> getLatestElement(Map<String, Object> item) {
    	
    	List<Map<String, Object>> historyList = (List<Map<String, Object>>) item.get("history");
    	Map<String, Object> latestElement = historyList.get(0);
    	return latestElement;

    } */
	
}

/*
 * 
 * 
 */
//{history_size=2, folders=[logistics, office_hours], nr=281, data={embed_links=[]}, request_instructor=0, no_answer_followup=0, change_log=[{anon=no, uid=ky4w3gvue3fbc, data=m72prlqpp8rj8, v=all, type=create, when=2025-02-13T02:21:37Z}, {anon=no, uid=lljvnbpqdze3xm, data=m8qdfw0c5815ly, v=all, type=update, when=2025-03-26T20:22:45Z}], type=note, anon_map={}, my_favorite=false, is_tag_good=false, unique_views=2, children=[], is_bookmarked=true, i_edits=[], s_edits=[], id=m72prlqjhxgj5, q_edits=[], created=2025-02-13T02:21:37Z, bucket_order=0, bookmarked=2, bucket_name=Pinned, request_instructor_me=false, drafts={}, history=[{anon=no, uid=lljvnbpqdze3xm, subject=Signing up for Office Hours, created=2025-03-26T20:22:45Z, content=<p>Here are the times and guidelines for office hours.</p>
//<p></p>
//<p>Please let us know <strong>at least one hour</strong> before the start of each office hour period if you will join us during the period, otherwise we may not start the Zoom session or terminate the Zoom session before the end of the period. Also let us know when during the period you plan on joining.</p>
//<p></p>
//<p>Example incomplete post:</p>
//<p></p>
//<p><strong><em>9/1 at 11:15</em></strong></p>
//<p></p>
//<p>Does not give the reason for the visit.</p>
//<p></p>
//<p>Example complete post:</p>
//<p></p>
//<p><strong><em>11/17 at 2:20pm</em></strong></p>
//<p></p>
//<p><strong><em>Help with setting up IndentifierAtom</em></strong></p>
//<p></p>
//<p>Gives the time and reason</p>
//<p></p>
//<p>Schedule a meeting by publicly commenting on the appropriate instructor discussion in this thread.</p>
//<p></p>
//<p>In your comment reference the related Piazza post if it exists or describes the problem publicly if possible and say private question if not possible. <strong>Give as much detail as possible. Saying you have a problem with homework 1 or with localchecks is not specific; give the exact problem.</strong></p>
//<p></p>
//<p>If your problem requires a screenshot or your stack trace it should be its own post. Please reference that post as part of your help request but do not include screenshots or stack traces in your help request post. Give a text trace when possible and screenshots only for GUIs.</p>
//<p></p>
//<p><strong>Times:</strong></p>
//<p></p>
//<p>(These times will be subject to change while the semester progresses, but will be updated days in advance)</p>
//<p></p>
//<p>[INSERT OFFICE HOURS DAYS/TIMES/LOCATIONS]</p>
//<p></p>
//<p>#pin </p>
//<p></p>}, {anon=no, uid=ky4w3gvue3fbc, subject=Signing up for Office Hours, created=2025-02-13T02:21:37Z, content=<md>
//Here are the times and guidelines for office hours. 
//
//Please let us know **at least one hour** before the start of each office hour period if you will join us during the period, otherwise we may not start the Zoom session or terminate the Zoom session before the end of the period. Also let us know when during the period you plan on joining. 
//
//Example incomplete post:
//
//**_9/1 at 11:15_**
//
//Does not give the reason for the visit.
//
//Example complete post: 
//
//**_11/17 at 2:20pm_**
//
//**_Help with setting up IndentifierAtom_**
//
//Gives the time and reason
//
//Schedule a meeting by publicly commenting on the appropriate instructor discussion in this thread.
//
//In your comment reference the related Piazza post if it exists or describes the problem publicly if possible and say private question if not possible. **Give as much detail as possible. Saying you have a problem with homework 1 or with localchecks is not specific; give the exact problem.**
//
//If your problem requires a screenshot or your stack trace it should be its own post. Please reference that post as part of your help request but do not include screenshots or stack traces in your help request post. Give a text trace when possible and screenshots only for GUIs.
//
//**Times:**
//
//(These times will be subject to change while the semester progresses, but will be updated days in advance)
//
//[INSERT OFFICE HOURS DAYS/TIMES/LOCATIONS]
//
//#pin</md>}], num_favorites=0, tags=[instructor-note, logistics, office_hours, pin], tag_good=[], t=1744228707800, default_anonymity=no, tag_good_arr=[], anon_icons=true, config={editor=rte, has_emails_sent=1}, status=active}
