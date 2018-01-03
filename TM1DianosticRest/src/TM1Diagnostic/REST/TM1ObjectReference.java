package TM1Diagnostic.REST;

import org.apache.wink.json4j.OrderedJSONObject;

public class TM1ObjectReference {
	
	public TM1Server tm1server;
	public TM1Dimension dimension;
	public TM1Hierarchy hierarchy;
	public String name;
	public String entity;
	public String entitySet;
	public OrderedJSONObject transferJSON;
	
	private String id;
	public int referenceType;
	
	public TM1ObjectReference(String name, TM1Server tm1server, String id, int referenceType){
		this.name = name;
		this.tm1server = tm1server;
		
		this.id = id;
		this.referenceType = referenceType;
	}
}
