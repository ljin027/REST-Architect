package TM1Diagnostic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class FunctionsMenuContent {

	private String sourcefile;
	private List<FunctionMenuGroup> functionmenugroups;
	
	public FunctionsMenuContent(String sourcefile){
		this.sourcefile = sourcefile;
		functionmenugroups = new ArrayList<FunctionMenuGroup>();
		try {
			FileReader fw = new FileReader(sourcefile);
			BufferedReader br = new BufferedReader(fw);
			String line = br.readLine();
			while (line != null){
				if (!line.startsWith("#") && !line.isEmpty()){
					String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
					if (tokens.length == 4){
						FunctionMenuOption function = new FunctionMenuOption(tokens[1].replaceAll("\"", ""), tokens[2].replaceAll("\"", ""), tokens[3].replaceAll("\"", ""));
						boolean found = false;
						for (int i=0; i<functionmenugroups.size(); i++){
							if (functionmenugroups.get(i).getname().equals(tokens[0].replaceAll("\"", ""))){
								functionmenugroups.get(i).add(function);
								//System.out.println("Added " + tokens[1].replaceAll("\"", "") + " to existing group " + tokens[0].replaceAll("\"", ""));
								found = true;
								break;
							}
						}
						if (!found){
							FunctionMenuGroup newfunctionmenugroup = new FunctionMenuGroup(tokens[0].replaceAll("\"", ""));
							newfunctionmenugroup.add(function);
							functionmenugroups.add(newfunctionmenugroup);
							//System.out.println("Added " + tokens[1].replaceAll("\"", "") + " to new group " + tokens[0].replaceAll("\"", ""));
						}
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
	
	public int getcount(){
		return functionmenugroups.size(); 
	}
	
	public FunctionMenuGroup getmenugroup(int i){
		return functionmenugroups.get(i);
	}
	
	
	public class FunctionMenuGroup {
		String name;
		List<FunctionMenuOption> functionmenuoptions;
		
		public FunctionMenuGroup(String name){
			this.name = name;
			functionmenuoptions = new ArrayList<FunctionMenuOption>();
		}
		
		public String getname(){
			return name;
		}
		
		public int getcount(){
			return functionmenuoptions.size(); 
		}

		public void add(FunctionMenuOption functionmenuoption){
			functionmenuoptions.add(functionmenuoption);
		}
		
		public FunctionMenuOption getfunctionmenuoption(int i){
			return functionmenuoptions.get(i);
		}
	}
	
	public class FunctionMenuOption {
		private String name;
		private String syntax;
		private String docurl;

		public FunctionMenuOption(String name, String syntax, String docurl){
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
