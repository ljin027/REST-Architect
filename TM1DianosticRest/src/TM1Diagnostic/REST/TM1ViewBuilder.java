package TM1Diagnostic.REST;

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

public class TM1ViewBuilder {

	private TM1Server tm1server;
	private TM1Cube cube;
	private String name;

	private String cellsetid;
	private List<TM1Cell> cells;
	private List<TM1ViewAxes> axes;
	private List<CubeViewerPosition> columns;
	private List<CubeViewerPosition> rows;
	private List<CubeViewerPosition> filters;
	private List<CubeViewerPosition> dimension_positions;

	private List<TM1Sandbox> sandboxes;

	public boolean suppressEmptyColumns;
	public boolean suppressEmptyRows;

	public TM1ViewBuilder(TM1Cube cube) {
		name = "";
		this.cube = cube;
		this.tm1server = cube.getServer();

		cells = new ArrayList<TM1Cell>();
		axes = new ArrayList<TM1ViewAxes>();

		//columns = new ArrayList<CubeViewerPosition>();
		//rows = new ArrayList<CubeViewerPosition>();
		//filters = new ArrayList<CubeViewerPosition>();

		dimension_positions = new ArrayList<CubeViewerPosition>(); 
		sandboxes = new ArrayList<TM1Sandbox>(); 

	}

	public TM1View getDefaultView() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		TM1View view = new TM1View("", cube, tm1server);
		readDimensionsPositionFromServer();
		return view;
	}

	public void readDimensionsPositionFromServer() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		for (int i=cube.dimensionCount(); i<=0; i--){
			TM1Dimension dimension = cube.getDimension(i);
			TM1Hierarchy hierarchy = dimension.getDefaultHierarchy();
			hierarchy.readHierarchyFromServer();
			if (i==1){
				TM1Subset subset = hierarchy.getSubsetAll();
				CubeViewerPosition position = new CubeViewerPosition(1, dimension, hierarchy, subset, null);
				dimension_positions.add(position);
			} else if (i==0){
				TM1Subset subset = hierarchy.getSubsetAll();
				CubeViewerPosition position = new CubeViewerPosition(0, dimension, hierarchy, subset, null);
				dimension_positions.add(position);
			} else {
				TM1Element element = hierarchy.getFirstTopElement();
				CubeViewerPosition position = new CubeViewerPosition(2, dimension, hierarchy, null, element);
				dimension_positions.add(position);
			}

		}
	}

	public int cellCount(){
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

	public CubeViewerPosition getcolumn(int i) {
		return columns.get(i);
	}

	public int getrowscount() {
		return rows.size();
	}

	public CubeViewerPosition getrow(int i) {
		return rows.get(i);
	}

	public int getfilterscount() {
		return filters.size();
	}

	public CubeViewerPosition getfilter(int i) {
		return filters.get(i);
	}

	public int sandboxCount(){
		return sandboxes.size();
	}

	public TM1Sandbox getSandbox(int i){
		return sandboxes.get(i);
	}


	public boolean readSandboxesFromServer() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		String request = "Sandboxes";
		tm1server.get(request);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		JSONArray jsandboxes = jresponse.getJSONArray("value");
		for (int i = 0; i < jsandboxes.length(); i++) {
			OrderedJSONObject jsandbox = (OrderedJSONObject)jsandboxes.getJSONObject(i);
			String sandboxname = jsandbox.getString("Name");
			boolean isactive = jsandbox.getBoolean("IsActive");
			TM1Sandbox sandbox = new TM1Sandbox(sandboxname, isactive);
			sandboxes.add(sandbox);
		}
		return true;
	}


	public void write_updates_to_server() throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = "Cellsets('" + cellsetid + "')/Cells";
		JSONArray payload = new JSONArray();
		for (int i=0; i<cells.size(); i++){
			TM1Cell cell = cells.get(i);
			if (cell.updated){
				OrderedJSONObject jcell = new OrderedJSONObject();
				jcell.put("Ordinal", cell.ordinal);
				if (cell.type == TM1Cell.NUMERIC){
					jcell.put("Value", Double.parseDouble(cell.value));
				} else {
					jcell.put("Value", cell.value);
				}
				payload.put(jcell);
			}
		}
		tm1server.patch(request, payload);
	}


}
