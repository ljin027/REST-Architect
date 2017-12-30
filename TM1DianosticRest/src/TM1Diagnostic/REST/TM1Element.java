package TM1Diagnostic.REST;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.apache.wink.json4j.JSONArray;

public class TM1Element extends TM1Object {

	public String uniqueName;
	public String elementType;
	public String alias;
	public int level;
	public int index;
	public int weight;

	public TM1Hierarchy hierarchy;

	protected List<TM1Element> children;
	protected List<TM1Element> parents;

	public List<Alias> aliases;

	static public String NUMERIC = "Numeric";
	static public String STRING = "String";
	static public String CONSOLIDATED = "Consolidated";

	public TM1Element(String name, TM1Server tm1server, TM1Hierarchy parent) {
		super(name, TM1Object.ELEMENT, parent, tm1server);
		this.hierarchy = parent;
		uniqueName = "[" + parent.parent.displayName + "].[" + parent.displayName + "].[" + displayName + "]";
		children = new ArrayList<TM1Element>();
		parents = new ArrayList<TM1Element>();
		aliases = new ArrayList<Alias>();
		alias = name;
	}

	public TM1Element(String name, TM1Server tm1server, TM1Hierarchy parent, String elementType, int index, int weight, int level) {
		super(name, TM1Object.ELEMENT, parent, tm1server);
		this.hierarchy = parent;
		uniqueName = "[" + parent.parent.displayName + "].[" + parent.displayName + "].[" + displayName + "]";
		children = new ArrayList<TM1Element>();
		parents = new ArrayList<TM1Element>();
		aliases = new ArrayList<Alias>();
		alias = name;
		this.elementType = elementType;
		this.index = index;
		this.weight = weight;
		this.level = level;
	}

	public void setJson(OrderedJSONObject object) {
		json = object;
	}

	public void setUniqueName(String unique_name) {
		this.uniqueName = unique_name;
	}

	public void addAlias(String alias, String value) {
		aliases.add(new Alias(alias, value));
	}

	public void setAliasValue(String value) {
		// System.out.println("Set element " + name + " to Alias " + alias);
		this.alias = value;
	}

	public void setAlias(String alias) {
		for (int i = 0; i < aliases.size(); i++) {
			Alias a = aliases.get(i);
			String aliasName = a.getName();
			if (aliasName.equals(alias)) {
				this.alias = a.getValue();
				return;
			}
		}
		alias = name;
	}

	public void setElementType(String elementType) {
		this.elementType = elementType;
	}

	public void addChildElement(TM1Element childElement) {
		children.add(childElement);
	}

	public int childElementCount() {
		return children.size();
	}

	public TM1Element childElement(int i) {
		return children.get(i);
	}

	public void set_level(int level) {
		this.level = level;
	}

	public void set_index(int index) {
		this.index = index;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TM1Element))
			return false;
		TM1Element checkElement = (TM1Element) obj;
		if (checkElement.uniqueName.matches(this.uniqueName)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isConsolidated() {
		if (elementType.equals(CONSOLIDATED))
			return true;
		return false;
	}

	public boolean isNumberic() {
		if (elementType.equals(NUMERIC))
			return true;
		return false;
	}

	public boolean isString() {
		if (elementType.equals(STRING))
			return true;
		return false;
	}

	public void removeElement() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = entity;
		tm1server.delete(request);
	}

	public void removeElementWithChildern() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		for (int i = 0; i < children.size(); i++) {
			TM1Element childElement = children.get(i);
			childElement.removeElementWithChildern();
		}
		String request = entity;
		tm1server.delete(request);
	}

	public void setComponentOnServer(TM1Element childElement, TM1Element beforeElement) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = entity + "/tm1.SetComponent";
		OrderedJSONObject payload = new OrderedJSONObject();
		payload.put("Element@odata.bind", childElement.entity);
		if (beforeElement != null) {
			payload.put("Before@odata.bind", beforeElement.entity);
		}
		tm1server.post(request, payload);
	}



}
