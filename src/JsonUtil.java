import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonNode;

/**
 * Name:JsonUtil 
 * Created Date: May 26, 2015
 * Created By : Harshit Jain
 * Description: This class contain method related to JSON,which fetch records and provide response in JSON format. 
 * 
 */
public class JsonUtil {

	private final String SF_PREFIX_URL;
	private final Set<String> OBJECT_SUFFIX = new HashSet<String>(Arrays.asList(new String[] {"History", "kav", "Feed", "Role", "Partner", "Share", "Member", "Tag", "Apex", "Item"}));

	/**
	 * Constructor : This Constructor initialize URL Which is need to connect
	 * environment.
	 * 
	 * @param SF_PREFIX_URL
	 *            : Must pass Salesforce URL when performing operation on
	 *            Objects
	 * 
	 */
	public JsonUtil(String SF_PREFIX_URL) {
		this.SF_PREFIX_URL = SF_PREFIX_URL;

	}

	/**
	 * Method Name : doQueryJsonFormat() This method execute the query which is
	 * passed to Service URI and provide the response result as JSON format. To
	 * execute Query HttpGet method is used.
	 * 
	 * @param fQuery
	 *            : String Type which you want to access.
	 * @return JsonNode object
	 * @throws Exception
	 */
	public JsonNode doQueryJsonFormat(String fQuery,String ObjectName) throws Exception {
		JsonNode jsonObject = null;
		HttpClient restClient = new DefaultHttpClient();
		if( !PropertyConfiguration.Proxy_Ip.isEmpty() && !PropertyConfiguration.Proxy_Port.isEmpty() ) {
			// Use Proxy setting for Internet connection
			HttpHost proxy = new HttpHost(PropertyConfiguration.Proxy_Ip,Integer.parseInt(PropertyConfiguration.Proxy_Port.trim()));
			restClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
		HttpGet getMethod = new HttpGet(SF_PREFIX_URL
				+ PropertyConfiguration.SF_QUERY_URL + "?q=" + fQuery);
		AccountDataExtrationUtility.setAuthorizationHeaders(getMethod);
		try {
			String response = restClient.execute(getMethod,
					new BasicResponseHandler());
			jsonObject = AccountDataExtrationUtility.parseResponse(response);
			//CsvUtill.WriteCsv(PropertyConfiguration.File_Path,jsonObject,ObjectName);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return jsonObject;
	}
	
	/**
	 * Method Name : doRestCallJsonFormat() This method execute the rest call which is
	 * passed to Service URI and provide the response result as JSON format. To
	 * execute Query HttpGet method is used.
	 * 
	 * @param query: Rest call URL
	 * @return JsonNode object
	 * @throws Exception
	 */
	public JsonNode doRestCallJsonFormat(String query) throws Exception {
		JsonNode jsonObject = null;
		HttpClient restClient = new DefaultHttpClient();
		if( !PropertyConfiguration.Proxy_Ip.isEmpty() && !PropertyConfiguration.Proxy_Port.isEmpty() ) {
			// Use Proxy setting for Internet connection
			HttpHost proxy = new HttpHost(PropertyConfiguration.Proxy_Ip,Integer.parseInt(PropertyConfiguration.Proxy_Port.trim()));
			restClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
		HttpGet getMethod = new HttpGet(SF_PREFIX_URL
				+ query);
		AccountDataExtrationUtility.setAuthorizationHeaders(getMethod);
		try {
			String response = restClient.execute(getMethod,
					new BasicResponseHandler());
			jsonObject = AccountDataExtrationUtility.parseResponse(response);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return jsonObject;
	}
	
	/**
	 * Method Name : getObjectDetailJsonFormat() This method sent an API call to SFDC which is
	 * passed to Service URI and provide the list of objects response result as JSON format. To
	 * execute HttpGet method is used.
	 * 
	 * 
	 * @return JsonNode object
	 * @throws Exception
	 */
	public Map<String, JsonNode> getObjectListJsonFormat() throws Exception {
		Map<String, JsonNode> queryableObjectMap = new HashMap<String, JsonNode>(); //List of all objects
		System.out.println("=======Connection established=========");
		HttpClient restClient = new DefaultHttpClient();
		if( !PropertyConfiguration.Proxy_Ip.isEmpty() && !PropertyConfiguration.Proxy_Port.isEmpty() ) {
			// Use Proxy setting for Internet connection
			HttpHost proxy = new HttpHost(PropertyConfiguration.Proxy_Ip,Integer.parseInt(PropertyConfiguration.Proxy_Port.trim()));
			restClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
		HttpGet getMethod = new HttpGet(SF_PREFIX_URL
				+ PropertyConfiguration.SF_DESCRIBE_URL);
		AccountDataExtrationUtility.setAuthorizationHeaders(getMethod);
		String response = restClient.execute(getMethod,
				new BasicResponseHandler());

		JsonNode jsonObject = AccountDataExtrationUtility.parseResponse(response);
		//jsonObject.get("sobjects").size()
		for(int i=0;i<jsonObject.get("sobjects").size();i++) {
			if(jsonObject.get("sobjects").get(i).get("queryable").asBoolean() && !jsonObject.get("sobjects").get(i).get("custom").asBoolean() && !OBJECT_SUFFIX.contains(jsonObject.get("sobjects").get(i).get("name").asText())) {
				JsonNode jsonDescribeObject = doRestCallJsonFormat("/services/data/v34.0/sobjects/"+jsonObject.get("sobjects").get(i).get("name").asText()+"/describe/");
				for(int j=0;j<jsonDescribeObject.get("fields").size();j++) {
					if(!queryableObjectMap.containsKey(jsonObject.get("sobjects").get(i).get("name").asText())) {
						queryableObjectMap.put(jsonObject.get("sobjects").get(i).get("name").asText(), jsonDescribeObject);
					}
				}
			}
		}
		return queryableObjectMap;
	}
}
