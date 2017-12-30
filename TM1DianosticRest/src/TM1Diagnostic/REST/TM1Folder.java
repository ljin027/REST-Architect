package TM1Diagnostic.REST;

import java.util.ArrayList;
import java.util.List;

public class TM1Folder extends TM1Object {
	
	private String ID;
	private TM1Folder parentFolder;
	private List<TM1Folder> folders;
	private List<TM1ObjectReference> references;
	
	public TM1Folder(String name, TM1Server tm1server){
		super(name, TM1Object.FOLDER, tm1server);
		//this.parentFolder = parentFolder;
		folders = new ArrayList<TM1Folder>();	
		references = new ArrayList<TM1ObjectReference>();	
		
	}
	
	public void addfolder(TM1Folder folder){
		folders.add(folder);
	}
	
	public int getFolderCount(){
		return folders.size();
	}
	
	public TM1Folder getFolder(int i){
		return folders.get(i);
	}
	
	public void addReference(TM1ObjectReference reference){
		references.add(reference);
	}
	
	public int getReferenceCount(){
		return references.size();
	}
	
	public TM1ObjectReference getReference(int i){
		return references.get(i);
	}
	
		
}
