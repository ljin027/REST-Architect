package TM1Diagnostic;

public class HTTPLogEntry {
	
	private String request;
	private String requestBody;
	private int responseStatus;
	private String responseBody;

	
	public HTTPLogEntry(String request, String requestBody, int responseStatus, String responseBody){
		this.request = request;
		this.requestBody = requestBody;
		this.responseStatus = responseStatus;
		this.responseBody = responseBody;
	}
	
	public String[] showLogEntry(){
		return new String[] { Integer.toString(responseStatus), request, requestBody, responseBody };
	}
	
}
