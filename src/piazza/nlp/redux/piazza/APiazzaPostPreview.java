package piazza.nlp.redux.piazza;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import piazza.nlp.redux.exceptions.AnonymousDataAccessException;

public class APiazzaPostPreview extends AnAbstractPiazzaPost implements PiazzaPost {
	
	public APiazzaPostPreview(Map<String, Object> postData, String classID) {
		super(postData, classID);
	}
	
	public APiazzaPostPreview(Map<String, Object> postData) {
		super(postData, (String) postData.get("nid"));
	}
	
	
	
	/* ForumPost METHODS */

	@Override
	public String getSubject() {
		return (String) this.postData.get("subject");
	}
	
	@Override
	public String getBody() {
		String previewDisclaimer = "THE FOLLOWING IS A SNIPPET OF POST @" + this.getPostNumber() + ". PLEASE CALL piazzaForum.getPostNumber(" + this.getPostNumber() + ") TO ACCESS THE FULL CONTENT.\n\n";
		return previewDisclaimer + this.postData.get("content_snipet");
		
	}

	@Override
	public String getAuthorID() throws AnonymousDataAccessException {
		String authorID = (String) this.getActivityLog().get(0).get("u");
		if (authorID == null) {
			throw new AnonymousDataAccessException("Attempting to access author of anonymous Piazza post preview @" + this.getPostNumber() + " in class " + this.getCourseID() + ".");
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
		// NOTE: it looks like this may be stored in the "updated" field, but that name is confusing so I'm doing it this way which is known to be correct
		List<Map<String, Object>> postActivity = this.getPostActivity();
		String createTime = (String) postActivity.get(0).get("t");
		Instant createInstant = Instant.parse(createTime);
		return Date.from(createInstant);
	}

	@Override
	public Date getDateUpdated() {
		// NOTE: it looks like this may be stored in the "modified" field, but that name is confusing so I'm doing it this way which is known to be correct
		List<Map<String, Object>> postActivity = this.getPostActivity();
		String updateTime = (String) postActivity.get(postActivity.size()-1).get("t");
		Instant updateInstant = Instant.parse(updateTime);
		return Date.from(updateInstant);
	}

	
	
	/* PiazzaPost METHODS */

	@Override
	public int getRevisionNumber() {
		// NOTE: this.postData.get("version") increments when responses are made, which is not what we want here
		// 	we can make a different method for that if we need
		return this.getPostActivity().size();
	}



	/* HELPER METHODS */
	
	protected List<Map<String, Object>> getActivityLog() {
		// NOTE: this is in chronological order, with the initial creation activity at index 0
		return (List<Map<String, Object>>) this.postData.get("log");
	}
	
	protected List<Map<String, Object>> getPostActivity() {
		List<Map<String, Object>> postActivity = new ArrayList<>();
		for (Map<String, Object> a : this.getActivityLog()) {
			String aType = (String) a.get("n");
			if (aType.equals("create") || aType.equals("update")) {
				postActivity.add(a);
			}
		}
		return postActivity;
	}
	
	protected List<Map<String, Object>> getResponseActivity() {
		List<Map<String, Object>> respActivity = new ArrayList<>();
		for (Map<String, Object> a : this.getActivityLog()) {
			String aType = (String) a.get("n");
			if (aType.equals("followup") || aType.equals("i_answer") || aType.equals("s_answer")) {
				respActivity.add(a);
			}
		}
		return respActivity;
	}
	
}

/* EXAMPLES OF this.postData */



	/*
	 * 
	 * {fol=logistics|office_hours|, folders=[logistics, office_hours], nr=281, log=[{t=2025-02-13T02:21:37Z, u=ky4w3gvue3fbc, n=create}, {t=2025-03-26T20:22:45Z, u=lljvnbpqdze3xm, n=update}], main_version=2, request_instructor=0, subject=Signing up for Office Hours, book=1, no_answer_followup=0, nid=m0mymncloco2ty, type=note, score=2.0, pin=1, unique_views=2, content_snipet=Here are the times and guidelines for office hours.

Please let us know at least one hour before the start of each offic, modified=2025-03-26T20:22:45Z, id=m72prlqjhxgj5, is_new=false, bucket_order=0, bucket_name=Pinned, num_favorites=0, m=1743020565712, version=2, tags=[instructor-note, logistics, office_hours, pin], gd_f=0, view_adjust=0, updated=2025-02-13T02:21:37Z, rq=0, status=active}
	 * 
	 */
	
	/*
	 * 
	 * {fol=logistics|office_hours|, folders=[logistics, office_hours], nr=281, log=[{t=2025-02-13T02:21:37Z, u=ky4w3gvue3fbc, n=create}, {t=2025-03-26T20:22:45Z, u=lljvnbpqdze3xm, n=update}, {t=2025-04-30T04:06:49Z, u=lljvnbpqdze3xm, n=update}, {t=2025-04-30T04:06:55Z, u=lljvnbpqdze3xm, n=update}, {t=2025-04-30T04:08:21Z, u=lljvnbpqdze3xm, n=followup}], main_version=5, request_instructor=0, subject=Signing up for Office Hours, book=1, no_answer_followup=0, nid=m0mymncloco2ty, type=note, score=2.0, pin=1, unique_views=2, content_snipet=Here are the times and guidelines for office hours.

Please let us know at least one hour before the start of each offic, modified=2025-04-30T04:08:21Z, id=m72prlqjhxgj5, is_new=false, bucket_order=0, bucket_name=Pinned, num_favorites=0, m=1745986101362, version=5, tags=[instructor-note, logistics, office_hours, pin], gd_f=0, view_adjust=0, updated=2025-02-13T02:21:37Z, rq=0, status=active}

	 * 
	 */
	
