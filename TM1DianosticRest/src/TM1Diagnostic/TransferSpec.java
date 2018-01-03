package TM1Diagnostic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.TreeItem;

import TM1Diagnostic.REST.TM1Chore;
import TM1Diagnostic.REST.TM1Cube;
import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1Hierarchy;
import TM1Diagnostic.REST.TM1Process;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;
import TM1Diagnostic.REST.TM1Subset;

public class TransferSpec {

	private int transferType;
	private TM1Server targetModel;
	private TM1Server sourceModel;
	private File exportFile;
	private File importFile;

	static public int IMPORT = 1;
	static public int EXPORT = 2;
	static public int TRANSFER = 3;

	private List<TM1Cube> cubes;
	private List<TM1Dimension> dimensions;
	private List<TM1Hierarchy> hierarchies;
	private List<TM1Subset> subsets;
	private List<TM1Process> processes;
	private List<TM1Chore> chores;

	public TransferSpec() {
		transferType = 0;
		cubes = new ArrayList<TM1Cube>();
		dimensions = new ArrayList<TM1Dimension>();
		hierarchies = new ArrayList<TM1Hierarchy>();
		subsets = new ArrayList<TM1Subset>();
		processes = new ArrayList<TM1Process>();
		chores = new ArrayList<TM1Chore>();
	}

	public int getTransferType() {
		return transferType;
	}

	public void setImport(File importFile, TM1Server targetModel) {
		this.importFile = importFile;
		this.targetModel = targetModel;
		this.transferType = TransferSpec.IMPORT;
	}

	public void setExport(TM1Server sourceModel, File exportFile) {
		this.sourceModel = sourceModel;
		this.exportFile = exportFile;
		this.transferType = TransferSpec.EXPORT;
	}

	public void setExport(TM1Server sourceModel, TM1Server targetModel) {
		this.sourceModel = sourceModel;
		this.targetModel = targetModel;
		this.transferType = TransferSpec.TRANSFER;
	}

	public void setTargetModel(TM1Server targetModel) {
		this.targetModel = targetModel;
	}

	public void setSourceModel(TM1Server sourceMode) {
		this.sourceModel = sourceModel;
	}

	public int getDimensionCount() {
		return dimensions.size();
	}

