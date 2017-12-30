package TM1Diagnostic;

public class ProcessVariable {
	
	/*
	 * {"@odata.context":"../$metadata#Processes('Sample%20Export%20Process')/Variables",
	 * "value":
	 * [
	 * {"Name":"Measures","Type":"String","Position":5,"StartByte":0,"EndByte":0},
	 * {"Name":"Locations","Type":"String","Position":1,"StartByte":0,"EndByte":0},
	 * {"Name":"Months","Type":"String","Position":2,"StartByte":0,"EndByte":0},
	 * {"Name":"Value","Type":"Numeric","Position":6,"StartByte":0,"EndByte":0}
	 * ]
	 * }
	 * 
	 */
	
	static public int NUMERIC = 0;
	static public int STRING = 1;
	
	private String name;
	private String type;
	private int position;
	private int startbyte;
	private int endbyte;
	
	public ProcessVariable(String name, String type, int position, int startbyte, int endbyte){
		this.name = name;
		this.type = type;
		this.position = position;
		this.startbyte = startbyte;
		this.endbyte = endbyte;
	}
	
	public ProcessVariable(String name, String type, int position){
		this.name = name;
		this.type = type;
		this.position = position;
		this.startbyte = startbyte;
		this.endbyte = endbyte;
	}
	
	public String getname(){
		return name;
	}
	
	public String gettype(){
		return type;
	}
	
	public int getposition(){
		return position;
	}
	
	public int startbyte(){
		return startbyte;
	}
	
	public int endbyte(){
		return endbyte;
	}

}
