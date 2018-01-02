package TM1Diagnostic.REST;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.apache.wink.json4j.JSONArray;

import TM1Diagnostic.ProcessParameter;
import TM1Diagnostic.ProcessVariable;
import TM1Diagnostic.SearchResult;

public class TM1Process extends TM1Object {

	// "asciiDecimalSeparator":".","asciiDelimiterChar":",","asciiDelimiterType":"Character","asciiHeaderRecords":1,"asciiQuoteCharacter":"\"","asciiThousandSeparator":",","dataSourceNameForClient":".\\data_load\\budget_input\\Plan_Load_Budget_ascii.csv","dataSourceNameForServer":".\\data_load\\budget_input\\Plan_Load_Budget_ascii.csv"}}

	private String datasourcetype;
	// Text type
	private String asciiDecimalSeparator;
	private String asciiDelimiterChar;
	private String asciiDelimiterType;
	private int asciiHeaderRecords;
	private String asciiQuoteCharacter;
	private String dataSourceNameForClient;
	private String dataSourceNameForServer;
	// ODBC Type
	private String password;
	private String query;
	private String userName;
	private boolean usesUnicode;
	// TM1CubeView
	private String view;
	// TM1Subset
	private String subset;

	private String prolog_procedure;
	private String metadata_procedure;
	private String data_procedure;
	private String epilog_procedure;

	private List<ProcessParameter> parameters;
	private List<ProcessVariable> variables;

	public TM1Process(String name, TM1Server tm1server) {
		super(name, TM1Object.PROCESS, tm1server);
		parameters = new ArrayList<ProcessParameter>();
		variables = new ArrayList<ProcessVariable>();
	}

	public int variable_count() {
		return variables.size();
	}

	public ProcessVariable getvariable(int i) {
		return variables.get(i);
	}

	public void clear_variables() {
		variables.clear();
	}

	public void addvariable(ProcessVariable variable) {
		variables.add(variable);
	}

	public int parametercount() {
		return parameters.size();
	}

	public ProcessParameter getparameter(int i) {
		return parameters.get(i);
	}

	public String getViewSource() {
		return view;
	}

	public String getSubsetSource() {
		return subset;
	}

	public void clear_parameters() {
		parameters.clear();
	}

	public void addparameter(ProcessParameter parameter) {
		parameters.add(parameter);
	}

	public String getprocess_prolog() {
		return prolog_procedure;
	}

	public void set_prolog(String prolog) {
		prolog_procedure = prolog;
	}

	public String getprocess_metadata() {
		return metadata_procedure;
	}

	public void set_metadata(String metadata) {
		metadata_procedure = metadata;
	}

	public String getprocess_data() {
		return data_procedure;
	}

	public void set_data(String data) {
		data_procedure = data;
	}

	public String getprocess_epilog() {
		return epilog_procedure;
	}

	public void set_epilog(String epilog) {
		epilog_procedure = epilog;
	}

	public void removeParameter(int i) {
		parameters.remove(i);
	}

