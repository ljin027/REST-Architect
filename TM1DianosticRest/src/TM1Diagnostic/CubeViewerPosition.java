package TM1Diagnostic;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONException;

import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1Element;
import TM1Diagnostic.REST.TM1Hierarchy;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;
import TM1Diagnostic.REST.TM1Subset;

public class CubeViewerPosition {

	public TM1Dimension dimension;
	public TM1Hierarchy hierarchy;
	public TM1Subset subset;
	public TM1Element element;
	public int position;

	public CubeViewerPosition(int position, TM1Dimension dimension, TM1Hierarchy hierarchy, TM1Subset subset, TM1Element element) {
		this.position = position;
		this.dimension = dimension;
		this.hierarchy = hierarchy;
		this.subset = subset;
		this.element = element;
	}

	public boolean hasSubset() {
		if (subset != null)
			return true;
		return false;
	}

	public String dimensionName() {
		return dimension.name;
	}

	public String hierarchyName() {
		return hierarchy.name;
	}

	public String subsetName() {
		if (subset != null) {
			return subset.name;
		} else {
			return "";
		}
	}

	public String elementName() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		if (element == null){
			hierarchy.readDefaultElementFromServer();
			element = hierarchy.getDefaultElement();
			return element.name;
		}
		return element.name;
	}

}
