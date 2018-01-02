package TM1Diagnostic.REST;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.apache.wink.json4j.JSONArray;

import TM1Diagnostic.ChoreTask;
import TM1Diagnostic.ProcessParameter;

public class TM1Chore extends TM1Object {

	public String startDateTime;
	public boolean dstSensitive;
	public String executionMode;
	public boolean active;
	public String frequency;
	public List<ChoreTask> choreTasks;

	public TM1Chore(String name, TM1Server tm1server) {
		super(name, TM1Object.CHORE, tm1server);
		choreTasks = new ArrayList<ChoreTask>();
	}

	public int choreTaskCount(){
		return choreTasks.size();
	}

	public ChoreTask getChoreTask(int i){
		return choreTasks.get(i);
	}

	/*public boolean writeToFile(String directory) throws TM1RestException {
		String request = entity;
		if (tm1server.get(request)) {
			String response = tm1server.response.toString();
			try {
				FileWriter fw = new FileWriter(directory + "//" + displayName + ".chore", false);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(response);
				bw.close();
				fw.close();
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
		return false;
	} */

	public void remove() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = entity;
		tm1server.delete(request);
	}

	public void readChoreTasksFromServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		choreTasks.clear();
		String request = entity;
		String query = "$expand=Tasks($expand=Process($select=Name))";
		tm1server.get(request, query);
		OrderedJSONObject jchore = new OrderedJSONObject(tm1server.response);
		startDateTime = jchore.getString("StartTime");
		dstSensitive = jchore.getBoolean("DSTSensitive");
		executionMode = jchore.getString("ExecutionMode");
		frequency = jchore.getString("Frequency");
		active = jchore.getBoolean("Active");
		JSONArray jtasks = jchore.getJSONArray("Tasks");
		for (int i = 0; i < jtasks.length(); i++) {
			OrderedJSONObject jtask = (OrderedJSONObject) jtasks.getJSONObject(i);
			OrderedJSONObject jprocess = (OrderedJSONObject)jtask.getJSONObject("Process");
			int step = jtask.getInt("Step");
			String processName = jprocess.getString("Name");
			TM1Process process = new TM1Process(processName, tm1server) ;
			ChoreTask task = new ChoreTask(step, process);
			JSONArray jProcessParameters = jtask.getJSONArray("Parameters");
			for (int j=0; j<jProcessParameters.length(); j++){
				OrderedJSONObject jProcessParameter = (OrderedJSONObject) jProcessParameters.getJSONObject(j);
				String paramName = jProcessParameter.getString("Name");
				String paramValue = "";
				int paramType = ProcessParameter.STRING;
				if (jProcessParameter.has("Value")){
					Object o = jProcessParameter.get("Value");
					if (o instanceof Integer){
						paramValue = Integer.toString(((Integer)o));
						paramType = ProcessParameter.NUMERIC;
					} else if (o instanceof String) {
						paramValue = (String)o;
						paramType = ProcessParameter.STRING;
					}
				}
				ProcessParameter pp = new ProcessParameter(paramName, paramType, paramValue, ""); 
				process.addparameter(pp);
			}
			choreTasks.add(task);
		}
	}

	public void execute() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException{
		String request = entity + "/tm1.Execute";
		OrderedJSONObject payload = new OrderedJSONObject();
		tm1server.post(request, payload);
	}

	public void activate() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException{
		String request = entity + "/tm1.Activate";
		OrderedJSONObject payload = new OrderedJSONObject();
		tm1server.post(request, payload);
	}


	public void deactivate() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException{ 
		String request = entity + "/tm1.Deactivate";
		OrderedJSONObject payload = new OrderedJSONObject();
		tm1server.post(request, payload);
	}

	public boolean isActive(){
		return active;
	}

	public boolean checkServerIsActive() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException{
		// $select=Active
		choreTasks.clear();
		String request = entity;
		String query = "$select=Active";
		tm1server.get(request, query);
		OrderedJSONObject jchore = new OrderedJSONObject(tm1server.response);
		active = jchore.getBoolean("Active");
		return active;
	}

	public void clearChoreTasks(){
		choreTasks.clear();
	}

	public void addChoreTask(ChoreTask choreTask){
		choreTasks.add(choreTask);
	}

	public void writeToServer(){
	}

	public void writeToServerAs(String choreName) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException{
		String request = entitySet;
		OrderedJSONObject payload = new OrderedJSONObject();
		payload.put("Name", choreName);
		payload.put("StartTime", startDateTime);
		payload.put("DSTSensitive", dstSensitive);
		payload.put("ExecutionMode", executionMode);
		payload.put("Frequency", frequency);
		payload.put("Active", active);
		JSONArray jtasks = new JSONArray();
		for (int i=0; i<choreTasks.size(); i++){
			ChoreTask task = choreTasks.get(i);
			OrderedJSONObject jtask = new OrderedJSONObject();
			jtask.put("Step", i);
			JSONArray jtaskparameters = new JSONArray();
			for (int j=0; j<task.taskParameters.size(); j++){
				System.out.println("Parameter " + j);
				ProcessParameter pp = task.taskParameters.get(j);
				OrderedJSONObject jpp = new OrderedJSONObject();
				jpp.put("Name", pp.getname());
				jpp.put("Value", pp.getvalue());
				jtaskparameters.put(jpp);
			}
			jtask.put("Parameters", jtaskparameters);
			String processReference = task.taskProcess.entity;
			jtask.put("Process@odata.bind", processReference);
			jtasks.put(jtask);

		}
		payload.put("Tasks", jtasks);
		System.out.println("Chore saveAs payload " + payload.toString());
		tm1server.post(request, payload);
	}
	
	public boolean writeChoreToFile(String dir) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		String choreDirName = dir + "//cho";
		File choreDir = new File(choreDirName);
		if (!choreDir.exists()){
			choreDir.mkdir();
		}
		String request = entity;
		tm1server.get(request);
		FileWriter fw = new FileWriter(dir + "//cho//" + displayName + ".cho", false);
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
