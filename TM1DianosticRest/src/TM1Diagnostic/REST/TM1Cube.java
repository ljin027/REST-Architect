package TM1Diagnostic.REST;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.apache.wink.json4j.OrderedJSONObject;

import com.fasterxml.jackson.core.*;
//import com.ibm.xtq.ast.parsers.xpath.Test;

import TM1Diagnostic.SearchResult;

//import org.json.JSONObject;

public class TM1Cube extends TM1Object {

	static public int PUBLIC = 0;
	static public int PRIVATE = 1;

	private List<TM1View> views;
	private List<TM1View> privateViews;

	private List<TM1Dimension> dimensions;
	private List<TM1Dimension> import_dimensions;
	private String rules;

	public boolean dimensionsExpandedInServerExplorer;
	public boolean viewsExpandedInServerExplorer;

	public TM1Cube(String name, TM1Server tm1server) {
		super(name, TM1Object.CUBE, tm1server);
		views = new ArrayList<TM1View>();
		privateViews = new ArrayList<TM1View>();
		dimensions = new ArrayList<TM1Dimension>();
		rules = "";

		dimensionsExpandedInServerExplorer = false;
		viewsExpandedInServerExplorer = false;
	}

	public void clearDimensions() {
		dimensions.clear();
	}

	public int viewCount() {
		return views.size();
	}

	public int privateViewCount() {
		return privateViews.size();
	}

	public String getrules() {
		return rules;
	}

	public int dimensionCount() {
		return dimensions.size();
	}

