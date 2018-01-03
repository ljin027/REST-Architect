package TM1Diagnostic.REST;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.apache.wink.json4j.JSONArray;

import TM1Diagnostic.CubeViewerPosition;
import TM1Diagnostic.TM1ViewMember;

public class TM1View  {
	
	public TM1Server tm1server;
	public TM1Dimension dimension;
	public TM1Hierarchy hierarchy;
	public String name;
	public String entity;
	public String entitySet;
	public OrderedJSONObject transferJSON;

	static public int ROW = 0;
	static public int COLUMN = 1;
	static public int FILTER = 2;

	static public int PUBLIC = 0;
	static public int PRIVATE = 1;

	private String cellsetid;

	public TM1Cube cube;

	private List<TM1Cell> cells;
	private List<TM1ViewAxes> axes;

	private List<CubeViewerPosition> rows;
	private List<CubeViewerPosition> columns;
	private List<CubeViewerPosition> titles;

	private List<TM1Sandbox> sandboxes;

	public boolean suppressEmptyColumns;
	public boolean suppressEmptyRows;

	public int security;

	public TM1View(String name, TM1Cube parent, TM1Server tm1server) {
		this.name = name;
		this.tm1server = tm1server;
		this.cube  = parent;

		cells = new ArrayList<TM1Cell>();
		axes = new ArrayList<TM1ViewAxes>();

		columns = new ArrayList<CubeViewerPosition>();
		rows = new ArrayList<CubeViewerPosition>();
		titles = new ArrayList<CubeViewerPosition>();
		sandboxes = new ArrayList<TM1Sandbox>();

		this.security = PRIVATE;
		entity = "PrivateViews('" + name + "')";
	}

	public TM1View(String name, TM1Cube parent, TM1Server tm1server, int security) {
		this.name = name;
		this.tm1server = tm1server;
		this.cube  = parent;
		
		cells = new ArrayList<TM1Cell>();
		axes = new ArrayList<TM1ViewAxes>();

		columns = new ArrayList<CubeViewerPosition>();
		rows = new ArrayList<CubeViewerPosition>();
		titles = new ArrayList<CubeViewerPosition>();
		//dimensionPositions = new ArrayList<CubeViewerPosition>();
		sandboxes = new ArrayList<TM1Sandbox>();

		this.security = security;
		if (security == PRIVATE) {
			entity = "PrivateViews('" + name + "')";
		} else if (security == PUBLIC) {
			entity = "Views('" + name + "')";
		}
	}
	
	public TM1Cube getCube() {
		return cube;
	}

	public void clearRows(){
		rows.clear();
	}

	public void clearColumns(){
		columns.clear();
	}

	public void clearTitles(){
		titles.clear();
	}

	public void setRowDimensionPosition(CubeViewerPosition cubeViewerPosition){
		rows.add(cubeViewerPosition);
	}

	public void setColumnDimensionPosition(CubeViewerPosition cubeViewerPosition){
		columns.add(cubeViewerPosition);
	}

	public void setTitleDimensionPosition(CubeViewerPosition cubeViewerPosition){
		titles.add(cubeViewerPosition);
	}

	public int cellCount() {
		return cells.size();
	}

	public TM1Cell getCell(int i) {
		return cells.get(i);
	}

	public TM1ViewAxes getColumnAxes() {
		return axes.get(0);
	}

	public TM1ViewAxes getrowaxes() {
		return axes.get(1);
	}

	public TM1ViewAxes getcontextaxes() {
		return axes.get(2);
	}

	public int columnCount() {
		return columns.size();
	}

	public CubeViewerPosition getColumnDimensionPosition(int i) {
		return columns.get(i);
	}

	public int getrowscount() {
		return rows.size();
	}

	public CubeViewerPosition getRowDimensionPosition(int i) {
		return rows.get(i);
	}

	public int getfilterscount() {
		return titles.size();
	}

	public CubeViewerPosition getTitleDimensionPosition(int i) {
		return titles.get(i);
	}

	public int sandboxCount() {
		return sandboxes.size();
	}

