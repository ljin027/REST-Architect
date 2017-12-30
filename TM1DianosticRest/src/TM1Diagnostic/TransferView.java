package TM1Diagnostic;

import java.util.ArrayList;
import java.util.List;

import org.apache.wink.json4j.OrderedJSONObject;

public class TransferView {

	public String name;
	public OrderedJSONObject json;
	public List<TransferSubset> subsets;
	
	public TransferView(String name, OrderedJSONObject json){
		this.name = name;
		this.json = json;
		subsets = new ArrayList<TransferSubset>();
	}
	
}
