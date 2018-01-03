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

public class TM1MDXView {

	
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

	String mdxquery;
	private String cellsetid;

	private List<TM1Cell> cells;
	private List<TM1ViewAxes> axes;

	private List<CubeViewerPosition> columns;
	private List<CubeViewerPosition> rows;
	private List<CubeViewerPosition> filters;
	private List<CubeViewerPosition> dimensionPositions;

	public TM1MDXView (String name, TM1Server tm1server){
		this.name = name;
		this.tm1server = tm1server;
		cells = new ArrayList<TM1Cell>();
		axes = new ArrayList<TM1ViewAxes>();

		columns = new ArrayList<CubeViewerPosition>();
		rows = new ArrayList<CubeViewerPosition>();
		filters = new ArrayList<CubeViewerPosition>();
		dimensionPositions = new ArrayList<CubeViewerPosition>(); 
		mdxquery = "";
	}

	public void setQuery(String mdxquery){
		this.mdxquery = mdxquery;
	}

	public String getQuery(){
		return mdxquery;
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

	public void executeMDXView() throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException{
		// http://vottstuartv22.canlab.ibm.com:40002/api/v1/ExecuteMDX?$expand=Axes($expand=Hierarchies($select=Name),
		//	Tuples($expand=Members($select=Name))),Cells

		cells.clear();
		String request = "ExecuteMDX";
		String query = "$expand=Axes($expand=Hierarchies($select=Name),Tuples($count;$expand=Members($select=Name;$expand=Element($select=Name,Type)))),Cells($select=Value,FormattedValue,FormatString,Status,Updateable,RuleDerived,Annotated,Consolidated)";
		OrderedJSONObject payload = new OrderedJSONObject();
		payload.put("MDX", mdxquery);
		System.out.println("Payload " + payload.toString());
		tm1server.post(request, query, payload);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		cellsetid = jresponse.getString("ID");
		JSONArray jaxes = jresponse.getJSONArray("Axes");
		for (int i = 0; i < jaxes.length(); i++) {
			TM1ViewAxes axe = new TM1ViewAxes();
			OrderedJSONObject jaxe = (OrderedJSONObject)jaxes.getJSONObject(i);
			axe.ordinal = jaxe.getInt("Ordinal");
			JSONArray jhierarchies = jaxe.getJSONArray("Hierarchies");
			for (int j = 0; j < jhierarchies.length(); j++){
				axe.addHierarchyName(jhierarchies.getJSONObject(j).getString("Name"));
			}
			JSONArray jtuples = jaxe.getJSONArray("Tuples");
			for (int j = 0; j < jtuples.length(); j++) {
				OrderedJSONObject jtuple = (OrderedJSONObject)jtuples.getJSONObject(j);
				TM1ViewTuple tuple = new TM1ViewTuple();
				tuple.ordinal = jtuple.getInt("Ordinal");
				JSONArray jmembers = jtuple.getJSONArray("Members");
				//System.out.println("Member " + jmembers.toString());
				for (int k = 0; k < jmembers.length(); k++) {
					OrderedJSONObject jmember = (OrderedJSONObject)jmembers.getJSONObject(k);
					OrderedJSONObject jelement = (OrderedJSONObject)jmember.getJSONObject("Element");
					TM1ViewMember member = new TM1ViewMember(jelement.getString("Name"), jelement.getString("Type"));
					tuple.members.add(member);

				}
				axe.addTuple(tuple);
			}
			axes.add(axe);

		}
		JSONArray jcells = jresponse.getJSONArray("Cells");
		for (int i = 0; i < jcells.length(); i++) {
			// Ordinal,Value,FormattedValue,FormatString,Status,Updateable,RuleDerived,Annotated,Consolidated
			OrderedJSONObject jcell = (OrderedJSONObject)jcells.getJSONObject(i);
			//int ordinal = jcell.getInt("Ordinal");
			Object value_object = jcell.get("Value");
			String value = "";
			if (value_object instanceof String) {
				value = (String)value_object;
			} else {
				try {
					value = Double.toString((Double)value_object);
				} catch (Exception ex){
					value = "ERR";
				}
			}
			String status = jcell.getString("Status");
			String formattedvalue = jcell.getString("FormattedValue");
			String formatstring = jcell.getString("FormatString");
			int updateable = jcell.getInt("Updateable");
			boolean rulederived = jcell.getBoolean("RuleDerived");
			boolean annotated = jcell.getBoolean("Annotated");
			boolean consolidated= jcell.getBoolean("Consolidated");

			TM1Cell cell = new TM1Cell(i, 0, status, value, formattedvalue, formatstring, updateable, rulederived, annotated, consolidated);
			cells.add(cell);
		}
	}

}
