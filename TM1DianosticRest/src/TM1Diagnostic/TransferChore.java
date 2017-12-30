package TM1Diagnostic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.OrderedJSONObject;

import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

public class TransferChore {

	public String name;
	public OrderedJSONObject json;
	
	public TransferChore(String name, OrderedJSONObject json){
		this.name = name;
		this.json = json;
	}
	
	public void importToServer(TM1Server server) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException{
		String request = "Chores";
		server.post(request, json);
	}
}
