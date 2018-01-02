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
import TM1Diagnostic.REST.TM1Object;
import TM1Diagnostic.REST.TM1Process;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

public class TransferSpec {

	private int transferType;
	private TM1Server targetModel;
	private TM1Server sourceModel;
	private File exportFile;
	private File importFile;

	static public int IMPORT = 1;
	static public int EXPORT = 2;
	static public int TRANSFER = 3;

	private List<TransferCube> cubes;
	private List<TransferDimension> dimensions;
	private List<TransferProcess> processes;
	private List<TransferChore> chores;

	public TransferSpec() {
		transferType = 0;
		cubes = new ArrayList<TransferCube>();
		dimensions = new ArrayList<TransferDimension>();
		processes = new ArrayList<TransferProcess>();
		chores = new ArrayList<TransferChore>();
	}

	public TransferSpec(File importFile, TM1Server targetModel) {
		transferType = IMPORT;
		this.importFile = importFile;
		this.targetModel = targetModel;

		cubes = new ArrayList<TransferCube>();
		dimensions = new ArrayList<TransferDimension>();
		processes = new ArrayList<TransferProcess>();
		chores = new ArrayList<TransferChore>();
	}

	public TransferSpec(TM1Server sourceModel, File exportFile) {
		transferType = EXPORT;
		this.sourceModel = sourceModel;
		this.exportFile = exportFile;

		cubes = new ArrayList<TransferCube>();
		dimensions = new ArrayList<TransferDimension>();
		processes = new ArrayList<TransferProcess>();
		chores = new ArrayList<TransferChore>();
	}

	public TransferSpec(TM1Server sourceModel, TM1Server targetModel) {
		transferType = TRANSFER;
		this.sourceModel = sourceModel;
		this.targetModel = targetModel;

		cubes = new ArrayList<TransferCube>();
		dimensions = new ArrayList<TransferDimension>();
		processes = new ArrayList<TransferProcess>();
		chores = new ArrayList<TransferChore>();
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

	public void addDimension(TransferDimension dimension) {
		dimensions.add(dimension);
	}

	public void addDimension(TM1Dimension dimension) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		TransferDimension transferDimension = new TransferDimension(dimension.displayName, dimension.getExportJSON());
		dimensions.add(transferDimension);
	}

	public TransferDimension getDimension(int i) {
		return dimensions.get(i);
	}

	public int getCubeCount() {
		return cubes.size();
	}

	public void addCube(TransferCube cube) {
		cubes.add(cube);
	}

