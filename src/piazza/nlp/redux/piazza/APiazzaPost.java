package piazza.nlp.redux.piazza;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import piazza.nlp.redux.exceptions.AnonymousDataAccessException;

public class APiazzaPost extends AnAbstractPiazzaPost implements PiazzaPost {
	
	public APiazzaPost(Map<String, Object> postData, String classID) {
		super(postData, classID);
	}
	
	
	
	/* ForumPost METHODS */

	@Override
	public String getSubject() {
		return (String) this.getLatestVersion().get("subject");
	}
	
	@Override
	public String getBody() {
		return (String) this.getLatestVersion().get("content");
	}
	
	@Override
	public String getAuthorID() throws AnonymousDataAccessException {
		String authorID = (String) this.getLatestVersion().get("uid");
		if (authorID == null) {
			throw new AnonymousDataAccessException("Attempting to access author of anonymous Piazza post @" + this.getPostNumber() + " in class " + this.getCourseID() + ".");
			// Note that if a student makes a post anonymously to instructors, and then edits it non-anonymously, the original author is still seen as anonymous
			// This matches the behavior of the Piazza UI, which shows the post as having been updated by two users (one anonymous and one not)
			// If we want to return that student's ID, it could potentially be done bc the only people who can edit posts not made by them are instructors
			// Thus, if an anonymous student makes a post which is edited by a non-anonymous student, I believe the non-anonymous student must be OP
			// This is an extreme edge case that I don't think we need to worry about, but I'm making note of it here in case it comes up
		}
		return authorID;
	}
	
	@Override
	public Date getDateCreated() {
		String createTime = (String) this.postData.get("created");
		Instant createInstant = Instant.parse(createTime);
		return Date.from(createInstant);
	}	

	@Override
	public Date getDateUpdated() {
		String updateTime = (String) this.getLatestVersion().get("created");
		Instant updateInstant = Instant.parse(updateTime);
		return Date.from(updateInstant);
	}
	
	
	
	/* PiazzaPost METHODS */

	@Override
	public int getRevisionNumber() {
		return (int) this.postData.get("history_size");
	}



	/* HELPER METHODS */
	
	protected List<Map<String, Object>> getVersionHistory() {
		// NOTE: this is in REVERSE chronological order, with the latest version of the post at index 0
		//System.out.println(this.postData);
		
		return (List<Map<String, Object>>) this.postData.get("history");
	}
		
	protected Map<String, Object> getLatestVersion() {
		List<Map<String, Object>> historyList = this.getVersionHistory();
    	Map<String, Object> latestElement = historyList.get(0);
    	return latestElement;
	}
	
}



