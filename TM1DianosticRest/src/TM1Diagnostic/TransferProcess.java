package TM1Diagnostic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.OrderedJSONObject;

import TM1Diagnostic.REST.TM1Process;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

public class TransferProcess {

	public String name;
	public OrderedJSONObject json;
	private TM1Process process;
	
	public TransferProcess(String name, OrderedJSONObject json){
		this.name = name;
		this.json = json;
	}
	
	public TransferProcess(TM1Process process){
		this.process = process;
		name = process.displayName;
	}
	
	public void importToServer(TM1Server server) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException{
		String request = "Processes";
		server.post(request, json);
	}
	
	public boolean writeToFile(String dir) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		String processDirName = dir + "//pro";
		File processDir = new File(processDirName);
		if (!processDir.exists()){
			processDir.mkdir();
		}
		String request = process.getEntity();
		TM1Server tm1server = process.getServer();
		tm1server.get(request);
		FileWriter fw = new FileWriter(dir + "//pro//" + process.displayName + ".pro", false);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(tm1server.response.toString());
		bw.close();
		fw.close();
		return true;
	}
}