	public void addCube(TM1Cube cube) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		TransferCube transferCube = new TransferCube(cube.displayName, cube.getExportJSON());
		cubes.add(transferCube);
	}

	public TransferCube getCube(int i) {
		return cubes.get(i);
	}

	public int getProcessCount() {
		return processes.size();
	}

	public void addProcess(TransferProcess process) {
		processes.add(process);
	}

	public void addProcess(TM1Process process) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		TransferProcess transferProcess = new TransferProcess(process.displayName, process.getExportJSON());
		processes.add(transferProcess);
	}

	public TransferProcess getProcess(int i) {
		return processes.get(i);
	}

	public int getChoreCount() {
		return chores.size();
	}

	public void addChore(TransferChore chore) {
		chores.add(chore);
	}

	public void addChore(TM1Chore chore) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		TransferChore transferChore = new TransferChore(chore.displayName, chore.getExportJSON());
		chores.add(transferChore);
	}

	public TransferChore getChore(int i) {
		return chores.get(i);
	}

	public void addModelObject(TM1Object transferObject) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		if (transferObject instanceof TM1Dimension) {
			TM1Dimension dimension = (TM1Dimension)transferObject;
			TransferDimension transferDimension = new TransferDimension(dimension.displayName, dimension);
			addDimension(transferDimension);
		} else if (transferObject instanceof TM1Cube) {
			TM1Cube cube = (TM1Cube)transferObject;
			TransferCube transferCube = new TransferCube(cube.displayName, cube);
			addCube(transferCube);
		} else if (transferObject instanceof TM1Process) {
			TM1Process process = (TM1Process)transferObject;
			TransferProcess transferProcess = new TransferProcess(process);
			addProcess(transferProcess);
		} else if (transferObject instanceof TM1Chore) {
			TM1Chore chore= (TM1Chore)transferObject;
			TransferChore transferChore = new TransferChore(chore);
			addChore(chore);
		} else {
			// Unknown object type
		}
		System.out.println("Added " + transferObject + " to transfer spec");
	}

	public void writeAll() {
		for (int i = 0; i < dimensions.size(); i++) {
			TransferDimension dimension = dimensions.get(i);
			System.out.println("Dimension - " + dimension.name);
			for (int j = 0; j < dimension.hierarchies.size(); j++) {
				TransferHierarchy hierarchy = dimension.hierarchies.get(j);
				System.out.println("Hierarchy - " + hierarchy.name);
				for (int k = 0; k < hierarchy.subsets.size(); k++) {
					TransferSubset subset = hierarchy.subsets.get(k);
					System.out.println("Subset - " + subset.name);

				}
			}
		}
	}

	public void writeToZipFile(String exportBaseDirectory) throws Exception {

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String datetimeString = sdf.format(date);

		String exportTempDirectoryString = exportBaseDirectory + "//export_" + datetimeString;
		File exportTempDirectory = new File(exportTempDirectoryString);
		if (!exportTempDirectory.exists()) {
			exportTempDirectory.mkdirs();
		}

		for (int i = 0; i < dimensions.size(); i++) {
			TransferDimension exportDimension = dimensions.get(i);
			exportDimension.writeDimensionToFile(exportTempDirectory.toString());
		}

		FileOutputStream zipWriter = new FileOutputStream(exportTempDirectoryString + ".zip");
		ZipOutputStream zip = new ZipOutputStream(zipWriter);

		String dimensionTempExportDirString = exportTempDirectory.getAbsolutePath() + "//dim";
		File dimensionTempExportDir = new File(dimensionTempExportDirString);
		String cubeTempExportDirString = exportTempDirectory.getAbsolutePath() + "//cub";
		File cubeTempExportDir = new File(cubeTempExportDirString);
		String processTempExportDirString = exportTempDirectory.getAbsolutePath() + "//pro";
		File processTempExportDir = new File(processTempExportDirString);
		String choreTempExportDirString = exportTempDirectory.getAbsolutePath() + "//cho";
		File choreTempExportDir = new File(choreTempExportDirString);

		if (dimensionTempExportDir.exists()) {
			ZipUtils.addFolderToZip("", dimensionTempExportDirString, zip);
		}

		if (cubeTempExportDir.exists()) {
			ZipUtils.addFolderToZip("", cubeTempExportDirString, zip);
		}

		if (processTempExportDir.exists()) {
			ZipUtils.addFolderToZip("", processTempExportDirString, zip);
		}

		if (choreTempExportDir.exists()) {
			ZipUtils.addFolderToZip("", choreTempExportDirString, zip);
		}

		zip.close();
		zipWriter.close();
		// recursiveDelete(exportTempDirectory);

	}

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
							TransferDimension transferDimension = new TransferDimension(dimensionName, dimensionJSON);
							this.addDimension(transferDimension);
							br.close();
							for (File hierarchyEntry : dimensionDir.listFiles()) {
								if (hierarchyEntry.isFile()) {
									// System.out.println("Found hierarchy file " + hierarchyEntry.getName());
									String hierarchyName = hierarchyEntry.getName().substring(0, hierarchyEntry.getName().lastIndexOf('.'));
									String hiearchyDirectoryName = dimensionDir + "//" + hierarchyName;
									FileReader hierarchyFileReader = new FileReader(hierarchyEntry);
									BufferedReader hierarchyBufferedReader = new BufferedReader(hierarchyFileReader);
									OrderedJSONObject hierarchyJSON = new OrderedJSONObject(hierarchyBufferedReader);
									TransferHierarchy transferHierarchy = new TransferHierarchy(hierarchyName, dimensionName, hierarchyJSON);
									transferDimension.addHierarchy(transferHierarchy);
									hierarchyBufferedReader.close();
									File hierarchyDirectory = new File(hiearchyDirectoryName);
									if (hierarchyDirectory.exists()) {
										// System.out.println("Found hierarchy directory " + hierarchyDirectory);
										for (File subsetEntry : hierarchyDirectory.listFiles()) {
											if (subsetEntry.isFile()) {
												String subsetName = subsetEntry.getName().substring(0, subsetEntry.getName().lastIndexOf('.'));
												FileReader subsetFileReader = new FileReader(subsetEntry);
												BufferedReader subsetBufferedReader = new BufferedReader(subsetFileReader);
												OrderedJSONObject subsetJSON = new OrderedJSONObject(subsetBufferedReader);
												TransferSubset transferSubset = new TransferSubset(subsetName, hierarchyName, dimensionName, subsetJSON);
												transferHierarchy.addSubset(transferSubset);
												subsetBufferedReader.close();
												// System.out.println("Found subset file " + subsetEntry.getName());
											}
										}
									} else {
										System.out.println("Failed to find hierarchy directory " + hierarchyDirectory);
									}
								}
							}
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
						String cubeName = cubeEntry.getName().substring(0, cubeEntry.getName().lastIndexOf('.'));
						FileReader fr = new FileReader(cubeEntry);
						BufferedReader br = new BufferedReader(fr);
						OrderedJSONObject cubeJSON = new OrderedJSONObject(br);
						TransferCube transferCube = new TransferCube(cubeName, cubeJSON);
						File dataFile = new File(cubeEntry.getAbsolutePath().replace(".cube", ".data"));
						if (dataFile.exists()){
							System.out.println("Found data file: " + dataFile.getAbsolutePath());		
							transferCube.readImportData(dataFile);				
						}
						this.addCube(transferCube);
					}
				}
			}

			File processDirectory = new File(baseImportDirectory + "//pro");
			if (processDirectory.exists()) {
				for (File processEntry : processDirectory.listFiles()) {
					if (processEntry.isFile() && processEntry.getName().endsWith(".pro")) {
						String processName = processEntry.getName().substring(0, processEntry.getName().lastIndexOf('.'));
						FileReader fr = new FileReader(processEntry);
						BufferedReader br = new BufferedReader(fr);
						OrderedJSONObject processJSON = new OrderedJSONObject(br);
						TransferProcess transferProcess = new TransferProcess(processName, processJSON);
						this.addProcess(transferProcess);
					} 
				}
			}

			File choreDirectory = new File(baseImportDirectory + "//cho");
			if (choreDirectory.exists()) {
				for (File choreEntry : choreDirectory.listFiles()) {
					if (choreEntry.isFile() && choreEntry.getName().endsWith(".cho")) {
						String choreName = choreEntry.getName().substring(0, choreEntry.getName().lastIndexOf('.'));
						FileReader fr = new FileReader(choreEntry);
						BufferedReader br = new BufferedReader(fr);
						OrderedJSONObject choreJSON = new OrderedJSONObject(br);
						TransferChore transferChore = new TransferChore(choreName, choreJSON);
						this.addChore(transferChore);
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
				TransferDimension dimension = dimensions.get(i);
				dimension.importToServer(targetModel);
			}
			for (int i = 0; i < cubes.size(); i++) {
				TransferCube cube = cubes.get(i);
				cube.importToServer(targetModel);
			}
			for (int i = 0; i < processes.size(); i++) {
				TransferProcess process = processes.get(i);
				process.importToServer(targetModel);
			}
			for (int i = 0; i < chores.size(); i++) {
				TransferChore chore = chores.get(i);
				chore.importToServer(targetModel);
			}
		} else if (transferType == TransferSpec.EXPORT) {
		
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String datetimeString = sdf.format(date);

			String exportTempDirectoryString = ".//temp//export_" + datetimeString;
			File exportTempDirectory = new File(exportTempDirectoryString);
			if (!exportTempDirectory.exists()) {
				exportTempDirectory.mkdirs();
			}
		
			for (int i = 0; i < dimensions.size(); i++) {
				TransferDimension transferDimension = dimensions.get(i);
				transferDimension.writeDimensionToFile(exportTempDirectoryString);
			}
			
			for (int i = 0; i < cubes.size(); i++) {
				TransferCube transferCube = cubes.get(i);
				transferCube.writeCubeToFile(exportTempDirectoryString);
			}
			
			for (int i = 0; i < processes.size(); i++) {
				TransferProcess process = processes.get(i);
				process.writeToFile(exportTempDirectoryString);
			}
			for (int i = 0; i < chores.size(); i++) {
				TransferChore chore = chores.get(i);
				chore.writeToFile(exportTempDirectoryString);
			}
			
			FileOutputStream zipWriter = new FileOutputStream(this.exportFile);
			ZipOutputStream zip = new ZipOutputStream(zipWriter);
			String dimensionTempExportDirString = exportTempDirectory.getAbsolutePath() + "//dim";
			File dimensionTempExportDir = new File(dimensionTempExportDirString);
			String cubeTempExportDirString = exportTempDirectory.getAbsolutePath() + "//cub";
			File cubeTempExportDir = new File(cubeTempExportDirString);
			String processTempExportDirString = exportTempDirectory.getAbsolutePath() + "//pro";
			File processTempExportDir = new File(processTempExportDirString);
			String choreTempExportDirString = exportTempDirectory.getAbsolutePath() + "//cho";
			File choreTempExportDir = new File(choreTempExportDirString);

			if (dimensionTempExportDir.exists()) {
				ZipUtils.addFolderToZip("", dimensionTempExportDirString, zip);
			}

			if (cubeTempExportDir.exists()) {
				ZipUtils.addFolderToZip("", cubeTempExportDirString, zip);
			}

			if (processTempExportDir.exists()) {
				ZipUtils.addFolderToZip("", processTempExportDirString, zip);
			}

			if (choreTempExportDir.exists()) {
				ZipUtils.addFolderToZip("", choreTempExportDirString, zip);
			}

			zip.close();
			zipWriter.close();
			//recursiveDelete(exportTempDirectory);
			
		} else if (transferType == TransferSpec.TRANSFER) {

		}
	}

}
