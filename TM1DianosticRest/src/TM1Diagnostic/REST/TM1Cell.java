package TM1Diagnostic.REST;

public class TM1Cell {
	
	static public int NUMERIC = 0;
	static public int STRING = 1;
	
	public int ordinal;
	public int type;
	public String status;
	public String value;
	public String formattedvalue;
	public String formatstring;
	public int updateable;
	public boolean rulederived;
	public boolean annotated;
	public boolean consolidated;
	
	public boolean updated;
	
	
	public TM1Cell(int ordinal, int type, String status, String value, String formattedvalue, String formatstring, int updateable, boolean rulederived, boolean annotated, boolean consolidated){
		this.ordinal = ordinal;
		this.type = type;
		this.status = status;
		this.value = value;
		this.formattedvalue = formattedvalue;
		this.formatstring = formatstring;
		this.updateable = updateable;
		this.rulederived = rulederived;
		this.annotated = annotated;
		this.consolidated = consolidated;
		
		updated = false;
	}
	


	
}
