package TM1Diagnostic.REST;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.apache.wink.json4j.JSONArray;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;

import TM1Diagnostic.Credential;
import TM1Diagnostic.SearchResult;

public class TM1Server {

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

	static public int BASICSECURITY = 1;
	static public int CAMSECUIRTY = 5;

	public String restURI;
	public String cookieDomain;

	public String request;
	public String response;
	public String errorCode;
	public String errorMessage;
	public int responseStatus;

	static private SSLConnectionSocketFactory sslsf;
	private PoolingHttpClientConnectionManager connectionManager;
	private String keystoreFile;
	private String keystorePass;

	private CookieStore cookieStore;
	private BasicClientCookie cookie;

	public boolean httpDebug = true;

	protected String name = "";
	private String adminhost = "";
	private InetAddress inetAddress;
	private String securitymode = "";
	private String connecteduser = "";

	private int port = 0;
	private String protocol = "http";
	protected String TM1SessionId;
	private boolean authenticated = false;

	private boolean serverexpanded = false;
	private boolean applicationsexpanded = false;
	private boolean cubesexpanded = false;
	private boolean dimensionsexpanded = false;
	private boolean processesexpanded = false;
	private boolean choresexpanded = false;

	private List<TM1Folder> folders;
	private List<TM1Cube> cubes;
	private List<TM1Dimension> dimensions;
	private List<TM1Process> processes;
	private List<TM1Chore> chores;
	private List<TM1Blob> blobs;
	private List<TM1Sandbox> sandboxes;

	private File logDirectory;
	private File logFile;
	private FileWriter logFileWriter;
	private BufferedWriter logBufferedWriter;
	private PrintWriter logPrintWriter;

	private StyledText httpTraceDisplay;

	/*
	 * Contructor for TM1 Local
	 */

	public TM1Server(String adminhost, String servername, int port, boolean usessl) throws Exception {
		this.name = servername;
		inetAddress = InetAddress.getByName(adminhost);
		this.adminhost = inetAddress.getCanonicalHostName();
		this.port = port;
		if (usessl)
			protocol = "https";
		authenticated = false;
		securitymode = "UNKNOWN";
		restURI = "api/v1/";
		cookieDomain = "";
		int hostnamelength = this.adminhost.indexOf('.');
		if (hostnamelength != -1) {
			cookieDomain = this.adminhost.substring(hostnamelength, this.adminhost.length());
		}
		logDirectory = new File(".//logs//" + this.adminhost + "//" + this.name);
		logFile = new File(".//logs//" + this.adminhost + "//" + this.name + "//http.log");
		if (!logDirectory.exists())
			logDirectory.mkdirs();
		if (!logFile.exists())
			logFile.createNewFile();
		logFileWriter = new FileWriter(logFile.getAbsoluteFile(), true);
		logBufferedWriter = new BufferedWriter(logFileWriter);
		logPrintWriter = new PrintWriter(logBufferedWriter);
		readConfigurationFromFile();
		KeyStore keyStore = KeyStore.getInstance("JKS");
		File keystoreF = new File(keystoreFile);
		System.out.println("Keystore file: " + keystoreF.getAbsolutePath());
		FileInputStream instream = new FileInputStream(keystoreFile);
		keyStore.load(instream, keystorePass.toCharArray());
		instream.close();
		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(keyStore).build();
		sslsf = new SSLConnectionSocketFactory(sslContext, new String[] { "TLSv1.2" }, null, new NoopHostnameVerifier());
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", new PlainConnectionSocketFactory()).register("https", sslsf).build();
		connectionManager = new PoolingHttpClientConnectionManager(registry);
		connectionManager.setMaxTotal(200);
		connectionManager.setDefaultMaxPerRoute(20);
		HttpHost httphost = new HttpHost(adminhost, 80);
		connectionManager.setMaxPerRoute(new HttpRoute(httphost), 50);

		folders = new ArrayList<TM1Folder>();
		cubes = new ArrayList<TM1Cube>();
		dimensions = new ArrayList<TM1Dimension>();
		processes = new ArrayList<TM1Process>();
		chores = new ArrayList<TM1Chore>();
		blobs = new ArrayList<TM1Blob>();
		sandboxes = new ArrayList<TM1Sandbox>();
	}

	public TM1Server(String cloudurl, String cloudname) throws Exception {
		this.name = cloudname;
		this.adminhost = cloudurl;
		this.port = 443;
		protocol = "https";
		restURI = "tm1/api/" + cloudname + "/api/v1/";

		cookieDomain = "";
		int hostnamelength = adminhost.indexOf('.');
		if (hostnamelength != -1) {
			cookieDomain = adminhost.substring(hostnamelength, adminhost.length());
		}

		logDirectory = new File(".//logs//" + this.adminhost + "//" + this.name);
		logFile = new File(".//logs//" + this.adminhost + "//" + this.name + "//http.log");
		if (!logDirectory.exists())
			logDirectory.mkdirs();
		if (!logFile.exists())
			logFile.createNewFile();
		logFileWriter = new FileWriter(logFile.getAbsoluteFile(), true);
		logBufferedWriter = new BufferedWriter(logFileWriter);
		logPrintWriter = new PrintWriter(logBufferedWriter);
		readConfigurationFromFile();
		KeyStore keyStore = KeyStore.getInstance("JKS");
		File keystoreF = new File(keystoreFile);
		System.out.println("Keystore file: " + keystoreF.getAbsolutePath());
		FileInputStream instream = new FileInputStream(keystoreFile);
		keyStore.load(instream, keystorePass.toCharArray());
		instream.close();
		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(keyStore).build();
		sslsf = new SSLConnectionSocketFactory(sslContext, new String[] { "TLSv1.2" }, null, new NoopHostnameVerifier());
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", new PlainConnectionSocketFactory()).register("https", sslsf).build();
		connectionManager = new PoolingHttpClientConnectionManager(registry);
		connectionManager.setMaxTotal(200);
		connectionManager.setDefaultMaxPerRoute(20);
		HttpHost httphost = new HttpHost(adminhost, 80);
		connectionManager.setMaxPerRoute(new HttpRoute(httphost), 50);

		authenticated = false;
		securitymode = "";

		folders = new ArrayList<TM1Folder>();
		cubes = new ArrayList<TM1Cube>();
		dimensions = new ArrayList<TM1Dimension>();
		processes = new ArrayList<TM1Process>();
		chores = new ArrayList<TM1Chore>();
		blobs = new ArrayList<TM1Blob>();
		sandboxes = new ArrayList<TM1Sandbox>();

	}

