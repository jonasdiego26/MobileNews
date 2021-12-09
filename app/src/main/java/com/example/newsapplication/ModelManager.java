package es.upm.hcid.pui.assignment;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import es.upm.hcid.pui.assignment.exceptions.AuthenticationError;
import es.upm.hcid.pui.assignment.exceptions.ServerCommunicationError;

public class ModelManager {
	
	private String idUser;
	private String authType;
	private String apikey;
	
	private boolean isAdministrator = false;
	
	private String serviceUrl ;
	
	private boolean requireSelfSigned = false;
	
	public static final String ATTR_LOGIN_USER = "username";
	public static final String ATTR_LOGIN_PASS = "password";
	public static final String ATTR_SERVICE_URL = "service_url";
	public static final String ATTR_REQUIRE_SELF_CERT = "require_self_signed_cert";
	public static final String ATTR_PROXY_HOST = "";
	public static final String ATTR_PROXY_PORT = "";
	public static final String ATTR_PROXY_USER = "";
	public static final String ATTR_PROXY_PASS = "";
	public static final String ATTR_APACHE_AUTH_USER = "";
	public static final String ATTR_APACHE_AUTH_PASS = "";
	
	/**
	 * 
	 * @param ini Initializes entity manager urls and users
	 * @throws AuthenticationError
	 */
	public ModelManager(Properties ini) throws AuthenticationError{
		if (!ini.containsKey(ATTR_SERVICE_URL)){
			throw new IllegalArgumentException("Required attribute '"+ ATTR_SERVICE_URL+"' not found!");
		}
		
		// disable auth from self signed certificates
		requireSelfSigned =  (ini.containsKey(ATTR_REQUIRE_SELF_CERT) && ((String)ini.get(ATTR_REQUIRE_SELF_CERT)).equalsIgnoreCase("TRUE"));
		
		// add proxy http/https to the system
		if (ini.contains(ATTR_PROXY_HOST) && ini.contains(ATTR_PROXY_PORT)){
			String proxyHost = (String)ini.get(ATTR_PROXY_HOST);
			String proxyPort = (String)ini.get(ATTR_PROXY_PORT);
			
			System.setProperty("http.proxyHost", proxyHost);
			System.setProperty("http.proxyPort", proxyPort);
			System.setProperty("https.proxyHost", proxyHost);
			System.setProperty("https.proxyPort", proxyPort);
		}
		
		if (ini.contains(ATTR_PROXY_USER) && ini.contains(ATTR_PROXY_PASS))	{
			final String proxyUser = (String)ini.get(ATTR_PROXY_USER);
			final String proxyPassword = (String)ini.get(ATTR_PROXY_PASS);
		
			System.setProperty("http.proxyUser", proxyUser);
			System.setProperty("http.proxyPassword", proxyPassword);
			System.setProperty("https.proxyUser", proxyUser);
			System.setProperty("https.proxyPassword", proxyPassword);

			Authenticator.setDefault(
			  new Authenticator() {
			    public PasswordAuthentication getPasswordAuthentication() {
			      return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
			    }
			  }
			);
		}
		
		serviceUrl = ini.getProperty(ATTR_SERVICE_URL);
		
		if (ini.containsKey(ATTR_LOGIN_USER) && ini.containsKey(ATTR_LOGIN_PASS)){
			
			login(ini.getProperty(ATTR_LOGIN_USER), ini.getProperty(ATTR_LOGIN_PASS));
		}
	}
	
	/**
	 * Login onto remote service
	 * @param username
	 * @param password
	 * @throws AuthenticationError
	 */
	@SuppressWarnings("unchecked")
	public void login(String username, String password) throws AuthenticationError{
		String res = "";
		try{
			String parameters =  "";
			String request = serviceUrl + "login";

			URL url = new URL(request); 
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();      
			if(requireSelfSigned)
				TrustModifier.relaxHostChecking(connection); 
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(false); 
			connection.setRequestMethod("POST"); 
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("charset", "utf-8");
			//connection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
			connection.setUseCaches (false);

			JSONObject jsonParam = new JSONObject();
			jsonParam.put("username", username);
			jsonParam.put("passwd", password);

			writeJSONParams(connection, jsonParam);

			int HttpResult =connection.getResponseCode();  
			if(HttpResult ==HttpURLConnection.HTTP_OK){  
				res = parseHttpStreamResult(connection); 
				
				JSONObject userJsonObject = readRestResultFromSingle(res);
				idUser = userJsonObject.get("user").toString();
				authType = userJsonObject.get("Authorization").toString();
				apikey = userJsonObject.get("apikey").toString();
				isAdministrator = userJsonObject.containsKey("administrator");

			}else{  
				Logger.log(Logger.ERROR,connection.getResponseMessage()); 

				throw new AuthenticationError(connection.getResponseMessage());
			}  
		} catch (MalformedURLException e) {  
			//e.printStackTrace();  
			throw new AuthenticationError(e.getMessage());
		}  
		catch (IOException e) {  
			//e.printStackTrace();  
			throw new AuthenticationError(e.getMessage());
		} 
		catch (Exception e) {  
			//e.printStackTrace();  
			throw new AuthenticationError(e.getMessage());
		} 
		
	}
	
