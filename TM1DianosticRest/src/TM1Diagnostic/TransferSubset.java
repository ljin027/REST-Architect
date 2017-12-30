package TM1Diagnostic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.OrderedJSONObject;

import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

public class TransferSubset {

	public String name;
	public String parentHierarchy;
	public String parentDimension;
	public OrderedJSONObject json;
	
	public TransferSubset(String subsetName, String hierarchyName, String dimensionName, OrderedJSONObject json){
		this.name = subsetName;
		this.parentHierarchy = hierarchyName;
		this.parentDimension = dimensionName;
		this.json = json;
	}
	
	public void importToServer(TM1Server server) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException{
		String request = "Dimensions('" + parentDimension + "')/Hierarchies('" + parentHierarchy + "')/Subsets";
		server.post(request, this.json);
	}

	
}