	public OrderedJSONObject buildProcessJson(String newprocessname) throws JSONException {
		OrderedJSONObject jp = new OrderedJSONObject();
		if (newprocessname.equals("")) {
			jp.put("Name", displayName);
		} else {
			jp.put("Name", newprocessname);
		}
		jp.put("HasSecurityAccess", false);
		jp.put("PrologProcedure", prolog_procedure);
		jp.put("MetadataProcedure", metadata_procedure);
		jp.put("DataProcedure", data_procedure);
		jp.put("EpilogProcedure", epilog_procedure);

		OrderedJSONObject ds = new OrderedJSONObject();
		if (datasourcetype.equals("None")) {
			ds.put("Type", "None");
		} else if (datasourcetype.equals("ASCII")) {
			ds.put("Type", "ASCII");
			ds.put("asciiDecimalSeparator", asciiDecimalSeparator);
			ds.put("asciiDelimiterChar", asciiDelimiterChar);
			ds.put("asciiDelimiterType", asciiDelimiterType);
			ds.put("asciiHeaderRecords", asciiHeaderRecords);
			ds.put("asciiQuoteCharacter", asciiQuoteCharacter);
			ds.put("dataSourceNameForClient", dataSourceNameForClient);
			ds.put("dataSourceNameForServer", dataSourceNameForServer);
		} else if (datasourcetype.equals("ODBC")) {
			ds.put("Type", "ODBC");
			ds.put("userName", userName);
			ds.put("password", asciiDelimiterChar);
			ds.put("usesUnicode", usesUnicode);
			ds.put("query", query);
		} else if (datasourcetype.equals("TM1CubeView")) {
			ds.put("Type", "TM1CubeView");
			ds.put("dataSourceNameForClient", dataSourceNameForServer);
			ds.put("dataSourceNameForServer", dataSourceNameForServer);
			ds.put("view", view);
		} else if (datasourcetype.equals("TM1DimensionSubset")) {
			ds.put("Type", "TM1DimensionSubset");
			ds.put("dataSourceNameForClient", dataSourceNameForServer);
			ds.put("dataSourceNameForServer", dataSourceNameForServer);
			ds.put("subset", subset);
		} else {
			ds.put("Type", "None");
		}
		jp.put("DataSource", ds);

		JSONArray jparams = new JSONArray();
		for (int i = 0; i < parameters.size(); i++) {
			OrderedJSONObject jparam = new OrderedJSONObject();
			System.out.println("N " + parameters.get(i).getname() + " T " + parameters.get(i).gettype() + " V " + parameters.get(i).getvalue());
			jparam.put("Name", parameters.get(i).getname());
			if (parameters.get(i).gettype() == ProcessParameter.NUMERIC) {
				jparam.put("Value", Double.parseDouble(parameters.get(i).getvalue()));
			} else if (parameters.get(i).gettype() == ProcessParameter.STRING) {
				jparam.put("Value", parameters.get(i).getvalue());
			}
			jparam.put("Prompt", parameters.get(i).getprompt());
			jparams.put(jparam);
		}
		jp.put("Parameters", jparams);

		JSONArray jvariables = new JSONArray();
		for (int i = 0; i < variables.size(); i++) {
			OrderedJSONObject jvariable = new OrderedJSONObject();
			jvariable.put("Name", variables.get(i).getname());
			jvariable.put("Type", variables.get(i).gettype());
			jvariable.put("Position", variables.get(i).getposition());
			jvariable.put("StartByte", 0);
			jvariable.put("EndByte", 0);
			jvariables.put(jvariable);
		}
		jp.put("Variables", jvariables);
		return jp;
	}