	public void addDimension(TM1Dimension dimension) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		System.out.println("addDimension " + dimension.name);
		if (!dimensions.contains(dimension)) {
			System.out.println("Dimension did not contain " + dimension.name);
			dimensions.add(dimension);
			if (dimension.checkServerForElementAttributes()) {
				System.out.println("Found attributes for " + dimension.name);
				TM1Dimension attributeDimension = dimension.getElementAttributeDimension();
				addAttributeDimension(attributeDimension);
				TM1Cube attributeCube = dimension.getElementAttributeCube();
				addAttributeCube(attributeCube);
			} else {
				System.out.println("No attribute for " + dimension.name);
			}
		}
	}
	
	public void addAttributeDimension(TM1Dimension dimension) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		System.out.println("addAttributeDimension " + dimension.name);
		if (!dimensions.contains(dimension)) {
			dimensions.add(dimension);
		}
	}
	
	public void addAttributeCube(TM1Cube cube) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		System.out.println("addAttributeCube " + cube.name);
		if (!cubes.contains(cube)) {
			cubes.add(cube);
		}
	}

	public TM1Dimension getDimension(int i) {
		return dimensions.get(i);
	}

	public int getCubeCount() {
		return cubes.size();
	}

	public void addCube(TM1Cube cube) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		System.out.println("addCube " + cube.name);
		if (!cubes.contains(cube)) {
			cubes.add(cube);
			for (int i = 0; i < cube.dimensionCount(); i++) {
				TM1Dimension dimension = cube.getDimension(i);
				if (!dimensions.contains(dimension)) {
					addDimension(dimension);
				}
			}
		}
	}

	public TM1Cube getCube(int i) {
		return cubes.get(i);
	}

	public int getProcessCount() {
		return processes.size();
	}

	public void addProcess(TM1Process process) {
		if (!processes.contains(process)) {
			processes.add(process);
		}
	}

	public TM1Process getProcess(int i) {
		return processes.get(i);
	}

	public int getChoreCount() {
		return chores.size();
	}

	public void addChore(TM1Chore chore) {
		if (!chores.contains(chore)) {
			chores.add(chore);
		}
	}

	public TM1Chore getChore(int i) {
		return chores.get(i);
	}

	public void addModelObject(Object transferObject) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		if (transferObject instanceof TM1Dimension) {
			TM1Dimension dimension = (TM1Dimension) transferObject;
			addDimension(dimension);
		} else if (transferObject instanceof TM1Cube) {
			TM1Cube cube = (TM1Cube) transferObject;
			addCube(cube);
		} else if (transferObject instanceof TM1Process) {
			TM1Process process = (TM1Process) transferObject;
			addProcess(process);
		} else if (transferObject instanceof TM1Chore) {
			TM1Chore chore = (TM1Chore) transferObject;
			addChore(chore);
		} else {
			// Unknown object type
		}
		System.out.println("Added " + transferObject + " to transfer spec");
	}

	/*
	 * public void writeAll() { for (int i = 0; i < dimensions.size(); i++) {
	 * TransferDimension dimension = dimensions.get(i);
	 * System.out.println("Dimension - " + dimension.name); for (int j = 0; j <
	 * dimension.hierarchies.size(); j++) { TransferHierarchy hierarchy =
	 * dimension.hierarchies.get(j); System.out.println("Hierarchy - " +
	 * hierarchy.name); for (int k = 0; k < hierarchy.subsets.size(); k++) {
	 * TransferSubset subset = hierarchy.subsets.get(k);
	 * System.out.println("Subset - " + subset.name);
	 * 
	 * } } } }
	 */

	/*
	 * public void writeToZipFile(String exportBaseDirectory) throws Exception {
	 * 
	 * Date date = new Date(); SimpleDateFormat sdf = new
	 * SimpleDateFormat("yyyyMMddHHmmss"); String datetimeString = sdf.format(date);
	 * 
	 * String exportTempDirectoryString = exportBaseDirectory + "//export_" +
	 * datetimeString; File exportTempDirectory = new
	 * File(exportTempDirectoryString); if (!exportTempDirectory.exists()) {
	 * exportTempDirectory.mkdirs(); }
	 * 
	 * for (int i = 0; i < dimensions.size(); i++) { TM1Dimension exportDimension =
	 * dimensions.get(i);
	 * exportDimension.writeDimensionToFile(exportTempDirectory.toString()); }
	 * 
	 * FileOutputStream zipWriter = new FileOutputStream(exportTempDirectoryString +
	 * ".zip"); ZipOutputStream zip = new ZipOutputStream(zipWriter);
	 * 
	 * String dimensionTempExportDirString = exportTempDirectory.getAbsolutePath() +
	 * "//dim"; File dimensionTempExportDir = new
	 * File(dimensionTempExportDirString); String cubeTempExportDirString =
	 * exportTempDirectory.getAbsolutePath() + "//cub"; File cubeTempExportDir = new
	 * File(cubeTempExportDirString); String processTempExportDirString =
	 * exportTempDirectory.getAbsolutePath() + "//pro"; File processTempExportDir =
	 * new File(processTempExportDirString); String choreTempExportDirString =
	 * exportTempDirectory.getAbsolutePath() + "//cho"; File choreTempExportDir =
	 * new File(choreTempExportDirString);
	 * 
	 * if (dimensionTempExportDir.exists()) { ZipUtils.addFolderToZip("",
	 * dimensionTempExportDirString, zip); }
	 * 
	 * if (cubeTempExportDir.exists()) { ZipUtils.addFolderToZip("",
	 * cubeTempExportDirString, zip); }
	 * 
	 * if (processTempExportDir.exists()) { ZipUtils.addFolderToZip("",
	 * processTempExportDirString, zip); }
	 * 
	 * if (choreTempExportDir.exists()) { ZipUtils.addFolderToZip("",
	 * choreTempExportDirString, zip); }
	 * 
	 * zip.close(); zipWriter.close(); // recursiveDelete(exportTempDirectory);
	 * 
	 * }
	 */

	public void readSourceZipFile(String importZipFile) {
		try {
			// transferSourceTree.removeAll();
			String uniqueID = "Import_" + UUID.randomUUID().toString();
			String baseImportDirectory = ".//temp//" + uniqueID;
			ZipUtils.unzip(importZipFile, baseImportDirectory);

			File dimensionImportPath = new File(baseImportDirectory + "//dim");
			if (dimensionImportPath.exists()) {
				for (File dimensionEntry : dimensionImportPath.listFiles()) {
					if (dimensionEntry.isFile()) {
						// System.out.println("Found dimension file " + dimensionEntry.getName());
						String dimensionName = dimensionEntry.getName().substring(0, dimensionEntry.getName().lastIndexOf('.'));
						String dimensionDirectoryString = dimensionImportPath.getAbsolutePath() + "//" + dimensionName;
						// System.out.println("Checking for dimension directory " +
						// dimensionDirectoryString);

						File dimensionDir = new File(dimensionDirectoryString);
						if (dimensionDir.exists()) {
							// System.out.println("Found dimension directory " + dimensionDir);
							FileReader fr = new FileReader(dimensionEntry);
							BufferedReader br = new BufferedReader(fr);
							OrderedJSONObject dimensionJSON = new OrderedJSONObject(br);
							TM1Dimension dimension = new TM1Dimension(dimensionJSON);
							this.addDimension(dimension);
							br.close();
							/*
							 * for (File hierarchyEntry : dimensionDir.listFiles()) { if
							 * (hierarchyEntry.isFile()) { // System.out.println("Found hierarchy file " +
							 * hierarchyEntry.getName()); String hierarchyName =
							 * hierarchyEntry.getName().substring(0,
							 * hierarchyEntry.getName().lastIndexOf('.')); String hiearchyDirectoryName =
							 * dimensionDir + "//" + hierarchyName; FileReader hierarchyFileReader = new
							 * FileReader(hierarchyEntry); BufferedReader hierarchyBufferedReader = new
							 * BufferedReader(hierarchyFileReader); OrderedJSONObject hierarchyJSON = new
							 * OrderedJSONObject(hierarchyBufferedReader); TM1Hierarchy hierarchy = new
							 * TM1Hierarchy(hierarchyJSON); dimension.addHierarchy(hierarchy);
							 * hierarchyBufferedReader.close(); File hierarchyDirectory = new
							 * File(hiearchyDirectoryName); if (hierarchyDirectory.exists()) { //
							 * System.out.println("Found hierarchy directory " + hierarchyDirectory); for
							 * (File subsetEntry : hierarchyDirectory.listFiles()) { if
							 * (subsetEntry.isFile()) { String subsetName =
							 * subsetEntry.getName().substring(0, subsetEntry.getName().lastIndexOf('.'));
							 * FileReader subsetFileReader = new FileReader(subsetEntry); BufferedReader
							 * subsetBufferedReader = new BufferedReader(subsetFileReader);
							 * OrderedJSONObject subsetJSON = new OrderedJSONObject(subsetBufferedReader);
							 * TransferSubset transferSubset = new TransferSubset(subsetName, hierarchyName,
							 * dimensionName, subsetJSON); hierarchy.addSubset(transferSubset);
							 * subsetBufferedReader.close(); // System.out.println("Found subset file " +
							 * subsetEntry.getName()); } } } else {
							 * System.out.println("Failed to find hierarchy directory " +
							 * hierarchyDirectory); } } }
							 */
						} else {
							System.out.println("Failed to find dimension directory " + dimensionDir);
						}
					}
				}
			}

			File cubeDirectory = new File(baseImportDirectory + "//cub");
			if (cubeDirectory.exists()) {
				for (File cubeEntry : cubeDirectory.listFiles()) {
					if (cubeEntry.isFile() && cubeEntry.getName().endsWith(".cube")) {
						TM1Cube transferCube = new TM1Cube(cubeEntry);
						this.addCube(transferCube);
					}
				}
			}

			File processDirectory = new File(baseImportDirectory + "//pro");
			if (processDirectory.exists()) {
				for (File processEntry : processDirectory.listFiles()) {
					if (processEntry.isFile() && processEntry.getName().endsWith(".pro")) {
						String processName = processEntry.getName().substring(0, processEntry.getName().lastIndexOf('.'));
						TM1Process transferProcess = new TM1Process(processEntry);
						this.addProcess(transferProcess);
					}
				}
			}

			File choreDirectory = new File(baseImportDirectory + "//cho");
			if (choreDirectory.exists()) {
				for (File choreEntry : choreDirectory.listFiles()) {
					if (choreEntry.isFile() && choreEntry.getName().endsWith(".cho")) {
						String choreName = choreEntry.getName().substring(0, choreEntry.getName().lastIndexOf('.'));

						TM1Chore chore = new TM1Chore(choreEntry);
						this.addChore(chore);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void runTransfer() throws Exception {
		if (transferType == TransferSpec.IMPORT) {
			for (int i = 0; i < dimensions.size(); i++) {
				TM1Dimension dimension = dimensions.get(i);
				// dimension.importToServer(targetModel);
			}
			for (int i = 0; i < cubes.size(); i++) {
				TM1Cube cube = cubes.get(i);
				// cube.importToServer(targetModel);
			}
			for (int i = 0; i < processes.size(); i++) {
				TM1Process process = processes.get(i);
				// process.importToServer(targetModel);
			}
			for (int i = 0; i < chores.size(); i++) {
				TM1Chore chore = chores.get(i);
				// chore.importToServer(targetModel);
			}
		} else if (transferType == TransferSpec.EXPORT) {
			fileExport();
		} else if (transferType == TransferSpec.TRANSFER) {

		}
	}

	public void fileExport() throws Exception {
		String uniqueID = UUID.randomUUID().toString();

		String exportDirectoryName = ".//temp//" + uniqueID;
		File exportDirectory = new File(exportDirectoryName);
		if (!exportDirectory.exists()) {
			exportDirectory.mkdirs();
		}

		FileOutputStream zipWriter = new FileOutputStream(this.exportFile);
		ZipOutputStream zip = new ZipOutputStream(zipWriter);

		if (dimensions.size() > 0) {
			String exportDimensionsDirectoryName = exportDirectoryName + "//dimensions";
			File exportDimensionsDirectory = new File(exportDimensionsDirectoryName);
			if (!exportDimensionsDirectory.exists()) {
				exportDimensionsDirectory.mkdirs();
			}
			for (int i = 0; i < dimensions.size(); i++) {

				TM1Dimension dimension = dimensions.get(i);

				String exportDimensionDirectoryName = exportDimensionsDirectoryName + "//" + dimension.name;
				File exportDimensionDirectory = new File(exportDimensionDirectoryName);
				if (!exportDimensionDirectory.exists()) {
					exportDimensionDirectory.mkdirs();
				}

				File exportFileName = new File(exportDimensionDirectoryName + "//" + dimension.name + ".dimension");
				dimension.fileExport(exportFileName);

				dimension.readHierarchiesFromServer();

				// int altHierarchyCount = 1;
				for (int j = 0; j < dimension.hierarchyCount(); j++) {
					TM1Hierarchy hierarchy = dimension.getHeirarchy(j);
					if (!hierarchy.name.equals(dimension.name) && !hierarchy.name.equals("Leaves")) {
						File hierarchyExportFile = new File(exportDimensionDirectoryName + "//" + hierarchy.name + ".hierarcy");
						hierarchy.fileExport(hierarchyExportFile);
						// altHierarchyCount++;
					}
				}
			}
			ZipUtils.addFolderToZip("", exportDimensionsDirectoryName, zip);
		}

		if (cubes.size() > 0) {
			String exportCubesDirectoryName = exportDirectoryName + "//cubes";
			File exportCubesDirectory = new File(exportCubesDirectoryName);
			if (!exportCubesDirectory.exists()) {
				exportCubesDirectory.mkdirs();
			}
			for (int i = 0; i < cubes.size(); i++) {
				TM1Cube exportCube = cubes.get(i);
				File exportFileName = new File(exportCubesDirectoryName + "//" + exportCube.name + ".cube");
				exportCube.fileExport(exportFileName);
			}
			ZipUtils.addFolderToZip("", exportCubesDirectoryName, zip);
		}

		if (processes.size() > 0) {
			String exportProcessesDirectoryName = exportDirectoryName + "//processes";
			File exportProcessesDirectory = new File(exportProcessesDirectoryName);
			if (!exportProcessesDirectory.exists()) {
				exportProcessesDirectory.mkdirs();
			}
			for (int i = 0; i < processes.size(); i++) {
				TM1Process exportProcess = processes.get(i);
				File exportFileName = new File(exportProcessesDirectoryName + "//" + exportProcess.name + ".process");
				exportProcess.fileExport(exportFileName);
			}
			ZipUtils.addFolderToZip("", exportProcessesDirectoryName, zip);
		}

		if (chores.size() > 0) {
			String exportChoresDirectoryName = exportDirectoryName + "//chores";
			File exportChoresDirectory = new File(exportChoresDirectoryName);
			if (!exportChoresDirectory.exists()) {
				exportChoresDirectory.mkdirs();
			}
			for (int i = 0; i < chores.size(); i++) {
				TM1Chore exportChore = chores.get(i);
				File exportFileName = new File(exportChoresDirectoryName + "//" + exportChore.name + ".chore");
				exportChore.fileExport(exportFileName);
			}
			ZipUtils.addFolderToZip("", exportChoresDirectoryName, zip);
		}

		zip.close();
		zipWriter.close();
		// recursiveDelete(exportTempDirectory);
	}

}