/* EXAMPLES OF this.postData */

	// STUDENT_ANON NOTE:
	// {history_size=1, folders=[hw1], nr=300, data={embed_links=[]}, request_instructor=0, no_answer_followup=0, change_log=[{anon=stud, uid=jzk5vujhfp6pa, uid_a=a_0, data=ma20g9v9vgnf2, v=private, type=create, when=2025-04-29T04:32:05Z}], type=note, anon_map={jzk5vujhfp6pa=a_0}, my_favorite=false, is_tag_good=false, unique_views=1, children=[], is_bookmarked=false, i_edits=[], s_edits=[], id=ma20g9v2lmsf0, q_edits=[], created=2025-04-29T04:32:05Z, bucket_order=3, bookmarked=1, bucket_name=Today, request_instructor_me=false, drafts={}, history=[{anon=stud, uid=jzk5vujhfp6pa, uid_a=a_0, subject=Test partial anonymous note, created=2025-04-29T04:32:05Z, content=test body - posted as anonymous to classmates}], num_favorites=0, tags=[hw1, student], tag_good=[], t=1745901141751, default_anonymity=no, tag_good_arr=[], anon_icons=true, config={editor=rte, feed_groups=instr_m0mymncloco2ty,jzk5vujhfp6pa}, status=private}

	// FULL_ANON QUESTION:
	// {history_size=1, folders=[hw1], nr=297, data={embed_links=[]}, request_instructor=0, no_answer_followup=0, change_log=[{anon=full, uid_a=a_0, data=m9aeize78dl2wn, v=private, type=create, when=2025-04-09T20:48:33Z}], type=question, anon_map={}, my_favorite=false, is_tag_good=false, unique_views=1, children=[], is_bookmarked=false, i_edits=[], s_edits=[], id=m9aeizdyk4n2wm, q_edits=[], created=2025-04-09T20:48:33Z, bucket_order=3, bookmarked=1, bucket_name=Today, request_instructor_me=false, drafts={}, history=[{anon=full, uid_a=a_0, subject=Test Anonymous Question, created=2025-04-09T20:48:33Z, content=Can you help with HW1?}], num_favorites=0, tags=[hw1, student, unanswered], tag_good=[], t=1745901215222, default_anonymity=no, tag_good_arr=[], no_answer=1, anon_icons=true, config={editor=rte, feed_groups=instr_m0mymncloco2ty,anon_original}, status=private}

	// FULL_ANON QUESTION W/ RESPONSES:
	// {history_size=1, folders=[hw1], nr=297, data={embed_links=[]}, request_instructor=0, no_answer_followup=1, change_log=[{anon=full, uid_a=a_0, data=m9aeize78dl2wn, v=private, type=create, when=2025-04-09T20:48:33Z}, {anon=no, uid=lljvnbpqdze3xm, data=ma20khcf6cv2ad, to=m9aeizdyk4n2wm, type=i_answer, when=2025-04-29T04:35:21Z}, {anon=no, uid=lljvnbpqdze3xm, to=m9aeizdyk4n2wm, type=followup, when=2025-04-29T04:35:27Z, cid=ma20klt8c1v2gj}], type=question, anon_map={}, my_favorite=false, is_tag_good=false, unique_views=1, children=[{history_size=1, folders=[], data={embed_links=[]}, created=2025-04-29T04:35:21Z, bucket_order=3, tag_endorse=[], bucket_name=Today, history=[{anon=no, uid=lljvnbpqdze3xm, subject=, created=2025-04-29T04:35:21Z, content=Test instructor answer}], type=i_answer, tag_endorse_arr=[], children=[], is_tag_endorse=false, id=ma20khcb6rt2ac, config={editor=rte}}, {anon=no, folders=[], data=null, no_upvotes=0, subject=Test followup, created=2025-04-29T04:35:27Z, bucket_order=3, bucket_name=Today, type=followup, tag_good=[], uid=lljvnbpqdze3xm, children=[], tag_good_arr=[], no_answer=1, id=ma20klt8c1v2gj, updated=2025-04-29T04:35:27Z, config={editor=rte}}], is_bookmarked=true, i_edits=[], s_edits=[], id=m9aeizdyk4n2wm, q_edits=[], created=2025-04-09T20:48:33Z, bucket_order=3, bookmarked=2, bucket_name=Today, request_instructor_me=false, drafts={}, history=[{anon=full, uid_a=a_0, subject=Test Anonymous Question, created=2025-04-09T20:48:33Z, content=Can you help with HW1?}], num_favorites=0, tags=[hw1, student], tag_good=[], t=1745901358248, default_anonymity=no, tag_good_arr=[], no_answer=0, anon_icons=true, config={editor=rte, feed_groups=instr_m0mymncloco2ty,anon_original}, status=private}

	// FULL_ANON QUESTION W/ RESPONSES AND INSTRUCTOR EDIT:
	// {history_size=2, folders=[hw1], nr=297, data={embed_links=[]}, request_instructor=0, no_answer_followup=1, change_log=[{anon=full, uid_a=a_0, data=m9aeize78dl2wn, v=private, type=create, when=2025-04-09T20:48:33Z}, {anon=no, uid=lljvnbpqdze3xm, data=ma20khcf6cv2ad, to=m9aeizdyk4n2wm, type=i_answer, when=2025-04-29T04:35:21Z}, {anon=no, uid=lljvnbpqdze3xm, to=m9aeizdyk4n2wm, type=followup, when=2025-04-29T04:35:27Z, cid=ma20klt8c1v2gj}, {anon=no, uid=lljvnbpqdze3xm, data=ma3elj68hzr1l9, v=private, type=update, when=2025-04-30T03:55:51Z}], type=question, anon_map={}, my_favorite=false, is_tag_good=false, unique_views=1, children=[{history_size=1, folders=[], data={embed_links=[]}, created=2025-04-29T04:35:21Z, bucket_order=3, tag_endorse=[], bucket_name=Today, history=[{anon=no, uid=lljvnbpqdze3xm, subject=, created=2025-04-29T04:35:21Z, content=Test instructor answer}], type=i_answer, tag_endorse_arr=[], children=[], is_tag_endorse=false, id=ma20khcb6rt2ac, config={editor=rte}}, {anon=no, folders=[], data=null, no_upvotes=0, subject=Test followup, created=2025-04-29T04:35:27Z, bucket_order=4, bucket_name=Yesterday, type=followup, tag_good=[], uid=lljvnbpqdze3xm, children=[], tag_good_arr=[], no_answer=1, id=ma20klt8c1v2gj, d-bucket=Yesterday, updated=2025-04-29T04:35:27Z, config={editor=rte}}], is_bookmarked=true, i_edits=[], s_edits=[], id=m9aeizdyk4n2wm, q_edits=[], created=2025-04-09T20:48:33Z, bucket_order=3, bookmarked=2, bucket_name=Today, request_instructor_me=false, drafts={}, history=[{anon=no, uid=lljvnbpqdze3xm, subject=Test Anonymous Question, created=2025-04-30T03:55:51Z, content=<p>Can you help with HW1?</p><p></p><p>instructor edit</p>}, {anon=full, uid_a=a_0, subject=Test Anonymous Question, created=2025-04-09T20:48:33Z, content=Can you help with HW1?}], num_favorites=0, tags=[hw1, student], tag_good=[], t=1745985390755, default_anonymity=no, tag_good_arr=[], no_answer=0, anon_icons=true, config={editor=rte, feed_groups=instr_m0mymncloco2ty,anon_original,lljvnbpqdze3xm}, status=private}