	public void createOnServer(String processName) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = "Processes";
		OrderedJSONObject payload = buildProcessJson(processName);
		tm1server.post(request, payload);
	}

	public void updateOnServer(String processName) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = entity;
		OrderedJSONObject payload = buildProcessJson(displayName);
		tm1server.patch(request, payload);
	}

	public void execute() throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		OrderedJSONObject payload = new OrderedJSONObject();
		JSONArray json_parameter_array = new JSONArray();
		for (int i = 0; i < parameters.size(); i++) {
			OrderedJSONObject json_parameter = new OrderedJSONObject();
			json_parameter.put("Name", parameters.get(i).getname());
			if (parameters.get(i).gettype() == ProcessParameter.STRING) {
				json_parameter.put("Value", parameters.get(i).getvalue());
			} else {
				json_parameter.put("Value", Double.parseDouble(parameters.get(i).getvalue()));
			}
			json_parameter_array.put(json_parameter);
		}
		payload.put("Parameters", json_parameter_array);

		String request = entity + "/tm1.Execute";
		tm1server.threadSafePost(request, payload);

	}

	public boolean checkServerForSecurityAccess() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		String request = entity;
		String query = "$select=HasSecurityAccess";
		tm1server.get(request, query);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		if (jresponse.has("HasSecurityAccess")) {
			return jresponse.getBoolean("HasSecurityAccess");
		}
		return false;
	}

	public void writeSecurityAccessToServer(boolean security) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = entity;
		OrderedJSONObject payload = new OrderedJSONObject();
		payload.put("HasSecurityAccess", security);
		tm1server.patch(request, payload);
	}

	public void remove() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = entity;
		tm1server.delete(request);
	}

	public void readProcessFromServer() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		String request = entity;
		tm1server.get(request);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		if (!jresponse.has("Name"))
			return;
		// Data Source
		if (jresponse.has("DataSource")) {
			OrderedJSONObject json_datasource = (OrderedJSONObject) jresponse.getJSONObject("DataSource");
			datasourcetype = json_datasource.getString("Type");
			if (datasourcetype.equals("NONE")) {
			} else if (datasourcetype.equals("ASCII")) {
				asciiDecimalSeparator = json_datasource.getString("asciiDecimalSeparator");
				asciiDelimiterChar = json_datasource.getString("asciiDelimiterChar");
				asciiDelimiterType = json_datasource.getString("asciiDelimiterType");
				asciiHeaderRecords = json_datasource.getInt("asciiHeaderRecords");
				asciiQuoteCharacter = json_datasource.getString("asciiQuoteCharacter");
				dataSourceNameForClient = json_datasource.getString("dataSourceNameForClient");
				dataSourceNameForServer = json_datasource.getString("dataSourceNameForServer");
			} else if (datasourcetype.equals("ODBC")) {
				password = json_datasource.getString("password");
				query = json_datasource.getString("query");
				userName = json_datasource.getString("userName");
				usesUnicode = json_datasource.getBoolean("usesUnicode");
			} else if (datasourcetype.equals("TM1CubeView")) {
				dataSourceNameForClient = json_datasource.getString("dataSourceNameForClient");
				dataSourceNameForServer = json_datasource.getString("dataSourceNameForServer");
				view = json_datasource.getString("view");
			} else if (datasourcetype.equals("TM1DimensionSubset")) {
				dataSourceNameForClient = json_datasource.getString("dataSourceNameForClient");
				dataSourceNameForServer = json_datasource.getString("dataSourceNameForServer");
				subset = json_datasource.getString("subset");
			}
		}

		// Variables
		variables.clear();
		if (jresponse.has("Variables")) {
			JSONArray json_variables = jresponse.getJSONArray("Variables");
			for (int i = 0; i < json_variables.length(); i++) {
				OrderedJSONObject json_variable = (OrderedJSONObject) json_variables.getJSONObject(i);
				String name = json_variable.getString("Name");
				String type = json_variable.getString("Type");

				int position = json_variable.getInt("Position");
				int startbyte = json_variable.getInt("StartByte");
				int endbyte = json_variable.getInt("EndByte");
				ProcessVariable variable = new ProcessVariable(name, type, position, startbyte, endbyte);
				variables.add(variable);
			}
		}

		// Parameters
		parameters.clear();
		if (jresponse.has("Parameters")) {
			JSONArray json_parameters = jresponse.getJSONArray("Parameters");
			String parameter_name;
			String parameter_prompt;
			int parameter_type;
			String parameter_value;
			for (int i = 0; i < json_parameters.length(); i++) {
				OrderedJSONObject json_parameter = (OrderedJSONObject) json_parameters.getJSONObject(i);
				parameter_name = json_parameter.getString("Name");
				parameter_prompt = json_parameter.getString("Prompt");
				Object o = json_parameter.get("Value");
				if (o instanceof String) {
					parameter_type = ProcessParameter.STRING;
					parameter_value = (String) o;
				} else if (o instanceof Integer) {
					parameter_type = ProcessParameter.NUMERIC;
					parameter_value = String.valueOf((Integer) o);
				} else if (o instanceof Double) {
					parameter_type = ProcessParameter.NUMERIC;
					parameter_value = String.valueOf((Double) o);
				} else {
					System.out.println("Error trying to determine Ti process parameter type");
					return;
				}
				ProcessParameter p = new ProcessParameter(parameter_name, parameter_type, parameter_value, parameter_prompt);
				parameters.add(p);
			}
		}

		// Procedures
		prolog_procedure = "";
		if (jresponse.has("PrologProcedure")) {
			prolog_procedure = jresponse.getString("PrologProcedure");
		}
		metadata_procedure = "";
		if (jresponse.has("MetadataProcedure")) {
			metadata_procedure = jresponse.getString("MetadataProcedure");
		}
		data_procedure = "";
		if (jresponse.has("DataProcedure")) {
			data_procedure = jresponse.getString("DataProcedure");
		}
		epilog_procedure = "";
		if (jresponse.has("EpilogProcedure")) {
			epilog_procedure = jresponse.getString("EpilogProcedure");
		}
	}

	public String getdatasourcetype() {
		return datasourcetype;
	}

	public void setdatasourcetype(String type) {
		datasourcetype = type;
	}

	public String getasciiDecimalSeparator() {
		return asciiDecimalSeparator;
	}

	public void setasciiDecimalSeparator(String asciiDecimalSeparator) {
		this.asciiDecimalSeparator = asciiDecimalSeparator;
	}

	public String getasciiDelimiterChar() {
		return asciiDelimiterChar;
	}

	public void setasciiDelimiterChar(String asciiDelimiterChar) {
		this.asciiDelimiterChar = asciiDelimiterChar;
	}

	public String getasciiDelimiterType() {
		return asciiDelimiterType;
	}

	public void setasciiDelimiterType(String asciiDelimiterType) {
		this.asciiDelimiterType = asciiDelimiterType;
	}

	public int getasciiHeaderRecords() {
		return asciiHeaderRecords;
	}

	public void setasciiHeaderRecords(int asciiHeaderRecords) {
		this.asciiHeaderRecords = asciiHeaderRecords;
	}

	public String getasciiQuoteCharacter() {
		return asciiQuoteCharacter;
	}

	public void setasciiQuoteCharacter(String asciiQuoteCharacter) {
		this.asciiQuoteCharacter = asciiQuoteCharacter;
	}

	public String getdataSourceNameForClient() {
		return dataSourceNameForClient;
	}

	public void setdataSourceNameForClient(String dataSourceNameForClient) {
		this.dataSourceNameForClient = dataSourceNameForClient;
	}

	public String getdataSourceNameForServer() {
		return dataSourceNameForServer;
	}

	public void setdataSourceNameForServer(String dataSourceNameForServer) {
		this.dataSourceNameForServer = dataSourceNameForServer;
	}

	// ODBC Type

	public String getpassword() {
		return password;
	}

	public void setpassword(String password) {
		this.password = password;
	}

	public String getquery() {
		return query;
	}

	public void setquery(String query) {
		this.query = query;
	}

	public String getuserName() {
		return userName;
	}

	public void setuserName(String userName) {
		this.userName = userName;
	}

	public boolean usesUnicode() {
		return usesUnicode;
	}

	public boolean duplicate(String newname) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		String request = entity;
		tm1server.get(request);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		jresponse.put("Name", newname);
		request = entitySet;
		tm1server.post(request, jresponse);
		return true;

	}

	public List<SearchResult> findParentProcesses() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		List<SearchResult> dependantProcessList = new ArrayList<SearchResult>();
		String request = entitySet;
		String query = "$select=Name,PrologProcedure,MetadataProcedure,DataProcedure,EpilogProcedure";
		tm1server.get(request, query);
		OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
		JSONArray JSONProcessArray = jresponse.getJSONArray("value");
		String search = "ExecuteProcess('" + displayName.replaceAll(" ", "") + "')";
		int foundCount = 0;
		for (int i = 0; i < JSONProcessArray.length(); i++) {
			OrderedJSONObject processJSON = (OrderedJSONObject) JSONProcessArray.getJSONObject(i);
			boolean foundReference = false;
			String lines[];
			String currentProcessName = processJSON.getString("Name");
			String prolog = "";
			// System.out.println("Process check " + currentProcessName);
			if (!processJSON.isNull("PrologProcedure"))
				prolog = processJSON.getString("PrologProcedure");
			lines = prolog.split("\\r\\n");
			for (int j = 0; j < lines.length; j++) {
				if (!lines[j].startsWith("#") && !lines[j].isEmpty()) {
					if (lines[j].replaceAll(" ", "").contains(search)) {
						TM1Process p = new TM1Process(currentProcessName, tm1server);
						SearchResult r = new SearchResult(p, "Prolog line " + j);
						dependantProcessList.add(r);
						foundReference = true;
						// break;
					}
				}
			}
			if (foundReference) {
				break;
			}

			String metadata = "";
			if (!processJSON.isNull("MetadataProcedure"))
				metadata = processJSON.getString("MetadataProcedure");
			lines = metadata.split("\r\n");
			for (int j = 0; j < lines.length; j++) {
				if (!lines[j].startsWith("#")) {
					if (lines[j].replaceAll(" ", "").contains(search)) {
						TM1Process p = new TM1Process(currentProcessName, tm1server);
						SearchResult r = new SearchResult(p, "Metadata line " + j);
						dependantProcessList.add(r);
						foundReference = true;
					}
				}
			}

			String data = "";
			if (!processJSON.isNull("DataProcedure"))
				data = processJSON.getString("DataProcedure");
			lines = data.split("\r\n");
			for (int j = 0; j < lines.length; j++) {
				if (!lines[j].startsWith("#")) {
					if (lines[j].replaceAll(" ", "").contains(search)) {
						TM1Process p = new TM1Process(currentProcessName, tm1server);
						SearchResult r = new SearchResult(p, "Data line " + j);
						dependantProcessList.add(r);
						foundReference = true;
					}
				}
			}

			String epilog = "";
			if (!processJSON.isNull("EpilogProcedure"))
				epilog = processJSON.getString("EpilogProcedure");
			lines = epilog.split("\r\n");
			for (int j = 0; j < lines.length; j++) {
				if (!lines[j].startsWith("#")) {
					if (lines[j].replaceAll(" ", "").contains(search)) {
						TM1Process p = new TM1Process(currentProcessName, tm1server);
						SearchResult r = new SearchResult(p, "Epilog line " + j);
						dependantProcessList.add(r);
						foundReference = true;
					}
				}
			}

			if (foundReference) {
				foundCount++;
			}
		}
		return dependantProcessList;
	}

	public boolean writeProcessToFile(String dir) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		String processDirName = dir + "//pro";
		File processDir = new File(processDirName);
		if (!processDir.exists()){
			processDir.mkdir();
		}
		String request = entity;
		tm1server.get(request);
		FileWriter fw = new FileWriter(dir + "//pro//" + displayName + ".pro", false);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(tm1server.response.toString());
		bw.close();
		fw.close();
		return true;
	}
	
	public OrderedJSONObject getExportJSON() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		tm1server.get(entity);
		return new OrderedJSONObject(tm1server.response);
	}

}
