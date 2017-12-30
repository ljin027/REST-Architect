package TM1Diagnostic.REST;

public class Alias{
	 private String name;
	 private String value;
	
	 public Alias(String name, String value){
		 this.name = name;
		 this.value = value;
	 }
	 
	 public String getName(){
		 return this.name;
	 }
	 
	 public String getValue(){
		 return this.value;
	 }
	 
}