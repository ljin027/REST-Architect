package TM1Diagnostic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.OrderedJSONObject;

import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

public class TransferDimension {

	public String name;
	public OrderedJSONObject json;
	public List<TransferHierarchy> hierarchies;
	
	public boolean selected;
	
	public TransferDimension(String name, OrderedJSONObject json){
		this.name = name;
		this.json = json;
		hierarchies = new ArrayList<TransferHierarchy>();
		selected = true;
	}
	
	public void addHierarchy(TransferHierarchy transferHierarchy){
		hierarchies.add(transferHierarchy);
	}
	
	public TransferHierarchy getHierarchy(int i){
		return hierarchies.get(i);
	}
	
	public int getHierarchyCount(){
		return hierarchies.size();
	}
	
	public void importToServer(TM1Server server) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException{
		String request = "Dimensions";
		server.post(request, this.json);
		for (int i=0; i<hierarchies.size(); i++){
			TransferHierarchy hierarchy = hierarchies.get(i);
			if (!hierarchy.name.equals("Leaves")){
				hierarchy.importToServer(server);
			}
		}
	}
	
}