	public TM1Sandbox getSandbox(int i) {
		return sandboxes.get(i);
	}

	public void remove() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = cube.entity + "/" + entity;
		tm1server.delete(request);
	}

	// {"@odata.context":"../../$metadata#Cubes('Test1')/Views/ibm.tm1.api.v1.NativeView/$entity","@odata.etag":"W/\"17712\"",
	// "Name":"Stuart","Columns":[{}],"Rows":[{}],"Titles":[{}],
	// "SuppressEmptyColumns":true,"SuppressEmptyRows":true,"FormatString":"0.#########"}

	public void setRowSuppression(boolean status) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = cube.entity + "/" + entity + "/tm1.NativeView";
		OrderedJSONObject payload = new OrderedJSONObject();
		payload.put("SuppressEmptyRows", status);
		tm1server.patch(request, payload);
	}

	public void setColumnSuppression(boolean status) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = cube.entity + "/" + entity + "/tm1.NativeView";
		OrderedJSONObject payload = new OrderedJSONObject();
		payload.put("SuppressEmptyColumns", status);
		tm1server.patch(request, payload);				
		//OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
	}

	public void readSandboxesFromServer() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		String req = "Sandboxes";
		tm1server.get(req);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		JSONArray jsandboxes = jresponse.getJSONArray("value");
		for (int i = 0; i < jsandboxes.length(); i++) {
			OrderedJSONObject jsandbox = (OrderedJSONObject) jsandboxes.getJSONObject(i);
			String sandboxname = jsandbox.getString("Name");
			boolean isactive = jsandbox.getBoolean("IsActive");
			TM1Sandbox sandbox = new TM1Sandbox(sandboxname, isactive);
			sandboxes.add(sandbox);
		}
	}

	// http://vottstuartv22.canlab.ibm.com:40002/api/v1/Cubes('Stuart')/Views('Default')?$expand=tm1.NativeView/Rows/Subset($select=Name;$expand=Hierarchy($expand=Dimension)),tm1.NativeView/Columns/Subset($select=Name;$expand=Hierarchy($expand=Dimension)),tm1.NativeView/Titles/Selected($select=Name;$expand=Hierarchy($expand=Dimension))

	public boolean readAxesFromServer() throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		rows.clear();
		columns.clear();
		titles.clear();
		String request = cube.entity + "/" + entity;
		String query = "$expand=tm1.NativeView/Rows/Subset($select=Name;$expand=Hierarchy($expand=Dimension)),tm1.NativeView/Columns/Subset($select=Name;$expand=Hierarchy($expand=Dimension)),tm1.NativeView/Titles/Subset($select=Name;$expand=Hierarchy($expand=Dimension)),tm1.NativeView/Titles/Selected($select=Name;$expand=Hierarchy($expand=Dimension))";
		tm1server.get(request, query);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);

		//System.out.println(jresponse.toString());

		JSONArray jrows = jresponse.getJSONArray("Rows");
		for (int i = 0; i < jrows.length(); i++) {
			OrderedJSONObject jrow = (OrderedJSONObject) jrows.getJSONObject(i);

			OrderedJSONObject jsubset = (OrderedJSONObject) jrow.getJSONObject("Subset");
			String subset_name = jsubset.getString("Name");
			OrderedJSONObject jhierarchy = (OrderedJSONObject) jsubset.getJSONObject("Hierarchy");
			String hierarchy_name = jhierarchy.getString("Name");
			OrderedJSONObject jdimension = (OrderedJSONObject) jhierarchy.getJSONObject("Dimension");
			String dimension_name = jdimension.getString("Name");

			TM1Dimension dimension = new TM1Dimension(dimension_name, tm1server);
			TM1Hierarchy hierarchy = new TM1Hierarchy(hierarchy_name, tm1server, dimension);
			TM1Subset subset = new TM1Subset(subset_name, tm1server, hierarchy, TM1Subset.PUBLIC);

			rows.add(new CubeViewerPosition(1, dimension, hierarchy, subset, null));
			//dimensionPositions.add(new CubeViewerPosition(1, dimension, hierarchy, subset, null));
		}


		JSONArray jcolumns = jresponse.getJSONArray("Columns");
		for (int i = 0; i < jcolumns.length(); i++) {
			OrderedJSONObject jcolumn = (OrderedJSONObject) jcolumns.getJSONObject(i);
			OrderedJSONObject jsubset = (OrderedJSONObject) jcolumn.getJSONObject("Subset");

			String subset_name = jsubset.getString("Name");
			OrderedJSONObject jhierarchy = (OrderedJSONObject) jsubset.getJSONObject("Hierarchy");
			String hierarchy_name = jhierarchy.getString("Name");
			OrderedJSONObject jdimension = (OrderedJSONObject) jhierarchy.getJSONObject("Dimension");
			String dimension_name = jdimension.getString("Name");

			TM1Dimension dimension = new TM1Dimension(dimension_name, tm1server);
			TM1Hierarchy hierarchy = new TM1Hierarchy(hierarchy_name, tm1server, dimension);
			TM1Subset subset = new TM1Subset(subset_name, tm1server, hierarchy, TM1Subset.PUBLIC);

			columns.add(new CubeViewerPosition(0, dimension, hierarchy, subset, null));
			//dimensionPositions.add(new CubeViewerPosition(0, dimension, hierarchy, subset, null));
		}

		JSONArray jtitles = jresponse.getJSONArray("Titles");
		for (int i = 0; i < jtitles.length(); i++) {
			OrderedJSONObject jtitle = (OrderedJSONObject) jtitles.getJSONObject(i);
			OrderedJSONObject jselected = (OrderedJSONObject) jtitle.getJSONObject("Selected");
			String title_element = jselected.getString("Name");

			String subsetName = "";
			if (jtitle.has("Subset")) {
				OrderedJSONObject jsubset = (OrderedJSONObject) jtitle.getJSONObject("Subset");
				subsetName = jsubset.getString("Name");
			}

			OrderedJSONObject jhierarchy = (OrderedJSONObject) jselected.getJSONObject("Hierarchy");
			String hierarchy_name = jhierarchy.getString("Name");

			OrderedJSONObject jdimension = (OrderedJSONObject) jhierarchy.getJSONObject("Dimension");
			String dimension_name = jdimension.getString("Name");

			TM1Dimension dimension = new TM1Dimension(dimension_name, tm1server);
			TM1Hierarchy hierarchy = new TM1Hierarchy(hierarchy_name, tm1server, dimension);
			TM1Subset subset = new TM1Subset(subsetName, tm1server, hierarchy, TM1Subset.PUBLIC);
			TM1Element element = new TM1Element(title_element, tm1server, hierarchy);

			titles.add(new CubeViewerPosition(2, dimension, hierarchy, subset, element));
			//dimensionPositions.add(new CubeViewerPosition(2, dimension, hierarchy, subset, element));
		}
		return true;
	}

	public boolean readSuppressionFromServer() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		String request = cube.entity + "/" + entity + "/tm1.NativeView";
		tm1server.get(request);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		suppressEmptyColumns = jresponse.getBoolean("SuppressEmptyColumns");
		suppressEmptyRows = jresponse.getBoolean("SuppressEmptyRows");
		return true;

	}

	public void writeCellValueUpdatesToServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		String request = "Cellsets('" + cellsetid + "')/Cells";
		JSONArray payload = new JSONArray();
		for (int i = 0; i < cells.size(); i++) {
			TM1Cell cell = cells.get(i);
			if (cell.updated) {
				OrderedJSONObject jcell = new OrderedJSONObject();
				jcell.put("Ordinal", cell.ordinal);
				if (cell.type == TM1Cell.NUMERIC) {
					jcell.put("Value", Double.parseDouble(cell.value));
				} else {
					jcell.put("Value", cell.value);
				}
				payload.put(jcell);
			}
		}
		tm1server.patch(request, payload);
	}

	public void recalculate() throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		cells.clear();
		String request = cube.entity + "/" + entity + "/tm1.Execute";
		String query = "$expand=Axes($select=Ordinal;$expand=Hierarchies($select=Name),Tuples($count;$expand=Members($select=Name;$expand=Element($select=Name,Type)))),Cells($select=Value,FormattedValue,FormatString,Status,Updateable,RuleDerived,Annotated,Consolidated)";
		OrderedJSONObject payload = new OrderedJSONObject();
		axes.clear();
		tm1server.post(request, query, payload);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		cellsetid = jresponse.getString("ID");
		JSONArray jaxes = jresponse.getJSONArray("Axes");
		for (int i = 0; i < jaxes.length(); i++) {
			TM1ViewAxes axe = new TM1ViewAxes();
			OrderedJSONObject jaxe = (OrderedJSONObject) jaxes.getJSONObject(i);
			axe.ordinal = jaxe.getInt("Ordinal");
			JSONArray jhierarchies = jaxe.getJSONArray("Hierarchies");
			for (int j = 0; j < jhierarchies.length(); j++) {
				axe.addHierarchyName(jhierarchies.getJSONObject(j).getString("Name"));
			}
			JSONArray jtuples = jaxe.getJSONArray("Tuples");
			for (int j = 0; j < jtuples.length(); j++) {
				OrderedJSONObject jtuple = (OrderedJSONObject) jtuples.getJSONObject(j);
				TM1ViewTuple tuple = new TM1ViewTuple();
				tuple.ordinal = jtuple.getInt("Ordinal");
				JSONArray jmembers = jtuple.getJSONArray("Members");
				// System.out.println("Member " + jmembers.toString());
				for (int k = 0; k < jmembers.length(); k++) {
					OrderedJSONObject jmember = (OrderedJSONObject) jmembers.getJSONObject(k);
					OrderedJSONObject jelement = (OrderedJSONObject) jmember.getJSONObject("Element");
					TM1ViewMember member = new TM1ViewMember(jelement.getString("Name"), jelement.getString("Type"));
					tuple.members.add(member);

				}
				axe.addTuple(tuple);
			}
			axes.add(axe);

		}
		JSONArray jcells = jresponse.getJSONArray("Cells");
		for (int i = 0; i < jcells.length(); i++) {
			OrderedJSONObject jcell = (OrderedJSONObject) jcells.getJSONObject(i);
			Object value_object = jcell.get("Value");
			String value = "";
			if (value_object instanceof String) {
				value = (String) value_object;
			} else {
				try {
					value = Double.toString((Double) value_object);
				} catch (Exception ex) {
					value = "ERR";
				}
			}
			String status = jcell.getString("Status");
			String formattedvalue = jcell.getString("FormattedValue");
			String formatstring = jcell.getString("FormatString");
			int updateable = jcell.getInt("Updateable");
			boolean rulederived = jcell.getBoolean("RuleDerived");
			boolean annotated = jcell.getBoolean("Annotated");
			boolean consolidated = jcell.getBoolean("Consolidated");

			TM1Cell cell = new TM1Cell(i, 0, status, value, formattedvalue, formatstring, updateable, rulederived, annotated, consolidated);
			cells.add(cell);
		}

	}

	public void writeViewToServer(String viewName, boolean isPrivate) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		//this.readCubeDimensionsFromServer();
		OrderedJSONObject updatedViewJSON = new OrderedJSONObject();
		updatedViewJSON.put("@odata.type", "#ibm.tm1.api.v1.NativeView");
		updatedViewJSON.put("Name", viewName);
		JSONArray rowDimensionsJSON = new JSONArray();
		JSONArray columnDimensionsJSON = new JSONArray();
		JSONArray titleDimensionsJSON = new JSONArray();
		for (int i=0; i<rows.size(); i++){
			CubeViewerPosition rowPosition = rows.get(i);
			TM1Dimension dimension = rowPosition.dimension;
			TM1Hierarchy hierarchy = rowPosition.hierarchy;
			TM1Subset subset = rowPosition.subset;
			if (subset.name.isEmpty()){
				subset.readElementListFromServer();
			}
			OrderedJSONObject dimensionJSON = new OrderedJSONObject();
			OrderedJSONObject subsetJSON = new OrderedJSONObject();
			subsetJSON.put("Hierarchy@odata.bind",	dimension.entity + "/" + hierarchy.entity);
			JSONArray elementJSONArray = new JSONArray();
			for (int j=0; j<subset.elementCount(); j++){
				TM1Element element = subset.getElement(j);
				elementJSONArray.put(dimension.entity + "/" + hierarchy.entity + "/" + element.entity);
			}
			subsetJSON.put("Elements@odata.bind", elementJSONArray);
			dimensionJSON.put("Subset", subsetJSON);
			rowDimensionsJSON.put(dimensionJSON);
		}
		for (int i=0; i<columns.size(); i++){
			CubeViewerPosition columnPosition = columns.get(i);
			TM1Dimension dimension = columnPosition.dimension;
			TM1Hierarchy hierarchy = columnPosition.hierarchy;
			TM1Subset subset = columnPosition.subset;
			if (subset.name.isEmpty()){
				subset.readElementListFromServer();
			}
			OrderedJSONObject dimensionJSON = new OrderedJSONObject();
			OrderedJSONObject subsetJSON = new OrderedJSONObject();
			subsetJSON.put("Hierarchy@odata.bind",	dimension.entity + "/" + hierarchy.entity);
			JSONArray elementJSONArray = new JSONArray();
			for (int j=0; j<subset.elementCount(); j++){
				TM1Element element = subset.getElement(j);
				elementJSONArray.put(dimension.entity + "/" + hierarchy.entity + "/" + element.entity);
			}
			subsetJSON.put("Elements@odata.bind", elementJSONArray);
			dimensionJSON.put("Subset", subsetJSON);
			columnDimensionsJSON.put(dimensionJSON);
		}
		for (int i=0; i<titles.size(); i++){
			CubeViewerPosition titlePosition = titles.get(i);
			TM1Dimension dimension = titlePosition.dimension;
			TM1Hierarchy hierarchy = titlePosition.hierarchy;
			TM1Subset subset = titlePosition.subset;
			TM1Element element = titlePosition.element;
			OrderedJSONObject dimensionJSON = new OrderedJSONObject();
			OrderedJSONObject subsetJSON = new OrderedJSONObject();
			subsetJSON.put("Hierarchy@odata.bind", dimension.entity + "/" + hierarchy.entity);
			JSONArray jelements = new JSONArray();
			jelements.put(element.entity);
			subsetJSON.put("Elements@odata.bind", jelements);
			dimensionJSON.put("Selected@odata.bind", dimension.entity + "/" + hierarchy.entity + "/" + element.entity);
			dimensionJSON.put("Subset", subsetJSON);
			titleDimensionsJSON.put(dimensionJSON);
		}

		updatedViewJSON.put("Rows", rowDimensionsJSON);
		updatedViewJSON.put("Columns", columnDimensionsJSON);
		updatedViewJSON.put("Titles", titleDimensionsJSON);
		updatedViewJSON.put("SuppressEmptyColumns", false);
		updatedViewJSON.put("SuppressEmptyRows", false);
		updatedViewJSON.put("FormatString", "0.#########");

		String request = "";
		if (isPrivate) {
			request = cube.entity + "/PrivateViews";
		} else {
			request = cube.entity + "/Views";
		}
		tm1server.post(request, updatedViewJSON);
	}

	public void setPublic(){
	}

	public void setPrivate(){
	}


	public void updateViewToServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {

		OrderedJSONObject updatedViewJSON = new OrderedJSONObject();
		updatedViewJSON.put("@odata.type", "#ibm.tm1.api.v1.NativeView");
		if (security == PRIVATE){
			updatedViewJSON.put("@odata.context", "../$metadata#Cubes('" + cube.name + "')/PrivateViews/ibm.tm1.api.v1.NativeView/$entity");
		}

		JSONArray rowDimensionsJSON = new JSONArray();
		JSONArray columnDimensionsJSON = new JSONArray();
		JSONArray titleDimensionsJSON = new JSONArray();
		for (int i=0; i<rows.size(); i++){
			CubeViewerPosition rowPosition = rows.get(i);
			TM1Hierarchy hierarchy = rowPosition.hierarchy;
			TM1Subset subset = rowPosition.subset;
			if (subset.name.isEmpty()){
				subset.readElementListFromServer();
			}
			OrderedJSONObject dimensionJSON = new OrderedJSONObject();
			OrderedJSONObject subsetJSON = new OrderedJSONObject();
			subsetJSON.put("Hierarchy@odata.bind",	hierarchy.entity);
			JSONArray elementJSONArray = new JSONArray();
			for (int j=0; j<subset.elementCount(); j++){
				TM1Element element = subset.getElement(j);
				elementJSONArray.put(element.entity);
			}
			subsetJSON.put("Elements@odata.bind", elementJSONArray);
			dimensionJSON.put("Subset", subsetJSON);
			rowDimensionsJSON.put(dimensionJSON);
		}
		for (int i=0; i<columns.size(); i++){
			CubeViewerPosition columnPosition = columns.get(i);
			TM1Hierarchy hierarchy = columnPosition.hierarchy;
			TM1Subset subset = columnPosition.subset;
			if (subset.name.isEmpty()){
				subset.readElementListFromServer();
			}
			OrderedJSONObject dimensionJSON = new OrderedJSONObject();
			OrderedJSONObject subsetJSON = new OrderedJSONObject();
			subsetJSON.put("Hierarchy@odata.bind",	hierarchy.entity);
			JSONArray elementJSONArray = new JSONArray();
			for (int j=0; j<subset.elementCount(); j++){
				TM1Element element = subset.getElement(j);
				elementJSONArray.put(element.entity);
			}
			subsetJSON.put("Elements@odata.bind", elementJSONArray);
			dimensionJSON.put("Subset", subsetJSON);
			columnDimensionsJSON.put(dimensionJSON);
		}
		for (int i=0; i<titles.size(); i++){
			CubeViewerPosition titlePosition = titles.get(i);
			TM1Hierarchy hierarchy = titlePosition.hierarchy;
			TM1Subset subset = titlePosition.subset;
			TM1Element element = titlePosition.element;
			OrderedJSONObject dimensionJSON = new OrderedJSONObject();
			OrderedJSONObject subsetJSON = new OrderedJSONObject();
			subsetJSON.put("Hierarchy@odata.bind", hierarchy.entity);
			JSONArray jelements = new JSONArray();
			jelements.put(element.entity);
			subsetJSON.put("Elements@odata.bind", jelements);
			dimensionJSON.put("Selected@odata.bind", element.entity);
			dimensionJSON.put("Subset", subsetJSON);
			titleDimensionsJSON.put(dimensionJSON);
		}

		updatedViewJSON.put("Rows", rowDimensionsJSON);
		updatedViewJSON.put("Columns", columnDimensionsJSON);
		updatedViewJSON.put("Titles", titleDimensionsJSON);
		updatedViewJSON.put("SuppressEmptyColumns", false);
		updatedViewJSON.put("SuppressEmptyRows", false);
		updatedViewJSON.put("FormatString", "0.#########");

		String request = entity;

		tm1server.patch(request, updatedViewJSON);

	}





	public boolean writeViewToFile(String directory) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = entity;
		String query = "$expand=tm1.NativeView/Rows/Subset($select=Name;$expand=Hierarchy($expand=Dimension)),tm1.NativeView/Columns/Subset($select=Name;$expand=Hierarchy($expand=Dimension)),tm1.NativeView/Titles/Subset($select=Name;$expand=Hierarchy($expand=Dimension)),tm1.NativeView/Titles/Selected($select=Name;$expand=Hierarchy($expand=Dimension))";
		tm1server.get(request, query);
		FileWriter fw = new FileWriter(directory + "//" + name, false);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(tm1server.response.toString());
		bw.close();
		fw.close();
		return true;
	}


}
