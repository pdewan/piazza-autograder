package piazza.nlp.redux.piazza;

import java.util.List;

import piazza.PiazzaSession;
import piazza.nlp.redux.general.DiscussionForum;
import piazza.nlp.redux.general.ForumPost;
import piazza.nlp.redux.general.ForumUser;
import piazza.nlp.redux.general.DiscussionForum.EditorType;

public interface PiazzaForum extends DiscussionForum {
	
	public List<ForumUser> getUsers(String[] userIDs); // get user info for a batch of users at once
	public ForumPost getPost(int postNumber); // get post from number, instead of ID
	public List<APiazzaPostPreview> getFeed(); // get post headers from the feed
	public String createFollowupReply(String followupID, String body, EditorType editor); // create a reply to a followup -- potentially unify with createComment of Ed Discussion?

	// TODO: other Piazza-specific methods

	public PiazzaSession getForumSession(); // TODO: make ForumSession interface and put this in DiscussionForum?
	public PiazzaSession swapForumSession(PiazzaSession newSession); // returns the current session
	
}
