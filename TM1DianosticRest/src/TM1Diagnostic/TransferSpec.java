package TM1Diagnostic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.wink.json4j.OrderedJSONObject;

import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;



public class TransferSpec {
	
	private TM1Server importTM1Server;

	private List<TransferCube> cubes;
	private List<TransferDimension> dimensions;
	private List<TransferProcess> processes;
	private List<TransferChore> chores;
	
	public TransferSpec(TM1Server tm1server) {
		this.importTM1Server = tm1server;
		
		cubes = new ArrayList<TransferCube>();
		dimensions = new ArrayList<TransferDimension>();
		processes = new ArrayList<TransferProcess>();
		chores = new ArrayList<TransferChore>();
	}
	
	public int getDimensionCount(){
		return dimensions.size();
	}

	public void addDimension(TransferDimension dimension){
		dimensions.add(dimension);
	}
	
	public TransferDimension getDimension(int i){
		return dimensions.get(i);
	}
	
	public int getCubeCount(){
		return cubes.size();
	}
	
	public void addCube(TransferCube cube){
		cubes.add(cube);
	}
	
	public TransferCube getCube(int i){
		return cubes.get(i);
	}
	
	
	
	public int getProcessCount(){
		return processes.size();
	}
	
	public void addProcess(TransferProcess process){
		processes.add(process);
	}
	
	public TransferProcess getProcess(int i){
		return processes.get(i);
	}
	
	
	public int getChoreCount(){
		return chores.size();
	}
	
	public void addChore(TransferChore chore){
		chores.add(chore);
	}
	
	public TransferChore getChore(int i){
		return chores.get(i);
	}
	
	
	public void writeAll(){
		for (int i=0; i<dimensions.size(); i++){
			TransferDimension dimension = dimensions.get(i);
			System.out.println("Dimension - " + dimension.name);
			for (int j=0; j<dimension.hierarchies.size(); j++){
				TransferHierarchy hierarchy = dimension.hierarchies.get(j);
				System.out.println("Hierarchy - " + hierarchy.name);
				for (int k=0; k<hierarchy.subsets.size(); k++){
					TransferSubset subset = hierarchy.subsets.get(k);
					System.out.println("Subset - " + subset.name);
					
				}
			}
		}
	}
	
	public void runTransfer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException{

		for (int i=0; i<dimensions.size(); i++){
			TransferDimension dimension = dimensions.get(i);
			dimension.importToServer(importTM1Server);
		}
		
		for (int i=0; i<cubes.size(); i++){
			TransferCube cube = cubes.get(i);
			cube.importToServer(importTM1Server);
		}

		for (int i=0; i<processes.size(); i++){
			TransferProcess process = processes.get(i);
			process.importToServer(importTM1Server);
		}
		
		for (int i=0; i<chores.size(); i++){
			TransferChore chore = chores.get(i);
			chore.importToServer(importTM1Server);
		}
	}
	
}
