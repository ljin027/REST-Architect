package TM1Diagnostic;

import java.util.Objects;

public class Credential {

	public String username;
	public String password;
	public String namespace;
	public String securitymode;
	
	public Credential(String username, String password){
		this.username = username;
		this.password = password;
		this.namespace = "";
		this.securitymode = "BASIC";
	}
	
	public Credential(String username, String password, String namespace){
		this.username = username;
		this.password = password;
		this.namespace = namespace;
		this.securitymode = "CAM";
	}
	
	public String getsecuritymode(){
		return securitymode;
	}
	
	public String getusername(){
		return username;
	}
	
	public String getpassword(){
		return password;
	}
	
	public String getnamespace(){
		return namespace;
	}
	
	@Override
	public boolean equals(Object o) {
	    // self check
	    if (this == o)
	        return true;
	    // null check
	    if (o == null)
	        return false;
	    // type check and cast
	    if (getClass() != o.getClass())
	        return false;
	    Credential credential = (Credential) o;
	    // field comparison
	    return Objects.equals(username, credential.username) && Objects.equals(securitymode, credential.securitymode) && Objects.equals(namespace, credential.namespace);
	}
	
}
