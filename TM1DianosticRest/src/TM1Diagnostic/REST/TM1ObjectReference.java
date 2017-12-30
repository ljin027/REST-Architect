package TM1Diagnostic.REST;

public class TM1ObjectReference extends TM1Object{
	
	private String id;
	public int referenceType;
	
	public TM1ObjectReference(String name, TM1Server tm1server, String id, int referenceType){
		super(name, TM1Object.REFERENCE, tm1server);
		this.id = id;
		this.referenceType = referenceType;
	}
}
