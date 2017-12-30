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

public class TM1Hierarchy extends TM1Object {

	static public int PUBLIC = 0;
	static public int PRIVATE = 1;

	static public String NUMERIC = "Numeric";
	static public String STRING = "String";
	static public String CONSOLIDATED = "Consolidated";

	private List<TM1Subset> subsets;
	private List<TM1Subset> privateSubsets;
	private List<String> aliases;

	public TM1Dimension dimension;
	private TM1Subset subsetAll;

	private List<TM1Element> roots;
	private String UniqueName;

	public String alias;

	public boolean subsetsExpandedInServerExplorer;
	public boolean privateSubsetsExpandedInServerExplorer;

	private TM1Element defaultElement;

	public TM1Hierarchy(String name, TM1Server tm1server, TM1Dimension parent) {
		super(name, TM1Object.HIERARCHY, parent, tm1server);
		this.dimension = parent;
		subsets = new ArrayList<TM1Subset>();
		privateSubsets = new ArrayList<TM1Subset>();
		aliases = new ArrayList<String>();
		roots = new ArrayList<TM1Element>();
		UniqueName = parent.unique_name + ".[" + displayName + "]";

		subsetsExpandedInServerExplorer = false;
		privateSubsetsExpandedInServerExplorer = false;
	}

	public int aliasCount() {
		return aliases.size();
	}

	public String getAlias(int i) {
		return aliases.get(i);
	}

	public int subsetCount() {
		return subsets.size();
	}

	public TM1Subset getSubset(int i) {
		return subsets.get(i);
	}

	public int privateSubsetCount() {
		return privateSubsets.size();
	}

	public TM1Subset getPrivateSubset(int i) {
		return privateSubsets.get(i);
	}

	public TM1Element getDefaultElement() {
		return defaultElement;
	}

	public void clearElements() {
		roots.clear();
	}

	public void addRootElement(TM1Element rootElement) {
		roots.add(rootElement);
	}

	public boolean readDefaultElementFromServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		String request = dimension.entity + "/" + entity + "/DefaultMember";
		String query = "$select=Element&$expand=Element";

