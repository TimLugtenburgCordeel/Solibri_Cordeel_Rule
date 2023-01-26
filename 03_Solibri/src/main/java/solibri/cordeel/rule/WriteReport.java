package solibri.cordeel.rule;

import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WriteReport {
	private ArrayList<String> reportList;
	private String finalReport;
	
	public WriteReport(ArrayList<String> report){
		reportList = new ArrayList<String>();
		// Setup the data
		dateTime();
		addStringData(report);
		joinString();
	}
	
	private void dateTime() {
		// Get local date time in the right format
		LocalDateTime myDateObj = LocalDateTime.now();
		DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		String formattedDate = myDateObj.format(myFormatObj);
		
		// Add to the report list
		reportList.add(formattedDate);
	}
	
	private void addStringData(ArrayList<String> report) {
		// Iterate over the string values and add to the report
		for (String string : report) {
			reportList.add(string);
		}
	}
	
	private void joinString() {
		finalReport = String.join(",", reportList);
	}
	
	public void writeToFile(String filePath, String columnDefinition) {
		// If the file is not present create one
		File file = new File(filePath);
		if ( !file.exists()) {
			try {
				// Create file
				file.createNewFile();
				
				// Open file to write column names
				FileWriter writer = new FileWriter(filePath, true);
				
				// Write column names.
				writer.write(columnDefinition);
				
				// Close file.
				writer.close();
			} catch (IOException e) {
				return;
			}
		}
		
		// Add data to the file
		try {
			// Open file
			FileWriter writer = new FileWriter(filePath, true);
				
			// Write line whit line separator.
			writer.write(System.lineSeparator() + finalReport);
			
			// Close writer
			writer.close();
				
			// If there is an error just return.
		} catch (IOException e) {
			return;
		}
	}
}