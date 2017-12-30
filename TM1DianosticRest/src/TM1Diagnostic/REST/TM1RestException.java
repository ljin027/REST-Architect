package TM1Diagnostic.REST;

public class TM1RestException extends Exception {
	private int response;
	
	TM1RestException(int response){
		this.response = response;
	}
	
	public int getResponse(){
		return response;
	}
}
