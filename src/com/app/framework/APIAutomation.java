package com.app.framework;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jetty.util.log.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class APIAutomation {
	
	String url;
	HashMap<String, String> Environment;
	static Logger log = LoggerFactory.getLogger(APIAutomation.class);
	
	/**
	 * Constructor 
	 * 
	 * @param url : application endpoint URL 
	 * @param env
	 */
	public APIAutomation(String url, HashMap<String, String> env){
		this.url = url;
		this.Environment = env;
	}
	
	/**
	 * Parse JSON response(InputStream) to JSONObject
	 * 
	 * @param jsonStream
	 * @return JSONObject
	 * @throws JSONException 
	 * @throws IOException 
	 */
	public JSONObject convertToJSON(Reader jsonStream) throws JSONException, IOException{
		String jsonText = readAll(jsonStream);
  	    JSONObject json = new JSONObject(jsonText);  	    
		return json;
	}
	
	/**
	 * Add headers to HTTP(S) request
	 * 
	 * @param urlCon
	 * @param key
	 * @param value
	 * @return
	 */
	public URLConnection addHeaders(URLConnection urlCon, String[] key, String[] value){
		for(int i = 0 ; i < key.length; i++){
			urlCon.addRequestProperty(key[i].trim(), value[i].trim());
		}
		
		return urlCon;
	}
	
	/**
	 * Set headers to HTTP(S) request
	 * 
	 * @param urlCon
	 * @param key
	 * @param value
	 * @return
	 */
	public URLConnection setHeaders(URLConnection urlCon, String[] key, String[] value){
		for(int i = 0 ; i < key.length; i++){
			urlCon.setRequestProperty(key[i].trim(), value[i].trim());
		}
		
		return urlCon;
	}
	
	/**
	 * Get response from endpoint url
	 * 
	 * @param format : XML or JSON
	 * @param key
	 * @param value
	 * @return InputStream
	 * @throws Exception 
	 */
	public InputStream get(String url_path, String format, String[] key, String[] value) throws Exception{
		InputStream is = null;
        final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
            }
            @Override
            public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        } };
        
        final SSLContext sslContext = SSLContext.getInstance( "SSL" );
        sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();            
        
        URLConnection urlCon = new URL(url + url_path).openConnection();
        urlCon.setRequestProperty("Method", "GET");
        if(format.trim().equalsIgnoreCase("XML"))
        	urlCon.setRequestProperty("Accept", "text/xml; charset=ISO-8859-1");
        else
        	urlCon.setRequestProperty("Accept", "application/json");
        
        if(key != null && value != null){
        	urlCon = setHeaders(urlCon, key, value);
        }
        
        int responseCode = 0;
        
        if(url.trim().toLowerCase().startsWith("https://")){
        	( (HttpsURLConnection) urlCon ).setSSLSocketFactory( sslSocketFactory );
        	responseCode = ((HttpsURLConnection)urlCon).getResponseCode();
        }
        else{
        	responseCode = ((HttpURLConnection)urlCon).getResponseCode();
        }
        
        log.info("Server response code : " + responseCode);
        
        Map<String, List<String>> headers = urlCon.getHeaderFields();
        Set<String> keys = headers.keySet();
        Iterator<String> iter = keys.iterator();
        String rawResponse = "", rawRequest = "";
        while(iter.hasNext()){
        	String keyName = iter.next();
        	if(keyName == null || keyName.trim().equalsIgnoreCase("null"))
        		continue;
        	rawRequest += keyName + " : ";
        	List<String> values = headers.get(keyName);
        	int i = 0;
        	rawRequest += values.get(i);
        	for(i = 1 ; i < values.size(); i++){
        		rawRequest += ", " + values.get(i);
        	}
        	rawRequest += "\n";
        }
        
        rawRequest += "Url : " + url + url_path + "\n";
        rawRequest += "Request method : GET\n\n";
        
        if(responseCode == 200){
        	is = urlCon.getInputStream();    
            
            InputStream[] input = getClonedStream(is, 2);
            String response = readAll(new BufferedReader(new InputStreamReader(input[0])));
            
            rawResponse += response.startsWith("{") ? new JSONObject(response).toString(1) : response.startsWith("[") ? new JSONArray(response).toString(1) : response;
            Environment.put("RAW_RESPONSE", rawResponse.replaceAll("(\r\n|\n)", "<br />"));
            Environment.put("RAW_REQUEST", rawRequest.replaceAll("(\r\n|\n)", "<br />"));
            is = input[1];
        }
        else{
        	Environment.put("RAW_REQUEST", rawRequest.replaceAll("(\r\n|\n)", "<br />"));
        	Environment.put("RAW_RESPONSE", "HTTP server code : " + responseCode);
        	throw new Exception("Server response code : " + responseCode);
        }
		return is;
	}
	
	/**
	 * Post request to endpoint url and retreive response
	 * 
	 * @param format : XML or JSON
	 * @param payloadFileName
	 * @param payloadPath : can be null or empty
	 * @param key
	 * @param value
	 * @return InputStream
	 * @throws Exception 
	 */
	@SuppressWarnings("deprecation")
	public InputStream post(String url_path, String format, String payloadFileName, String payloadPath, String[] key, String[] value) throws Exception{
		InputStream is = null;
        final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
            }
            @Override
            public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        } };
        
        final SSLContext sslContext = SSLContext.getInstance( "SSL" );
        sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();            
        
        URLConnection urlCon = new URL(url + url_path).openConnection();            
        urlCon.setRequestProperty("Method", "POST");
        if(format.trim().equalsIgnoreCase("XML"))
        	urlCon.setRequestProperty("Accept", "text/xml; charset=ISO-8859-1");
        else
        	urlCon.setRequestProperty("Accept", "application/json");
        
        if(key != null && value != null)
        	urlCon = setHeaders(urlCon, key, value);
        
        urlCon.setDoOutput(true);	            	            
        // Send request
        DataOutputStream wr = new DataOutputStream(urlCon.getOutputStream());
        if(payloadPath == null || payloadPath.trim().equalsIgnoreCase("")){
        	payloadPath = Environment.get("ROOTPATH") + Environment.get("restAPIFolder");
        }
        
        if(!payloadPath.trim().endsWith(OSValidator.delimiter))
        	payloadPath = payloadPath.trim() + OSValidator.delimiter;
        
        if(payloadFileName == null || payloadFileName.trim().equalsIgnoreCase("")){
        	Log.info("payloadFileName is null or empty");
        	return null;
        }
        
        BufferedReader br = new BufferedReader(new FileReader(payloadPath + payloadFileName));
        String sCurrentLine;
        String payloadText = "";
        while ((sCurrentLine = br.readLine()) != null) {
        	payloadText += sCurrentLine + "\n";
        	wr.writeBytes(sCurrentLine);
		}
        
        br.close();
        wr.flush();
        wr.close();
        
        int responseCode = 0;
        
        if(url.trim().toLowerCase().startsWith("https://")){
        	( (HttpsURLConnection) urlCon ).setSSLSocketFactory( sslSocketFactory );
        	responseCode = ((HttpsURLConnection)urlCon).getResponseCode();
        }
        else{
        	responseCode = ((HttpURLConnection)urlCon).getResponseCode();
        }
        
        log.info("Server response code : " + responseCode);
        
        Map<String, List<String>> headers = urlCon.getHeaderFields();
        Set<String> keys = headers.keySet();
        Iterator<String> iter = keys.iterator();
        String rawResponse = "", rawRequest = "";
        while(iter.hasNext()){
        	String keyName = iter.next();
        	if(keyName == null || keyName.trim().equalsIgnoreCase("null"))
        		continue;
        	rawRequest += keyName + " : ";
        	List<String> values = headers.get(keyName);
        	int i = 0;
        	rawRequest += values.get(i);
        	for(i = 1 ; i < values.size(); i++){
        		rawRequest += ", " + values.get(i);
        	}
        	rawRequest += "\n";
        }
        
        rawRequest += "Url : " + url + url_path + "\n";
        rawRequest += "Request method : POST\n\n";
        rawRequest += payloadText + "\n";
        
        if(responseCode == 200){
        	is = urlCon.getInputStream();
        	InputStream[] input = getClonedStream(is, 2);
            String response = readAll(new BufferedReader(new InputStreamReader(input[0])));
            
            rawResponse += response.startsWith("{") ? new JSONObject(response).toString(1) : response.startsWith("[") ? new JSONArray(response).toString(1) : response;
            Environment.put("RAW_RESPONSE", rawResponse.replaceAll("(\r\n|\n)", "<br />"));
            Environment.put("RAW_REQUEST", rawRequest.replaceAll("(\r\n|\n)", "<br />"));
            is = input[1];
        }
        else{
        	Environment.put("RAW_REQUEST", rawRequest.replaceAll("(\r\n|\n)", "<br />"));
        	Environment.put("RAW_RESPONSE", "HTTP server code : " + responseCode);
        	throw new Exception("Server response code : " + responseCode);
        }
		return is;
	}
	
	/**
	 * Clone input stream
	 * 
	 * @param input
	 * @param count
	 * @return
	 * @throws IOException
	 */
	public InputStream[] getClonedStream(InputStream input, int count) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = input.read(buffer)) > -1 ) {
		    baos.write(buffer, 0, len);
		}
		baos.flush();

		InputStream[] is = new InputStream[count];
		for(int i = 0 ; i < count; i++){
			is[i] = new ByteArrayInputStream(baos.toByteArray());
		}
		
		return is;
	}
	
	/**
	 * Read buffered reader stream and converts it to string
	 * 
	 * @param rd
	 * @return String
	 * @throws IOException
	 */
	private static String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    
	    return sb.toString();
	}
	
	/**
	 * Save input stream into a file of type xml or json based on the format
	 * 
	 * @return String : path of response file
	 * @param input
	 * @param fileName : can be null or empty
	 * @param path : can be null or empty
	 * @param format : XML or JSON
	 * @throws IOException
	 */
	public String saveResponse(InputStream input, String fileName, String path, String format) throws IOException{
		if(path == null || path.trim().equalsIgnoreCase("")){
	        if(!new File(Environment.get("CURRENTEXECUTIONFOLDER") + OSValidator.delimiter + "REST OUTPUT").exists()){
	        	new File(Environment.get("CURRENTEXECUTIONFOLDER") + OSValidator.delimiter + "REST OUTPUT").mkdirs();	                
	        }
	        
	        path = Environment.get("CURRENTEXECUTIONFOLDER") + OSValidator.delimiter + "REST OUTPUT" + OSValidator.delimiter;
		}
			
		if(fileName == null || fileName.trim().equalsIgnoreCase("")){
			java.util.Date today = new java.util.Date();
			Timestamp now = new java.sql.Timestamp(today.getTime());
			String tempNow[] = now.toString().split("\\.");
			final String sStartTime = tempNow[0].replaceAll(":", ".").replaceAll(" ", "T");
			fileName = path + format.trim().toUpperCase().replace(".", "") + "_" + sStartTime + "." + format.trim().toLowerCase().replace(".", "");
		}
		else{
			fileName = path + fileName + "." + format.trim().toLowerCase().replace(".", "");
		}
		
		File outputFile = new File(fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);            
        byte[] bytes = new byte[1024];
        int c = 0;
        while ( ( c = input.read(bytes) ) != -1 ) {
        	fileOutputStream.write(bytes, 0, c);
        }
        fileOutputStream.close();
        input.close();
        
        return fileName;
	}
	
	/**
	 * Get headers
	 * 
	 * @param con
	 * @param header
	 * @return
	 */
	 String[] getHeaders(URLConnection con, String header) {
	  List<String> values = new ArrayList<String>();
	  int idx = (con.getHeaderFieldKey(0) == null) ? 1 : 0;
	  while (true) {
	    String key = con.getHeaderFieldKey(idx);
	    if (key == null)
	      break;
	    if (header.equalsIgnoreCase(key))
	      values.add(con.getHeaderField(idx));
	    ++idx;
	  }
	  return values.toArray(new String[values.size()]);
	}
	
	 /**
	  * Get all headers
	  * 
	  * @param con
	  * @return
	  */
	String[] getAllHeaders(URLConnection con){
		List<String> values = new ArrayList<String>();
		int idx = (con.getHeaderFieldKey(0) == null) ? 1 : 0;
		 while (true) {
		    String key = con.getHeaderFieldKey(idx);
		    if (key == null)
		      break;
		    values.add(con.getHeaderField(idx));
		    ++idx;
		 }
		 
		 return values.toArray(new String[values.size()]);
	}
	
	/**
	 * Retreive tag value based on tag name and parent name
	 * 
	 * @param tagName
	 * @param index
	 * @param parent : Root will be considered in case of null or empty value
	 * @param parentIndex
	 * @param path : path of response file
	 * @return String : tag value
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 */
	public String getTagValue(String tagName, int index, String parent, int parentIndex, String path) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		File fXmlFile = new File(path);
		String tagValue = null;
		
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbFac.newDocumentBuilder();
		Document xmldoc = docBuilder.parse(fXmlFile);
		
		if(parent == null || parent.trim().equalsIgnoreCase(""))
			tagValue = xmldoc.getElementsByTagName(tagName).item(index).getTextContent();
		else{
			XPathFactory xPathfac = XPathFactory.newInstance();
		    XPath xpath = xPathfac.newXPath();
		    XPathExpression expr = xpath.compile("//" + parent);
		    NodeList nl = ((NodeList)expr.evaluate(xmldoc, XPathConstants.NODESET)).item(parentIndex).getChildNodes();
		    int count = 0;
		    for (int child = 0; child < nl.getLength(); child++) {
		    	if(nl.item(child).getNodeName().trim().equalsIgnoreCase(tagName)){
		    		if(count == index){
		    			tagValue = nl.item(child).getTextContent();
		    			break;
		    		}
		    		else{
		    			count++;
		    		}
		    	}
		    }
		}
		
		return tagValue;
	}
	
	/**
	 * Retreive attribute value based on attribute name, tag name and parent name
	 * 
	 * @param attributeName
	 * @param tagName
	 * @param index
	 * @param parent : Root will be considered in case of null or empty value
	 * @param parentIndex
	 * @param path : path of response file
	 * @return String : tag value
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 */
	public String getAttributeValue(String attributeName, String tagName, int index, String parent, int parentIndex, String path) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		File fXmlFile = new File(path);
		String attributeValue = null;
		
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbFac.newDocumentBuilder();
		Document xmldoc = docBuilder.parse(fXmlFile);
		NamedNodeMap nm = null;
		
		if(parent == null || parent.trim().equalsIgnoreCase(""))
			nm = xmldoc.getElementsByTagName(tagName).item(index).getAttributes();
		else{
			XPathFactory xPathfac = XPathFactory.newInstance();
		    XPath xpath = xPathfac.newXPath();
		    XPathExpression expr = xpath.compile("//" + parent);
		    NodeList nl = ((NodeList)expr.evaluate(xmldoc, XPathConstants.NODESET)).item(parentIndex).getChildNodes();
		    int count = 0;
		    for (int child = 0; child < nl.getLength(); child++) {
		    	if(nl.item(child).getNodeName().trim().equalsIgnoreCase(tagName)){
		    		if(count == index){
		    			nm = nl.item(child).getAttributes();
		    			break;
		    		}
		    		else{
		    			count++;
		    		}
		    	}
		    }
		}
		
		if(nm != null){
			for(int i = 0 ; i < nm.getLength(); i++){
	  		  	if(nm.item(i).getNodeName().equalsIgnoreCase(attributeName)){
	  		  		attributeValue =  nm.item(i).getTextContent();
	  		  		break;
	  		  	}
			}
		}
		
		return attributeValue;
	}
	
	/**
	 * Returns value based on Key
	 * 
	 * @return String : Key value
	 * @param keyName
	 * @param parent
	 * @param jsonObject
	 * @throws JSONException 
	 */
	@SuppressWarnings("unchecked")
	public String getKeyValue(String keyName, String parent, JSONObject jsonObject) throws JSONException{
		String keyValue = null;
		
		if(parent == null || parent.trim().equalsIgnoreCase("")){
			if(jsonObject.has(keyName))
				keyValue = jsonObject.getString(keyName);
			else{
				Iterator<Object> iterator = jsonObject.keys();
				while (iterator.hasNext()) {
					Object obj = iterator.next();
					if (jsonObject.get((String) obj) instanceof JSONObject) {
						keyValue = getKeyValue(keyName, parent, (JSONObject)jsonObject.get((String) obj));
					}
				}
			}
				
		}
		else{
			if(jsonObject.has(parent)){
				jsonObject = jsonObject.getJSONObject(parent);
				if(jsonObject.has(keyName))
					keyValue = jsonObject.getString(keyName);
			}
			else{
				Iterator<Object> iterator = jsonObject.keys();
				while (iterator.hasNext()) {
					Object obj = iterator.next();
					if (jsonObject.get((String) obj) instanceof JSONObject) {
						keyValue = getKeyValue(keyName, parent, (JSONObject)jsonObject.get((String) obj));
					}
				}
			}
		}
	    
		return keyValue;
	}
	
	/**
	 * Returns JSONObject based on Key
	 * 
	 * @return JSONObject : Key value
	 * @param keyName
	 * @param parent
	 * @param jsonObject
	 * @throws JSONException 
	 */
	@SuppressWarnings("unchecked")
	public JSONObject getJSONObject(String keyName, String parent, JSONObject jsonObject) throws JSONException{
		JSONObject keyValue = null;
		
		if(parent == null || parent.trim().equalsIgnoreCase("")){
			if(jsonObject.has(keyName))
				keyValue = jsonObject.getJSONObject(keyName);
			else{
				Iterator<Object> iterator = jsonObject.keys();
				while (iterator.hasNext()) {
					Object obj = iterator.next();
					if (jsonObject.get((String) obj) instanceof JSONObject) {
						keyValue = getJSONObject(keyName, parent, (JSONObject)jsonObject.get((String) obj));
					}
				}
			}
				
		}
		else{
			if(jsonObject.has(parent)){
				jsonObject = jsonObject.getJSONObject(parent);
				if(jsonObject.has(keyName))
					keyValue = jsonObject.getJSONObject(keyName);
			}
			else{
				Iterator<Object> iterator = jsonObject.keys();
				while (iterator.hasNext()) {
					Object obj = iterator.next();
					if (jsonObject.get((String) obj) instanceof JSONObject) {
						keyValue = getJSONObject(keyName, parent, (JSONObject)jsonObject.get((String) obj));
					}
				}
			}
		}
	    
		return keyValue;
	}
	
	/**
	 * Returns Object based on Key
	 * 
	 * @return Object : Key value
	 * @param keyName
	 * @param parent
	 * @param jsonObject
	 * @throws JSONException 
	 */
	@SuppressWarnings("unchecked")
	public Object getObject(String keyName, String parent, JSONObject jsonObject) throws JSONException{
		Object keyValue = null;
		
		if(parent == null || parent.trim().equalsIgnoreCase("")){
			if(jsonObject.has(keyName))
				keyValue = jsonObject.get(keyName);
			else{
				Iterator<Object> iterator = jsonObject.keys();
				while (iterator.hasNext()) {
					Object obj = iterator.next();
					if(jsonObject.get((String) obj) instanceof JSONObject)
						keyValue = getObject(keyName, parent, (JSONObject)jsonObject.get((String) obj));
				}
			}
				
		}
		else{
			if(jsonObject.has(parent)){
				jsonObject = jsonObject.getJSONObject(parent);
				if(jsonObject.has(keyName))
					keyValue = jsonObject.get(keyName);
			}
			else{
				Iterator<Object> iterator = jsonObject.keys();
				while (iterator.hasNext()) {
					Object obj = iterator.next();
					if (jsonObject.get((String) obj) instanceof JSONObject) {
						keyValue = getObject(keyName, parent, (JSONObject)jsonObject.get((String) obj));
					}
				}
			}
		}
	    
		return keyValue;
	}
	
	/**
	 * Returns JSONArray based on Key
	 * 
	 * @return JSONArray : Key value
	 * @param keyName
	 * @param parent
	 * @param jsonObject
	 * @throws JSONException 
	 */
	@SuppressWarnings("unchecked")
	public JSONArray getJSONArray(String keyName, String parent, JSONObject jsonObject) throws JSONException{
		JSONArray keyValue = null;
		
		if(parent == null || parent.trim().equalsIgnoreCase("")){
			if(jsonObject.has(keyName))
				keyValue = jsonObject.getJSONArray(keyName);
			else{
				Iterator<Object> iterator = jsonObject.keys();
				while (iterator.hasNext()) {
					Object obj = iterator.next();
					if (jsonObject.get((String) obj) instanceof JSONObject) {
						keyValue = getJSONArray(keyName, parent, (JSONObject)jsonObject.get((String) obj));
					}
				}
			}
				
		}
		else{
			if(jsonObject.has(parent)){
				jsonObject = jsonObject.getJSONObject(parent);
				if(jsonObject.has(keyName))
					keyValue = jsonObject.getJSONArray(keyName);
			}
			else{
				Iterator<Object> iterator = jsonObject.keys();
				while (iterator.hasNext()) {
					Object obj = iterator.next();
					if (jsonObject.get((String) obj) instanceof JSONObject) {
						keyValue = getJSONArray(keyName, parent, (JSONObject)jsonObject.get((String) obj));
					}
				}
			}
		}
	    
		return keyValue;
	}
	
	/**
	 * Convert JSONArray into list of strings
	 * 
	 * @param array : JSONArray
	 * @return List<String>
	 * @throws JSONException
	 */
	public List<String> convertJSONArrayIntoList(JSONArray array) throws JSONException{
		List<String> lst = new ArrayList<String>();
	    for (int k = 0; k < array.length(); k++) {
	        if (array.get(k) instanceof JSONObject) {
	        	//Do Nothing
	        } else {
	            lst.add(array.getString(k));
	        }
	    }
		return lst;
	}
	
	/**
	 * update XML
	 * 
	 * @param payloadFileName
	 * @param payloadPath
	 * @param nodeName
	 * @param index
	 * @param nodeValue
	 */
	@SuppressWarnings("deprecation")
	public void updateXML(String payloadFileName, String payloadPath, String[] nodeName, int[] index, String[] nodeValue) {
		try
	    {
			if(payloadPath == null || payloadPath.trim().equalsIgnoreCase("")){
            	payloadPath = Environment.get("ROOTPATH") + Environment.get("restAPIFolder");
            }
            
            if(!payloadPath.trim().endsWith(OSValidator.delimiter))
            	payloadPath = payloadPath.trim() + OSValidator.delimiter;
            
            if(payloadFileName == null || payloadFileName.trim().equalsIgnoreCase("")){
            	Log.info("payloadFileName is null or empty");
            	return;
            }
			
	      File fXmlFile = new File(payloadPath + payloadFileName);
	      
	      DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
	      DocumentBuilder docBuilder = dbFac.newDocumentBuilder();
	      Document xmldoc = docBuilder.parse(fXmlFile);
	      
	      for(int i = 0; i< nodeName.length; i++){
	    	  xmldoc.getElementsByTagName(nodeName[i]).item(index[i]).setTextContent(nodeValue[i]);
	      }
	      
	      TransformerFactory transformerFactory = TransformerFactory.newInstance();
	      Transformer transformer = transformerFactory.newTransformer();
	      DOMSource source = new DOMSource(xmldoc);
	      StreamResult result = new StreamResult(new File(payloadPath + payloadFileName));
	      transformer.transform(source, result);
	    }
	    catch (Exception excep){
	    	log.info("Threw a Exception in APIAutomation::updateXML, full stack trace follows:", excep);
	    }
	}
	
	/**
	 * update XML based on parent tag
	 * 
	 * @param payloadFileName
	 * @param payloadPath
	 * @param nodeName
	 * @param index
	 * @param nodeValue
	 * @param parent
	 * @param parentIndex
	 */
	@SuppressWarnings("deprecation")
	public void updateXML(String payloadFileName, String payloadPath, String[] nodeName, int[] index, String[] nodeValue, String parent, int parentIndex) {
		try
	    {
			if(payloadPath == null || payloadPath.trim().equalsIgnoreCase("")){
            	payloadPath = Environment.get("ROOTPATH") + Environment.get("restAPIFolder");
            }
            
            if(!payloadPath.trim().endsWith(OSValidator.delimiter))
            	payloadPath = payloadPath.trim() + OSValidator.delimiter;
            
            if(payloadFileName == null || payloadFileName.trim().equalsIgnoreCase("")){
            	Log.info("payloadFileName is null or empty");
            	return;
            }
			
	      File fXmlFile = new File(payloadPath + payloadFileName);
	      
	      DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
	      DocumentBuilder docBuilder = dbFac.newDocumentBuilder();
	      Document xmldoc = docBuilder.parse(fXmlFile);
	      
	      XPathFactory xPathfac = XPathFactory.newInstance();
	      XPath xpath = xPathfac.newXPath();

	      XPathExpression expr = xpath.compile("//" + parent);
	      NodeList nl = ((NodeList)expr.evaluate(xmldoc, XPathConstants.NODESET)).item(parentIndex).getChildNodes();
	      for (int child = 0; child < nl.getLength(); child++) {
	    	  for(int i = 0; i< nodeName.length; i++){
	    		  if(nl.item(child).getNodeName().equalsIgnoreCase(nodeName[i])){
	    			  nl.item(child).setTextContent(nodeValue[i]);  
	    		  }
	    	  }
	      }
	      
	      TransformerFactory transformerFactory = TransformerFactory.newInstance();
	      Transformer transformer = transformerFactory.newTransformer();
	      DOMSource source = new DOMSource(xmldoc);
	      StreamResult result = new StreamResult(new File(payloadPath + payloadFileName));
	      transformer.transform(source, result);
	    }
	    catch (Exception excep){
	    	log.info("Threw a Exception in APIAutomation::updateXML, full stack trace follows:", excep);
	    }
	}
	
	/**
	 * Update JSON
	 * 
	 * @param payloadFileName
	 * @param payloadPath
	 * @param keyName
	 * @param keyValue
	 * @throws IOException 
	 */
	@SuppressWarnings({ "deprecation" })
	public void updateJSON(String payloadFileName, String payloadPath, String[] keyName, String[] keyValue) throws IOException{
		BufferedWriter out = null;
		try
	    {
			if(payloadPath == null || payloadPath.trim().equalsIgnoreCase("")){
            	payloadPath = Environment.get("ROOTPATH") + Environment.get("restAPIFolder");
            }
            
            if(!payloadPath.trim().endsWith(OSValidator.delimiter))
            	payloadPath = payloadPath.trim() + OSValidator.delimiter;
            
            if(payloadFileName == null || payloadFileName.trim().equalsIgnoreCase("")){
            	Log.info("payloadFileName is null or empty");
            	return;
            }
            
            JSONObject obj = convertToJSON(new FileReader(payloadPath + payloadFileName));
        	for(int i = 0; i< keyName.length; i++){
        		obj = update(obj, keyName[i], keyValue[i]);
        	}
        	
        	out = new BufferedWriter(new FileWriter(payloadPath + payloadFileName));
            out.write(obj.toString(1));
	    }
		catch (Exception excep){
			log.info("Threw a Exception in APIAutomation::updateJSON, full stack trace follows:", excep);
	    }
		finally{
			if(out != null)
				out.close();
		}
	}
	
	/**
	 * Update JSON Arrays
	 * 
	 * @param payloadFileName
	 * @param payloadPath
	 * @param keyName
	 * @param keyValue
	 * @throws IOException 
	 */
	@SuppressWarnings({ "deprecation" })
	public void updateJSONArrays(String payloadFileName, String payloadPath, String[] keyName, List<List<String>> keyValue) throws IOException{
		BufferedWriter out = null;
		try
	    {
			if(payloadPath == null || payloadPath.trim().equalsIgnoreCase("")){
            	payloadPath = Environment.get("ROOTPATH") + Environment.get("restAPIFolder");
            }
            
            if(!payloadPath.trim().endsWith(OSValidator.delimiter))
            	payloadPath = payloadPath.trim() + OSValidator.delimiter;
            
            if(payloadFileName == null || payloadFileName.trim().equalsIgnoreCase("")){
            	Log.info("payloadFileName is null or empty");
            	return;
            }
            
            JSONObject obj = convertToJSON(new FileReader(payloadPath + payloadFileName));
        	for(int i = 0; i< keyName.length; i++){
        		if(obj.has(keyName[i])){
        			obj.put(keyName[i], (List<String>)keyValue.get(i));
        		}
        	}
        	
        	out = new BufferedWriter(new FileWriter(payloadPath + payloadFileName));
            out.write(obj.toString(1));
	    }
		catch (Exception excep){
			log.info("Threw a Exception in APIAutomation::updateJSONArrays, full stack trace follows:", excep);
	    }
		finally{
			if(out != null)
				out.close();
		}
	}
	
	/**
	 * Convert JSON file into JSONObject
	 * 
	 * @param payloadFileName
	 * @param payloadPath
	 * @return JSONObject
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JSONException
	 */
	@SuppressWarnings("deprecation")
	public JSONObject convertToJSON(String payloadFileName, String payloadPath) throws FileNotFoundException, IOException, JSONException{
		
		if(payloadPath == null || payloadPath.trim().equalsIgnoreCase("")){
        	payloadPath = Environment.get("ROOTPATH") + Environment.get("restAPIFolder");
        }
        
        if(!payloadPath.trim().endsWith(OSValidator.delimiter))
        	payloadPath = payloadPath.trim() + OSValidator.delimiter;
        
        if(payloadFileName == null || payloadFileName.trim().equalsIgnoreCase("")){
        	Log.info("payloadFileName is null or empty");
        	return null;
        }
        
        String jsonText = readAll(new FileReader(payloadPath + payloadFileName));
  	    JSONObject json = new JSONObject(jsonText);  	    
		return json;
	}
	
	/**
	 * Update JSON recursively
	 * 
	 * @param obj
	 * @param keyMain
	 * @param newValue
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public JSONObject update(JSONObject obj, String keyMain, String newValue) throws Exception {
	    Iterator iterator = obj.keys();
	    String key = null;
	    while (iterator.hasNext()) {
	        key = (String) iterator.next();
	        // if object is just string we change value in key
	        if ((obj.optJSONArray(key)==null) && (obj.optJSONObject(key)==null)) {
	            if ((key.equals(keyMain))) {
	                // put new value
	                obj.put(key, newValue);
	                return obj;
	            }
	        }

	        // if it's jsonobject
	        if (obj.optJSONObject(key) != null) {
	        	update(obj.getJSONObject(key), keyMain, newValue);
	        }

	        // if it's jsonarray
	        if (obj.optJSONArray(key) != null) {
	            JSONArray jArray = obj.getJSONArray(key);
	            int flag = 0;
	            for (int i = 0; i < jArray.length(); i++) {
	            	if(jArray.get(i) instanceof JSONObject){
	            		flag = 1;
            			update(jArray.getJSONObject(i), keyMain, newValue);
	            	}
	            }
	            if(flag == 0){
		            if(newValue.trim().contains("&&")){
		            	if ((key.equals(keyMain))) {
			                // put new value
		            		List<String> newValues = new ArrayList<String>(Arrays.asList(newValue.trim().split("&&")));
		            		obj.put(key, newValues);
			                return obj;
			            }
		            }
	            }
	        }
	    }
	    return obj;
	}
	
	/**
	 * Add key to JSON
	 * 
	 * @param key
	 * @param value
	 * @param keys
	 * @param values
	 * @param parent
	 * @param payloadFileName
	 * @param payloadPath
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public JSONObject addKey(String key, String value, String[] keys, String[] values, String parent, String payloadFileName, String payloadPath) throws IOException{
		BufferedWriter out = null;
		JSONObject obj = null;
		try
	    {
			if(payloadPath == null || payloadPath.trim().equalsIgnoreCase("")){
            	payloadPath = Environment.get("ROOTPATH") + Environment.get("restAPIFolder");
            }
            
            if(!payloadPath.trim().endsWith(OSValidator.delimiter))
            	payloadPath = payloadPath.trim() + OSValidator.delimiter;
            
            if(payloadFileName == null || payloadFileName.trim().equalsIgnoreCase("")){
            	Log.info("payloadFileName is null or empty");
            	return obj;
            }
            
            obj = convertToJSON(new FileReader(payloadPath + payloadFileName));
            
            if(!value.trim().equalsIgnoreCase("")){
            	if(!parent.trim().equalsIgnoreCase("")){
            		JSONObject parentKey = getJSONObject(parent, "", obj);
            		if(key.trim().contains("||")){
            			String[] tempKeys = key.trim().split("\\|\\|");
            			String[] tempValues = value.trim().split("\\|\\|");
            			for(int i = 0; i < tempKeys.length; i++){
            				parentKey.put(tempKeys[i], tempValues[i]);
            			}
            		}
            		else{
            			parentKey.put(key, value);
            		}
            	}
            	else{
            		if(key.trim().contains("||")){
            			String[] tempKeys = key.trim().split("\\|\\|");
            			String[] tempValues = value.trim().split("\\|\\|");
            			for(int i = 0; i < tempKeys.length; i++){
            				obj.put(tempKeys[i], tempValues[i]);
            			}
            		}
            		else{
            			obj.put(key, value);
            		}
            	}
            }
            else{
            	JSONArray jsonArray = new JSONArray();
        		
        		for(int i = 0 ; i < keys.length; i++){
        			String[] arrayKeys = keys[i].split("&&");
        			String[] arrayValues = values[i].split("&&");
        			JSONObject newJSONObj = new JSONObject();
        			JSONArray newJSONArray = new JSONArray();
        			
        			for(int j = 0 ; j < arrayKeys.length; j++){
        				if(arrayValues[j].trim().contains(";")){
        					List<String> intArrayValues = new ArrayList<String>(Arrays.asList(arrayValues[j].trim().split(";")));
        					newJSONArray.put(intArrayValues);
        				}
        				else{
        					newJSONObj.put(arrayKeys[j], arrayValues[j]);
        				}
        			}
        			if(newJSONObj.length() > 0)
        				jsonArray.put(newJSONObj);
        			else
        				jsonArray.put(newJSONArray);
        		}
            	if(!parent.trim().equalsIgnoreCase("")){
            		JSONObject parentKey = getJSONObject(parent, "", obj);
            		parentKey.put(key, jsonArray);
            	}
            	else{
            		obj.put(key, jsonArray);
            	}
            }
        	
        	out = new BufferedWriter(new FileWriter(payloadPath + payloadFileName));
            out.write(obj.toString(1));
	    }
		catch (Exception excep){
			log.info("Threw a Exception in APIAutomation::addKey, full stack trace follows:", excep);
	    }
		finally{
			if(out != null)
				out.close();
		}
		
		return obj;
	}
	
	/**
	 * Remove key from JSON
	 * 
	 * @param key
	 * @param payloadFileName
	 * @param payloadPath
	 * @return
	 * @throws IOException 
	 */
	@SuppressWarnings("deprecation")
	public JSONObject removeKey(String key, String parent, String payloadFileName, String payloadPath) throws IOException{
		BufferedWriter out = null;
		JSONObject obj = null;
		try
	    {
			if(payloadPath == null || payloadPath.trim().equalsIgnoreCase("")){
            	payloadPath = Environment.get("ROOTPATH") + Environment.get("restAPIFolder");
            }
            
            if(!payloadPath.trim().endsWith(OSValidator.delimiter))
            	payloadPath = payloadPath.trim() + OSValidator.delimiter;
            
            if(payloadFileName == null || payloadFileName.trim().equalsIgnoreCase("")){
            	Log.info("payloadFileName is null or empty");
            	return obj;
            }
            
            obj = convertToJSON(new FileReader(payloadPath + payloadFileName));
            
            if(!parent.trim().equalsIgnoreCase("")){
        		JSONObject parentKey = getJSONObject(parent, "", obj);
        		if(key.trim().contains("||")){
	            	String[] tempKeys = key.trim().split("\\|\\|");
	            	for(int i = 0; i < tempKeys.length; i++){
	            		parentKey.remove(tempKeys[i]);
	            	}
	            }
	            else{
	            	parentKey.remove(key);
	            }
            }
            else{
	            if(key.trim().contains("||")){
	            	String[] tempKeys = key.trim().split("\\|\\|");
	            	for(int i = 0; i < tempKeys.length; i++){
	            		obj.remove(tempKeys[i]);
	            	}
	            }
	            else{
	            	obj.remove(key);
	            }
            }
        	
        	out = new BufferedWriter(new FileWriter(payloadPath + payloadFileName));
            out.write(obj.toString(1));
	    }
		catch (Exception excep){
			log.info("Threw a Exception in APIAutomation::removeKey, full stack trace follows:", excep);
	    }
		finally{
			if(out != null)
				out.close();
		}
		
		return obj;
	}
	
	/**
	 * Get key values (String)
	 * 
	 * @param obj
	 * @param keyMain
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public String get(JSONObject obj, String keyMain) throws Exception {
		String value = null;
	    Iterator iterator = obj.keys();
	    String key = null;
	    while (iterator.hasNext()) {
	        key = (String) iterator.next();
	        if ((obj.optJSONArray(key)==null) && (obj.optJSONObject(key)==null) && (key.equals(keyMain))) {
	        	value = obj.getString(keyMain); 
	        	break;
	        }
	        else if (obj.optJSONObject(key) != null) {
	        	value = get(obj.getJSONObject(key), keyMain);
	        }
	        else if (obj.optJSONArray(key) != null) {
	            JSONArray jArray = obj.getJSONArray(key);
	            int flag = 0;
	            for (int i = 0; i < jArray.length(); i++) {
	            	if(jArray.get(i) instanceof JSONObject){
	            		flag = 1;
	            		value = get(jArray.getJSONObject(i), keyMain);
	            	}
	            }
	            if(flag == 0){
	            	if ((key.equals(keyMain))) {
	            		for (int i = 0; i < jArray.length(); i++) {
	            			if (!(jArray.get(i) instanceof JSONObject && jArray.get(i) instanceof JSONArray)) {
	            				value += jArray.get(i) + ",";
	            			}
	            		}
	            		value = value.trim().substring(0, value.trim().length() - 1);
	            		break;
		            }
	            }
	        }
	    }
		return value;
	}
}