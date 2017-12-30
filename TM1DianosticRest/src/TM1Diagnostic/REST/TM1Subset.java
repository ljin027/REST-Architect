package TM1Diagnostic.REST;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;

public class TM1Subset extends TM1Object {

	static public int PUBLIC = 0;
	static public int PRIVATE = 1;

	static public String NUMERIC = "Numeric";
	static public String STRING = "String";
	static public String CONSOLIDATED = "Consolidated";

	public TM1Dimension dimension;
	public TM1Hierarchy hierarchy;

	public String uniqueName;
	public boolean aliasOn;
	public String alias;
	public int security;

	private List<TM1Element> elements;

	public TM1Subset(String name, TM1Server tm1server, TM1Hierarchy parent) {
		super(name, TM1Object.SUBSET, parent, tm1server);
		this.hierarchy = parent;
		this.dimension = hierarchy.dimension;
		elements = new ArrayList<TM1Element>();
		security = PRIVATE;
		entity = parent.entity + "/PrivateSubsets('" + displayName + "')";
		aliasOn = false;
		alias = "";
	}

	public TM1Subset(String name, TM1Server tm1server, TM1Hierarchy parent, int security) {
		super(name, TM1Object.SUBSET, parent, tm1server);
		this.hierarchy = parent;
		this.dimension = hierarchy.dimension;
		elements = new ArrayList<TM1Element>();
		this.security = security;
		if (security == PRIVATE) {
			entity = "PrivateSubsets('" + displayName + "')";
		} else if (security == PUBLIC) {
			entity = "Subsets('" + displayName + "')";
		}
		aliasOn = false;
		alias = "";
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}

	public int elementCount() {
		return elements.size();
	}

	public TM1Element getElement(int i) {
		return elements.get(i);
	}

