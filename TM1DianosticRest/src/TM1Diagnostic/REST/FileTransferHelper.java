package TM1Diagnostic.REST;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONException;

import TM1Diagnostic.SearchResult;

public class FileTransferHelper {

	private TM1Server tm1server;
	private File directory;
	
	// private List<TM1Folder> folders;
	private List<TM1Cube> cubes;
	private List<TM1Dimension> dimensions;
	private List<TM1Process> processes;
	private List<TM1Chore> chores;
	// private List<TM1Blob> blobs;

	public boolean cubesexpanded;
	public boolean dimensionsexpanded;
	public boolean processesexpanded;
	public boolean choresexpanded;

	public FileTransferHelper(TM1Server tm1server) {

		// folders = new ArrayList<TM1Folder>();
		cubes = new ArrayList<TM1Cube>();
		dimensions = new ArrayList<TM1Dimension>();
		processes = new ArrayList<TM1Process>();
		chores = new ArrayList<TM1Chore>();
		// blobs = new ArrayList<TM1Blob>();

		cubesexpanded = true;
		dimensionsexpanded = true;
		processesexpanded = true;
		choresexpanded = true;
		
		this.tm1server = tm1server;

	}
	

	
	public void setExportDirectory(File directory){
		this.directory = directory;
	}

	public int cubeCount() {
		return cubes.size();
	}

	public TM1Cube getCube(int i) {
		return cubes.get(i);
	}

	public void addCube(TM1Cube cube) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		if (!cubes.contains(cube)) {
			if (cubes.size() > 0) {
				for (int i = 0; i < cubes.size(); i++) {
					TM1Cube currentCube = cubes.get(i);
					if (currentCube.displayName.toLowerCase().charAt(0) > cube.displayName.toLowerCase().charAt(0)) {
						cubes.add(i, cube);
						return;
					}
				}
			}
			cubes.add(cube);
			List<SearchResult> sourceCubesByRule = cube.getRuleSourceCubesList();
			for (int i=0; i<sourceCubesByRule.size(); i++){
				TM1Cube referencedCube = (TM1Cube)sourceCubesByRule.get(i).tm1object;
				addCube(referencedCube);
			}
		}
	}

	public int dimensionCount() {
		return dimensions.size();
	}

	public TM1Dimension getDimension(int i) {
		return dimensions.get(i);
	}

	public void addDimension(TM1Dimension dimension) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		if (!dimensions.contains(dimension)) {
			if (dimensions.size() > 0) {
				for (int i = 0; i < dimensions.size(); i++) {
					TM1Dimension currentDimension = dimensions.get(i);
					if (currentDimension.displayName.toLowerCase().charAt(0) > dimension.displayName.toLowerCase().charAt(0)) {
						dimensions.add(i, dimension);
						return;
					}
				}
			}
			dimensions.add(dimension);
		}
		
		if (dimension.checkServerForElementAttributes()){
			TM1Dimension elementAttributeDimension = dimension.getElementAttributeDimension();
			addDimension(elementAttributeDimension);
			TM1Cube elementAttributeCube = dimension.getElementAttributeCube();
			addCube(elementAttributeCube);
		}
		
	}

	public int processCount() {
		return processes.size();
	}

	public TM1Process getProcess(int i) {
		return processes.get(i);
	}

	public void addProcess(TM1Process process) {
		if (!processes.contains(process)) {
			if (processes.size() > 0) {
				for (int i = 0; i < processes.size(); i++) {
					TM1Process currentProcess = processes.get(i);
					if (currentProcess.displayName.toLowerCase().charAt(0) > process.displayName.toLowerCase().charAt(0)) {
						processes.add(i, process);
						return;
					}
				}
			}
			processes.add(process);
		}
	}

	public int choreCount() {
		return chores.size();
	}

	public TM1Chore getChore(int i) {
		return chores.get(i);
	}

	public void addChore(TM1Chore chore) {
		if (!chores.contains(chore)) {
			if (processes.size() > 0) {
				for (int i = 0; i < chores.size(); i++) {
					TM1Chore currentChore = chores.get(i);
					if (currentChore.displayName.toLowerCase().charAt(0) > chore.displayName.toLowerCase().charAt(0)) {
						chores.add(i, chore);
						return;
					}
				}
			}
			chores.add(chore);
		}
	}
	
	public boolean exportToFile() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {

		File processDir = new File(directory + "//processes");
		if (!processDir.exists()){
			processDir.mkdirs();
		}
		for (int i=0; i<processes.size(); i++){
			TM1Process process = processes.get(i);
			process.writeToFile(processDir.toString());
		}
		
		File choreDir = new File(directory + "//chores");
		if (!choreDir.exists()){
			choreDir.mkdirs();
		}
		for (int i=0; i<chores.size(); i++){
			TM1Chore chore = chores.get(i);
			chore.writeToFile(choreDir.toString());
		}
		
		File dimensionDir = new File(directory + "//dimensions");
		if (!dimensionDir.exists()){
			dimensionDir.mkdirs();
		}
		for (int i=0; i<dimensions.size(); i++){
			TM1Dimension dimension = dimensions.get(i);
			dimension.readHierarchiesFromServer();
			dimension.writeToFile(dimensionDir.toString());
			File dimensionContentsDir = new File(dimensionDir + "//" + dimension.displayName + "_hierarchies");
			if (!dimensionContentsDir.exists()){
				dimensionContentsDir.mkdirs();
			}
			for (int j=0; j<dimension.hierarchyCount(); j++){
				TM1Hierarchy hierarchy = dimension.getHeirarchy(j);
				hierarchy.readHierarchyFromServer();
				hierarchy.writeToFile(dimensionContentsDir.toString());
				File hierarchyContentsDir = new File(dimensionContentsDir + "//" + hierarchy.displayName + "_subsets");
				if (!hierarchyContentsDir.exists()){
					hierarchyContentsDir.mkdirs();
				}
				hierarchy.readSubsetsFromServer();
				for (int k=0; k<hierarchy.subsetCount(); k++){
					TM1Subset subset = hierarchy.getSubset(k);
					subset.readElementListFromServer();
					subset.writeToFile(hierarchyContentsDir.toString());
					
				}
			}
		}
		
		File cubeDir = new File(directory + "//cubes");
		if (!cubeDir.exists()){
			cubeDir.mkdirs();
		}
		for (int i=0; i<cubes.size(); i++){
			TM1Cube cube = cubes.get(i);
			cube.writeToFile(cubeDir.toString());
			if (cube.checkServerForRules()){
				// cube export rules
			}
			File cubeContentsDir = new File(cubeDir + "//" + cube.displayName + "_views");
			if (!cubeContentsDir.exists()){
				cubeContentsDir.mkdirs();
				cube.readCubeViewsFromServer();
				for (int j=0; j<cube.viewCount(); j++){
					TM1View view = cube.getview(j);
					view.writeToFile(cubeContentsDir.toString());
				}
			}
		}
		return true;
	}

}
