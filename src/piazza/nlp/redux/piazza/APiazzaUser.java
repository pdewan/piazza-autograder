package piazza.nlp.redux.piazza;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class APiazzaUser implements PiazzaUser {
	
	protected Map<String, Object> userData;
	
	public APiazzaUser(Map<String, Object> userData) {
		this.userData = userData;
	}
	
	/* ForumUser METHODS */
	
	@Override
	public Map<String, Object> getAllData() {
		return this.userData;
	}
	
	@Override
	public String getID() {
		return (String) this.userData.get("id");
	}
	
	@Override
	public String getName() {
		return (String) this.userData.get("name");
	}
	
	@Override
	public List<String> getEmails() {
		return Arrays.asList(((String) this.userData.get("email")).split(", "));
	}
	
	@Override
	public String getRole() {
		return (String) this.userData.get("role");
	}

	@Override
	public boolean getAdmin() {
		return (boolean) this.userData.get("admin");
	}
	
	/* PiazzaUser METHODS */
	
	@Override
	public boolean getPublished() {
		try {
			return (boolean) this.userData.get("published");
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	@Override
	public int getAdminPermission() {
		return (int) this.userData.get("admin_permission");
	}
	
	@Override
	public Map<String, Object> getEndorser() {
		return (Map<String, Object>) this.userData.get("endorser");
	}
	
}

/*

	Examples of userData:

	{role=student, name=Example Student, endorser={}, admin=false, photo=null, id=jzk5vujhfp6pa, photo_url=null, published=true, email=mlaney@live.unc.edu, mlaney@email.unc.edu, masonmlaney@gmail.com, us=false, facebook_id=null}
	{role=ta, name=Mason Boyles, endorser={}, admin=true, photo=null, id=ky4w3gvue3fbc, photo_url=null, published=true, email=mboyles@unc.edu, masonwboyles@gmail.com, us=false, admin_permission=5, facebook_id=null}
	{role=ta, name=Mason Laney, endorser={}, admin=true, photo=null, id=lljvnbpqdze3xm, photo_url=null, email=mlaney@cs.unc.edu, us=false, admin_permission=5, facebook_id=null}
	{role=instructor, name=Yuvraj, endorser={}, admin=true, photo=null, id=kstfi2k46j36cl, photo_url=null, published=true, email=yjain@unc.edu, 4309chris@gmail.com, us=false, admin_permission=10, facebook_id=null}

 */
