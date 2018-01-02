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

import TM1Diagnostic.REST.TM1Chore;
import TM1Diagnostic.REST.TM1Process;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

public class TransferChore {

	public String name;
	public OrderedJSONObject json;
	private TM1Chore chore;
	
	public TransferChore(String name, OrderedJSONObject json){
		this.name = name;
		this.json = json;
	}
	
	public TransferChore(TM1Chore chore){
		this.chore = chore;
		name = chore.displayName;
	}
	
	public void importToServer(TM1Server server) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException{
		String request = "Chores";
		server.post(request, json);
	}
	
	public boolean writeToFile(String dir) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		String choreDirName = dir + "//cho";
		File choreDir = new File(choreDirName);
		if (!choreDir.exists()){
			choreDir.mkdir();
		}
		String request = chore.getEntity();
		TM1Server tm1server = chore.getServer();
		tm1server.get(request);
		FileWriter fw = new FileWriter(dir + "//cho//" + chore.displayName + ".cho", false);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(tm1server.response.toString());
		bw.close();
		fw.close();
		return true;
	}
}
