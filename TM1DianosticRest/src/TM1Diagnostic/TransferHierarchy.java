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

public class TransferHierarchy {

	public String name;
	public String parentDimension;
	private TM1Hierarchy hierarchy;
	public OrderedJSONObject transferjson;
	public List<TransferSubset> subsets;
	
	public TransferHierarchy(String name, String parentDimension, OrderedJSONObject transferjson){
		this.name = name;
		this.parentDimension = parentDimension;
		this.transferjson = transferjson;
		subsets = new ArrayList<TransferSubset>();
	}
	
	public TransferHierarchy(String name, String parentDimension, TM1Hierarchy hierarchy){
		this.name = name;
		this.parentDimension = parentDimension;
		this.hierarchy = hierarchy;
		subsets = new ArrayList<TransferSubset>();
	}
	
	public TM1Hierarchy getHierarchy() {
		return hierarchy;
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
		server.post(request, transferjson);
		for (int i=0; i<subsets.size(); i++){
			TransferSubset subset = subsets.get(i);
			//subset.importToServer(server);
		}
	}
	
	public boolean writeHierarchyToFile(String dimensionDir) throws IOException, JSONException, TM1RestException, URISyntaxException {
		System.out.println("Function -> writeHierarchyToFile");
		
		File hierarchyDir = new File(dimensionDir + "//" + hierarchy.displayName);
		if (!hierarchyDir.exists()){
			hierarchyDir.mkdir();
		} 
		
		TM1Server tm1server = hierarchy.getServer();
		TM1Dimension dimension = hierarchy.dimension;
		
		String request = hierarchy.dimension.getEntity() + "/" + hierarchy.getEntity();
		String query = "$expand=Elements";
		tm1server.get(request, query);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);

		FileWriter fw = new FileWriter(dimensionDir + "//" + hierarchy.displayName + ".hier", false);
		BufferedWriter bw = new BufferedWriter(fw);
		try {
			bw.write(jresponse.toString());
		} catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	    	bw.close();
	    	bw = null;
	    }
		return true;
	}
	
	
}