	private String parseHttpStreamResult(HttpURLConnection connection) throws UnsupportedEncodingException, IOException {
		String res = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(  
				connection.getInputStream(),"utf-8"));  
		String line = null;  
		while ((line = br.readLine()) != null) { 
			res += line + "\n";
			  
		}  
		br.close(); 
		return res;
	}
	
	@SuppressWarnings("unchecked")
	private int readRestResultFromInsert(String res) throws ParseException, ServerCommunicationError {
		Object o = JSONValue.parseWithException(res);
		if (o instanceof JSONObject){
			JSONObject jsonResult = (JSONObject) JSONValue.parseWithException(res);
			Set<String> keys = jsonResult.keySet();
			if (keys.contains("id"))
				return Integer.parseInt((String) jsonResult.get("id"));
			else{
				throw new ServerCommunicationError("Error: No id in json returned");
			}	
		}else{
			throw new ServerCommunicationError("Error: No json returned");
		}
	}

	@SuppressWarnings("unchecked")
	private JSONObject readRestResultFromGetObject(String res) throws ParseException, ServerCommunicationError {
		Object o = JSONValue.parseWithException(res);
		if (o instanceof JSONObject){
			JSONObject jsonResult = (JSONObject) JSONValue.parseWithException(res);
			return jsonResult;	
		}else{
			throw new ServerCommunicationError("Error: No json returned");
		}
	}

	private List<JSONObject> readRestResultFromList(String res) throws AuthenticationError{
		List<JSONObject> result = new ArrayList<JSONObject>() ;
		try {
			Object o = JSONValue.parseWithException(res);
			if (o instanceof JSONObject){
				JSONObject jsonResult = (JSONObject) JSONValue.parseWithException(res);
				@SuppressWarnings("unchecked")
				Set<Object> keys = jsonResult.keySet();
				for (Object keyRow : keys) {
					JSONObject jsonObj = (JSONObject)jsonResult.get(keyRow);
					result.add(jsonObj);
				}
			} else if (o instanceof JSONArray){
				JSONArray jsonArray = (JSONArray) JSONValue.parseWithException(res);
				for (Object row : jsonArray) {
					JSONObject jsonObj = (JSONObject)row;
					result.add(jsonObj);
				}
			} else{
				throw new AuthenticationError("Result is not an Json Array nor Object");
			}
		} catch (ParseException e) {
			throw new AuthenticationError(e.getMessage());
		}
		return result;
	}

	private JSONObject readRestResultFromSingle(String res) throws ParseException {
		return (JSONObject) JSONValue.parseWithException(res);
	}

	private void writeJSONParams(HttpURLConnection connection, JSONObject json) throws IOException{
		// Send POST output.

		DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
		wr.writeBytes(json.toJSONString());//(URLEncoder.encode(json.toJSONString(),"UTF-8"));
		wr.flush();
		wr.close();
	}

	/****************************/
	
	/**
	 * 
	 * @return user id logged in
	 */
	public String getIdUser(){
		return idUser;
	}

	/**
	 * 
	 * @return true if user logged is an administrator
	 */
	public boolean isAdministrator(){
		return isAdministrator;
	}
	

	/**
	 * 
	 * @return auth token header for user logged in
	 */
	private  String getAuthTokenHeader(){
		String authHeader = authType + " apikey=" + apikey;
		return authHeader;
	}
	
	/**
	 * 
	 * @return the list of articles in remote service
	 * @throws ServerCommunicationError
	 */
	public List<Article> getArticles() throws ServerCommunicationError{
		return getArticles(-1,-1); 
	}
		
	/**
	 * 
	 * @return the list of articles in remote service with pagination
	 * @throws ServerCommunicationError
	 */
	public List<Article> getArticles(int buffer, int offset) throws ServerCommunicationError{
		String limits = "";
		if (buffer>0 && offset >=0){
			limits = "/"+buffer+"/"+offset;
		}
		
		List<Article> result = new ArrayList<Article>();
		try{
			String parameters =  "";
			String request = serviceUrl + "articles" + limits;

			URL url = new URL(request); 
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();      
			if(requireSelfSigned)
				TrustModifier.relaxHostChecking(connection); 
			//connection.setDoOutput(true);
			//connection.setDoInput(false);
			connection.setInstanceFollowRedirects(false); 
			connection.setRequestMethod("GET"); 
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Authorization", getAuthTokenHeader()); 
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
			connection.setUseCaches (false);

			int HttpResult =connection.getResponseCode();  
			if(HttpResult ==HttpURLConnection.HTTP_OK){  
				String res = parseHttpStreamResult(connection);
				List<JSONObject> objects = this.readRestResultFromList(res);
				for (JSONObject jsonObject : objects) {
					result.add(new Article(this,jsonObject));
				}
				Logger.log (Logger.INFO, objects.size() + " objects (Article) retrieved");
			}else{  
				throw new ServerCommunicationError(connection.getResponseMessage());
			}  
		} catch (Exception e) {  
			Logger.log (Logger.ERROR, "Listing articles :" + e.getClass() + " ( "+e.getMessage() + ")");
			throw new ServerCommunicationError(e.getClass() + " ( "+e.getMessage() + ")");
		}  

		return result;
	}

	/**
	 * 
	 * @return the article in remote service with id idArticle
	 * @throws ServerCommunicationError
	 */
	public Article getArticle(int idArticle) throws ServerCommunicationError{
		
		Article result = null;
		try{
			String parameters =  "";
			String request = serviceUrl + "article/" + idArticle;

			URL url = new URL(request); 
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();      
			if(requireSelfSigned)
				TrustModifier.relaxHostChecking(connection); 
			//connection.setDoOutput(true);
			//connection.setDoInput(false);
			connection.setInstanceFollowRedirects(false); 
			connection.setRequestMethod("GET"); 
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Authorization", getAuthTokenHeader()); 
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
			connection.setUseCaches (false);

			int HttpResult =connection.getResponseCode();  
			if(HttpResult ==HttpURLConnection.HTTP_OK){  
				String res = parseHttpStreamResult(connection);
				JSONObject object = this.readRestResultFromGetObject(res);
				result = new Article(this,object);
				Logger.log (Logger.INFO, " object (Article) retrieved");
			}else{  
				throw new ServerCommunicationError(connection.getResponseMessage());
			}  
		} catch (Exception e) {  
			Logger.log (Logger.ERROR, "Getting article :" + e.getClass() + " ( "+e.getMessage() + ")");
			throw new ServerCommunicationError(e.getClass() + " ( "+e.getMessage() + ")");
		}  

		return result;
	}

	
	private int saveArticle(Article a) throws ServerCommunicationError{
		try{
			String parameters =  "";
			String request = serviceUrl + "article";

			URL url = new URL(request); 
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();      
			if(requireSelfSigned)
				TrustModifier.relaxHostChecking(connection); 
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(false); 
			connection.setRequestMethod("POST"); 
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", getAuthTokenHeader()); 
			connection.setRequestProperty("charset", "utf-8");
			connection.setUseCaches (false);

			writeJSONParams(connection, a.toJSON());

			int HttpResult =connection.getResponseCode();
			if(HttpResult ==HttpURLConnection.HTTP_OK){  
				String res = parseHttpStreamResult(connection);
				// get id from status ok when saved
				int id = readRestResultFromInsert(res); 
				Logger.log(Logger.INFO, "Object inserted, returned id:" + id);
				return id;
			}else{  
				throw new ServerCommunicationError(connection.getResponseMessage());
			}  
		} catch (Exception e) {  
			Logger.log(Logger.ERROR,"Inserting article ["+a+"] : " + e.getClass() + " ( "+e.getMessage() + ")");
			throw new ServerCommunicationError(e.getClass() + " ( "+e.getMessage() + ")");
		}
	}

	private void deleteArticle(int idArticle) throws ServerCommunicationError{
		try{
			String parameters =  "";
			String request = serviceUrl + "article/" + idArticle;

			URL url = new URL(request); 
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();      
			if(requireSelfSigned)
				TrustModifier.relaxHostChecking(connection); 
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(false); 
			connection.setRequestMethod("DELETE"); 
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"); 
			connection.setRequestProperty("Authorization", getAuthTokenHeader()); 
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
			connection.setUseCaches (false);

			int HttpResult =connection.getResponseCode();  
			if(HttpResult ==HttpURLConnection.HTTP_OK  || HttpResult ==HttpURLConnection.HTTP_NO_CONTENT){  			
				Logger.log (Logger.INFO,"Article (id:"+idArticle+") deleted with status "+HttpResult+":"+parseHttpStreamResult(connection));
			}else{
				throw new ServerCommunicationError(connection.getResponseMessage());
			}   
		} catch (Exception e) {  
			Logger.log(Logger.ERROR, "Deleting article (id:"+idArticle+") : " + e.getClass() + " ( "+e.getMessage() + ")");
			throw new ServerCommunicationError(e.getClass() + " ( "+e.getMessage() + ")");
		}  
	}
	
	private int saveImage(Image i) throws ServerCommunicationError{
		try{
			String parameters =  "";
			String request = serviceUrl + "article/image";

			URL url = new URL(request); 
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();      
			if(requireSelfSigned)
				TrustModifier.relaxHostChecking(connection); 
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(false); 
			connection.setRequestMethod("POST"); 
			connection.setRequestProperty("Content-Type","application/json; charset=UTF-8"); 
			connection.setRequestProperty("Authorization", getAuthTokenHeader()); 
			connection.setRequestProperty("charset", "utf-8");
			connection.setUseCaches (false);

			writeJSONParams(connection, i.toJSON());

			int HttpResult =connection.getResponseCode();
			if(HttpResult ==HttpURLConnection.HTTP_OK){  
				String res = parseHttpStreamResult(connection);
				//Logger.log (Logger.INFO, res);
				// get id from status ok when saved
				int id = readRestResultFromInsert(res);
				Logger.log(Logger.INFO, "Object image saved with id:" + id);
				return id;
			}else{  
				throw new ServerCommunicationError(connection.getResponseMessage());
			}  
		} catch (Exception e) {  
			Logger.log(Logger.ERROR,"Saving image ["+i+"] : " + e.getClass() + " ( "+e.getMessage() + ")");
			throw new ServerCommunicationError(e.getClass() + " ( "+e.getMessage() + ")");
		}
	}

	private void deleteImage(int idArticle) throws ServerCommunicationError{
		try{
			String parameters =  "";
			String request = serviceUrl + "image/" + idArticle;

			URL url = new URL(request); 
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();   
			if(requireSelfSigned)
				TrustModifier.relaxHostChecking(connection);   
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(false); 
			connection.setRequestMethod("DELETE"); 
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"); 
			connection.setRequestProperty("Authorization", getAuthTokenHeader()); 
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
			connection.setUseCaches (false);

			int HttpResult =connection.getResponseCode();  
			if(HttpResult ==HttpURLConnection.HTTP_OK || HttpResult ==HttpURLConnection.HTTP_NO_CONTENT){  
				Logger.log (Logger.INFO,"Image of article (id:"+idArticle+") deleted with status "+HttpResult+":"+parseHttpStreamResult(connection));
			}else{  
				throw new ServerCommunicationError(connection.getResponseMessage());
			}  
		} catch (Exception e) {  
			Logger.log (Logger.ERROR, "Deleting image of article (id:"+idArticle+") : " + e.getClass() + " ( "+e.getMessage() + ")");
			throw new ServerCommunicationError(e.getClass() + " ( "+e.getMessage() + ")");
		}  
	}
		
	protected int save(ModelEntity o) throws ServerCommunicationError{
		int returnedId = -1;
		if (o instanceof Image){
			returnedId = saveImage((Image)o);
		}
		if (o instanceof Article){
			returnedId = saveArticle((Article)o);
		}
		return returnedId;
	}
	
	protected void delete(ModelEntity o) throws ServerCommunicationError{
		if (o instanceof Image){
			deleteImage(((Image)o).getId());
		}
		if (o instanceof Article){
			deleteArticle(((Article)o).getId());
		}
	}
}
