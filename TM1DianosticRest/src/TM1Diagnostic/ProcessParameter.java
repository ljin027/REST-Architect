package TM1Diagnostic;

public class ProcessParameter {

	static public int NUMERIC = 0;
	static public int STRING = 1;
	
	private String name;
	private int type;
	private String value;
	private String prompt;

	public ProcessParameter(String parameter_name, int type, String value, String prompt){
		this.name = parameter_name;
		this.type = type;
		this.value = value;
		this.prompt = prompt;
	}
	
	public String getname(){
		return name;
	}
	
	public int gettype(){
		return type;
	}
	
	public String getvalue(){
		return value;
	}
	
	public String getprompt(){
		return prompt;
	}
	
	public void setvalue(String value){
		this.value = value;
	}
	
}
