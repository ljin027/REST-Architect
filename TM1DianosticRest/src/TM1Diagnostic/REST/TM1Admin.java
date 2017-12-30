package TM1Diagnostic.REST;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.URI;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.wink.json4j.OrderedJSONObject;
import org.apache.wink.json4j.JSONArray;

public class TM1Admin {

	public String hostname = "";
	private int port = 5898;
	
	private String protocol = "https";
	private String keystoreFile = "";
	private String keystorePass = "";
	
	private boolean expanded = false;
	
	static private SSLConnectionSocketFactory sslsf;
	private PoolingHttpClientConnectionManager connectionManager;
	
	private List<TM1ServerStub> tm1ServerList;
	
	public TM1Admin(String hostname, int port){
		this.hostname = hostname;
		this.port = port;
		if (port == 5898){
			protocol = "https";
		} else if (port == 5895){
			protocol = "http";
		} else {
			protocol = "https";
		}
		tm1ServerList = new ArrayList<TM1ServerStub>();
		
		readConfigurationFromFile();
		
		try {
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		    java.io.FileInputStream fis = null;
	        fis = new java.io.FileInputStream(keystoreFile);
	        ks.load(fis, keystorePass.toCharArray());
	        if (fis != null) {
	            fis.close();
	        }
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(ks).build();
			sslsf = new SSLConnectionSocketFactory(sslContext,
			        new String[]{"TLSv1.2"},
			        null,
			        new NoopHostnameVerifier());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", new PlainConnectionSocketFactory()).register("https", sslsf).build();
		connectionManager = new PoolingHttpClientConnectionManager(registry);
		connectionManager.setMaxTotal(200);
		connectionManager.setDefaultMaxPerRoute(20);
		HttpHost httphost = new HttpHost(hostname, 80);
		connectionManager.setMaxPerRoute(new HttpRoute(httphost), 50);
	}
	
	public TM1Admin(String cloudhost){
		this.hostname = cloudhost;
		this.port = 443;
		protocol = "https";
		tm1ServerList = new ArrayList<TM1ServerStub>();
		
		readConfigurationFromFile();
		
		try {
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		    java.io.FileInputStream fis = null;
	        fis = new java.io.FileInputStream(keystoreFile);
	        ks.load(fis, keystorePass.toCharArray());
	        if (fis != null) {
	            fis.close();
	        }
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(ks).build();
			sslsf = new SSLConnectionSocketFactory(sslContext,
			        new String[]{"TLSv1.2"},
			        null,
			        new NoopHostnameVerifier());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", new PlainConnectionSocketFactory()).register("https", sslsf).build();
		connectionManager = new PoolingHttpClientConnectionManager(registry);
		connectionManager.setMaxTotal(200);
		connectionManager.setDefaultMaxPerRoute(20);
		HttpHost httphost = new HttpHost(hostname, 80);
		connectionManager.setMaxPerRoute(new HttpRoute(httphost), 50);
		
	}

	public CloseableHttpClient getHttpClientBetter() {
		try {
			CloseableHttpClient httpClient = null;
			RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();
			if (protocol.equals("http")) {
				httpClient = HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(globalConfig).build();
				return httpClient;
			} else if (protocol.equals("https")) {
				httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).setConnectionManager(connectionManager).setDefaultRequestConfig(globalConfig).setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER).build();
				return httpClient;
			} else {
				return null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	
	public void addCloudServer(TM1ServerStub tm1server){
		tm1ServerList.add(tm1server);
	}
	
	public void expand(){
		expanded = true;
	}
	
	public void collapse(){
		expanded = false; 
	}
	
	public boolean isExpanded(){
		return expanded;
	}
	
	public List<TM1ServerStub> getservers(){
		return tm1ServerList;
	}
	
	public TM1ServerStub getserver(int i){
		return tm1ServerList.get(i);
	}
	
	public boolean readTM1ServersFromServer(){
		try {
			URI uri = new URIBuilder().setScheme(protocol).setHost(hostname).setPort(port).setPath("/api/v1/Servers").build();
			HttpGet getrequest = new HttpGet(uri);
			CloseableHttpClient httpClient = getHttpClientBetter();
			HttpResponse httpresponse = httpClient.execute(getrequest);
			int responseStatus = httpresponse.getStatusLine().getStatusCode();
			if (responseStatus >= 200 && responseStatus < 300) {
				HttpEntity entity = httpresponse.getEntity();
				String response_string = EntityUtils.toString(entity);
				OrderedJSONObject jresponse = new OrderedJSONObject(response_string);
				JSONArray ja = (JSONArray)jresponse.get("value");
				List<TM1ServerStub> foundservers = new ArrayList<TM1ServerStub>();
				
				for (int i=0; i<ja.length(); i++){
					OrderedJSONObject jx = (OrderedJSONObject)ja.getJSONObject(i);
					String tm1servername = jx.getString("Name");
					int httpportnumber = jx.getInt("HTTPPortNumber");
					boolean usessl = jx.getBoolean("UsingSSL");
					String ipaddress = jx.getString("IPAddress");
					InetAddress addr = InetAddress.getByName(ipaddress);
					TM1ServerStub foundserver = new TM1ServerStub(tm1servername, addr.getHostName(), httpportnumber, usessl);
					if (jx.getBoolean("AcceptingClients")) foundservers.add(foundserver);
				}

				for (int i=0; i<foundservers.size(); i++){
					boolean tm1modelalreadyknown = false;
					for (int j=0; j<tm1ServerList.size(); j++){
						if(foundservers.get(i).name.equals(tm1ServerList.get(j).name)){
							tm1modelalreadyknown = true;
							break;
						}
					}
					if (!tm1modelalreadyknown) tm1ServerList.add(foundservers.get(i));
				}

				for (int i=0; i<tm1ServerList.size(); i++){
					boolean stillthere = false;
					for (int j=0; j<foundservers.size(); j++){
						if(tm1ServerList.get(i).name.equals(foundservers.get(j).name)){
							stillthere = true;
							break;
						}
					}
					if (!stillthere) tm1ServerList.remove(i);
				}
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	public boolean readConfigurationFromFile() {
		String configFileName = ".//config//config";
		try {
			FileReader fr = new FileReader(configFileName);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null) {
				String[] tokens = line.split(":");
				String parameter = tokens[0];
				String value = tokens[1];
				if (parameter.equals("keystoreFile")){
					keystoreFile = value;
				} else if (parameter.equals("keystorePass")){
					keystorePass = value;
				}
				line = br.readLine();
			}
			br.close();
			fr.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
}
