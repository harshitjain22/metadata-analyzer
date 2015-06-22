import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.codehaus.jackson.JsonNode;

import com.csvreader.CsvWriter;

/**
 * Name:CsvUtill 
 * Created Date: May 26, 2015
 * Created By : Appirio offshore (Harshit Jain)
 * Description: This class contain method related to CSV Writer. 
 * 
 */
public class CsvUtill{
	/**
	 *MethodName : WriteCsv() which create CSV file based on records pass through parameters,records provided in JSON format.
	 *@param fileName : File Name 
	 *@param jsonObject : records in JSON format
	 */
	public static void WriteCsv(String fileName,JsonNode jsonObjectQuery, JsonNode jsonFieldQuery, JsonNode jsonFieldDescribe, String objectName) {
		try {
			File recordfile = new File(fileName);
			Boolean isExist = recordfile.exists();
			// use FileWriter constructor that specifies open for appending
			CsvWriter csvOutput = new CsvWriter(new FileWriter(fileName, true), ',');
			if (!isExist) {
				// We need to write header line for the file if file already not exists.
				csvOutput.write("Object Name");
				csvOutput.write("Field Name");
				csvOutput.write("Type");
				csvOutput.write("Populated On(%)");
				csvOutput.endRecord();
			}
			// write out records
			csvOutput.write(objectName);
			csvOutput.write(jsonFieldDescribe.get("name").asText());
			csvOutput.write(jsonFieldDescribe.get("type").asText());
			Integer populatedOn = 0;
			if(String.valueOf(jsonObjectQuery.get("records")) != "" && jsonObjectQuery.get("records").get(0).get("expr0").asInt() > 0) {
				populatedOn = (int) ((jsonFieldQuery.get("records").get(0).get("expr0").asDouble()/jsonObjectQuery.get("records").get(0).get("expr0").asDouble())*100);
			}
			csvOutput.write(populatedOn+"%");
			csvOutput.endRecord();
			csvOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
