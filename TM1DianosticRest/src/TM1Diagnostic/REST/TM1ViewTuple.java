package TM1Diagnostic.REST;

import java.util.ArrayList;
import java.util.List;

import TM1Diagnostic.TM1ViewMember;

public class TM1ViewTuple {

	public int ordinal;
	public List<TM1ViewMember> members;
	
	public TM1ViewTuple(){
		members = new ArrayList<TM1ViewMember>();
	}
	
	
	
}
