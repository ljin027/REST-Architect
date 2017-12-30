package TM1Diagnostic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.OrderedJSONObject;

import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

public class TransferHierarchy {

	public String name;
	public String parentDimension;
	public OrderedJSONObject json;
	public List<TransferSubset> subsets;
	
	public TransferHierarchy(String name, String parentDimension, OrderedJSONObject json){
		this.name = name;
		this.parentDimension = parentDimension;
		this.json = json;
		subsets = new ArrayList<TransferSubset>();
	}
	
	public int getSubsetCount(){
		return subsets.size();
	}
	
	public void addSubset(TransferSubset transferSubset){
		subsets.add(transferSubset);
	}
	
	public TransferSubset getSubset(int i){
		return subsets.get(i);
	}
	
	public void importToServer(TM1Server server) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException{
		String request = "Dimensions('" + parentDimension + "')/Hierarchies";
		server.post(request, json);
		for (int i=0; i<subsets.size(); i++){
			TransferSubset subset = subsets.get(i);
			//subset.importToServer(server);
		}
	}
	
}
