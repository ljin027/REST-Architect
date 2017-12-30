package TM1Diagnostic.REST;

import java.util.ArrayList;
import java.util.List;

public class TM1ViewAxes {

	public int ordinal;
	public List<String> hierarchy_names;
	public List<TM1ViewTuple> tuples;
	
	
	public TM1ViewAxes(){
		hierarchy_names = new ArrayList<String>(); 
		tuples = new ArrayList<TM1ViewTuple>();
	}
	
	public void addTuple(TM1ViewTuple tuple){
		this.tuples.add(tuple);
	}
	
	public void addHierarchyName(String hierarchy_name){
		hierarchy_names.add(hierarchy_name);
	}
}
