package TM1Diagnostic.REST;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;

public class TM1Dimension {
	
	public TM1Server tm1server;
	public String name;
	public String entity;
	public String entitySet;

	static public String NUMERIC = "Numeric";
	static public String STRING = "Sting";
	static public String CONSOLIDATED = "Consolidated";

	public boolean expandedInExplorerTree;
	public boolean heirarchiesExpandedInServerExplorer;
	private OrderedJSONObject importJSON;

	private List<TM1Hierarchy> heirarchies;

	private String update_name;
	protected String unique_name;
	
	
	public TM1Dimension(OrderedJSONObject importJSON) throws JSONException {
		this.importJSON = importJSON;
		this.name = importJSON.getString("Name");
		this.entity = "Dimensions('" + name + "')";
		this.heirarchies = new ArrayList<TM1Hierarchy>();
		
		expandedInExplorerTree = false;
		heirarchiesExpandedInServerExplorer = false;

	}

	public TM1Dimension(String name, TM1Server tm1server) {
		this.name = name;
		this.tm1server = tm1server;
		this.entity = "Dimensions('" + name + "')";
		this.heirarchies = new ArrayList<TM1Hierarchy>();
		unique_name = "[" + name + "]";
		heirarchiesExpandedInServerExplorer = false;
	}
	
	/*public void createOnServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException{
		String request = "Dimensions";
		tm1server.post(request, importJSON);
		for (int i=0; i<heirarchies.size(); i++){
			TM1Hierarchy hierarchy = heirarchies.get(i);
			if (!hierarchy.name.equals("Leaves")){
				hierarchy.createOnServer();
			}
		}
	}*/

	public TM1Server getServer() {
		return tm1server;
	}
	
	public int hierarchyCount() {
		return heirarchies.size();
	}

