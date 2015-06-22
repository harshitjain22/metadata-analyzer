import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Name:AccountDataExtrationUtility
 * Created Date: May 26, 2015 
 * Created By : Harshit Jain
 * Usage : This is java class is used to interact with Salesforce account.Perform following Task. 
 * 1.)Create Connection with Force.com using HTTP methods. 
 * 2.)Create CSV file of object name and their total record count
 * 
 */
public class AccountDataExtrationUtility {

	private static String OAUTH_TOKEN = null;
	public static boolean isSandBox = false;
	public static Map<String, JsonNode> queryableObjectMap = new HashMap<String, JsonNode>();
	static String SF_PREFIX_URL = ""; // Creating URL in Constructor

	/**
	 * Constructor : This Constructor check environment need to connect and
	 * create URL accordingly
	 * 
	 * @param isSandBox
	 *            : Must pass argument when performing operation on Objects
	 */
	public AccountDataExtrationUtility(boolean isSandBox) {
		AccountDataExtrationUtility.isSandBox = isSandBox;
		SF_PREFIX_URL = "https://" + (AccountDataExtrationUtility.isSandBox ? "test" : "login")
				+ ".salesforce.com";
	}

	/**
	 * Method Name : main() This method provide connection with Salesforce's
	 * Account and then call functions to get account records and create CSV file. 
	 * @param : args[] String type.
	 */
	public static void main(String args[]) {
		try {
			System.out.println("Going to connection.......");
			AccountDataExtrationUtility oauthObject = new AccountDataExtrationUtility(Boolean.parseBoolean(PropertyConfiguration.Is_Sandbox.trim()));
			OAUTH_TOKEN = oauthObject.doConnection();
			System.out.println(": OAUTH_TOKEN :::"+OAUTH_TOKEN );
			if (OAUTH_TOKEN == null) {
				System.err
						.println("Unable to generate OAUTH Token, Pls check logs above");
				System.exit(0);
			} else {
				JsonUtil jsonUtill = new JsonUtil(SF_PREFIX_URL);
				queryableObjectMap = jsonUtill.getObjectListJsonFormat();
				
				JsonNode jsonObjectQuery;
				JsonNode jsonFieldDescribeObject;
				JsonNode jsonFieldQuery;
				// Methods here for Fetching
				for(String objectName : queryableObjectMap.keySet())
				{	
					
						System.out.println("Processing "+objectName+" object.....");
						PropertyConfiguration.Acc_Query="Select+COUNT(Id)+From+"+objectName;
						jsonObjectQuery = jsonUtill.doQueryJsonFormat(PropertyConfiguration.Acc_Query,objectName);
						System.out.println("=======1"+jsonObjectQuery);
						
						jsonFieldDescribeObject = queryableObjectMap.get(objectName);
						for(int i=0; i<jsonFieldDescribeObject.get("fields").size(); i++) {
							if (jsonFieldDescribeObject.get("fields").get(i).get("custom").asBoolean() && !jsonFieldDescribeObject.get("customSetting").asBoolean()) {
								String query = "Select+COUNT(Id)+From+"+objectName+"+Where+"+jsonFieldDescribeObject.get("fields").get(i).get("name").asText()+"!=null";
								jsonFieldQuery = jsonUtill.doQueryJsonFormat(query, objectName);
								System.out.println("========2"+jsonFieldDescribeObject);
								if(jsonFieldQuery != null) {
									CsvUtill.WriteCsv(PropertyConfiguration.File_Path, jsonObjectQuery, jsonFieldQuery, jsonFieldDescribeObject.get("fields").get(i), objectName);
								}
							}
						}
						//CsvUtill.WriteCsv(PropertyConfiguration.File_Path,jsonObject,ObjectName);
				}
				System.out.println("********Finish***********");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method Name : doConnection() This method take all parameters for generate
	 * connection and call method createJsonObject() to return a JsonNode
	 * object. To Create connection HttpPost method is used.
	 * 
	 * @return : Access Token (String Type)
	 */
	public String doConnection() {
		String token = null;
		try {
			HttpClient restClient = new DefaultHttpClient();
			if( !PropertyConfiguration.Proxy_Ip.isEmpty() && !PropertyConfiguration.Proxy_Port.isEmpty() ) {
				// Use Proxy setting for Internet connection
				HttpHost proxy = new HttpHost(PropertyConfiguration.Proxy_Ip,Integer.parseInt(PropertyConfiguration.Proxy_Port.trim()));
				restClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			}
			HttpPost postMethod = new HttpPost(SF_PREFIX_URL
					+ PropertyConfiguration.SF_OAUTH_URL);
			List<BasicNameValuePair> formparams = new ArrayList<BasicNameValuePair>();
			// Set up the form parameters as this flow uses the POST request
			// The client_id/client_secret is obtained by configuring the
			// REMOTE_ACCESS setting in your org
			formparams.add(new BasicNameValuePair("client_id",
					PropertyConfiguration.SALESFORCE_CLIENTID));
			formparams.add(new BasicNameValuePair("client_secret",
					PropertyConfiguration.SALESFORCE_CLIENTSECRET));
			formparams.add(new BasicNameValuePair("username",
					PropertyConfiguration.SALESFORCE_USERNAME));
			formparams.add(new BasicNameValuePair("password",
					PropertyConfiguration.SALESFORCE_PASSWORD
							+ PropertyConfiguration.SALESFORCE_SECURITYTOKEN));
			// The grant_type = password is required for this flow
			formparams.add(new BasicNameValuePair("grant_type", "password"));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams,"UTF-8");
			postMethod.setEntity(entity);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = restClient.execute(postMethod, responseHandler);
			// * Do Parsing Here and fetching OAUTH TOKEN
			JsonNode actualObj = parseResponse(response);
			token = actualObj.get("access_token").asText();
			SF_PREFIX_URL = actualObj.get("instance_url").asText();

		} catch (Exception e) {
			e.printStackTrace();
			token = null;
		}
		return token;
	}

	/**
	 * Method Name : parseResponse() This method do parsing of response and
	 * create a JsonNode object basis of JsonParser.
	 * 
	 * @param response
	 *            : String Type which holds response of HttpRequest
	 * @return JsonNode object
	 * @throws Exception
	 */
	public static JsonNode parseResponse(String response) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonFactory factory = mapper.getJsonFactory();
		JsonParser jp = factory.createJsonParser(response);
		JsonNode actualObj = mapper.readTree(jp);
		return actualObj;
	}

	/**
	 * Method Name : setAuthorizationHeaders() This method set header
	 * information in HttpRequestBase object.
	 * 
	 * @param request
	 *            : Object of HttpRequestBase
	 */
	public static void setAuthorizationHeaders(HttpRequestBase request) {
		request.setHeader("Authorization", "OAuth " + OAUTH_TOKEN);
		request.setHeader("X-PrettyPrint", "1");
	}
}