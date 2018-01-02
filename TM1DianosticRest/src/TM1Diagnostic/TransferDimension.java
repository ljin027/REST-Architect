package TM1Diagnostic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;

import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1Hierarchy;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

public class TransferDimension {

	public String name;
	private TM1Dimension dimension;
	public OrderedJSONObject json;
	public List<TransferHierarchy> hierarchies;
	
	public TransferDimension(String name, OrderedJSONObject json){
		this.name = name;
		this.json = json;
		hierarchies = new ArrayList<TransferHierarchy>();
	}
	
	public TransferDimension(String name, TM1Dimension dimension){
		this.name = name;
		this.dimension = dimension;
		hierarchies = new ArrayList<TransferHierarchy>();
	}
	
	public TM1Dimension getDimension() {
		return dimension;
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
	
	public boolean writeDimensionToFile(String directory) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		// dimension
		String dimensionsDirString = directory + "//dim";
		File dimensionsDir = new File(dimensionsDirString);
		if (!dimensionsDir.exists()){
			dimensionsDir.mkdir();
		}
		
		String request = dimension.getEntity();
		TM1Server tm1server = dimension.getServer();
		tm1server.get(request);
		OrderedJSONObject response = new OrderedJSONObject(tm1server.response);

		FileWriter fw = new FileWriter(dimensionsDirString + "//" + dimension.displayName + ".dim", false);
		BufferedWriter bw = new BufferedWriter(fw);
		try {
			bw.write(response.toString());
		} catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	    	bw.close();
	    	bw = null;
	    }
		
		dimension.readHierarchiesFromServer();
		
		File dimensionDir = new File(dimensionsDirString + "//" + dimension.displayName);
		if (!dimensionDir.exists()){
			dimensionDir.mkdir();
		}
		
		for (int i=0; i<getHierarchyCount(); i++){
			TM1Hierarchy hierarchy = this.getHierarchy(i).getHierarchy();
			hierarchy.writeHierarchyToFile(dimensionsDirString + "//" + hierarchy.displayName);
		}
		return true;
	}
	
	
	
	
}