	public void setHttpTraceDisplay(StyledText styledText) {
		httpTraceDisplay = styledText;
	}

	public CloseableHttpClient gethttpclient() {
		try {
			cookieStore = new BasicCookieStore();
			cookie = new BasicClientCookie("TM1SessionId", TM1SessionId);
			cookie.setPath("/");
			cookie.setDomain(cookieDomain);
			cookie.setAttribute(ClientCookie.DOMAIN_ATTR, "true");
			cookieStore.addCookie(cookie);
			CloseableHttpClient httpClient = null;
			RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();
			if (protocol.equals("http")) {
				httpClient = HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore).build();
				return httpClient;
			} else if (protocol.equals("https")) {
				httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).setConnectionManager(connectionManager).setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore).setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER).build();
				return httpClient;
			} else {
				return null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// HTTP GET
	public void get(String request) throws TM1RestException, URISyntaxException, ClientProtocolException, IOException {
		errorCode = null;
		errorMessage = null;
		URI uri = new URIBuilder().setScheme(protocol).setHost(adminhost).setPort(port).setPath(restURI + request).build();
		HttpGet getrequest = new HttpGet(uri);
		CloseableHttpClient httpClient = gethttpclient();
		HttpResponse httpresponse = httpClient.execute(getrequest);
		responseStatus = httpresponse.getStatusLine().getStatusCode();
		if (httpDebug) {
			String message = "GET, HTTP " + responseStatus + ", " + uri.toString();
			logPrintWriter.println(message);
			logPrintWriter.flush();
			System.out.println(message);
			httpTraceDisplay.append(message + "\n");
			httpTraceDisplay.setTopIndex(httpTraceDisplay.getLineCount() - 1);
		}
		if (responseStatus >= 200 && responseStatus < 300) {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null)
				response = EntityUtils.toString(entity);
			return;
		} else if (responseStatus == 401) {
			authenticated = false;
			throw new TM1RestException(responseStatus, "");
		} else if (responseStatus == 404) {
			throw new TM1RestException(responseStatus, "");
		} else {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null) {
				response = EntityUtils.toString(entity);
				processErrorResponse(response);
			}
			throw new TM1RestException(responseStatus, "");
		}
	}

	// HTTP GET
	public void get(String request, String query) throws TM1RestException, URISyntaxException, ClientProtocolException, IOException {
		errorCode = null;
		errorMessage = null;
		URI uri = new URIBuilder().setScheme(protocol).setHost(adminhost).setPort(port).setPath(restURI + request).setCustomQuery(query).build();
		HttpGet getrequest = new HttpGet(uri);
		CloseableHttpClient httpClient = gethttpclient();
		HttpResponse httpresponse = httpClient.execute(getrequest);
		responseStatus = httpresponse.getStatusLine().getStatusCode();
		if (httpDebug) {
			String message = "GET, HTTP " + responseStatus + ", " + uri.toString();
			logPrintWriter.println(message);
			logPrintWriter.flush();
			System.out.println(message);
			httpTraceDisplay.append(message + "\n");
			httpTraceDisplay.setTopIndex(httpTraceDisplay.getLineCount() - 1);
		}
		if (responseStatus >= 200 && responseStatus < 300) {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null)
				response = EntityUtils.toString(entity);
			return;
		} else if (responseStatus == 401) {
			authenticated = false;
			throw new TM1RestException(responseStatus, "");
		} else if (responseStatus == 404) {
			throw new TM1RestException(responseStatus, "");
		} else {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null) {
				response = EntityUtils.toString(entity);
				processErrorResponse(response);
			}
			throw new TM1RestException(responseStatus, "");
		}
	}

	// HTTP POST
	public void post(String request, OrderedJSONObject payload) throws TM1RestException, URISyntaxException, ClientProtocolException, IOException {
		errorCode = null;
		errorMessage = null;
		URI uri = new URIBuilder().setScheme(protocol).setHost(adminhost).setPort(port).setPath(restURI + request).build();
		HttpPost postrequest = new HttpPost(uri);
		StringEntity input = new StringEntity(payload.toString());
		input.setContentType("application/json");
		postrequest.setEntity(input);
		CloseableHttpClient httpClient = gethttpclient();
		HttpResponse httpresponse = httpClient.execute(postrequest);
		responseStatus = httpresponse.getStatusLine().getStatusCode();
		if (httpDebug) {
			String message = "POST, HTTP " + responseStatus + ", " + uri.toString();
			logPrintWriter.println(message);
			logPrintWriter.println(payload.toString());
			logPrintWriter.flush();
			System.out.println(message);
			System.out.println(payload.toString());
			httpTraceDisplay.append(message + "\n");
			httpTraceDisplay.append(payload.toString() + "\n");
			httpTraceDisplay.setTopIndex(httpTraceDisplay.getLineCount() - 1);
		}
		if (responseStatus >= 200 && responseStatus < 300) {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null)
				response = EntityUtils.toString(entity);
			return;
		} else if (responseStatus == 401) {
			authenticated = false;
			throw new TM1RestException(responseStatus, "");
		} else {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null)
				processErrorResponse(EntityUtils.toString(entity));
			throw new TM1RestException(responseStatus, "");
		}
	}

	public void threadSafePost(String request, final OrderedJSONObject payload) throws TM1RestException, URISyntaxException, ClientProtocolException, IOException {
		errorCode = null;
		errorMessage = null;
		URI uri = new URIBuilder().setScheme(protocol).setHost(adminhost).setPort(port).setPath(restURI + request).build();
		HttpPost postrequest = new HttpPost(uri);
		StringEntity input = new StringEntity(payload.toString());
		input.setContentType("application/json");
		postrequest.setEntity(input);
		CloseableHttpClient httpClient = gethttpclient();
		HttpResponse httpresponse = httpClient.execute(postrequest);
		responseStatus = httpresponse.getStatusLine().getStatusCode();
		if (httpDebug) {
			final String message = "POST, HTTP " + responseStatus + ", " + uri.toString();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					logPrintWriter.println(message);
					logPrintWriter.println(payload.toString());
					logPrintWriter.flush();
					System.out.println(message);
					System.out.println(payload.toString());
					httpTraceDisplay.append(message + "\n");
					httpTraceDisplay.append(payload.toString() + "\n");
					httpTraceDisplay.setTopIndex(httpTraceDisplay.getLineCount() - 1);
				}
			});
		}
		if (responseStatus >= 200 && responseStatus < 300) {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null)
				response = EntityUtils.toString(entity);
			return;
		} else if (responseStatus == 401) {
			authenticated = false;
			throw new TM1RestException(responseStatus, "");
		} else {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null) {
				processErrorResponse(EntityUtils.toString(entity));
			}
			throw new TM1RestException(responseStatus, "");
		}
	}

	// HTTP POST
	public void post(String request, String query, OrderedJSONObject payload) throws TM1RestException, URISyntaxException, ClientProtocolException, IOException {
		errorCode = null;
		errorMessage = null;
		URI uri = new URIBuilder().setScheme(protocol).setHost(adminhost).setPort(port).setPath(restURI + request).setCustomQuery(query).build();
		HttpPost postrequest = new HttpPost(uri);
		StringEntity input = new StringEntity(payload.toString());
		input.setContentType("application/json");
		postrequest.setEntity(input);
		CloseableHttpClient httpClient = gethttpclient();
		HttpResponse httpresponse = httpClient.execute(postrequest);
		responseStatus = httpresponse.getStatusLine().getStatusCode();
		if (httpDebug) {
			String message = "POST, HTTP " + responseStatus + ", " + uri.toString();
			logPrintWriter.println(message);
			logPrintWriter.println(payload.toString());
			logPrintWriter.flush();
			System.out.println(message);
			System.out.println(payload.toString() + "\n");
			httpTraceDisplay.append(message + "\n");
			httpTraceDisplay.setTopIndex(httpTraceDisplay.getLineCount() - 1);
		}
		if (responseStatus >= 200 && responseStatus < 300) {
			HttpEntity entity = httpresponse.getEntity();
			response = EntityUtils.toString(entity);
			return;
		} else if (responseStatus == 401) {
			authenticated = false;
			throw new TM1RestException(responseStatus, "");
		} else {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null)
				processErrorResponse(EntityUtils.toString(entity));
			throw new TM1RestException(responseStatus, "");
		}
	}

	// HTTP PATCH
	public void patch(String request, OrderedJSONObject payload) throws TM1RestException, URISyntaxException, ClientProtocolException, IOException {
		errorCode = null;
		errorMessage = null;
		URI uri = new URIBuilder().setScheme(protocol).setHost(adminhost).setPort(port).setPath(restURI + request).build();
		HttpPatch patchrequest = new HttpPatch(uri);
		StringEntity input = new StringEntity(payload.toString());
		input.setContentType("application/json");
		patchrequest.setEntity(input);
		CloseableHttpClient httpClient = gethttpclient();
		HttpResponse httpresponse = httpClient.execute(patchrequest);
		responseStatus = httpresponse.getStatusLine().getStatusCode();
		if (httpDebug) {
			String message = "PATCH, HTTP " + responseStatus + ", " + uri.toString();
			logPrintWriter.println(message);
			logPrintWriter.println(payload.toString());
			logPrintWriter.flush();
			System.out.println(message);
			System.out.println(payload.toString());
			httpTraceDisplay.append(message + "\n");
			httpTraceDisplay.setTopIndex(httpTraceDisplay.getLineCount() - 1);
		}
		if (responseStatus >= 200 && responseStatus < 300) {
			HttpEntity entity = httpresponse.getEntity();
			response = EntityUtils.toString(entity);
			return;
		} else if (responseStatus == 401) {
			authenticated = false;
			throw new TM1RestException(responseStatus, "");
		} else {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null)
				processErrorResponse(EntityUtils.toString(entity));
			throw new TM1RestException(responseStatus, "");
		}
	}

	// HTTP PATCH
	public void patch(String request, JSONArray payload) throws TM1RestException, URISyntaxException, ClientProtocolException, IOException {
		errorCode = null;
		errorMessage = null;
		URI uri = new URIBuilder().setScheme(protocol).setHost(adminhost).setPort(port).setPath(restURI + request).build();
		HttpPatch patchrequest = new HttpPatch(uri);
		StringEntity input = new StringEntity(payload.toString());
		input.setContentType("application/json");
		patchrequest.setEntity(input);
		CloseableHttpClient httpClient = gethttpclient();
		HttpResponse httpresponse = httpClient.execute(patchrequest);
		responseStatus = httpresponse.getStatusLine().getStatusCode();
		if (httpDebug) {
			String message = "PATCH, HTTP " + responseStatus + ", " + uri.toString();
			logPrintWriter.println(message);
			logPrintWriter.flush();
			System.out.println(message);
			httpTraceDisplay.append(message + "\n");
			httpTraceDisplay.setTopIndex(httpTraceDisplay.getLineCount() - 1);
		}
		if (responseStatus >= 200 && responseStatus < 300) {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null)
				response = EntityUtils.toString(entity);
			return;
		} else if (responseStatus == 401) {
			authenticated = false;
			throw new TM1RestException(responseStatus, "");
		} else {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null)
				processErrorResponse(EntityUtils.toString(entity));
			throw new TM1RestException(responseStatus, "");
		}
	}

	// HTTP PATCH
	public void patch(String request, String payload) throws TM1RestException, URISyntaxException, ClientProtocolException, IOException {
		errorCode = null;
		errorMessage = null;
		URI uri = new URIBuilder().setScheme(protocol).setHost(adminhost).setPort(port).setPath(restURI + request).build();
		HttpPatch patchrequest = new HttpPatch(uri);
		StringEntity input = new StringEntity(payload);
		input.setContentType("application/json");
		patchrequest.setEntity(input);
		CloseableHttpClient httpClient = gethttpclient();
		HttpResponse httpresponse = httpClient.execute(patchrequest);
		responseStatus = httpresponse.getStatusLine().getStatusCode();
		if (httpDebug) {
			String message = "PATCH, HTTP " + responseStatus + ", " + uri.toString();
			logPrintWriter.println(message);
			logPrintWriter.flush();
			System.out.println(message);
			httpTraceDisplay.append(message + "\n");
			httpTraceDisplay.setTopIndex(httpTraceDisplay.getLineCount() - 1);
		}
		if (responseStatus >= 200 && responseStatus < 300) {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null)
				response = EntityUtils.toString(entity);
			return;
		} else if (responseStatus == 401) {
			authenticated = false;
			throw new TM1RestException(responseStatus, "");
		} else {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null)
				processErrorResponse(EntityUtils.toString(entity));
			throw new TM1RestException(responseStatus, "");
		}
	}

	public void delete(String request) throws TM1RestException, URISyntaxException, ClientProtocolException, IOException {
		errorCode = null;
		errorMessage = null;
		URI uri = new URIBuilder().setScheme(protocol).setHost(adminhost).setPort(port).setPath(restURI + request).build();
		HttpDelete deleterequest = new HttpDelete(uri);
		CloseableHttpClient httpClient = gethttpclient();
		HttpResponse httpresponse = httpClient.execute(deleterequest);
		responseStatus = httpresponse.getStatusLine().getStatusCode();
		if (httpDebug) {
			String message = "DELETE, HTTP " + responseStatus + ", " + uri.toString();
			logPrintWriter.println(message);
			logPrintWriter.flush();
			System.out.println(message);
			httpTraceDisplay.append(message + "\n");
			httpTraceDisplay.setTopIndex(httpTraceDisplay.getLineCount() - 1);
		}
		if (responseStatus >= 200 && responseStatus < 300) {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null)
				response = EntityUtils.toString(entity);
			return;
		} else if (responseStatus == 401) {
			authenticated = false;
			throw new TM1RestException(responseStatus, "");
		} else {
			HttpEntity entity = httpresponse.getEntity();
			if (entity != null)
				processErrorResponse(EntityUtils.toString(entity));
			throw new TM1RestException(responseStatus, "");
		}
	}

	private void processErrorResponse(String response) {
		try {
			OrderedJSONObject jresponse = new OrderedJSONObject(response);
			if (jresponse.has("error")) {
				OrderedJSONObject jerror = (OrderedJSONObject) jresponse.getJSONObject("error");
				if (jerror.has("code"))
					errorCode = jerror.getString("code");
				if (jerror.has("message"))
					errorMessage = jerror.getString("message");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Response is: " + response);
		}
	}

	public String toString() {
		return adminhost + " : " + name;
	}

	public String getsecuritymode() {
		return securitymode;
	}

	public String displayuser() {
		return connecteduser;
	}

	public void expandserver() {
		serverexpanded = true;
	}

	public void collapseserver() {
		serverexpanded = false;
	}

	public boolean serverexpanded() {
		return serverexpanded;
	}

	public boolean applicationsexpanded() {
		return applicationsexpanded;
	}

	public void expandapplications() {
		applicationsexpanded = true;
	}

	public void collapseapplications() {
		applicationsexpanded = false;
	}

	public boolean cubesexpanded() {
		return cubesexpanded;
	}

	public void expandcubes() {
		cubesexpanded = true;
	}

	public void collapsecubes() {
		cubesexpanded = false;
	}

	public boolean dimensionsexpanded() {
		return dimensionsexpanded;
	}

	public void expanddimensions() {
		dimensionsexpanded = true;
	}

	public void collapsedimensions() {
		dimensionsexpanded = false;
	}

	public boolean processesexpanded() {
		return processesexpanded;
	}

	public void expandprocesses() {
		processesexpanded = true;
	}

	public void collapseprocesses() {
		processesexpanded = false;
	}

	public boolean choresexpanded() {
		return choresexpanded;
	}

	public void expandchores() {
		choresexpanded = true;
	}

	public void collapsechores() {
		choresexpanded = false;
	}

	public String gettm1sessionid() {
		return TM1SessionId;
	}

	public String getAdminHostName() {
		return this.adminhost;
	}

	public String getName() {
		return name;
	}

	public boolean closeAllSession() {
		return true;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void refreshApplicationList() {
	}

	public String getErrorMessage() {
		if (errorMessage == null) {
			return "No error message";
		}
		return errorMessage;
	}

	public String getErrorCode() {
		if (errorCode == null) {
			return "Unknown";
		}
		return errorCode;
	}

	public int getSandboxCount() {
		return sandboxes.size();
	}

	public TM1Sandbox getSandbox(int i) {
		return sandboxes.get(i);
	}

	public boolean readSandboxesFromServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		sandboxes.clear();
		get("Sandboxes");
		OrderedJSONObject jresponse = new OrderedJSONObject(response);
		JSONArray jsandboxes = jresponse.getJSONArray("value");
		for (int i = 0; i < jsandboxes.length(); i++) {
			OrderedJSONObject jsandbox = (OrderedJSONObject) jsandboxes.getJSONObject(i);
			String name = jsandbox.getString("Name");
			Boolean isActive = jsandbox.getBoolean("IsActive");
			TM1Sandbox sandbox = new TM1Sandbox(name, isActive);
			sandboxes.add(sandbox);
		}
		return true;

	}

	/*
	 * Function: readCubesFromServer
	 * 
	 * Reads the list of cubes from the TM1 Server. This includes control cubes.
	 * Adds/removes cubes from the clients list of cubes.
	 */
	public void readCubesFromServer() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		get("Cubes");
		OrderedJSONObject jresponse = new OrderedJSONObject(response);
		JSONArray jcubes = jresponse.getJSONArray("value");
		for (int i = 0; i < jcubes.length(); i++) {
			OrderedJSONObject cubeJSON = (OrderedJSONObject) jcubes.getJSONObject(i);
			TM1Cube cube = new TM1Cube(cubeJSON.getString("Name"), this);
			boolean cubeAdded = false;
			if (!cubes.contains(cube)) {
				for (int j = 0; j < cubes.size(); j++) {
					if (cube.displayName.compareToIgnoreCase(cubes.get(j).displayName) < 0) {
						cubes.add(j, cube);
						cubeAdded = true;
						break;
					}
				}
				if (!cubeAdded) {
					cubes.add(cube);
				}
			}
		}
		for (int i = cubes.size() - 1; i >= 0; i--) {
			TM1Cube cube = cubes.get(i);
			if (!jcubes.toString().contains("\"Name\":\"" + cube.displayName + "\"")) {
				cubes.remove(i);
			}
		}
	}

	public int getFoldersCount() {
		return folders.size();
	}

	public TM1Folder getFolder(int i) {
		return folders.get(i);
	}

	public boolean readFoldersFromServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		folders.clear();
		String request = "Contents('Applications')";
		String query = "$expand=tm1.Folder/Contents($expand=tm1.Folder/Contents($expand=tm1.Folder/Contents($expand=tm1.Folder/Contents($expand=tm1.Folder/Contents))))";
		get(request, query);

		OrderedJSONObject jresponse = new OrderedJSONObject(response);
		JSONArray jcontents = jresponse.getJSONArray("Contents");
		for (int i = 0; i < jcontents.length(); i++) {
			OrderedJSONObject jchild = (OrderedJSONObject) jcontents.getJSONObject(i);
			if (jchild.getString("@odata.type").equals("#ibm.tm1.api.v1.Folder")) {
				String childname = jchild.getString("Name");
				TM1Folder childfolder = new TM1Folder(childname, this);
				readFolderChildren(jchild.getJSONArray("Contents"), childfolder);
				folders.add(childfolder);
			}
		}
		return true;

	}

	public void readFolderChildren(JSONArray jcontents, TM1Folder parent) throws JSONException {
		for (int i = 0; i < jcontents.length(); i++) {
			OrderedJSONObject jchild = (OrderedJSONObject) jcontents.getJSONObject(i);
			String childname = jchild.getString("Name");
			String childid = jchild.getString("Name");
			if (jchild.getString("@odata.type").equals("#ibm.tm1.api.v1.Folder")) {
				TM1Folder childfolder = new TM1Folder(childname, this);
				parent.addfolder(childfolder);
				if (jchild.has("Contents")) {
					readFolderChildren(jchild.getJSONArray("Contents"), childfolder);
				}
			} else if (jchild.getString("@odata.type").equals("#ibm.tm1.api.v1.DocumentReference")) {
				TM1ObjectReference reference = new TM1ObjectReference(childname, this, childid, DOCUMENT);
				parent.addReference(reference);
			} else if (jchild.getString("@odata.type").equals("#ibm.tm1.api.v1.ViewReference")) {
				TM1ObjectReference reference = new TM1ObjectReference(childname, this, childid, VIEW);
				parent.addReference(reference);
			} else if (jchild.getString("@odata.type").equals("#ibm.tm1.api.v1.CubeReference")) {
				TM1ObjectReference reference = new TM1ObjectReference(childname, this, childid, CUBE);
				parent.addReference(reference);
			} else if (jchild.getString("@odata.type").equals("#ibm.tm1.api.v1.ProcessReference")) {
				TM1ObjectReference reference = new TM1ObjectReference(childname, this, childid, PROCESS);
				parent.addReference(reference);
			}
		}
	}

	public int getBlobCount() {
		return blobs.size();
	}

	public TM1Blob getBlob(int i) {
		return blobs.get(i);
	}

	public boolean readBlobsFromServer() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		blobs.clear();
		String req = "Contents('Blobs')/Contents";
		get(req);
		OrderedJSONObject jresponse = new OrderedJSONObject(response);
		JSONArray blobJSONArray = jresponse.getJSONArray("value");
		for (int i = 0; i < blobJSONArray.length(); i++) {
			OrderedJSONObject blobJSON = (OrderedJSONObject) blobJSONArray.getJSONObject(i);
			TM1Blob blob = new TM1Blob(blobJSON.getString("Name"), this);
			boolean added = false;
			if (!blobs.contains(blob)) {
				for (int j = 0; j < blobs.size(); j++) {
					if (blob.displayName.compareToIgnoreCase(blobs.get(j).displayName) < 0) {
						blobs.add(j, blob);
						added = true;
						break;
					}
				}
				if (!added) {
					blobs.add(blob);
				}
			}
		}
		for (int i = blobs.size() - 1; i >= 0; i--) {
			TM1Blob blob = blobs.get(i);
			if (!blobJSONArray.toString().contains("\"Name\":\"" + blob.displayName + "\"")) {
				blobs.remove(i);
			}
		}
		return true;
	}

	public int cubeCount() {
		return cubes.size();
	}

	public TM1Cube getCube(int i) {
		return cubes.get(i);
	}

	public void readDimensionsFromServer() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		String req = "Dimensions";
		get(req);
		OrderedJSONObject jresponse = new OrderedJSONObject(response);
		JSONArray jdimensions = jresponse.getJSONArray("value");
		for (int i = 0; i < jdimensions.length(); i++) {
			OrderedJSONObject dimensionJSON = (OrderedJSONObject) jdimensions.getJSONObject(i);
			TM1Dimension dimension = new TM1Dimension(dimensionJSON.getString("Name"), this);
			if (!dimensions.contains(dimension)) {
				boolean added = false;
				if (!dimensions.contains(dimension)) {
					for (int j = 0; j < dimensions.size(); j++) {
						if (dimension.displayName.compareToIgnoreCase(dimensions.get(j).displayName) < 0) {
							dimensions.add(j, dimension);
							added = true;
							break;
						}
					}
					if (!added) {
						dimensions.add(dimension);
					}
				}
			}
		}
		for (int i = dimensions.size() - 1; i >= 0; i--) {
			TM1Dimension dimension = dimensions.get(i);
			if (!jdimensions.toString().contains("\"Name\":\"" + dimension.displayName + "\"")) {
				dimensions.remove(i);
			}
		}
	}

	public int dimensionCount() {
		return dimensions.size();
	}

	public TM1Dimension getDimension(int i) {
		return dimensions.get(i);
	}

	public void readProcessesFromServer() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		processes.clear();
		String req = "Processes";
		get(req);
		OrderedJSONObject jresponse = new OrderedJSONObject(response);
		JSONArray processJSONArray = jresponse.getJSONArray("value");
		for (int i = 0; i < processJSONArray.length(); i++) {
			OrderedJSONObject processJSON = (OrderedJSONObject) processJSONArray.getJSONObject(i);
			TM1Process process = new TM1Process(processJSON.getString("Name"), this);
			boolean added = false;
			if (!processes.contains(process)) {
				for (int j = 0; j < processes.size(); j++) {
					if (process.displayName.compareToIgnoreCase(processes.get(j).displayName) < 0) {
						processes.add(j, process);
						added = true;
						break;
					}
				}
				if (!added) {
					processes.add(process);
				}
			}
		}
		for (int i = processes.size() - 1; i >= 0; i--) {
			TM1Process process = processes.get(i);
			if (!processJSONArray.toString().contains("\"Name\":\"" + process.displayName + "\"")) {
				processes.remove(i);
			}
		}
	}

	public int processCount() {
		return processes.size();
	}

	public TM1Process getProcess(int i) {
		return processes.get(i);
	}

	public void readChoresFromServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		chores.clear();
		String request = "Chores";
		String query = "$select=Name,Active";
		get(request, query);
		OrderedJSONObject jresponse = new OrderedJSONObject(response);
		JSONArray choreJSONArray = jresponse.getJSONArray("value");
		for (int i = 0; i < choreJSONArray.length(); i++) {
			OrderedJSONObject choreJSON = (OrderedJSONObject) choreJSONArray.getJSONObject(i);
			TM1Chore chore = new TM1Chore(choreJSON.getString("Name"), this);
			chore.active = choreJSON.getBoolean("Active");
			boolean added = false;
			if (!chores.contains(chore)) {
				for (int j = 0; j < chores.size(); j++) {
					if (chore.displayName.compareToIgnoreCase(chores.get(j).displayName) < 0) {
						chores.add(j, chore);
						added = true;
						break;
					}
				}
				if (!added) {
					chores.add(chore);
				}
			}

		}
		for (int i = chores.size() - 1; i >= 0; i--) {
			TM1Chore chore = chores.get(i);
			if (!choreJSONArray.toString().contains("\"Name\":\"" + chore.displayName + "\"")) {
				chores.remove(i);
			}
		}
	}

	public int choreCount() {
		return chores.size();
	}

	public TM1Chore getChore(int i) {
		return chores.get(i);
	}

	/*
	 * Function: readSecurityMode
	 * 
	 * Used to determine the security mode of the TM1 Server
	 */
	public void readSecurityMode() throws URISyntaxException, ClientProtocolException, IOException {
		URI uri = new URIBuilder().setScheme(protocol).setHost(getAdminHostName() + ":" + port).setPath(restURI + "Configuration").build();
		CloseableHttpClient httpclient = gethttpclient();
		HttpClientContext context = HttpClientContext.create();
		HttpGet httpget = new HttpGet(uri);
		CloseableHttpResponse response = httpclient.execute(httpget, context);
		int responseStatus = response.getStatusLine().getStatusCode();
		if (responseStatus == 401) {
			Header[] headers = response.getAllHeaders();
			for (int i = 0; i < headers.length; i++) {
				Header header = headers[i];
				if (header.getName().equals("WWW-Authenticate")) {
					if (header.getValue().startsWith("CAMPassport")) {
						securitymode = "CAM";
					} else if (header.getValue().startsWith("Basic")) {
						securitymode = "BASIC";
					}
				}
			}
		}
	}

	public String base64Encode(String unencoded) {
		byte[] message = unencoded.getBytes(StandardCharsets.UTF_8);
		String encoded = DatatypeConverter.printBase64Binary(message);
		return encoded;
	}

	public boolean authenticate(Credential c) throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, ClientProtocolException, IOException, TM1RestException, KeyStoreException, CertificateException {
		try {
			String authHeader;
			if (securitymode.equals("CAM")) {
				String encoding = base64Encode(c.getusername() + ":" + c.getpassword() + ":" + c.getnamespace());
				connecteduser = c.getnamespace() + "\\" + c.getusername();
				authHeader = "CAMNamespace " + new String(encoding);
				//System.out.println("Auth header: " + authHeader);
			} else if (securitymode.equals("BASIC")) {
				String encoding = base64Encode(c.getusername() + ":" + c.getpassword());
				System.out.println("base64Encode " + encoding);
				connecteduser = c.getusername();
				authHeader = "Basic " + new String(encoding);
				//System.out.println("Auth header: " + authHeader);
			} else {
				TM1RestException ex = new TM1RestException(1, "Failed to read server security mode");
				throw ex;
			}

			URI uri = new URIBuilder().setScheme(protocol).setHost(getAdminHostName()).setPort(port).setPath(restURI + "Configuration").build();
			
			HttpClient httpclient = null;
			HttpClientContext context = HttpClientContext.create();
			CookieStore cookieStore = new BasicCookieStore();
			HttpClientBuilder builder = HttpClientBuilder.create().setDefaultCookieStore(cookieStore);

			if (protocol.equals("https")) {
				KeyStore keyStore = KeyStore.getInstance("JKS");
				File keystoreF = new File(keystoreFile);
				FileInputStream instream = new FileInputStream(keystoreFile);
				keyStore.load(instream, keystorePass.toCharArray());
				instream.close();
				SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(keyStore).build();
				sslsf = new SSLConnectionSocketFactory(sslContext, new String[] { "TLSv1.2" }, null, new NoopHostnameVerifier());
				Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", new PlainConnectionSocketFactory()).register("https", sslsf).build();
				httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultCookieStore(cookieStore).build();
			} else {
				httpclient = builder.build();
			} 

			// System.out.println("Authenticate URI: " + uri.toString());
			HttpGet httpget = new HttpGet(uri);
			httpget.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
			HttpResponse response = httpclient.execute(httpget, context);
			int responseStatus = response.getStatusLine().getStatusCode();
			//System.out.println("AUTH " + responseStatus);
			if (responseStatus >= 200 && responseStatus < 300) {
				List<Cookie> cookies = cookieStore.getCookies();
				for (int i = 0; i < cookies.size(); i++) {
					Cookie cookie = cookies.get(i);
					if (cookie.getName().equals("TM1SessionId")) {
						// System.out.println("TM1SessionID is now " + cookie.getValue());
						TM1SessionId = cookie.getValue();
						authenticated = true;
						return true;
					}
				}
			} else {
				System.out.println("Failed to authenticate. Server returned " + responseStatus + " response");
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return false;
	}

	public boolean isRestEnabled() {
		if (port > 0)
			return true;
		return false;
	}

	public boolean unauthenticate() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		String request = "ActiveSession";
		get(request);
		OrderedJSONObject jresponse = new OrderedJSONObject(response);
		int activeSessions = jresponse.getInt("ID");
		request = "Sessions(" + activeSessions + ")/tm1.Close";
		post(request, new OrderedJSONObject());
		authenticated = false;
		return true;
	}

	public void exportallprocesses(String directory) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		for (int i = 0; i < processes.size(); i++) {
			processes.get(i).writeToFile(directory);
		}
	}

	public boolean checkServerForObject(TM1Object tm1object) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		String request = tm1object.entity;
		get(request);
		return true;
	}

	/*
	 * import_object_from_file
	 * 
	 * Imports a TM1 Object into this model This accepts a TM1Object that was
	 * created based on a json file The TM1Object would have it's basic properties
	 * and json variable populated using a file The String paramtere is used to
	 * rename the object prior to import into this model
	 */

	public void importObjectFromFile(TM1Object tm1object, String newname) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		String entityset = "";
		OrderedJSONObject payload = new OrderedJSONObject();
		if (tm1object.json.has("@odata.context")) {
			if (tm1object.json.getString("@odata.context").equals("$metadata#Cubes(Dimensions)/$entity")) {
				entityset = "Cubes";
				payload = ((TM1Cube) tm1object).buildJsonForImport(newname);
			} else if (tm1object.json.getString("@odata.context").equals("$metadata#Dimensions/$entity")) {
				entityset = "Dimensions";
				payload = ((TM1Dimension) tm1object).build_json_for_import(newname);
			} else if (tm1object.json.getString("@odata.context").equals("$metadata#Processes/$entity")) {
				entityset = "Processes";
				payload = (OrderedJSONObject) tm1object.json.put("Name", newname);
			} else if (tm1object.json.getString("@odata.context").equals("$metadata#Chores/$entity")) {
				entityset = "Chores";
				payload = (OrderedJSONObject) tm1object.json.put("Name", newname);
			} else {
				// System.out.println("unknown object type");
			}

			if (payload.has("@odata.context"))
				payload.remove("@odata.context");
			String req = entityset;
			post(req, payload);
		}
	}

	public void import_object(TM1Object tm1object, String name) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String req = tm1object.entitySet;
		OrderedJSONObject payload = tm1object.json;
		payload.put("Name", name);
		post(req, payload);
	}

	public void import_dimension(TM1Dimension dimension, String name) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		String req = dimension.entitySet;
		OrderedJSONObject payload = dimension.build_object_json(name);
		post(req, payload);
	}

	public void import_cube(TM1Cube cube, String name) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String req = cube.entitySet;
		OrderedJSONObject payload = cube.getJsonForTransfer(name);
		post(req, payload);
	}

	public String getSessionID() {
		return TM1SessionId;
	}

	public String readVersionFromServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		String request = "Configuration";
		get(request);
		OrderedJSONObject configJSON = new OrderedJSONObject(response);
		return configJSON.getString("ProductVersion");

	}

	/*
	 * Function: search
	 * 
	 * Used to search all objects in the model by name. Rereads all cubes,
	 * dimension, processes, etc from the server. Performs search only on objects
	 * known by the client.
	 */
	public List<SearchResult> search(String searchTerm, boolean includeControlObjects, boolean regex) throws TM1RestException, ClientProtocolException, JSONException, URISyntaxException, IOException {
		List<SearchResult> results = new ArrayList<SearchResult>();
		for (int i = 0; i < cubes.size(); i++) {
			TM1Cube cube = cubes.get(i);
			if (!includeControlObjects && cube.displayName.startsWith("}"))
				continue;
			if (cube.displayName.contains(searchTerm)) {
				results.add(new SearchResult(cube, ""));
			}
			if (cube.readCubeViewsFromServer()) {
				for (int j = 0; j < cube.viewCount(); j++) {
					TM1View view = cube.getview(j);
					if (!includeControlObjects && view.displayName.startsWith("}"))
						continue;
					if (view.displayName.contains(searchTerm)) {
						results.add(new SearchResult(view, "Cube " + cube.displayName));
					}
				}
			}
		}

		for (int i = 0; i < dimensions.size(); i++) {
			TM1Dimension dimension = dimensions.get(i);
			if (!includeControlObjects && dimension.displayName.startsWith("}"))
				continue;
			if (dimension.displayName.contains(searchTerm)) {
				results.add(new SearchResult(dimension, ""));
			}
			dimension.readHierarchiesFromServer();
			for (int j = 0; j < dimension.hierarchyCount(); j++) {
				TM1Hierarchy hierarchy = dimension.getHeirarchy(j);
				if (!includeControlObjects && hierarchy.displayName.startsWith("}"))
					continue;
				if (hierarchy.displayName.contains(searchTerm)) {
					results.add(new SearchResult(hierarchy, " Hierarchy " + hierarchy.displayName));
				}
				hierarchy.readSubsetsFromServer();
				for (int k = 0; k < hierarchy.subsetCount(); k++) {
					TM1Subset subset = hierarchy.getSubset(k);
					if (!includeControlObjects && subset.displayName.startsWith("}"))
						continue;
					if (subset.displayName.contains(searchTerm)) {
						results.add(new SearchResult(subset, "Dimension " + dimension.displayName + " Hierarchy " + hierarchy.displayName));
					}
				}

			}
		}

		for (int i = 0; i < processes.size(); i++) {
			TM1Process process = processes.get(i);
			if (!includeControlObjects && process.displayName.startsWith("}"))
				continue;
			if (process.displayName.contains(searchTerm)) {
				results.add(new SearchResult(process, ""));
			}
		}

		for (int i = 0; i < chores.size(); i++) {
			TM1Chore chore = chores.get(i);
			if (!includeControlObjects && chore.displayName.startsWith("}"))
				continue;
			if (chore.displayName.contains(searchTerm)) {
				results.add(new SearchResult(chore, ""));
			}
		}

		return results;
	}

	/*
	 * Function: keepAlive
	 * 
	 * Simple request used to keep the connection to the server alive
	 */
	public void keepAlive() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String req = "ActiveUser";
		get(req);
	}

	/*
	 * Function: readConfigurationFromFile
	 * 
	 * Reads the clients config file Currently only used for the keystore file and
	 * keystore password
	 */
	public void readConfigurationFromFile() throws IOException {
		System.out.println("Reading config file...");
		String configFileName = ".//config//config";
		FileReader fr = new FileReader(configFileName);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		while (line != null) {
			String[] tokens = line.split(":");
			String parameter = tokens[0];
			String value = tokens[1];
			if (parameter.equals("keystoreFile")) {
				keystoreFile = value;
				System.out.println("Read keystore file " + keystoreFile);
			} else if (parameter.equals("keystorePass")) {
				keystorePass = value;
				System.out.println("Read keystore pass " + keystorePass);
			}
			line = br.readLine();
		}
		br.close();
		fr.close();
	}

}
