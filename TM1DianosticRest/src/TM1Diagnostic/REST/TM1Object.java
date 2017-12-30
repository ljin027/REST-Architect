package TM1Diagnostic.REST;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Objects;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.OrderedJSONObject;

//import org.json.JSONObject;

public class TM1Object {

	static public int SERVER = 0;
	static public int CUBE = 1;
	static public int DIMENSION = 2;
	static public int PROCESS = 3;
	static public int CHORE = 4;
	static public int VIEW = 5;
	static public int HIERARCHY = 6;
	static public int SUBSET = 7;
	static public int ELEMENT = 8;
	static public int APPLICATION = 9;
	static public int CELL = 10;
	static public int BLOB = 11;
	static public int FOLDER = 12;
	static public int REFERENCE = 13;
	static public int DOCUMENT = 14;
	static public int MDXVIEW = 15;

	static public String APP_ICON = ".\\images\\icon_app.gif";
	static public String DIM_ICON = ".\\images\\icon_dimension.gif";
	static public String CUB_ICON = ".\\images\\icon_cube.gif";
	static public String PRO_ICON = ".\\images\\icon_process.gif";
	static public String CHO_ICON = ".\\images\\icon_chore.gif";
	static public String VIE_ICON = ".\\images\\icon_view.gif";
	static public String SUB_ICON = ".\\images\\icon_subset.gif";
	static public String HIE_ICON = ".\\images\\icon_hierarchy.gif";
	static public String RUL_ICON = ".\\images\\icon_rules.gif";
	static public String ELE_ICON = ".\\images\\icon_element.gif";
	static public String FOL_ICON = ".\\images\\icon_folder.gif";

	public int type;
	public String name;
	public String displayName;
	protected TM1Object parent;
	protected TM1Server tm1server;

	public boolean expandedInExplorerTree;

	protected OrderedJSONObject json;
	protected OrderedJSONObject transferJson;

	protected String entity;
	protected String entitySet;
	protected String icon;

	private String extension = "";

	public TM1Object(String name, int type, TM1Server tm1server) {
		try {
			expandedInExplorerTree = false;
			this.displayName = name;
			this.name = name.replaceAll(" ", "");
			this.type = type;
			this.tm1server = tm1server;
			this.parent = null;
			json = new OrderedJSONObject("{}");
			if (type == TM1Object.CUBE) {
				entity = "Cubes('" + displayName + "')";
				entitySet = "Cubes";
				icon = TM1Object.CUB_ICON;
				extension = "cube";
			} else if (type == TM1Object.DIMENSION) {
				entity = "Dimensions('" + displayName + "')";
				entitySet = "Dimensions";
				icon = TM1Object.DIM_ICON;
				extension = "dimension";
			} else if (type == TM1Object.PROCESS) {
				entity = "Processes('" + displayName + "')";
				entitySet = "Processes";
				icon = TM1Object.PRO_ICON;
				extension = "process";
			} else if (type == TM1Object.CHORE) {
				entity = "Chores('" + displayName + "')";
				entitySet = "Chores";
				icon = TM1Object.CHO_ICON;
				extension = "chore";
			} else if (type == TM1Object.VIEW) {
				entity = "Views('" + displayName + "')";
				entitySet = "Views";
				icon = TM1Object.VIE_ICON;
				extension = "view";
			} else if (type == TM1Object.HIERARCHY) {
				entity = "Hierarchies('" + displayName + "')";
				entitySet = "Hierarchies";
				icon = TM1Object.HIE_ICON;
				extension = "hierarchy";
			} else if (type == TM1Object.SUBSET) {
				entity = "Subsets('" + displayName + "')";
				entitySet = "Subsets";
				icon = TM1Object.SUB_ICON;
				extension = "subset";
			} else if (type == TM1Object.ELEMENT) {
				entity = "Elements('" + displayName + "')";
				entitySet = "Elements";
				icon = TM1Object.ELE_ICON;
				extension = "element";
			} else if (type == TM1Object.APPLICATION) {
				entity = "Contents('" + displayName + "')";
				entitySet = "Contents";
				icon = TM1Object.FOL_ICON;
				extension = "blob";
			} else if (type == TM1Object.BLOB) {
				entity = "Contents('Blobs')/Contents('" + displayName + "')";
				entitySet = "Contents('Blobs')/Contents";
				icon = TM1Object.FOL_ICON;
				extension = "blob";
			} else {
			}
		} catch (Exception ex) {
			ex.printStackTrace();

		}

	}