	public boolean checkServerForDefaultView() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		try {
			String request = entity + "/PrivateViews('Default')";
			tm1server.get(request);
			return true;
		} catch (TM1RestException ex){
			if (ex.getErrorCode() == 404){
				return false;
			} else {
				throw ex;
			}
		}
	}

	public OrderedJSONObject buildJsonForImport(String newname) {
		OrderedJSONObject jcube = new OrderedJSONObject();
		try {
			jcube.put("Name", newname);
			String rules = json.getString("Rules");
			JSONArray jimport_dimensions = new JSONArray();
			for (int i = 0; i < dimensions.size(); i++) {
				String dimname = dimensions.get(i).displayName;
				String importname = import_dimensions.get(i).get_update_name();
				jimport_dimensions.put("Dimensions('" + importname + "')");
				rules = rules.replaceAll("!" + dimname, "!" + importname);
				rules = rules.replaceAll(dimname + ":", importname + ":");
			}
			jcube.put("Rules", rules);
			jcube.put("Dimensions@odata.bind", jimport_dimensions);
		} catch (Exception ex) {

		}
		return jcube;
	}

	public boolean checkServerForCellSecurity() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		String req = "Cubes('}CellSecurity_" + displayName + "')";
		try {
		tm1server.get(req); 
		} catch (TM1RestException ex){
			if (ex.getErrorCode() == 404){
				return false;
			} else {
				throw ex;
			}
		}
		return true;
	}

	public TM1Cube getCellSecurityCube(){
		TM1Cube cellSecurityCube = new TM1Cube("}CellSecurity_" + displayName, tm1server); 
		return cellSecurityCube;
	}

	public boolean readCubeViewsFromServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		// views.clear();
		// privateViews.clear();
		String request = entity;
		String query = "$select=Views,PrivateViews&$expand=Views($select=Name),PrivateViews($select=Name)";
		tm1server.get(request, query);
		try {

			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray jViews = jresponse.getJSONArray("Views");
			for (int i = 0; i < jViews.length(); i++) {
				OrderedJSONObject viewJSON = (OrderedJSONObject) jViews.getJSONObject(i);
				TM1View view = new TM1View(viewJSON.getString("Name"), this, tm1server, PUBLIC);
				if (!views.contains(view)) {
					views.add(view);
				}
			}
			for (int i = 0; i < views.size(); i++) {
				TM1View view = views.get(i);
				if (!jViews.toString().contains("\"Name\":\"" + view.displayName + "\"")) {
					views.remove(i);
				}
			}

			JSONArray jPrivateViews = jresponse.getJSONArray("PrivateViews");
			for (int i = 0; i < jPrivateViews.length(); i++) {
				OrderedJSONObject viewJSON = (OrderedJSONObject) jPrivateViews.getJSONObject(i);
				TM1View privateView = new TM1View(viewJSON.getString("Name"), this, tm1server, PRIVATE);
				if (!privateViews.contains(privateView)) {
					privateViews.add(privateView);
				}
			}
			for (int i = 0; i < privateViews.size(); i++) {
				TM1View privateView = privateViews.get(i);
				if (!jPrivateViews.toString().contains("\"Name\":\"" + privateView.displayName + "\"")) {
					privateViews.remove(i);
				}
			}

		} catch (Exception ex) {
			return false;
		}
		return true;
	}


	public boolean readRulesFromServer() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		String req = entity + "/Rules";
		tm1server.get(req);
		try {
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			rules = jresponse.getString("value");
		} catch (Exception ex) {

		}
		return true;
	}

	public void removeRules() throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = entity;
		OrderedJSONObject payload = new OrderedJSONObject();
		payload.put("Rules", OrderedJSONObject.NULL);
		tm1server.patch(request, payload);
	}

	public void writeRulesToServer(String rules) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		OrderedJSONObject payload = new OrderedJSONObject();
		payload.put("Rules", rules);
		tm1server.patch(entity, payload);
	}

	public void readCubeDimensionsFromServer() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		// dimensions.clear();
		String request = entity + "/Dimensions";
		tm1server.get(request);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		JSONArray jDimensions = jresponse.getJSONArray("value");
		for (int i = 0; i < jDimensions.length(); i++) {
			OrderedJSONObject dimensionJSON = (OrderedJSONObject) jDimensions.getJSONObject(i);
			TM1Dimension dimension = new TM1Dimension(dimensionJSON.getString("Name"), tm1server);
			if (!dimensions.contains(dimension)) {
				dimensions.add(dimension);
			}
		}
		for (int i = 0; i < dimensions.size(); i++) {
			TM1Dimension dimension = dimensions.get(i);
			if (!jDimensions.toString().contains("\"Name\":\"" + dimension.displayName + "\"")) {
				dimensions.remove(i);
			}
		}


	}

	public boolean checkServerForRules() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = entity;
		String query = "$select=Rules";
		tm1server.get(request, query);
		try {
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			if (jresponse.has("Rules")) {
				if (jresponse.isNull("Rules") || jresponse.getString("Rules").isEmpty()) {
					return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
		} catch (Exception ex) {
			return false;
		}
	}

	public void remove() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = entity;
		tm1server.delete(request);
	}

	public OrderedJSONObject getJsonForTransfer(String newname) {
		OrderedJSONObject transfer_cube_json = new OrderedJSONObject();
		try {
			transfer_cube_json.put("Name", newname);
			return transfer_cube_json;
		} catch (Exception ex) {
			return null;
		}
	}

	public boolean checkServerForView(String viewname) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		String request = entity + "/Views('" + viewname + "')";
		tm1server.get(request);
		return true;
	}

	public void createDefaultPrivateView() throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		//System.out.println("<Starting to create new view>");
		this.readCubeDimensionsFromServer();
		OrderedJSONObject jNewViewOrdered = new OrderedJSONObject();
		jNewViewOrdered.put("@odata.type", "#ibm.tm1.api.v1.NativeView");
		jNewViewOrdered.put("Name", "Default");
		JSONArray jrows = new JSONArray();
		JSONArray jcolumns = new JSONArray();
		JSONArray jtitles = new JSONArray();
		for (int i = 0; i < dimensions.size(); i++) {
			TM1Dimension dimension = dimensions.get(i);
			TM1Hierarchy hierarchy = dimension.getDefaultHierarchy();
			//System.out.println("<Dimension: " + dimension.displayName + " Hierarchy: " + hierarchy.displayName + ">");
			hierarchy.readDefaultElementFromServer();
			TM1Element defaultElement = hierarchy.getDefaultElement();
			if (i == 0) {
				OrderedJSONObject jrow = new OrderedJSONObject();
				OrderedJSONObject jsubset = new OrderedJSONObject();
				jsubset.put("Hierarchy@odata.bind", dimension.entity + "/" + hierarchy.entity);
				JSONArray jelements = new JSONArray();
				TM1Subset subset = hierarchy.readSubsetAllFromServer();
				for (int j = 0; j < subset.elementCount(); j++) {
					jelements.put(dimension.entity + "/" + hierarchy.entity + "/" + subset.getElement(j).entity);
				}
				jsubset.put("Elements@odata.bind", jelements);
				jrow.put("Subset", jsubset);
				jrows.put(jrow);
			} else if (i == 1) {
				OrderedJSONObject jcolumn = new OrderedJSONObject();
				OrderedJSONObject jsubset = new OrderedJSONObject();
				jsubset.put("Hierarchy@odata.bind", dimension.entity + "/" + hierarchy.entity);

				JSONArray jelements = new JSONArray();
				// jelements.put(defaultElement.entity);
				TM1Subset subset = hierarchy.readSubsetAllFromServer();
				for (int j = 0; j < subset.elementCount(); j++) {
					jelements.put(dimension.entity + "/" + hierarchy.entity + "/" + subset.getElement(j).entity);
				}

				jsubset.put("Elements@odata.bind", jelements);
				jcolumn.put("Subset", jsubset);
				jcolumns.put(jcolumn);
			} else {
				OrderedJSONObject jtitle = new OrderedJSONObject();
				OrderedJSONObject jsubset = new OrderedJSONObject();
				jsubset.put("Hierarchy@odata.bind", dimension.entity + "/" + hierarchy.entity);

				JSONArray jelements = new JSONArray();
				jelements.put(dimension.entity + "/" + hierarchy.entity + "/" + defaultElement.entity);

				jsubset.put("Elements@odata.bind", jelements);
				jtitle.put("Selected@odata.bind", dimension.entity + "/" + hierarchy.entity + "/" + defaultElement.entity);
				jtitle.put("Subset", jsubset);
				jtitles.put(jtitle);
			}
		}

		jNewViewOrdered.put("Rows", jrows);
		jNewViewOrdered.put("Columns", jcolumns);
		jNewViewOrdered.put("Titles", jtitles);

		jNewViewOrdered.put("SuppressEmptyColumns", false);
		jNewViewOrdered.put("SuppressEmptyRows", false);
		jNewViewOrdered.put("FormatString", "0.#########");

		//System.out.println("Built Default View JSON !");
		//System.out.println("-> " + jNewViewOrdered.toString());

		String request = entity + "/PrivateViews";
		tm1server.post(request, jNewViewOrdered);
	}

	public List<SearchResult> getRuleSourceCubesList() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		List<SearchResult> cubeRuleReferenceList = new ArrayList<SearchResult>();
		//System.out.println("Finding source cubes");

		String request = entity;
		String query = "$select=Rules";
		tm1server.get(request, query);
		OrderedJSONObject responseJSON = new OrderedJSONObject(tm1server.response);
		if (!responseJSON.isNull("Rules")) {
			String currentCubeRules = responseJSON.getString("Rules");
			String lines[] = currentCubeRules.split("\\r?\\n");
			for (int j=0; j<lines.length; j++){
				if (!lines[j].startsWith("#")){
					//System.out.println(lines[j]);
					Matcher m = Pattern.compile("DB\\('([^']+)").matcher(lines[j]);
					while (m.find()) {
						String match = m.group().substring(4);
						TM1Cube sourceCube = new TM1Cube(match, tm1server);
						SearchResult r = new SearchResult(sourceCube, "Line " + j);
						if (!cubeRuleReferenceList.contains(r)) {
							cubeRuleReferenceList.add(r);
						}
					}
				}
			}
		}

		return cubeRuleReferenceList;
	}

	public List<SearchResult> getRuleTargetCubesList() {
		List<SearchResult> cubeRuleReferenceList = new ArrayList<SearchResult>();
		try {
			//System.out.println("Finding target cubes");
			String request = "Cubes";
			String query = "$select=Name,Rules";
			tm1server.get(request, query);
			OrderedJSONObject responseJSON = new OrderedJSONObject(tm1server.response);
			JSONArray cubeRulesJSON = responseJSON.getJSONArray("value");
			String search = "DB('" + displayName + "'".replaceAll(" ", "");

			for (int i = 0; i < cubeRulesJSON.length(); i++) {
				OrderedJSONObject cubeJSON = (OrderedJSONObject) cubeRulesJSON.get(i);
				String currentCubeName = cubeJSON.getString("Name");
				String currentCubeRules = "";
				if (!cubeJSON.isNull("Rules")) {
					currentCubeRules = cubeJSON.getString("Rules");

					String lines[] = currentCubeRules.split("\\r?\\n");
					for (int j=0; j<lines.length; j++){
						if (!lines[j].startsWith("#")){
							if (lines[j].replaceAll(" ", "").contains(search)){
								TM1Cube targetCube = new TM1Cube(currentCubeName, tm1server);
								SearchResult r = new SearchResult(targetCube, "Line " + j);
								cubeRuleReferenceList.add(r);
							}
						}
					}

				}
			}
			return cubeRuleReferenceList;
		} catch (Exception ex) {
			ex.printStackTrace();
			return cubeRuleReferenceList;
		}
	}
	
	public boolean writeCubeToFile(String dir) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		// export cube and rules
		
		//System.out.println("Function -> writeCubeToFile");
	
		File cubeDir = new File(dir + "//cub");
		if (!cubeDir.exists()){
			cubeDir.mkdir();
		}
		
		String request = entity;
		tm1server.get(request);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		FileWriter fw = new FileWriter(cubeDir + "//" + displayName + ".cube", false);
		BufferedWriter bw = new BufferedWriter(fw);
		try {
			bw.write(jresponse.toString());
		} catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	    	bw.close();
	    	bw = null;
	    }
		
		File dimensionDir = new File(dir + "//dimensions");
		if (!dimensionDir.exists()){
			dimensionDir.mkdir();
		}
		
		this.readCubeDimensionsFromServer();
		for (int i=0; i<this.dimensionCount(); i++){
			TM1Dimension dimension = this.getDimension(i);
			dimension.writeDimensionToFile(dir);
		}
		
		FileReader fileReader = new FileReader(".//data/_exportDataTi");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		StringBuffer stringBuffer = new StringBuffer();
		String line = null;
		while((line =bufferedReader.readLine())!=null){
			stringBuffer.append(line).append("\n");
		}
		
		String uniqueID = "}Export_" + UUID.randomUUID().toString();
		String exportFileName = uniqueID + ".blb";
		String processEntity = "Processes('" + uniqueID  + "')";
		String blbEntity = "Contents('Blobs')/Contents('" + uniqueID + "')";
		

		// Create blb for exported data
		OrderedJSONObject exportDataBLBJSON = new OrderedJSONObject();
		exportDataBLBJSON.put("@odata.type", "#ibm.tm1.api.v1.Document");
		exportDataBLBJSON.put("ID", uniqueID);
		exportDataBLBJSON.put("Name", uniqueID);
		tm1server.post("Contents('Blobs')/Contents", exportDataBLBJSON);
		
		// Create ti process that exports to the blb file
		OrderedJSONObject exportDataProcessJSON = new OrderedJSONObject(stringBuffer.toString());
		exportDataProcessJSON.replace("Name", "_exportCube", uniqueID);
		tm1server.post("Processes", exportDataProcessJSON);
		
		// Run the Ti process that exports data to the blb file
		OrderedJSONObject exportProcessParametersJSON = new OrderedJSONObject();
		JSONArray exportProcessParameterArray = new JSONArray();
		OrderedJSONObject p1 = new OrderedJSONObject();
		p1.put("Name", "pCubeName");
		p1.put("Value", displayName);
		OrderedJSONObject p2 = new OrderedJSONObject();
		p2.put("Name", "pFileName");
		p2.put("Value", exportFileName);
		exportProcessParameterArray.add(p1);
		exportProcessParameterArray.add(p2);
		exportProcessParametersJSON.put("Parameters", exportProcessParameterArray);
		tm1server.post("Processes('" + uniqueID  + "')/tm1.Execute", exportProcessParametersJSON);
		
		// Read the blb file from server to client
		tm1server.get("Contents('Blobs')/Contents('" + uniqueID + "')/Content");
		FileWriter fw2 = new FileWriter(cubeDir + "//" + displayName + ".data", false);
		BufferedWriter bw2 = new BufferedWriter(fw2);
		String blbResponse = tm1server.response;
		try {
			bw2.write(blbResponse);
		} catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	    	bw2.close();
	    	bw2 = null;
	    }
		
		tm1server.delete(blbEntity);
		tm1server.delete(processEntity);
		
		return true;
	}

	public void addView(TM1View view) {
		views.add(view);
	}

	public TM1View getview(int i) {
		return views.get(i);
	}

	public TM1View getPrivateView(int i) {
		return privateViews.get(i);
	}

	public void addImportDimension(TM1Dimension dimension) {
		import_dimensions.add(dimension);
	}

	public TM1Dimension getDimension(int i) {
		return dimensions.get(i);
	}

}
