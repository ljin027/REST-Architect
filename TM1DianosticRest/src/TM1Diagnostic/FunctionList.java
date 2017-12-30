package TM1Diagnostic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class FunctionList {

	private String sourcefile;
	private List<Function> functions;
	
	public FunctionList(String sourcefile){
		this.sourcefile = sourcefile;
		functions = new ArrayList<Function>();
		try {
			FileReader fw = new FileReader(sourcefile);
			BufferedReader br = new BufferedReader(fw);
			String line = br.readLine();
			while (line != null){
				if (!line.startsWith("#") && !line.isEmpty()){
					String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
					if (tokens.length == 4){
						Function function = new Function(tokens[1].replaceAll("\"", ""), tokens[2].replaceAll("\"", ""), tokens[3].replaceAll("\"", ""));
						functions.add(function);
					}
				}
				line = br.readLine();
			}
			br.close();
			fw.close();
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	public int contains(String functionname){
		for (int i=0; i<functions.size(); i++){
			if (functions.get(i).name.toLowerCase().equals(functionname.toLowerCase())){
				return i;
			}
		}
		return -1;
	}
	
	public int getfunctioncount(){
		return functions.size();
	}
	
	public Function getfunction(int i){
		return functions.get(i);
	}
	
	public class Function {
		private String name;
		private String syntax;
		private String docurl;

		public Function(String name, String syntax, String docurl){
			this.name = name;
			this.syntax = syntax;
			this.docurl = docurl;
		}
		
		public String getname(){
			return name;
		}

		public String getsyntax(){
			return syntax;
		}

		public String getdocurl(){
			return docurl;
		}

		
	}

}