	public TM1Object(String name, int type, TM1Object parent, TM1Server tm1server) {
		try {
			expandedInExplorerTree = false;
			this.displayName = name;
			this.name = name.replaceAll(" ", "");
			this.type = type;
			this.tm1server = tm1server;
			this.parent = parent;
			json = new OrderedJSONObject("{}");
			if (type == TM1Object.CUBE) {
				entity = "Cubes('" + displayName + "')";
				entitySet = "Cubes";
				icon = TM1Object.CUB_ICON;
				extension = "cube";
			} else if (type == TM1Object.DIMENSION) {
				entity = "Dimensions('" + displayName + "')";
				entitySet = "Dimensions";
				icon = TM1Object.DIM_ICON;
				extension = "dimension";
			} else if (type == TM1Object.PROCESS) {
				entity = "Processes('" + displayName + "')";
				entitySet = "Processes";
				icon = TM1Object.PRO_ICON;
				extension = "process";
			} else if (type == TM1Object.CHORE) {
				entity = "Chores('" + displayName + "')";
				entitySet = "Chores";
				icon = TM1Object.CHO_ICON;
				extension = "chore";
			} else if (type == TM1Object.VIEW) {
				entity = "Views('" + displayName + "')";
				entitySet = "Views";
				icon = TM1Object.VIE_ICON;
				extension = "view";
			} else if (type == TM1Object.HIERARCHY) {
				entity = "Hierarchies('" + displayName + "')";
				entitySet = "Hierarchies";
				icon = TM1Object.HIE_ICON;
				extension = "hierarchy";
			} else if (type == TM1Object.SUBSET) {
				entity = "Subsets('" + displayName + "')";
				entitySet = "Subsets";
				icon = TM1Object.SUB_ICON;
				extension = "subset";
			} else if (type == TM1Object.ELEMENT) {
				entity = "Elements('" + displayName + "')";
				entitySet = "Elements";
				icon = TM1Object.ELE_ICON;
				extension = "element";
			} else if (type == TM1Object.APPLICATION) {
				entity = "Contents('" + displayName + "')";
				entitySet = "Contents";
				icon = TM1Object.FOL_ICON;
				extension = "blob";
			} else if (type == TM1Object.BLOB) {
				entity = "Contents('Blobs')/Contents('" + displayName + "')";
				entitySet = "Contents('Blobs')/Contents";
				icon = TM1Object.FOL_ICON;
				extension = "blob";
			} else {
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String get_icon() {
		return icon;
	}

	public TM1Server getServer() {
		return tm1server;
	}

	public TM1Object getParent() {
		return parent;
	}



	static public OrderedJSONObject read_json_from_file(String filename) {
		try {
			FileReader fr = new FileReader(filename);
			BufferedReader br = new BufferedReader(fr);
			OrderedJSONObject j = new OrderedJSONObject(br.readLine());
			br.close();
			fr.close();
			return j;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public String getEntity() {
		return entity;
	}

	public boolean change_name_in_json(String newname) {
		try {
			json.put("Name", newname);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public boolean writeToFile(String dir) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		String request = entity;
		tm1server.get(request);
			FileWriter fw = new FileWriter(dir + "//" + displayName, false);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(tm1server.response.toString());
			bw.close();
			fw.close();
			return true;
	}

	@Override
	public boolean equals(Object o) {

		if (o == this)
			return true;
		if (!(o instanceof TM1Object)) {
			return false;
		}
		TM1Object tm1object = (TM1Object) o;
		return tm1object.displayName.equals(this.displayName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(displayName);
	}

}
