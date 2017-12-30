package TM1Diagnostic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.OrderedJSONObject;

import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

public class TransferCube {

	public String name;
	public OrderedJSONObject json;
	public List<TransferView> views;
	public List<TransferDimension> dimensions;
	
	
	public TransferCube(String name, OrderedJSONObject json){
		this.name = name;
		this.json = json;
		views = new ArrayList<TransferView>();
		dimensions = new ArrayList<TransferDimension>();
	}
	
	public void importToServer(TM1Server server) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException{
		String request = "Cubes";
		server.post(request, this.json);
		for (int i=0; i<views.size(); i++){
			TransferView view = views.get(i);
		}
	}
}
