package TM1Diagnostic.REST;

public class TM1RestException extends Exception {
	private int errorCode;
	private String errorMessage;
	
	
	TM1RestException(int errorCode, String errorMessage){
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
	
	public int getErrorCode(){
		return errorCode;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
}
