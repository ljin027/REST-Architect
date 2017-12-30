package TM1Diagnostic.REST;

public class TM1Sandbox {
	
	public String name;
	public boolean isloaded;
	public boolean isactive;
	public boolean isqueued;
	
	public TM1Sandbox(String name, boolean isactive){
		this.name = name;
		this.isactive = isactive;
	}

}