	/* PRIVATE NOTE
	 * 
	 * fol=test_folder|, folders=[test_folder], nr=299, log=[{t=2025-04-11T21:57:39Z, u=kstfi2k46j36cl, n=create}], main_version=1, request_instructor=0, subject=Mason Laney&#39;s grades for Test 1, no_answer_followup=0, nid=m0mymncloco2ty, type=note, score=1.0, unique_views=1, content_snipet=Dear Mason Laney,

The automated grade generated for your response is as follows:

Grade: 60
Reason: Oversimplified resp, modified=2025-04-11T21:57:39Z, id=m9dbvk2wf0r7at, is_new=false, bucket_order=10, bucket_name=Week 4/6 - 4/12, feed_groups=lljvnbpqdze3xm,instr_m0mymncloco2ty, num_favorites=0, m=1744408659560, version=1, tags=[instructor-note, test_folder], gd_f=0, view_adjust=0, updated=2025-04-11T21:57:39Z, rq=0, status=private}
	 * 
	 */
	
	/* PRIVATE ANSWERED QUESTION
	 * 
	 * {fol=hw1|, folders=[hw1], nr=297, log=[{t=2025-04-09T20:48:33Z, n=create}, {t=2025-04-29T04:35:21Z, u=lljvnbpqdze3xm, n=i_answer}, {t=2025-04-29T04:35:27Z, u=lljvnbpqdze3xm, n=followup}, {t=2025-04-30T03:55:51Z, u=lljvnbpqdze3xm, n=update}], main_version=4, request_instructor=0, subject=Test Anonymous Question, book=1, no_answer_followup=1, nid=m0mymncloco2ty, type=question, score=1.0, unique_views=1, content_snipet=Can you help with HW1?

instructor edit, modified=2025-04-30T03:55:51Z, id=m9aeizdyk4n2wm, has_i=true, is_new=false, bucket_order=10, bucket_name=Week 4/6 - 4/12, feed_groups=instr_m0mymncloco2ty,jzk5vujhfp6pa,lljvnbpqdze3xm, num_favorites=0, m=1745985351252, version=4, tags=[hw1, student], gd_f=0, view_adjust=0, no_answer=0, updated=2025-04-09T20:48:33Z, rq=0, status=private}

	 * 
	 */
	
	/* PRIVATE UNANSWERED QUESTION
	 * 
	 * {fol=hw5|includes_code|, folders=[hw5, includes_code], nr=290, log=[{t=2025-02-15T00:09:58Z, u=jzk5vujhfp6pa, n=create}, {t=2025-02-15T00:13:47Z, u=lljvnbpqdze3xm, n=followup}, {t=2025-02-15T00:13:55Z, u=lljvnbpqdze3xm, n=followup}, {t=2025-02-15T00:14:08Z, u=lljvnbpqdze3xm, n=followup}, {t=2025-02-15T00:54:52Z, u=jzk5vujhfp6pa, n=update}], main_version=5, request_instructor=0, subject=Null Pointer Exceptions with toString methods, book=1, no_answer_followup=0, nid=m0mymncloco2ty, type=question, score=1.0, unique_views=1, content_snipet=I am running into a lot of Null Pointer Exceptions with the toString() methods.&nbsp; I have tried to debug using the de, modified=2025-02-15T00:54:52Z, id=m75fy0l4mrn4o3, is_new=false, bucket_order=18, bucket_name=Week 2/9 - 2/15, feed_groups=jzk5vujhfp6pa,instr_m0mymncloco2ty, num_favorites=0, m=1739580892591, version=5, tags=[hw5, includes_code, student, unanswered], gd_f=0, view_adjust=0, no_answer=1, updated=2025-02-15T00:09:58Z, rq=0, status=private}
	 * 
	 */
	
	/* CHANGING ANONYMITY
	 * 
	 * {fol=hw1|, folders=[hw1], nr=301, log=[{t=2025-05-06T23:24:34Z, n=create}, {t=2025-05-06T23:24:52Z, u=jzk5vujhfp6pa, n=update}, {t=2025-05-06T23:25:11Z, u=jzk5vujhfp6pa, n=update}], main_version=3, request_instructor=0, subject=Changing anonymity test, no_answer_followup=0, nid=m0mymncloco2ty, type=question, score=1.0, unique_views=1, content_snipet=This post was made anonymously to instructors

This first edit was made anonymously to classmates

This second edit was , modified=2025-05-06T23:25:11Z, id=mad4zmeiiau5hr, is_new=false, bucket_order=3, bucket_name=Today, num_favorites=0, m=1746573911748, version=3, tags=[hw1, student, unanswered], gd_f=0, view_adjust=0, no_answer=1, updated=2025-05-06T23:24:34Z, rq=0, status=active}

	 * 
	 */
	
