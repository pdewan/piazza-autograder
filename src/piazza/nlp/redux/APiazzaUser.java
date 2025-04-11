package piazza.nlp.redux;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class APiazzaUser implements ForumUser {
	
	protected boolean admin;
	protected boolean published;
	protected boolean us;
	protected int admin_permission;
	protected String role;
	protected String name;
	protected String id;
	protected String photo;
	protected String photo_url;
	protected String facebook_id;
	protected List<String> emails;
	protected Map<String, Object> endorser;
	
	
	public APiazzaUser(Map<String, Object> userInfo) {
		this.admin = (boolean) userInfo.get("admin");
		this.us = (boolean) userInfo.get("us");
		this.role = (String) userInfo.get("role");
		this.name = (String) userInfo.get("name");
		this.id = (String) userInfo.get("id");
		this.photo = (String) userInfo.get("photo");
		this.photo_url = (String) userInfo.get("photo_url");
		this.facebook_id = (String) userInfo.get("facebook_id");
		
		try {
			this.published = (boolean) userInfo.get("published");
		} catch (NullPointerException e) {
			this.published = false;
		}
		
		this.emails = Arrays.asList(((String) userInfo.get("email")).split(", "));
		this.endorser = (Map<String, Object>) userInfo.get("endorser");
	}
	
	public String getID() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public List<String> getEmails() {
		return this.emails;
	}
	
	public String getRole() {
		return this.role;
	}
	
	public boolean getAdmin() {
		return this.admin;
	}
	
}

/*
 * {role=student, name=Example Student, endorser={}, admin=false, photo=null, id=jzk5vujhfp6pa, photo_url=null, published=true, email=mlaney@live.unc.edu, mlaney@email.unc.edu, masonmlaney@gmail.com, us=false, facebook_id=null}
{role=ta, name=Mason Boyles, endorser={}, admin=true, photo=null, id=ky4w3gvue3fbc, photo_url=null, published=true, email=mboyles@unc.edu, masonwboyles@gmail.com, us=false, admin_permission=5, facebook_id=null}
{role=ta, name=Mason Laney, endorser={}, admin=true, photo=null, id=lljvnbpqdze3xm, photo_url=null, email=mlaney@cs.unc.edu, us=false, admin_permission=5, facebook_id=null}
{role=instructor, name=Yuvraj, endorser={}, admin=true, photo=null, id=kstfi2k46j36cl, photo_url=null, published=true, email=yjain@unc.edu, 4309chris@gmail.com, us=false, admin_permission=10, facebook_id=null}

 */
