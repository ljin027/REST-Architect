package TM1Diagnostic.REST;

import org.apache.wink.json4j.OrderedJSONObject;

public class TM1Application {
	
	public TM1Server tm1server;
	public TM1Dimension dimension;
	public TM1Hierarchy hierarchy;
	public String name;
	public String entity;
	public String entitySet;
	public OrderedJSONObject transferJSON;
	
	public TM1Application(String name, TM1Server tm1server){
		this.name = name;
		this.tm1server = tm1server;
	}
	
}