		tm1server.get(request, query);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		OrderedJSONObject jFirstElement = (OrderedJSONObject) jresponse.getJSONObject("Element");
		String defaultElementName = jFirstElement.getString("Name");
		String defaultElementType = jFirstElement.getString("Type");
		defaultElement = new TM1Element(defaultElementName, tm1server, this);
		defaultElement.setElementType(defaultElementType);
		return true;
	}

	public TM1Subset readSubsetAllFromServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		TM1Subset subsetAll = new TM1Subset("", tm1server, this);
		String request = subsetAll.dimension.entity + "/" + entity + "/Elements";
		String query = "$select=Name,Type,Index,UniqueName,Level";
		tm1server.get(request, query);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		JSONArray jelements = jresponse.getJSONArray("value");
		for (int i = 0; i < jelements.length(); i++) {
			OrderedJSONObject jelement = (OrderedJSONObject) jelements.getJSONObject(i);
			String element_name = jelement.getString("Name");
			String unique_name = jelement.getString("UniqueName");
			String element_type = jelement.getString("Type");
			int index = jelement.getInt("Index");
			int level = jelement.getInt("Level");
			TM1Element element = new TM1Element(element_name, tm1server, this);
			element.setUniqueName(unique_name);
			element.setElementType(element_type);
			element.set_index(index);
			element.set_level(level);
			subsetAll.addElement(element);
		}
		return subsetAll;
	}

	public boolean readLevelsFromServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		String request = dimension.entity + "/" + entity + "/Levels";
		tm1server.get(request);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		JSONArray jlevels = jresponse.getJSONArray("value");
		int level = 0;
		for (int i = 0; i < jlevels.length(); i++) {
			OrderedJSONObject jlevel = (OrderedJSONObject) jlevels.getJSONObject(i);
			if (jlevel.getInt("Number") > level) {
				level = jlevel.getInt("Number");
			}
		}
		return true;
	}

	public boolean writeHierarchyToFile(String dimensionDir) throws IOException, JSONException, TM1RestException, URISyntaxException {
		System.out.println("Function -> writeHierarchyToFile");
		
		File hierarchyDir = new File(dimensionDir + "//" + displayName);
		if (!hierarchyDir.exists()){
			hierarchyDir.mkdir();
		} 
		
		String request = dimension.entity + "/" + entity;
		String query = "$expand=Elements";
		tm1server.get(request, query);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);

		FileWriter fw = new FileWriter(dimensionDir + "//" + displayName + ".hier", false);
		BufferedWriter bw = new BufferedWriter(fw);
		try {
			bw.write(jresponse.toString());
		} catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	    	bw.close();
	    	bw = null;
	    }
	
		this.readSubsetsFromServer();
		for (int i=0; i<subsetCount(); i++){
			TM1Subset subset = this.getSubset(i);
			subset.writeSubsetToFile(dimensionDir);
		}
		
		return false;
	}

	public boolean readHierarchyFromServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		roots.clear();
		String request = dimension.entity + "/" + entity + "/Levels('0')/Members";
		String query = "$select=Name,Weight&$expand=Element,Children($select=Name,Weight;$expand=Element,Children($select=Name,Weight;$expand=Element,Children($select=Name,Weight;$expand=Element,Children)))";
		tm1server.get(request, query);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		JSONArray jmembers = jresponse.getJSONArray("value");
		for (int i = 0; i < jmembers.length(); i++) {
			OrderedJSONObject jmember = (OrderedJSONObject) jmembers.getJSONObject(i);
			int elementWeight = jmember.getInt("Weight");
			OrderedJSONObject jelement = (OrderedJSONObject) jmember.getJSONObject("Element");

			String elementName = jelement.getString("Name");
			String uniqueName = jelement.getString("UniqueName");
			String elementType = jelement.getString("Type");
			int elementIndex = jelement.getInt("Index");
			int elementLevel = jelement.getInt("Level");

			TM1Element element = new TM1Element(elementName, tm1server, this);

			element.uniqueName = uniqueName;
			element.elementType = elementType;
			element.index = elementIndex;
			element.level = elementLevel;
			element.weight = elementWeight;

			OrderedJSONObject jattributes = (OrderedJSONObject) jelement.getJSONObject("Attributes");
			for (int j = 0; j < aliasCount(); j++) {
				String aliasValue = jattributes.getString(getAlias(j));
				element.addAlias(getAlias(j), aliasValue);
			}

			JSONArray jChildrenMembers = jmember.getJSONArray("Children");
			if (jChildrenMembers.length() > 0) {
				readElementChildrenFromServer(element, jChildrenMembers);
			}

			roots.add(element);
		}
		return true;
	}

	public void remove() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = dimension.entity + "/" + entity;
		tm1server.delete(request);
	}

	public void readElementChildrenFromServer(TM1Element element, JSONArray jMembers) throws JSONException {
		for (int i = 0; i < jMembers.length(); i++) {
			OrderedJSONObject jMember = (OrderedJSONObject) jMembers.getJSONObject(i);
			int elementWeight = jMember.getInt("Weight");
			OrderedJSONObject jElement = (OrderedJSONObject) jMember.getJSONObject("Element");
			String elementName = jElement.getString("Name");
			String uniqueName = jElement.getString("UniqueName");
			String elementType = jElement.getString("Type");
			int elementIndex = jElement.getInt("Index");
			int elementLevel = jElement.getInt("Level");
			TM1Element childElement = new TM1Element(elementName, tm1server, this);
			childElement.uniqueName = uniqueName;
			childElement.elementType = elementType;
			childElement.index = elementIndex;
			childElement.level = elementLevel;
			childElement.weight = elementWeight;
			OrderedJSONObject jattributes = (OrderedJSONObject) jElement.getJSONObject("Attributes");
			for (int j = 0; j < aliasCount(); j++) {
				String aliasValue = jattributes.getString(getAlias(j));
				childElement.addAlias(getAlias(j), aliasValue);
			}

			JSONArray jMemberChildren = jMember.getJSONArray("Children");
			if (jMemberChildren.length() > 0) {
				readElementChildrenFromServer(childElement, jMemberChildren);
			}
			element.addChildElement(childElement);
		}
	}

	public int getRootElementCount() {
		return roots.size();
	}

	public TM1Element getRootElement(int i) {
		return roots.get(i);
	}

	public boolean readSubsetsFromServer() throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = dimension.entity + "/" + entity;
		String query = "$select=Subsets,PrivateSubsets&$expand=Subsets($select=Name,UniqueName,Alias),PrivateSubsets($select=Name,UniqueName,Alias)";
		tm1server.get(request, query);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);

		JSONArray jsubsets = jresponse.getJSONArray("Subsets");
		for (int i = 0; i < jsubsets.length(); i++) {
			//System.out.println(i);
			OrderedJSONObject jsubset = (OrderedJSONObject) jsubsets.getJSONObject(i);
			String subsetName = jsubset.getString("Name");
			String subsetAlias = jsubset.getString("Alias");
			String uniqueName = jsubset.getString("UniqueName");
			TM1Subset subset = new TM1Subset(subsetName, tm1server, this, PUBLIC);
			subset.setAlias(subsetAlias);
			subset.setUniqueName(uniqueName);
			if (!subsets.contains(subset)) {
				subsets.add(subset);
			}
		}
		for (int i = 0; i < subsets.size(); i++) {
			TM1Subset subset = subsets.get(i);
			if (!jsubsets.toString().contains("\"Name\":\"" + subset.displayName + "\"")) {
				subsets.remove(i);
			}
		}

		JSONArray jPrivateSubsets = jresponse.getJSONArray("PrivateSubsets");
		for (int i = 0; i < jPrivateSubsets.length(); i++) {
			OrderedJSONObject jPrivateSubset = (OrderedJSONObject) jPrivateSubsets.getJSONObject(i);
			String subsetName = jPrivateSubset.getString("Name");
			String subsetAlias = jPrivateSubset.getString("Alias");
			String uniqueName = jPrivateSubset.getString("UniqueName");
			TM1Subset privateSubset = new TM1Subset(subsetName, tm1server, this, PRIVATE);
			privateSubset.setAlias(subsetAlias);
			privateSubset.setUniqueName(uniqueName);
			if (!privateSubsets.contains(privateSubset)) {
				privateSubsets.add(privateSubset);
			}
		}
		for (int i = 0; i < privateSubsets.size(); i++) {
			TM1Subset subset = privateSubsets.get(i);
			if (!jPrivateSubsets.toString().contains("\"Name\":\"" + subset.displayName + "\"")) {
				privateSubsets.remove(i);
			}
		}

		return true;

	}


	public boolean readElementAliasesFromServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		aliases.clear();
		String request = dimension.entity + "/" + entity + "/ElementAttributes";
		String query = "$filter=Type eq tm1.AttributeType'Alias'&$select=Name";
		tm1server.get(request, query);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		JSONArray jaliases = jresponse.getJSONArray("value");
		for (int i = 0; i < jaliases.length(); i++) {
			OrderedJSONObject jalias = (OrderedJSONObject) jaliases.getJSONObject(i);
			String aliasName = jalias.getString("Name");
			aliases.add(aliasName);
		}
		return true;
	}

	public boolean checkServerForSubset(String subsetname) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		String request = dimension.entity + "/" + entity + "/Subsets('" + subsetname + "')";
		tm1server.get(request);
		return true;

	}

	public boolean checkServerForPrivateSubset(String subsetname) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		String request = dimension.entity + "/" + entity + "/PrivateSubsets('" + subsetname + "')";
		tm1server.get(request);
		return true;
	}

	public TM1Subset getPrivateSubsetByName(String subsetName) {
		for (int i = 0; i < privateSubsetCount(); i++) {
			TM1Subset subset = privateSubsets.get(i);
			if (subset.name.equals(subsetName))
				return subset;
		}
		return null;
	}

	public TM1Subset getSubsetAll() {
		return subsetAll;
	}

	public TM1Element getFirstTopElement() {
		TM1Element element = new TM1Element("", tm1server, this);
		return element;
	}

	public TM1Element getParentElement(TM1Element checkElement) {
		for (int i = 0; i < roots.size(); i++) {
			TM1Element rootElement = roots.get(i);
			if (rootElement.equals(checkElement))
				return null;
			if (rootElement.isConsolidated()) {
				TM1Element foundParentElement = checkChildForParentElement(rootElement, checkElement);
				if (foundParentElement != null)
					return foundParentElement;
			}
		}
		return null;
	}

	public TM1Element checkChildForParentElement(TM1Element element, TM1Element checkElement) {
		for (int i = 0; i < element.childElementCount(); i++) {
			TM1Element childElement = element.childElement(i);
			if (childElement.equals(checkElement))
				return element;
			if (childElement.isConsolidated()) {
				checkChildForParentElement(childElement, checkElement);
			}
		}
		return null;
	}

	private void addChildElementToJSON(OrderedJSONObject jParentElement, TM1Element parentElement) throws JSONException {
			JSONArray jChildElements = new JSONArray();
			for (int i = 0; i < parentElement.childElementCount(); i++) {
				TM1Element childElement = parentElement.childElement(i);
				OrderedJSONObject jChildElement = new OrderedJSONObject();
				jChildElement.put("Name", childElement.displayName);
				jChildElement.put("Type", childElement.elementType);
				jChildElement.put("Index", childElement.index);
				jChildElement.put("Level", childElement.level);
				if (childElement.isConsolidated()) {
					addChildElementToJSON(jChildElement, childElement);
				}
				jChildElements.put(jChildElement);
			}
			jParentElement.put("Components", jChildElements);
	}

	public void writeToServer() throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = dimension.entity + "/" + entity;
		OrderedJSONObject payload = new OrderedJSONObject();
		JSONArray jelements = new JSONArray();
		for (int i = 0; i < roots.size(); i++) {
			TM1Element rootElement = roots.get(i);
			OrderedJSONObject jRootElement = new OrderedJSONObject();
			jRootElement.put("Name", rootElement.displayName);
			jRootElement.put("Type", rootElement.elementType);
			jRootElement.put("Index", rootElement.index);
			jRootElement.put("Level", rootElement.level);
			if (rootElement.isConsolidated()) {
				addChildElementToJSON(jRootElement, rootElement);
			}
			jelements.put(jRootElement);
		}
		payload.put("Elements", jelements);

		tm1server.patch(request, payload);
	}

	public void moveElementBefore(TM1Element elementToMove, TM1Element beforeElement) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		String request = dimension.entity + "/" + entity + "/tm1.SetElement";
		OrderedJSONObject payload = new OrderedJSONObject();
		payload.put("Element@odata.bind", elementToMove.entity);
		if (beforeElement != null) 	payload.put("Before@odata.bind", beforeElement.entity);
		tm1server.post(request, payload);
	}

	public void moveElementToRootBefore(TM1Element elementToMove, TM1Element beforeElement) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		String request = dimension.entity + "/" + entity + "/tm1.SetElement";
		OrderedJSONObject payload = new OrderedJSONObject();
		payload.put("Element@odata.bind", elementToMove.entity);
		if (beforeElement != null) 	payload.put("Before@odata.bind", beforeElement.entity);
		tm1server.post(request, payload);

	}

	public void saveHierarchyAs(String newHierarchyName) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = dimension.entity + "/" + entity + "/tm1.SaveAs";
		OrderedJSONObject payload = new OrderedJSONObject();
		payload.put("Name", newHierarchyName);
		payload.put("Overwrite", true);
		payload.put("KeepExistingAttributes", true);
		tm1server.post(request, payload);
	}

}
