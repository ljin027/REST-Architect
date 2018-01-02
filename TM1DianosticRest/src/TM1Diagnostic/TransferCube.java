package TM1Diagnostic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;

import TM1Diagnostic.REST.TM1Cube;
import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

public class TransferCube {

	public String name;
	public OrderedJSONObject json;
	public List<TransferView> views;
	public List<TransferDimension> dimensions;
	private TM1Cube cube;
	private String importData;

	public TransferCube(String name, OrderedJSONObject json) {
		this.name = name;
		this.json = json;
		views = new ArrayList<TransferView>();
		dimensions = new ArrayList<TransferDimension>();
	}

	public TransferCube(String name, TM1Cube cube) {
		this.name = name;
		this.cube = cube;
		views = new ArrayList<TransferView>();
		dimensions = new ArrayList<TransferDimension>();
	}

	public void readImportData(File dataFile) throws IOException {
		InputStream is = new FileInputStream(dataFile);
		BufferedReader buf = new BufferedReader(new InputStreamReader(is)); 
		String line = buf.readLine();
		StringBuilder sb = new StringBuilder();
		while (line != null) {
			sb.append(line).append("\n");
			line = buf.readLine();
		}
		importData = sb.toString();
	}

	public void importToServer(TM1Server tm1server) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		String request = "Cubes";
		tm1server.post(request, this.json);

		FileReader fileReader = new FileReader(".//data/_ImportDataTi");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		StringBuffer stringBuffer = new StringBuffer();
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			stringBuffer.append(line).append("\n");
		}

		String uniqueID = "}Import_" + UUID.randomUUID().toString();
		String importFileName = uniqueID + ".blb";
		String processEntity = "Processes('" + uniqueID + "')";
		String blbEntity = "Contents('Blobs')/Contents('" + uniqueID + "')";

		// Create ti process that imports data to the cube from a blb file
		OrderedJSONObject importDataProcessJSON = new OrderedJSONObject(stringBuffer.toString());
		importDataProcessJSON.replace("Name", "_ImportCube", uniqueID);
		tm1server.post("Processes", importDataProcessJSON);

		// Create blb for importing data
		OrderedJSONObject exportDataBLBJSON = new OrderedJSONObject();
		exportDataBLBJSON.put("@odata.type", "#ibm.tm1.api.v1.Document");
		exportDataBLBJSON.put("ID", uniqueID);
		exportDataBLBJSON.put("Name", uniqueID);
		tm1server.post("Contents('Blobs')/Contents", exportDataBLBJSON);

		// Write to the blb file
		tm1server.patch("Contents('Blobs')/Contents('" + uniqueID + "')/Content", importData);

		// Run the Ti process that exports data to the blb file
		OrderedJSONObject exportProcessParametersJSON = new OrderedJSONObject();
		JSONArray exportProcessParameterArray = new JSONArray();
		OrderedJSONObject p1 = new OrderedJSONObject();
		p1.put("Name", "pCubeName");
		p1.put("Value", this.name);
		OrderedJSONObject p2 = new OrderedJSONObject();
		p2.put("Name", "pFileName");
		p2.put("Value", importFileName);
		exportProcessParameterArray.add(p1);
		exportProcessParameterArray.add(p2);
		exportProcessParametersJSON.put("Parameters", exportProcessParameterArray);
		tm1server.post("Processes('" + uniqueID + "')/tm1.Execute", exportProcessParametersJSON);

		tm1server.delete(blbEntity);
		tm1server.delete(processEntity);
		
	}

	public boolean writeCubeToFile(String dir) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {

		File cubeDir = new File(dir + "//cub");
		if (!cubeDir.exists()) {
			cubeDir.mkdir();
		}

		TM1Server tm1server = cube.getServer();

		String request = cube.getEntity();
		tm1server.get(request);
		OrderedJSONObject exportJSON = new OrderedJSONObject(tm1server.response);
		JSONArray dimensionJSONArrayExport = new JSONArray();
		cube.readCubeDimensionsFromServer();
		for (int i = 0; i < cube.dimensionCount(); i++) {
			System.out.println("Adding dim: " + i);
			TM1Dimension dimension = cube.getDimension(i);
			dimensionJSONArrayExport.add(dimension.getEntity());
		}
		exportJSON.put("Dimensions@odata.bind", dimensionJSONArrayExport);
		FileWriter fw = new FileWriter(cubeDir + "//" + cube.displayName + ".cube", false);
		BufferedWriter bw = new BufferedWriter(fw);
		try {
			bw.write(exportJSON.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			bw.close();
			bw = null;
		}

		File dimensionDir = new File(dir + "//dimensions");
		if (!dimensionDir.exists()) {
			dimensionDir.mkdir();
		}

		cube.readCubeDimensionsFromServer();
		for (int i = 0; i < cube.dimensionCount(); i++) {
			TM1Dimension dimension = cube.getDimension(i);
			dimension.writeDimensionToFile(dir);
		}

		FileReader fileReader = new FileReader(".//data/_exportDataTi");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		StringBuffer stringBuffer = new StringBuffer();
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			stringBuffer.append(line).append("\n");
		}

		String uniqueID = "}Export_" + UUID.randomUUID().toString();
		String exportFileName = uniqueID + ".blb";
		String processEntity = "Processes('" + uniqueID + "')";
		String blbEntity = "Contents('Blobs')/Contents('" + uniqueID + "')";

		// Create blb for exported data
		OrderedJSONObject exportDataBLBJSON = new OrderedJSONObject();
		exportDataBLBJSON.put("@odata.type", "#ibm.tm1.api.v1.Document");
		exportDataBLBJSON.put("ID", uniqueID);
		exportDataBLBJSON.put("Name", uniqueID);
		tm1server.post("Contents('Blobs')/Contents", exportDataBLBJSON);

		// Create ti process that exports to the blb file
		OrderedJSONObject exportDataProcessJSON = new OrderedJSONObject(stringBuffer.toString());
		exportDataProcessJSON.replace("Name", "_exportCube", uniqueID);
		tm1server.post("Processes", exportDataProcessJSON);

		// Run the Ti process that exports data to the blb file
		OrderedJSONObject exportProcessParametersJSON = new OrderedJSONObject();
		JSONArray exportProcessParameterArray = new JSONArray();
		OrderedJSONObject p1 = new OrderedJSONObject();
		p1.put("Name", "pCubeName");
		p1.put("Value", cube.displayName);
		OrderedJSONObject p2 = new OrderedJSONObject();
		p2.put("Name", "pFileName");
		p2.put("Value", exportFileName);
		exportProcessParameterArray.add(p1);
		exportProcessParameterArray.add(p2);
		exportProcessParametersJSON.put("Parameters", exportProcessParameterArray);
		tm1server.post("Processes('" + uniqueID + "')/tm1.Execute", exportProcessParametersJSON);

		// Read the blb file from server to client
		tm1server.get("Contents('Blobs')/Contents('" + uniqueID + "')/Content");
		FileWriter fw2 = new FileWriter(cubeDir + "//" + cube.displayName + ".data", false);
		BufferedWriter bw2 = new BufferedWriter(fw2);
		String blbResponse = tm1server.response;
		try {
			bw2.write(blbResponse);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			bw2.close();
			bw2 = null;
		}

		tm1server.delete(blbEntity);
		tm1server.delete(processEntity);

		return true;
	}

}
