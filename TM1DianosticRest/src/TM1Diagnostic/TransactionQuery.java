package TM1Diagnostic;

public class TransactionQuery {

	private String startdatetime;
	private String enddatetime;
	private String cube;
	private String client;

	
	public TransactionQuery(String startdatetime, String enddatetime, String cube, String client){
		this.startdatetime = startdatetime; 
		this.enddatetime = enddatetime;
		this.cube = cube;
		this.client = client;
	}
	
	public String processquery(){
		//TransactionLogEntries?$filter=User eq 'CJP/stuart' and Cube eq '123'
		String s = "TransactionLogEntries";
		if (!cube.equals("*") && !client.equals("*")){
			s = s + "?$filter=User%20eq%20'" + client + "' and Cube eq '" + cube + "'";
		} else if (!cube.equals("*") && client.equals("*")){
			s = s + "?$filter=Cube%20eq%20'" + cube + "'";
		} else if (cube.equals("*") && !client.equals("*")){
			s = s + "?$filter=User%20eq%20'" + client + "'";
		} else if (cube.equals("*") && client.equals("*")){

		}
		System.out.println("SSS: " + s);
		return s;
	}
}