	public void addElement(TM1Element element) {
		elements.add(element);
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public TM1Hierarchy getHierarchy() {
		return (TM1Hierarchy) parent;
	}

	public boolean contains(String elementname) {
		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i).name.equals(elementname))
				return true;
		}

		return false;
	}

	public boolean remove() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = dimension.entity + "/" + hierarchy.entity + "/" + entity;
		tm1server.delete(request);
		return true;
	}

	public boolean readElementListFromServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		// System.out.println("Reading element list for subset " + name);
		TM1Hierarchy hierarchy = (TM1Hierarchy) this.parent;
		hierarchy.readElementAliasesFromServer();
		elements.clear();

		if (displayName.equals("")) {
			//System.out.println("Getting all element for unnamed subset");
			String request = dimension.entity + "/" + hierarchy.entity + "/" + entity + "/Elements";
			String query = "$select=Name,Type,Index,UniqueName,Level,Attributes";
			tm1server.get(request, query);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray jelements = jresponse.getJSONArray("value");
			for (int i = 0; i < jelements.length(); i++) {
				OrderedJSONObject jelement = (OrderedJSONObject) jelements.getJSONObject(i);
				String element_name = jelement.getString("Name");
				TM1Element element = new TM1Element(element_name, tm1server, (TM1Hierarchy) this.parent);
				String unique_name = jelement.getString("UniqueName");
				element.setUniqueName(unique_name);
				String element_type = jelement.getString("Type");
				element.setElementType(element_type);
				int index = jelement.getInt("Index");
				element.set_index(index);
				int level = jelement.getInt("Level");
				element.set_level(level);
				OrderedJSONObject jattributes = (OrderedJSONObject) jelement.getJSONObject("Attributes");
				for (int j = 0; j < hierarchy.aliasCount(); j++) {
					String aliasValue = jattributes.getString(hierarchy.getAlias(j));
					element.addAlias(hierarchy.getAlias(j), aliasValue);
				}
				// element.setAliasValue(jattributes.getString(alias));
				elements.add(element);
			}
			return true;
		} else {
			String request = dimension.entity + "/" + hierarchy.entity + "/" + entity;
			String query = "$expand=Elements($select=Name,Type,Index,UniqueName,Level,Attributes)";
			tm1server.get(request, query);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray jelements = jresponse.getJSONArray("Elements");
			for (int i = 0; i < jelements.length(); i++) {
				OrderedJSONObject jelement = (OrderedJSONObject) jelements.getJSONObject(i);
				String element_name = jelement.getString("Name");
				String unique_name = jelement.getString("UniqueName");
				String element_type = jelement.getString("Type");
				int index = jelement.getInt("Index");
				int level = jelement.getInt("Level");
				TM1Element element = new TM1Element(element_name, tm1server, (TM1Hierarchy) this.parent);
				element.setUniqueName(unique_name);
				element.setElementType(element_type);
				element.set_index(index);
				element.set_level(level);
				OrderedJSONObject jattributes = (OrderedJSONObject) jelement.getJSONObject("Attributes");
				for (int j = 0; j < hierarchy.aliasCount(); j++) {
					String aliasValue = jattributes.getString(hierarchy.getAlias(j));
					// System.out.println("read alias " + aliasValue);
					element.addAlias(hierarchy.getAlias(j), aliasValue);
				}
				// element.setAlias(this.alias);

				elements.add(element);
			}
			return true;
		}
	}

	public List<String> findParentViews() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		List<String> views = new ArrayList<String>();
		String request = "Cubes";
		String query = "$select=Name&$expand=Views($expand=tm1.NativeView/Rows/Subset($select=Name,UniqueName),tm1.NativeView/Columns/Subset($select=Name,UniqueName),tm1.NativeView/Titles/Subset($select=Name,UniqueName))";
		tm1server.get(request, query);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		JSONArray jCubes = jresponse.getJSONArray("value");
		for (int i = 0; i < jCubes.length(); i++) {
			OrderedJSONObject jCube = (OrderedJSONObject) jCubes.getJSONObject(i);
			String cubeName = jCube.getString("Name");
			JSONArray jviews = jCube.getJSONArray("Views");
			for (int j = 0; j < jviews.size(); j++) {
				OrderedJSONObject jview = (OrderedJSONObject) jviews.getJSONObject(j);
				String viewName = jview.getString("Name");

				if (!jview.isNull("Rows")) {
					JSONArray jrows = jview.getJSONArray("Rows");
					for (int k = 0; k < jrows.size(); k++) {
						OrderedJSONObject jrow = (OrderedJSONObject) jrows.getJSONObject(k);
						OrderedJSONObject jsubset = (OrderedJSONObject) jrow.getJSONObject("Subset");
						if (jsubset.has("UniqueName")) {
							String subsetUniqueName = jsubset.getString("UniqueName");
							if (subsetUniqueName.equals(this.uniqueName)) {
								views.add(cubeName + ":" + viewName);
							}
						}
					}
				}

				if (!jview.isNull("Columns")) {
					JSONArray jcolumns = jview.getJSONArray("Columns");
					for (int k = 0; k < jcolumns.size(); k++) {
						OrderedJSONObject jcolumn = (OrderedJSONObject) jcolumns.getJSONObject(k);
						OrderedJSONObject jsubset = (OrderedJSONObject) jcolumn.getJSONObject("Subset");
						if (jsubset.has("UniqueName")) {
							String subsetUniqueName = jsubset.getString("UniqueName");
							if (subsetUniqueName.equals(this.uniqueName)) {
								views.add(cubeName + ":" + viewName);
							}
						}
					}
				}

				if (!jview.isNull("Titles")) {
					JSONArray jtitles = jview.getJSONArray("Titles");
					for (int k = 0; k < jtitles.size(); k++) {
						OrderedJSONObject jtitle = (OrderedJSONObject) jtitles.getJSONObject(k);
						OrderedJSONObject jsubset = (OrderedJSONObject) jtitle.getJSONObject("Subset");
						if (jsubset.has("UniqueName")) {
							String subsetUniqueName = jsubset.getString("UniqueName");
							if (subsetUniqueName.equals(this.uniqueName)) {
								views.add(cubeName + ":" + viewName);
							}
						}
					}
				}

			}
		}
		return views;
	}

	public boolean insertElement(TM1Element element) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException{
		String request = dimension.entity + "/" + hierarchy.entity + "/" + entity  + "/tm1.SetElement";;
		//String query = "$select=Alias";
		OrderedJSONObject payload = new OrderedJSONObject();
		TM1Hierarchy hierarchy = element.hierarchy;
		TM1Dimension dimension = hierarchy.dimension;
		payload.put("Element@odata.bind", dimension.entity + "/" + hierarchy.entity + "/" + element.entity);
		tm1server.post(request, payload);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		return true;
	}

	public void insertBeforeElement(TM1Element element, TM1Element beforeElement) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException{
		String request = dimension.entity + "/" + hierarchy.entity + "/" + entity + "/tm1.SetElement";
		//String query = "$select=Alias";
		OrderedJSONObject payload = new OrderedJSONObject();
		TM1Hierarchy hierarchy = element.hierarchy;
		TM1Dimension dimension = hierarchy.dimension;
		payload.put("Element@odata.bind", dimension.entity + "/" + hierarchy.entity + "/" + element.entity);
		if (beforeElement != null) {
			TM1Hierarchy beforeElementHierarchy = beforeElement.hierarchy;
			TM1Dimension beforeElementDimension = beforeElementHierarchy.dimension;
			payload.put("Before@odata.bind", beforeElementDimension.entity + "/" + beforeElementHierarchy.entity + "/" + beforeElement.entity);
		}
		tm1server.post(request, payload);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
	}

	public void insertAfterElement(TM1Element element, TM1Element afterElement) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException{
		String request = dimension.entity + "/" + hierarchy.entity + "/" + entity + "/tm1.SetElement";
		//String query = "$select=Alias";
		OrderedJSONObject payload = new OrderedJSONObject();
		TM1Hierarchy hierarchy = element.hierarchy;
		TM1Dimension dimension = hierarchy.dimension;
		payload.put("Element@odata.bind", dimension.entity + "/" + hierarchy.entity + "/" + element.entity);
		if (afterElement != null) {
			TM1Hierarchy beforeElementHierarchy = afterElement.hierarchy;
			TM1Dimension beforeElementDimension = beforeElementHierarchy.dimension;
			payload.put("After@odata.bind", beforeElementDimension.entity + "/" + beforeElementHierarchy.entity + "/" + afterElement.entity);
		}
		tm1server.post(request, payload);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
	}

	public void removeElement(TM1Element element) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException{
		String request = dimension.entity + "/" + hierarchy.entity + "/" + entity + "/" + element.entity;
		//String query = "$select=Alias";
		tm1server.delete(request);
		//OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		//this.alias = jresponse.getString("Alias");
	}

	public void readSubsetAliasFromServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		if (name.isEmpty()){
			alias = "";
		} else {
			String request = dimension.entity + "/" + hierarchy.entity + "/" + entity;
			String query = "$select=Alias";
			tm1server.get(request, query);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			this.alias = jresponse.getString("Alias");
		}
	}

	public boolean contains(TM1Element element) {
		for (int i = 0; i < elements.size(); i++) {
			TM1Element subsetElement = elements.get(i);
			if (subsetElement.equals(element)) {
				return true;
			}
		}
		return false;
	}

	public TM1Element findParentInSubset(TM1Element element) {
		TM1Hierarchy hierarchy = (TM1Hierarchy) this.parent;
		return hierarchy.getParentElement(element);
	}

	public void removeAllElements() {
		elements.clear();
	}

	public void writeSubsetToServer() throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = entity;
		OrderedJSONObject payload = new OrderedJSONObject();
		payload.put("@odata.type", "ibm.tm1.api.v1.Subset");
		payload.put("Name", displayName);
		JSONArray elementArray = new JSONArray();
		for (int i = 0; i < elements.size(); i++) {
			TM1Element element = elements.get(i);
			elementArray.put(element.entity);
		}
		payload.put("Elements@odata.bind", elementArray);
		tm1server.patch(request, payload);
	}

	public void writeToServerAs(String newSubsetName) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = dimension.entity + "/" + hierarchy.entity + "/" + entitySet;
		OrderedJSONObject payload = new OrderedJSONObject();
		payload.put("@odata.type", "ibm.tm1.api.v1.Subset");
		payload.put("Name", newSubsetName);
		JSONArray elementArray = new JSONArray();
		for (int i = 0; i < elements.size(); i++) {
			TM1Element element = elements.get(i);
			// System.out.println(element.entity);
			elementArray.put(element.entity);
		}
		payload.put("Elements@odata.bind", elementArray);
		tm1server.post(request, payload);

	}


	public void writeSubsetToFile(String dir) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		System.out.println("Function -> writeSubsetToFile");
		String request = dimension.entity + "/" + hierarchy.entity + "/" + entity;
		String query = "$expand=Elements";
		tm1server.get(request, query);
		OrderedJSONObject jo = new OrderedJSONObject(tm1server.response);
		File subsetDir = new File(dir + "//" + hierarchy.displayName);
		if (!subsetDir.exists()){
			subsetDir.mkdir();
		}
		FileWriter fw = new FileWriter(subsetDir + "//" + displayName + ".sub", false);
		BufferedWriter bw = new BufferedWriter(fw);
		try {
			bw.write(jo.toString());
		} catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	    	bw.close();
	    	bw = null;
	    }

		//fw.close();
	}
}
