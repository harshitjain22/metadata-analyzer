import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Name:PropertyConfiguration 
 * Created Date: May 26, 2015
 * Created By : Harshit Jain
 * Description: This class load a properties file and initialize the properties of this class by values get from properties file.
 * 
 */
public class PropertyConfiguration {

	public static String SF_OAUTH_URL;
	public static String SF_QUERY_URL;
	public static String SF_DESCRIBE_URL;
	public static String SALESFORCE_CLIENTID;
	public static String SALESFORCE_CLIENTSECRET;
	public static String SALESFORCE_USERNAME;
	public static String SALESFORCE_PASSWORD;
	public static String SALESFORCE_SECURITYTOKEN;
	public static String Acc_Query;
	public static String File_Path;
	public static String Is_Sandbox;
	public static String Proxy_Ip;
	public static String Proxy_Port;

	static {
		Properties prop = new Properties();
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
			String dateString = format.format( new Date()   );
			prop.load(new FileInputStream("config.properties"));
			SF_OAUTH_URL = prop.getProperty("SF_OAUTH_URL");
			SF_QUERY_URL = prop.getProperty("SF_QUERY_URL");
			SF_DESCRIBE_URL = prop.getProperty("SF_DESCRIBE_URL");
			SALESFORCE_CLIENTID = prop.getProperty("SALESFORCE_CLIENTID");
			SALESFORCE_CLIENTSECRET = prop
					.getProperty("SALESFORCE_CLIENTSECRET");
			SALESFORCE_USERNAME = prop.getProperty("SALESFORCE_USERNAME");
			SALESFORCE_PASSWORD = prop.getProperty("SALESFORCE_PASSWORD");
			SALESFORCE_SECURITYTOKEN = prop
					.getProperty("SALESFORCE_SECURITYTOKEN");
			Acc_Query = prop.getProperty("Acc_Query");
			File_Path = prop.getProperty("File_path");
			File_Path += "_"+dateString+".csv";
			Is_Sandbox = prop.getProperty("Is_Sandbox");
			Proxy_Ip = prop.getProperty("Proxy_Ip");
			Proxy_Port = prop.getProperty("Proxy_Port");
		} catch (IOException ex) {
			System.out.println("bye");
			System.exit(0);
		}

	}

}
