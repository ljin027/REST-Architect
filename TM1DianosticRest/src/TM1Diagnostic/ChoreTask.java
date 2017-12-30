package TM1Diagnostic;

import java.util.ArrayList;
import java.util.List;

import TM1Diagnostic.REST.TM1Process;

public class ChoreTask {

	public int step;
	public TM1Process taskProcess;
	public List<ProcessParameter> taskParameters;
	
	public ChoreTask(int step, TM1Process process){
		taskProcess = process;
		taskParameters = new ArrayList<ProcessParameter>();
	}
	
	
	public void addParameter(ProcessParameter parameter){
		taskParameters.add(parameter);
	}
	
	public TM1Process getProcess(){
		return taskProcess;
	}
	
}