	public void remove() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = entity;
		tm1server.delete(request);
	}
	
	public OrderedJSONObject getExportJSON() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		tm1server.get(entity);
		return new OrderedJSONObject(tm1server.response);
	}

	public void readHierarchiesFromServer() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		String request = entity + "/Hierarchies";
		tm1server.get(request);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		JSONArray jheirarchies = jresponse.getJSONArray("value");
		for (int i = 0; i < jheirarchies.length(); i++) {
			OrderedJSONObject heirarchyJSON = (OrderedJSONObject) jheirarchies.getJSONObject(i);
			TM1Hierarchy hierarchy = new TM1Hierarchy(heirarchyJSON.getString("Name"), tm1server, this);
			if (!heirarchies.contains(hierarchy)) {
				heirarchies.add(hierarchy);
			}
		}
		for (int i = 0; i < heirarchies.size(); i++) {
			TM1Hierarchy hierarchy = heirarchies.get(i);
			if (!jheirarchies.toString().contains("\"Name\":\"" + hierarchy.name + "\"")) {
				heirarchies.remove(i);
			}
		}
	}

	public void set_update_name(String name) {
		this.update_name = name;
	}

	public String get_update_name() {
		return update_name;
	}

	public List<String> findParentCubes() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		List<String> cubes = new ArrayList<String>();
		// Cubes?$select=Name&$expand=Dimensions($select=Name)
		String request = "Cubes";
		String query = "$select=Name&$expand=Dimensions($select=Name)";
		tm1server.get(request, query);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		JSONArray jCubes = jresponse.getJSONArray("value");
		for (int i = 0; i < jCubes.length(); i++) {
			OrderedJSONObject jCube = (OrderedJSONObject) jCubes.getJSONObject(i);
			String cubeName = jCube.getString("Name");
			JSONArray jdimensions = jCube.getJSONArray("Dimensions");
			for (int j = 0; j < jdimensions.size(); j++) {
				OrderedJSONObject jdimension = (OrderedJSONObject) jdimensions.getJSONObject(j);
				String dimensionName = jdimension.getString("Name");
				if (dimensionName.equals(name)) {
					cubes.add(cubeName);
				}
			}
		}
		return cubes;
	}

	/*public OrderedJSONObject build_json_for_import(String import_name) throws JSONException {
		String oldname = json.getString("Name");
		json.put("@odata.context", "$metadata#Dimensions/$entity");
		json.put("Name", import_name);
		json.put("UniqueName", "[" + import_name + "]");
		JSONArray hierarchies = json.getJSONArray("Hierarchies");
		for (int i = 0; i < hierarchies.length(); i++) {
			OrderedJSONObject jhierarchy = (OrderedJSONObject) hierarchies.getJSONObject(i);
			if (jhierarchy.getString("Name").equals(oldname)) {
				jhierarchy.put("Name", import_name);
				jhierarchy.put("UniqueName", "[" + import_name + "].[" + import_name + "]");
			} else {
				String hierarchyname = jhierarchy.getString("Name");
				jhierarchy.put("UniqueName", "[" + import_name + "].[" + hierarchyname + "]");
			}
			JSONArray jelements = jhierarchy.getJSONArray("Elements");
			for (int j = 0; j < jelements.length(); j++) {
				OrderedJSONObject jelement = (OrderedJSONObject) jelements.getJSONObject(j);
				String elementname = jelement.getString("Name");
				jelement.put("Name", elementname);
				jelement.put("UniqueName", "[" + import_name + "].[" + elementname + "]");
				if (jelement.getString("Type").equals("Consolidated")) {
					jelement.put("Components", update_name_in_json_children(jelement, import_name));
				}
			}
			jhierarchy.put("Elements", jelements);
		}
		json.put("Hierarchies", hierarchies);
		return json;
	}

	public JSONArray update_name_in_json_children(OrderedJSONObject jelement, String import_name) throws JSONException {
		JSONArray jcomponents = jelement.getJSONArray("Components");
		for (int i = 0; i < jcomponents.length(); i++) {
			OrderedJSONObject jcomponent = (OrderedJSONObject) jcomponents.getJSONObject(i);
			String elementname = jcomponent.getString("Name");
			jcomponent.put("UniqueName", "[" + import_name + "].[" + elementname + "]");
			if (jcomponent.getString("Type").equals("Consolidated")) {
				jcomponent.put("Components", update_name_in_json_children(jcomponent, import_name));
			}
		}
		return jcomponents;
	}

	public OrderedJSONObject build_object_json(String import_name) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		OrderedJSONObject jdimension = new OrderedJSONObject();
		jdimension.put("@odata.context", "$metadata#Dimensions/$entity");
		jdimension.put("Name", import_name);
		jdimension.put("UniqueName", "[" + import_name + "]");
		JSONArray hierarchies = new JSONArray();
		readHierarchiesFromServer();
		for (int i = 0; i < heirarchies.size(); i++) {
			TM1Hierarchy hierarchy = heirarchies.get(i);
			OrderedJSONObject jhierarchy = new OrderedJSONObject();
			if (heirarchies.get(i).name.equals(name)) {
				jhierarchy.put("Name", import_name);
				jhierarchy.put("UniqueName", "[" + import_name + "].[" + import_name + "]");
			} else {
				jhierarchy.put("Name", heirarchies.get(i).displayName);
				jhierarchy.put("UniqueName", heirarchies.get(i).displayName);
			}
			hierarchy.readHierarchyFromServer();
			JSONArray jelements = new JSONArray();
			for (int j = 0; j < hierarchy.getRootElementCount(); j++) {
				TM1Element element = hierarchy.getRootElement(j);
				OrderedJSONObject jelement = new OrderedJSONObject();
				jelement.put("Name", element.displayName);
				jelement.put("UniqueName", element.uniqueName);
				jelement.put("Type", element.elementType);
				jelement.put("Index", element.index);
				jelement.put("Level", element.level);
				if (element.elementType.equals(CONSOLIDATED)) {
					jelement.put("Components", make_post_json_children(element));
				}
				jelements.put(jelement);
			}
			hierarchies.put(jhierarchy);
			jhierarchy.put("Elements", jelements);
		}
		jdimension.put("Hierarchies", hierarchies);
		return jdimension;

	}

	public JSONArray make_post_json_children(TM1Element element) throws JSONException {
		JSONArray jcomponents = new JSONArray();
		for (int i = 0; i < element.childElementCount(); i++) {
			TM1Element childelement = element.childElement(i);
			OrderedJSONObject jelement = new OrderedJSONObject();
			jelement.put("Name", childelement.displayName);
			jelement.put("UniqueName", childelement.uniqueName);
			jelement.put("Type", childelement.elementType);
			jelement.put("Index", childelement.index);
			jelement.put("Level", childelement.level);
			if (childelement.elementType.equals(CONSOLIDATED)) {
				jelement.put("Components", make_post_json_children(childelement));
			}
			jcomponents.put(jelement);
		}
		return jcomponents;
	}
	*/

	public void fileExport(File exportFile) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		String request = entity;
		tm1server.get(request);
		OrderedJSONObject response = new OrderedJSONObject(tm1server.response);
		OrderedJSONObject exportJSON = new OrderedJSONObject();
		exportJSON.put("Name", response.getString("Name"));

		FileWriter fw = new FileWriter(exportFile);
		BufferedWriter bw = new BufferedWriter(fw);
		try {
			bw.write(exportJSON.toString());
		} catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	    	bw.close();
	    	bw = null;
	    }
	}

	public boolean checkServerForElementSecurity() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		try {
			String request = "Cubes('}ElementSecurity_" + name + "')";
			tm1server.get(request);
			return true;
		} catch (TM1RestException ex){
			if (((TM1RestException)ex).getErrorCode() == 404) {
				return false;
			}
			throw ex;
		}
	}

	public TM1Dimension getElementSecurityDimension() {
		String securityDimensionName = "}ElementSecurity_" + name;
		TM1Dimension elementSecurityDimension = new TM1Dimension(securityDimensionName, tm1server);
		return elementSecurityDimension;
	}

	public TM1Cube getElementSecurityCube() {
		String securityCubeName = "}ElementSecurity_" + name;
		TM1Cube elementSecurityCube = new TM1Cube(securityCubeName, tm1server);
		return elementSecurityCube;
	}

	public boolean checkServerForElementAttributes() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		try {
			System.out.println("Checking for }ElementAttributes_" + name);
			String request = "Cubes('}ElementAttributes_" + name + "')";
			tm1server.get(request);
			System.out.println("OK");
			return true;
		} catch (Exception ex){
			ex.printStackTrace();
			//throw ex;
		} finally {
			System.out.println("NO");
			return false;
		}
	}

	public TM1Dimension getElementAttributeDimension() {
		String attributeDimensionName = "}ElementAttributes_" + name;
		TM1Dimension elementAttributeDimension = new TM1Dimension(attributeDimensionName, tm1server);
		return elementAttributeDimension;
	}

	public TM1Cube getElementAttributeCube() {
		String attributeCubeName = "}ElementAttributes_" + name;
		TM1Cube elementAttributeCube = new TM1Cube(attributeCubeName, tm1server);
		return elementAttributeCube;
	}

	public TM1Hierarchy getDefaultHierarchy() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		this.readHierarchiesFromServer();
		for (int i = 0; i < heirarchies.size(); i++) {
			if (heirarchies.get(i).name.equals(name)) {
				return heirarchies.get(i);
			}
		}
		return null;
	}

	public TM1Hierarchy getHeirarchy(int i) {
		return heirarchies.get(i);
	}

	public boolean checkForHierarchy(String hierarchyName) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		String request = entity + "/Hierarchies('" + hierarchyName + "')";
		tm1server.get(request);
		return true;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof TM1Dimension)) {
			return false;
		}
		TM1Dimension dimension = (TM1Dimension) o;
		return dimension.name.equals(this.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
