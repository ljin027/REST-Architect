package TM1Diagnostic.REST;

public class TM1ServerStub {
	public String name;
	public String hostname;
	public int port;
	public boolean useSSL;
	public boolean isAuthenticated;
	public boolean isCloud;
	
	public TM1ServerStub(String name, String hostname, int port, boolean useSSL){
		this.name = name;
		this.hostname = hostname;
		this.port = port;
		this.useSSL = useSSL;
		isAuthenticated = false;
		isCloud = false;
	}

	public TM1ServerStub(String cloudname, String cloudhostname){
		this.name = cloudname;
		this.hostname = cloudhostname;
		this.port = 443;
		this.useSSL = true;
		isAuthenticated = false;
		isCloud = true;
	}

	
	public boolean isRestEnabled(){
		if (port != 0){
			return true;
		} 
		return false;
	}
	
	public void setAuthenticate(){
		isAuthenticated = true;
	}

	public void setUnAuthenticated(){
		isAuthenticated = false;
	}
}
