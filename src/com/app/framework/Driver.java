package com.app.framework;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.app.util.BaseUtil;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class Driver implements Runnable {
	
	String OS = System.getProperty("os.name").toLowerCase();
	private DesiredCapabilities dc;
	protected WebDriver driver;
	static String sReportFile;
	static String resultPath;
	static String ReportFilePath;
	static Logger log = LoggerFactory.getLogger(Driver.class);
	protected HashMapNew Dictionary;
	protected HashMapNew Environment;
	static HashMapNew deviceList;
	protected String driverType;
	protected Reporting Reporter;
	static boolean bThreadFlag = false;
	static int threadCount = 0;
	protected Assert Assert;
	protected SoftAssert SoftAssert;
	protected BaseUtil BaseUtil;
	private static CSVWriter csvOutput = null;
	private static Date g_StartTime;
	private static Date g_EndTime;
	private Date c_StartTime;
	private Date c_EndTime;
	private static int totalPassedTCs;
	private static int totalFailedTCs;
	private static int totalPassedMtds;
	private static int totalFailedMtds;
	private static HashMapNew duration;
	static ThreadLocal<Boolean> resetFUPCount = new ThreadLocal<Boolean>(){
		@Override protected Boolean initialValue() {
			return false;
		}
	};
	static ThreadLocal<Boolean> firstTCFlag = new ThreadLocal<Boolean>(){
		@Override protected Boolean initialValue() {
			return false;
		}
	};
	static ThreadLocal<WebDriver> sDriver = new ThreadLocal<WebDriver>(){
		@Override protected WebDriver initialValue() {
			return null;
		}
	};
	static ThreadLocal<Boolean> uninstallFlag = new ThreadLocal<Boolean>(){
		@Override protected Boolean initialValue() {
			return false;
		}
	};
	
	public Driver(){
		OSValidator.setPropValues(OS);
		Dictionary = new HashMapNew();
		Environment = getEnvValues();
		deviceList = new HashMapNew();
		duration = new HashMapNew();
		Reporter = new Reporting(driver, driverType, Dictionary, Environment, BaseUtil);
		BaseUtil = new BaseUtil(driver, driverType, Dictionary, Environment, Reporter, Assert, SoftAssert);
	}
	
	public HashMapNew getEnvValues(){
		HashMapNew temp =  GetXMLNodeValue(OSValidator.delimiter + "src" + OSValidator.delimiter + "Configuration.xml", "//common", 0);
		if(temp != null){
			String env = System.getProperty("environment") != null && !System.getProperty("environment").trim().equalsIgnoreCase("") ? System.getProperty("environment").trim() : temp.get("env");
			temp.put("env", env);
			String version = temp.get("version");
			String envFilePath = temp.get("envFilePath");
			if(!envFilePath.trim().equalsIgnoreCase("")){
				if(!env.trim().equalsIgnoreCase("") && !version.trim().equalsIgnoreCase(""))
					temp.putAll(GetXMLNodeValue(envFilePath, "//" + env + "/" + version, 0));
				else if(!env.trim().equalsIgnoreCase(""))
					temp.putAll(GetXMLNodeValue(envFilePath, "//" + env, 0));
			}
		}
		return temp;
	}
	
	@BeforeSuite(alwaysRun = true)
	public void setUpSuite() throws Exception{
		Driver.g_StartTime = new Date();
		try{
			  //******************* Fetch Current TimeStamp ************************
		      java.util.Date today = new java.util.Date();
		      Timestamp now = new java.sql.Timestamp(today.getTime());
		      String tempNow[] = now.toString().split("\\.");
		      final String sStartTime = tempNow[0].replaceAll(":", ".").replaceAll(" ", "T");
		      
			  ReportFilePath = System.getProperty("user.dir") + OSValidator.delimiter +  "Reports";
			  if(System.getProperty("branch") != null && !System.getProperty("branch").trim().equalsIgnoreCase("")){
				  ReportFilePath += OSValidator.delimiter + System.getProperty("branch").trim();
			  }
			  sReportFile = ReportFilePath + OSValidator.delimiter + "Report_" + sStartTime + ".html";
			  resultPath = ReportFilePath + OSValidator.delimiter + "Results.csv";
				
			  if(!new File(ReportFilePath).exists()){
				  new File(ReportFilePath).mkdirs();
			  }
			  
			  if(new File(resultPath).exists()){
				  new File(resultPath).delete();
			  }
			  
			  csvOutput = new CSVWriter(new FileWriter(resultPath, true));
			  
			  //Create report file                  
			  FileOutputStream foutStrm = new FileOutputStream(sReportFile, true);
		           
			  //Write in Report file
	          new PrintStream(foutStrm).println("<HTML><BODY><TABLE BORDER=0 CELLPADDING=3 CELLSPACING=1 WIDTH=100% BGCOLOR=BLACK>");
			  new PrintStream(foutStrm).println("<TR><TD WIDTH=90% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=3><B>" + Environment.get("orgName") + "</B></FONT></TD></TR><TR><TD ALIGN=CENTER BGCOLOR=" + Environment.get("reportColor") + "><FONT FACE=VERDANA COLOR=WHITE SIZE=3><B>Automation Framework Reporting</B></FONT></TD></TR></TABLE><TABLE CELLPADDING=3 WIDTH=100%><TR height=30><TD WIDTH=100% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=//0073C5 SIZE=2><B>&nbsp; Automation Result : " + new Date() + " on Machine " + InetAddress.getLocalHost().getHostName() + " by user " + System.getProperty("user.name") + "</B></FONT></TD></TR><TR HEIGHT=5></TR></TABLE>");  
	          new PrintStream(foutStrm).println("<TABLE  CELLPADDING=3 CELLSPACING=1 WIDTH=100%>");           
	          new PrintStream(foutStrm).println("<TR COLS=4 BGCOLOR=" + Environment.get("reportColor") + "><TD WIDTH=10%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Thread No.</B></FONT></TD><TD WIDTH=35%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Device Name</B></FONT></TD><TD  WIDTH=35%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Test Class</B></FONT></TD><TD  WIDTH=20%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Summary</B></FONT></TD></TR>");
		        
	          //Close the object
	          foutStrm.close();	      
			  log.info("Report File Path : " + sReportFile);
			  
		  }catch(Exception e){
			  log.info("Threw a Exception in Driver::setUpSuite, full stack trace follows:", e);
		  }
		
		if(System.getProperty("deviceId") != null && !System.getProperty("deviceId").trim().equalsIgnoreCase("")){
			String[] androidDeviceUids = System.getProperty("deviceId").trim().replace("\r\n", "\n").replace("\n\n", "\n").split("\n");
			for(int i = 0 ; i < androidDeviceUids.length; i++){
				deviceList.put("ANDROID" + (i+1) , androidDeviceUids[i]);
			}
		}
		else if(Environment.get("autoDetectAndroidDevices").trim().equalsIgnoreCase("true") || Environment.get("autoDetectAndroidDevices").trim().equalsIgnoreCase("y") || Environment.get("autoDetectAndroidDevices").trim().equalsIgnoreCase("yes")){
			List<String> androidDeviceUids = new ArrayList<String>();
			String adbPath = "";
			if(Environment.get("autoDetectADBPath").trim().equalsIgnoreCase("true") || Environment.get("autoDetectADBPath").trim().equalsIgnoreCase("y") || Environment.get("autoDetectADBPath").trim().equalsIgnoreCase("yes")){
				if(OSValidator.shellType.trim().equalsIgnoreCase("cmd")){
					adbPath = BaseUtil.runCommand(OSValidator.shellType, new String[]{"/c", "echo %ANDROID_HOME%"}, new boolean[]{false, false}, 3000);
					if(adbPath != null && !adbPath.trim().equalsIgnoreCase("")){
						adbPath += OSValidator.delimiter + "tools" + OSValidator.delimiter + "adb"; 
					}
				}
				else{
					adbPath = BaseUtil.runCommand(OSValidator.shellType, new String[]{"-l", "-c", "which adb"}, new boolean[]{false, false, false}, 3000);
				}
			}
			
			if(adbPath != null && !adbPath.trim().equalsIgnoreCase("")){
				//Do Nothing
			}
			else{
				if(!Environment.get("adbPath").trim().equalsIgnoreCase(""))
					adbPath = Environment.get("adbPath").trim();
				else{
					log.info("ADB path not found");
					return;
				}
			}
		  
			adbPath = adbPath.split("\n")[0].replace("//", "/");
			
			CommandLine command = new CommandLine(adbPath);
			command.addArgument("devices", false);
		  
			ByteArrayOutputStream stdout = new ByteArrayOutputStream();
			PumpStreamHandler psh = new PumpStreamHandler(stdout);
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
			DefaultExecutor executor = new DefaultExecutor();
			executor.setStreamHandler(psh);
			try {
				executor.execute(command, resultHandler);
				Long wait = 2000L;
				while(!stdout.toString().trim().contains("device") && wait > 0){
					Thread.sleep(1L);
					wait--;
				}
				System.out.print(stdout.toString());
				String[] deviceList = stdout.toString().split("\n");
				for(int i = 1; i < deviceList.length; i++){
					if(deviceList[i].toLowerCase().contains("device")){
						androidDeviceUids.add(deviceList[i].split("device")[0].trim());
					}
				}
			} catch (IOException e1) {
				log.info("Threw a Exception in Driver::getDevices, full stack trace follows:", e1);
			}
			catch(InterruptedException e2){
				log.info("Threw a Exception in Driver::getDevices, full stack trace follows:", e2);
			}
			
			if(androidDeviceUids != null && androidDeviceUids.size() > 0){
				for(int i = 0 ; i < androidDeviceUids.size(); i++){
					deviceList.put("ANDROID" + (i+1) , androidDeviceUids.get(i));
				}
			}
		}
		
		if(Environment.get("autoDetectIOSDevices").trim().equalsIgnoreCase("true") || Environment.get("autoDetectIOSDevices").trim().equalsIgnoreCase("y") || Environment.get("autoDetectIOSDevices").trim().equalsIgnoreCase("yes")){
			
			List<String> iosDeviceUids = new ArrayList<String>();
			String instrumentsPath = "";
			instrumentsPath = BaseUtil.runCommand(OSValidator.shellType, new String[]{"-l", "-c", "which instruments"}, new boolean[]{false, false, false}, 3000);
			
			if(instrumentsPath == null || instrumentsPath.trim().equalsIgnoreCase("")){
				log.info("Instruments path not found");
				return;
			}
		  
			instrumentsPath = instrumentsPath.split("\n")[0].replace("//", "/");
			
			CommandLine command = new CommandLine(instrumentsPath);
			command.addArgument("-s", false);
			command.addArgument("devices", false);
			command.addArgument("|", false);
			command.addArgument("grep", false);
			command.addArgument("-v", false);
			command.addArgument("'[A-Za-z0-9].*-[A-Za-z0-9].*-[A-Za-z0-9].*-[A-Za-z0-9].*-[A-Za-z0-9].*'", true);
		  
			ByteArrayOutputStream stdout = new ByteArrayOutputStream();
			PumpStreamHandler psh = new PumpStreamHandler(stdout);
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
			DefaultExecutor executor = new DefaultExecutor();
			executor.setStreamHandler(psh);
			try {
				executor.execute(command, resultHandler);
				Long wait = 2000L;
				while(!stdout.toString().trim().contains("Simulator") && wait > 0){
					Thread.sleep(1L);
					wait--;
				}
				System.out.print(stdout.toString());
				String[] deviceList = stdout.toString().split("\n");
				for(int i = 1; i < deviceList.length; i++){
					if(deviceList[i].toLowerCase().contains("[") && !deviceList[i].toLowerCase().contains("instruments")){
						String deviceId = deviceList[i].substring(deviceList[i].indexOf("[") + 1).trim().substring(0, deviceList[i].substring(deviceList[i].indexOf("[") + 1).trim().length() - 1);
						if(!Pattern.matches("[A-Za-z0-9].*-[A-Za-z0-9].*-[A-Za-z0-9].*-[A-Za-z0-9].*-[A-Za-z0-9].*", deviceId))
							iosDeviceUids.add(deviceId);
					}
				}
			} catch (IOException e1) {
				log.info("Threw a Exception in Driver::getDevices, full stack trace follows:", e1);
			}
			catch(InterruptedException e2){
				log.info("Threw a Exception in Driver::getDevices, full stack trace follows:", e2);
			}
			
			if(iosDeviceUids != null && iosDeviceUids.size() > 0){
				for(int i = 0 ; i < iosDeviceUids.size(); i++){
					deviceList.put("IOS" + (i+1) , iosDeviceUids.get(i));
				}
			}
		}
	}
	
	@Parameters({ "deviceName" })
	@BeforeTest(alwaysRun = true)
	public void getDevices(String deviceName) throws Exception{
		resetFUPCount.set(true);
		uninstallFlag.set(true);
		driverType = deviceName;
		firstTCFlag.set(true);
	}
	
	@Parameters({ "deviceName", "noReset", "network", "browserTest", "app", "appPackage", "appActivity", "appWaitActivity", "automationName" })
	@BeforeClass(alwaysRun = true)
	public void setUpDevice(String deviceName, @Optional("true") boolean noReset, @Optional("") String network, @Optional("false") String browserTest, @Optional("") String app, @Optional("") String appPackage, @Optional("") String appActivity, @Optional("") String appWaitActivity, @Optional("") String automationName) throws Exception{
		c_StartTime = new Date();
		driverType = deviceName;
		
		String User = System.getProperty("user.name");
	    String RootPath = System.getProperty("user.dir");
	    
	    try{
	    	HashMapNew temp = GetXMLNodeValue(OSValidator.delimiter + "src" + OSValidator.delimiter + "Configuration.xml", "//" + driverType.toLowerCase(), 0);
	    	if(temp != null){
	    		Environment.putAll(temp);
	    	}
	    }
	    catch (Exception excep){
	      log.info("Exception occurred while reading XML file for Browser " + driverType);
	      log.info("Threw a Exception in Driver::setUpDevice, full stack trace follows:", excep);
	      throw excep;
	    }

	    String ExecutionFolderPath = RootPath + OSValidator.delimiter + "Execution";
	    String relExecutionFolderPath = "Execution";
	    String CurrentExecutionFolder = ExecutionFolderPath + OSValidator.delimiter + (String)Environment.get("testSet") + OSValidator.delimiter + User;
	    String relCurrentExecutionFolder = relExecutionFolderPath + OSValidator.delimiter + (String)Environment.get("testSet") + OSValidator.delimiter + User;
	    
	    Date date = new Date();
	    String modifiedDate= new SimpleDateFormat("MM/dd/yyyy").format(date);
	    modifiedDate = modifiedDate.replace("/", "");
	    
	    Environment.put("ROOTPATH", RootPath);
	    Environment.put("EXECUTIONFOLDERPATH", ExecutionFolderPath);
	    Environment.put("CURRENTEXECUTIONFOLDER", CurrentExecutionFolder);
	    
		Reporter = new Reporting(driver, driverType, Dictionary, Environment, BaseUtil);
		
		Date today = new Date();
	    Timestamp now = new Timestamp(today.getTime());
	    String[] tempNow = now.toString().split("\\.");
	    String timeStamp = tempNow[0].replaceAll(":", ".").replaceAll(" ", "T");
	    
		String HTMPReports = Environment.get("CURRENTEXECUTIONFOLDER") + OSValidator.delimiter + driverType + OSValidator.delimiter + this.getClass().getName() + OSValidator.delimiter + "HTML_REP_" + timeStamp;
		String relHTMPReports = relCurrentExecutionFolder + OSValidator.delimiter + driverType + OSValidator.delimiter + this.getClass().getName() + OSValidator.delimiter + "HTML_REP_" + timeStamp;
	    String SnapshotsFolder = HTMPReports + OSValidator.delimiter + "Snapshots";
	    String relSnapshotsFolder = "Snapshots";
	    String LogsFolder = HTMPReports + OSValidator.delimiter + "Logs";
	    String relLogsFolder = "Logs";
	    
	    Environment.put("HTMLREPORTSPATH", HTMPReports);
	    Environment.put("RELHTMLREPORTSPATH", relHTMPReports);
	    Environment.put("SNAPSHOTSFOLDER", SnapshotsFolder);
	    Environment.put("RELSNAPSHOTSFOLDER", relSnapshotsFolder);
	    Environment.put("LOGSFOLDER", LogsFolder);
	    Environment.put("RELLOGSFOLDER", relLogsFolder);
	    
	    new File(LogsFolder).mkdirs();
	    boolean success = new File(SnapshotsFolder).mkdirs();

	    if (success) {
	      log.info("Directories: " + SnapshotsFolder + " created");
	    }
	    
	    Dictionary.put("TEST_CLASS_NAME", this.getClass().getName());
	    Reporter.fnCreateSummaryReport();
	    while (bThreadFlag) {
			try{
				Thread.sleep(500L);
			}
			catch (Exception localException1) {}
		}
	    
	    bThreadFlag = true;
	    if(System.getProperty("branch") != null && !System.getProperty("branch").trim().equalsIgnoreCase("")){
	    	Reporter.fnWriteThreadReport(++threadCount, sReportFile, this.getClass().getName(), "../../" + (String)Environment.get("RELHTMLREPORTSPATH") + OSValidator.delimiter + "SummaryReport.html");
	    }
	    else{
	    	Reporter.fnWriteThreadReport(++threadCount, sReportFile, this.getClass().getName(), "../" + (String)Environment.get("RELHTMLREPORTSPATH") + OSValidator.delimiter + "SummaryReport.html");
	    }
	    
	    bThreadFlag = false;
	    
	    Dictionary.put("APP", app);
        Dictionary.put("APP_PACKAGE", appPackage);
        Dictionary.put("APP_ACTIVITY", appActivity);
        Dictionary.put("APP_WAIT_ACTIVITY", appWaitActivity);
        Dictionary.put("NETWORK_TYPE", network);
        Dictionary.put("TEST_BROWSER", String.valueOf(browserTest));
        Dictionary.put("NO_RESET", String.valueOf(noReset));
        Dictionary.put("AUTOMATION_NAME", automationName.trim().equalsIgnoreCase("") ? Environment.get("automationName").trim() : automationName.trim());
	}
	
	@BeforeMethod(alwaysRun = true)
	public void createHTMLReport(Method method, Object[] testData) throws Exception {
		String testCase = "", app = Dictionary.get("APP"), appPackage = Dictionary.get("APP_PACKAGE"), appActivity = Dictionary.get("APP_ACTIVITY"), network = Dictionary.get("NETWORK_TYPE"), appWaitActivity = Dictionary.get("APP_WAIT_ACTIVITY");
		boolean noReset = Boolean.valueOf(Dictionary.get("NO_RESET"));
		boolean autoLaunch = Boolean.valueOf(Environment.get("autoLaunch").trim().equalsIgnoreCase("") ? "true" : Environment.get("autoLaunch").trim());
		boolean testBrowser = Boolean.valueOf(Dictionary.get("TEST_BROWSER"));
		String automationName = Dictionary.get("AUTOMATION_NAME").trim();
		String version = System.getProperty("appVersion") != null && !System.getProperty("appVersion").trim().equalsIgnoreCase("") ? System.getProperty("appVersion").trim() : Environment.get("appVersion").trim();
		System.out.println("App version : " + version);
		
        if (testData != null && testData.length > 0) {
            TestParameters testParams = null;
            for (Object testParameter : testData) {
                if (testParameter instanceof TestParameters) {
                    if(((TestParameters)testParameter).getTestMethodName().trim().equalsIgnoreCase(method.getName().trim())){
                    	testParams = (TestParameters)testParameter;
                    	break;
                    }
                }
            }
            if (testParams != null) {
                testCase = testParams.getTestName() == null ? method.getName().trim() : testParams.getTestName();
                Dictionary.put("TEST_NAME", testCase + "[" + method.getName().trim() + "]");
                Dictionary.put("ACTION", method.getName().trim());
                
                app = testParams.app != null && !testParams.app.trim().equalsIgnoreCase("") ? testParams.app.trim() : app;
                appPackage = testParams.appPackage != null && !testParams.appPackage.trim().equalsIgnoreCase("") ? testParams.appPackage.trim() : appPackage;
                appActivity = testParams.appActivity != null && !testParams.appActivity.trim().equalsIgnoreCase("") ? testParams.appActivity.trim() : appActivity;
                network = testParams.network != null && !testParams.network.trim().equalsIgnoreCase("") ? testParams.network.trim() : network;
                appWaitActivity = testParams.appWaitActivity != null && !testParams.appWaitActivity.trim().equalsIgnoreCase("") ? testParams.appWaitActivity.trim() : appWaitActivity;
                noReset = testParams.noReset;
                autoLaunch = testParams.autoLaunch;
                testBrowser = testParams.testBrowser;
                automationName = testParams.automationName;
                version = testParams.version;
                Reporter.fnCreateHtmlReport(Dictionary.get("ACTION").trim());
            }
            else{
            	Dictionary.put("TEST_NAME", method.getName().trim());
            	Dictionary.put("ACTION", method.getName().trim());
            	Reporter.fnCreateHtmlReport(Dictionary.get("ACTION").trim());
            }
        }
        else{
        	Dictionary.put("TEST_NAME", method.getName().trim());
        	Dictionary.put("ACTION", method.getName().trim());
        	Reporter.fnCreateHtmlReport(Dictionary.get("ACTION").trim());
        }
        
        Environment.putAll(getEnvValues());
        try{
	    	HashMapNew temp = GetXMLNodeValue(OSValidator.delimiter + "src" + OSValidator.delimiter + "Configuration.xml", "//" + driverType.toLowerCase(), 0);
	    	if(temp != null){
	    		Environment.putAll(temp);
	    	}
	    }
	    catch (Exception excep){
	      log.info("Exception occurred while reading XML file for Browser " + driverType);
	      log.info("Threw a Exception in Driver::setUpDevice, full stack trace follows:", excep);
	      throw excep;
	    }
        log.info("########################" + Dictionary.get("TEST_NAME").trim() + " EXECUTION STARTED########################");
        
    	if(firstTCFlag.get().booleanValue()){
    		driver = initDriver(autoLaunch, noReset, network, app, appPackage, appActivity, appWaitActivity, testBrowser, automationName, version);
    		sDriver.set(driver);
    	} else{
    		driver = sDriver.get();
    		Reporter.driver = driver;
			Assert = new Assert(Reporter);
			SoftAssert = new SoftAssert(Reporter);
			BaseUtil = new BaseUtil(driver, driverType, Dictionary, Environment, Reporter, Assert, SoftAssert);
    	}
	}
	
	public WebDriver initDriver(boolean autoLaunch, boolean noReset, String network, String app, String appPackage, String appActivity, String appWaitActivity, boolean testBrowser, String automationName, String version) throws Exception{
		try{
			BaseUtil = new BaseUtil(driver, driverType, Dictionary, Environment, Reporter, Assert, SoftAssert);
			if (driver != null){
				driver.quit();
			}
			
			if (driverType.trim().toUpperCase().contains("IOS")){
				JSONObject json = BaseUtil.readJsonFromUrl((String)Environment.get("ip") + "/status", false);
				if(json != null){
					if(json.has("sessionId")){
						if(!json.getString("sessionId").trim().equalsIgnoreCase("")){
							URL url = new URL((String)Environment.get("ip") + "/session/" + json.getString("sessionId"));
							HttpURLConnection connection = (HttpURLConnection) url.openConnection();
							connection.setRequestMethod("DELETE");
							connection.getResponseCode();
						}
					}
				}
				
				String appName = System.getProperty("appName") != null && !System.getProperty("appName").trim().equalsIgnoreCase("") ? System.getProperty("appName").trim() : Environment.get("app").trim();
				Environment.put("app", appName);
				
				if(!app.trim().equalsIgnoreCase("")){
		        	Environment.put("app", app.trim());
				}
				
				if(Environment.get("udid").trim().equalsIgnoreCase("")){
					if(deviceList.get(driverType.trim().toUpperCase()).trim().equalsIgnoreCase(""))
						throw new SkipException("IOS device - " + driverType.trim().toUpperCase() + " not found");
					Environment.put("udid", deviceList.get(driverType.trim().toUpperCase()).trim());
				}
				
				String[] details = getDeviceDetails(Environment.get("udid").trim());
				Dictionary.put(driverType.trim().toUpperCase() + "_OPERATOR", details.length > 0 ? details[0] : "");
				Dictionary.put(driverType.trim().toUpperCase() + "_VERSION", details.length > 1 ? details[1] : "");
				Dictionary.put(driverType.trim().toUpperCase() + "_MANUFACTURER", details.length > 2 ? details[2] : "");
				Dictionary.put(driverType.trim().toUpperCase() + "_MODEL", details.length > 3 ? details[3] : "");
	          
				this.dc = new DesiredCapabilities();
				
				if(!Environment.get("udid").trim().equalsIgnoreCase("")){
					this.dc.setCapability("udid", Environment.get("udid"));
				}
				if(Environment.get("browserTest").trim().equalsIgnoreCase("true") || Environment.get("browserTest").trim().equalsIgnoreCase("Y") || Environment.get("browserTest").trim().equalsIgnoreCase("Yes") || testBrowser){
					this.dc.setCapability("browserName", "Safari");
					this.dc.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				}
				else{
					if(!appPackage.equalsIgnoreCase(""))
						Environment.put("bundleId", appPackage.trim());
					if(!Environment.get("bundleId").trim().equalsIgnoreCase("")){
						this.dc.setCapability("bundleId", Environment.get("bundleId"));
					}
//					this.dc.setCapability("autoDismissAlerts", true);
					this.dc.setCapability("fullReset", Boolean.valueOf(Environment.get("fullReset")));
					this.dc.setCapability("autoLaunch", autoLaunch);
					this.dc.setCapability("noReset", noReset);
					if(noReset == false){
						//Uninstall IOS app
						if(!Environment.get("bundleId").trim().equalsIgnoreCase(""))
							BaseUtil.uninstallIOSAppUsingCLI(Environment.get("bundleId").trim(), Environment.get("udid").trim());
					}
					if(!Environment.get("app").trim().equalsIgnoreCase("")){
						if(Environment.get("app").trim().contains(OSValidator.delimiter)){
							if(!new File(Environment.get("app").trim()).exists()){
								String name = Environment.get("app").trim().split(OSValidator.delimiter)[Environment.get("app").trim().split(OSValidator.delimiter).length - 1];
								if(!version.equalsIgnoreCase("") && !version.equalsIgnoreCase("false")){
									boolean status = BaseUtil.downloadFromS3("QA-Builds/apps/" + version.replace(" ", ".") + "/", Environment.get("app").trim().substring(0, Environment.get("app").trim().length() - name.length()), name);
									if(!status){
										if(!Environment.get("lastAppVersion").trim().equalsIgnoreCase("")){
											BaseUtil.downloadFromS3("QA-Builds/apps/" + Environment.get("lastAppVersion").trim().replace(" ", ".") + "/", Environment.get("app").trim().substring(0, Environment.get("app").trim().length() - name.length()), name);
										}
									}
								}
								else{
									if(!Environment.get("lastAppVersion").trim().equalsIgnoreCase("")){
										BaseUtil.downloadFromS3("QA-Builds/apps/" + Environment.get("lastAppVersion").trim().replace(" ", ".") + "/", Environment.get("app").trim().substring(0, Environment.get("app").trim().length() - name.length()), name);
									}
									else{
										BaseUtil.downloadFromS3("QA-Builds/apps/", Environment.get("app").trim().substring(0, Environment.get("app").trim().length() - name.length()), name);
									}
								}
							}
							this.dc.setCapability("app", Environment.get("app").trim());
						}
						else{
							String userdir = "";
							if(!Environment.get("user").trim().equalsIgnoreCase("")){
								userdir = System.getProperty("user.dir").replace(System.getProperty("user.name"), Environment.get("user").trim());
							}
							else
								userdir = System.getProperty("user.dir");
							String path = "";
							if(!version.equalsIgnoreCase("")){
								path = Environment.get("appsFolder").trim() + version + "/";
							}
							else{
								path = Environment.get("appsFolder").trim();
							}
							File appDir = new File(new File(userdir), path);
							File apk = null;
							apk = new File(appDir, Environment.get("app").trim());
							if(Environment.get("user").trim().equalsIgnoreCase("")){
								if(!apk.exists()){
									if(!version.equalsIgnoreCase("") && !version.equalsIgnoreCase("false")){
										boolean status = BaseUtil.downloadFromS3("QA-Builds/apps/" + version.replace(" ", ".") + "/", appDir.getAbsolutePath(), Environment.get("app").trim());
										if(!status){
											if(!Environment.get("lastAppVersion").trim().equalsIgnoreCase("")){
												BaseUtil.downloadFromS3("QA-Builds/apps/" + Environment.get("lastAppVersion").trim().replace(" ", ".") + "/", appDir.getAbsolutePath(), Environment.get("app").trim());
											}
										}
									}
									else{
										if(!Environment.get("lastAppVersion").trim().equalsIgnoreCase("")){
											BaseUtil.downloadFromS3("QA-Builds/apps/" + Environment.get("lastAppVersion").trim().replace(" ", ".") + "/", appDir.getAbsolutePath(), Environment.get("app").trim());
										}
										else{
											BaseUtil.downloadFromS3("QA-Builds/apps/", appDir.getAbsolutePath(), Environment.get("app").trim());
										}
									}
								}	
							}
							this.dc.setCapability("app", apk.getAbsolutePath());
						}
					}
				}
	          
				this.dc.setCapability("platformName", Environment.get("platformName"));
				this.dc.setCapability("deviceName", Environment.get("deviceName"));
				this.dc.setCapability("newCommandTimeout", Environment.get("newCommandTimeout"));
				HashMapNew temp = GetXMLNodeValue(Environment.get("relativeDeviceConfigurationPath").trim(), "//" + "udid_" + Environment.get("udid").trim(), 0);
		    	if(temp != null){
		    		Environment.putAll(temp);
		    	}
		    	String restfupcount = System.getProperty("restfupcount") != null && !System.getProperty("restfupcount").trim().equalsIgnoreCase("") ? System.getProperty("restfupcount").trim() : Environment.get("resetFUPCount").trim();
		    	if(restfupcount.equalsIgnoreCase("true") || restfupcount.equalsIgnoreCase("yes") || restfupcount.equalsIgnoreCase("y")){
		    		if(resetFUPCount.get().booleanValue()){
		    			resetFUPCount.set(false);
		    			BaseUtil = new BaseUtil(driver, driverType, Dictionary, Environment, Reporter, Assert, SoftAssert);
		    			if(!Environment.get("deviceBasedUid").trim().equalsIgnoreCase("")){
		    				BaseUtil.setFUPCount(Environment.get("deviceBasedUid").trim(), "0");
		    			}
		    			if(!Environment.get("msisdnBasedUid").trim().equalsIgnoreCase("")){
		    				BaseUtil.setFUPCount(Environment.get("msisdnBasedUid").trim(), "0");
		    			}
		    		}
		    	}
				driver = new IOSDriver<MobileElement>(new URL(Environment.get("ip")), this.dc);
			}
			else if (driverType.trim().toUpperCase().contains("ANDROID")){
				JSONObject json = BaseUtil.readJsonFromUrl(Environment.get("ip") + "/status", false);
				if(json != null){
					if(json.has("sessionId")){
						if(!json.getString("sessionId").trim().equalsIgnoreCase("")){
							URL url = new URL(Environment.get("ip") + "/session/" + json.getString("sessionId"));
							HttpURLConnection connection = (HttpURLConnection) url.openConnection();
							connection.setRequestMethod("DELETE");
							connection.getResponseCode();
						}
					}
				}
	        
				String appName = System.getProperty("appName") != null && !System.getProperty("appName").trim().equalsIgnoreCase("") ? System.getProperty("appName").trim() : Environment.get("app").trim();
				Environment.put("app", appName);
				
				if(!app.trim().equalsIgnoreCase("")){
					Environment.put("app", app.trim());
	        	
					if(!appPackage.trim().equalsIgnoreCase("")){
						Environment.put("appPackage", appPackage.trim());
					}
					else{
						Environment.put("appPackage", "");
					}
	        	
					if(!appActivity.trim().equalsIgnoreCase("")){
						Environment.put("appActivity", appActivity.trim());
					}
					else{
						Environment.put("appActivity", "");
					}
					
					if(!appWaitActivity.trim().equalsIgnoreCase("")){
						Environment.put("appWaitActivity", appWaitActivity.trim());
					}
					else{
						Environment.put("appWaitActivity", "");
					}
				}
				else if(!appPackage.trim().equalsIgnoreCase("")){
					Environment.put("app", "");
					Environment.put("appPackage", appPackage.trim());
					if(!appActivity.trim().equalsIgnoreCase("")){
						Environment.put("appActivity", appActivity.trim());
					}
					else{
						throw new AssertionError("App activity cannot be null");
					}
					
					if(!appWaitActivity.trim().equalsIgnoreCase("")){
						Environment.put("appWaitActivity", appWaitActivity.trim());
					}
					else{
						Environment.put("appWaitActivity", "");
					}
				}
				
				if(Environment.get("udid").trim().equalsIgnoreCase("")){
					if(deviceList.get(driverType.trim().toUpperCase()).trim().equalsIgnoreCase(""))
						throw new SkipException("Android device - " + driverType.trim().toUpperCase() + " not found");
					Environment.put("udid", deviceList.get(driverType.trim().toUpperCase()).trim());
				}
				
				if(Environment.get("user").trim().equalsIgnoreCase("")){
					String[] details = getDeviceDetails(Environment.get("udid").trim());
					Dictionary.put(driverType.trim().toUpperCase() + "_OPERATOR", details.length > 0 ? details[0] : "");
					Dictionary.put(driverType.trim().toUpperCase() + "_VERSION", details.length > 1 ? details[1] : "");
					Dictionary.put(driverType.trim().toUpperCase() + "_MANUFACTURER", details.length > 2 ? details[2] : "");
					Dictionary.put(driverType.trim().toUpperCase() + "_MODEL", details.length > 3 ? details[3] : "");
				}
				this.dc = DesiredCapabilities.android();
				if(Environment.get("browserTest").trim().equalsIgnoreCase("true") || Environment.get("browserTest").trim().equalsIgnoreCase("Y") || Environment.get("browserTest").trim().equalsIgnoreCase("Yes") || testBrowser){
					this.dc.setCapability("browserName", "chrome");
				}
		        else{
		        	String uninstall = System.getProperty("uninstall") != null && !System.getProperty("uninstall").trim().equalsIgnoreCase("") ? System.getProperty("uninstall").trim() : Environment.get("uninstall").trim();
					if(uninstall.equalsIgnoreCase("true") || uninstall.equalsIgnoreCase("yes") || uninstall.equalsIgnoreCase("y")){
						if(uninstallFlag.get().booleanValue()){
							uninstallFlag.set(false);
							BaseUtil.runADBCommand(new String[]{"-s", Environment.get("udid"), "uninstall", Environment.get("appPackage").trim()}, new Boolean[]{false, false, false, false}, 6000);
						}
					}
					else{
						if(!version.trim().equalsIgnoreCase("") && !version.trim().equalsIgnoreCase("false") && version.trim().contains(".")){
							String oldVersion = checkAppVersion(Environment.get("appPackage").trim());
							if(!oldVersion.trim().equalsIgnoreCase("")){
								if(!version.trim().contains(oldVersion.trim())){
									BaseUtil.runADBCommand(new String[]{"-s", Environment.get("udid"), "uninstall", Environment.get("appPackage").trim()}, new Boolean[]{false, false, false, false}, 6000);
								}
							}
						}
					}
			        if(!Environment.get("appPackage").trim().equalsIgnoreCase("")){
			        	this.dc.setCapability("appPackage", Environment.get("appPackage"));
			        }
			        if(!Environment.get("appActivity").trim().equalsIgnoreCase("")){
			        	this.dc.setCapability("appActivity", Environment.get("appActivity"));
			        }
			        this.dc.setCapability("fullReset", Boolean.valueOf(Environment.get("fullReset")));
			        this.dc.setCapability("autoLaunch", false);
	      	  		this.dc.setCapability("noReset", noReset);
			        if(!Environment.get("app").trim().equalsIgnoreCase("")){
			        	if(Environment.get("app").trim().contains(OSValidator.delimiter)){
			        		if(!new File(Environment.get("app").trim()).exists()){
								String name = Environment.get("app").trim().split(OSValidator.delimiter)[Environment.get("app").trim().split(OSValidator.delimiter).length - 1];
								if(!version.equalsIgnoreCase("") && !version.equalsIgnoreCase("false")){
									boolean status = BaseUtil.downloadFromS3("QA-Builds/apps/" + version.replace(" ", ".") + "/", Environment.get("app").trim().substring(0, Environment.get("app").trim().length() - name.length()), name);
									if(!status){
										if(!Environment.get("lastAppVersion").trim().equalsIgnoreCase("")){
											BaseUtil.downloadFromS3("QA-Builds/apps/" + Environment.get("lastAppVersion").trim().replace(" ", ".") + "/", Environment.get("app").trim().substring(0, Environment.get("app").trim().length() - name.length()), name);
										}
									}
								}
								else{
									if(!Environment.get("lastAppVersion").trim().equalsIgnoreCase("")){
										BaseUtil.downloadFromS3("QA-Builds/apps/" + Environment.get("lastAppVersion").trim().replace(" ", ".") + "/", Environment.get("app").trim().substring(0, Environment.get("app").trim().length() - name.length()), name);
									}
									else{
										BaseUtil.downloadFromS3("QA-Builds/apps/", Environment.get("app").trim().substring(0, Environment.get("app").trim().length() - name.length()), name);
									}
								}
							}
							this.dc.setCapability("app", Environment.get("app").trim());
						}
						else{
				        	String userdir = "";
							if(!Environment.get("user").trim().equalsIgnoreCase("")){
								userdir = System.getProperty("user.dir").replace(System.getProperty("user.name"), Environment.get("user").trim());
							}
							else{
								userdir = System.getProperty("user.dir");
							}
							String path = "";
							if(!version.equalsIgnoreCase("")){
								path = Environment.get("appsFolder").trim() + version + "/";
							}
							else{
								path = Environment.get("appsFolder").trim();
							}
				        	File appDir = new File(new File(userdir), path);
				      		File apk = null;
				  			apk = new File(appDir, Environment.get("app").trim());
				  			if(Environment.get("user").trim().equalsIgnoreCase("")){
					  			if(!apk.exists()){
					  				if(!version.equalsIgnoreCase("") && !version.equalsIgnoreCase("false")){
										boolean status = BaseUtil.downloadFromS3("QA-Builds/apps/" + version.replace(" ", ".") + "/", appDir.getAbsolutePath(), Environment.get("app").trim());
										if(!status){
											if(!Environment.get("lastAppVersion").trim().equalsIgnoreCase("")){
												BaseUtil.downloadFromS3("QA-Builds/apps/" + Environment.get("lastAppVersion").trim().replace(" ", ".") + "/", appDir.getAbsolutePath(), Environment.get("app").trim());
											}
										}
					  				}
									else{
										if(!Environment.get("lastAppVersion").trim().equalsIgnoreCase("")){
											BaseUtil.downloadFromS3("QA-Builds/apps/" + Environment.get("lastAppVersion").trim().replace(" ", ".") + "/", appDir.getAbsolutePath(), Environment.get("app").trim());
										}
										else{
											BaseUtil.downloadFromS3("QA-Builds/apps/", appDir.getAbsolutePath(), Environment.get("app").trim());
										}
									}
					  			}
				  			}
				        	this.dc.setCapability("app", apk.getAbsolutePath());
						}
			        }
			        if(!Environment.get("appWaitActivity").trim().equalsIgnoreCase("")){
			        	this.dc.setCapability("appWaitActivity", Environment.get("appWaitActivity"));
			        }
			        if(!Environment.get("autoWebview").trim().equalsIgnoreCase("")){
			        	this.dc.setCapability("autoWebview", Boolean.valueOf(Environment.get("autoWebview")));
			        }
		        }
		        this.dc.setCapability("deviceName", Environment.get("deviceName"));
		        if(!Environment.get("udid").trim().equalsIgnoreCase("")){
		      	  this.dc.setCapability("udid", Environment.get("udid"));
		        }
		        this.dc.setCapability("platformName", Environment.get("platformName"));
		        this.dc.setCapability("newCommandTimeout", Environment.get("newCommandTimeout"));
		        if(!automationName.trim().equalsIgnoreCase("")){
		        	this.dc.setCapability("automationName", automationName);
		        }
		        HashMapNew temp = GetXMLNodeValue(Environment.get("relativeDeviceConfigurationPath").trim(), "//" + "udid_" + Environment.get("udid").trim(), 0);
		    	if(temp != null){
		    		Environment.putAll(temp);
		    	}
		    	String restfupcount = System.getProperty("restfupcount") != null && !System.getProperty("restfupcount").trim().equalsIgnoreCase("") ? System.getProperty("restfupcount").trim() : Environment.get("resetFUPCount").trim();
		    	if(restfupcount.equalsIgnoreCase("true") || restfupcount.equalsIgnoreCase("yes") || restfupcount.equalsIgnoreCase("y")){
		    		if(resetFUPCount.get().booleanValue()){
		    			resetFUPCount.set(false);
		    			BaseUtil = new BaseUtil(driver, driverType, Dictionary, Environment, Reporter, Assert, SoftAssert);
		    			if(!Environment.get("deviceBasedUid").trim().equalsIgnoreCase("")){
		    				BaseUtil.setFUPCount(Environment.get("deviceBasedUid").trim(), "0");
		    			}
		    			if(!Environment.get("msisdnBasedUid").trim().equalsIgnoreCase("")){
		    				BaseUtil.setFUPCount(Environment.get("msisdnBasedUid").trim(), "0");
		    			}
		    		}
		    	}
		        driver = new AndroidDriver<MobileElement>(new URL(Environment.get("ip")), this.dc);
		        Reporter.driver = driver;
		        networkSetUp(network, autoLaunch, testBrowser, automationName);
			}
			else if(driverType.trim().toUpperCase().contains("API")){
				driver = new HtmlUnitDriver(); 
			}
			else if(driverType.trim().toUpperCase().contains("CHROME")){
				if(!new File("chromedriver").exists()){
					log.info("Chromedriver executable not found in root directory");
					throw new SkipException("Chromedriver executable not found in root directory");
				}
				System.setProperty("webdriver.chrome.driver", "chromedriver");
				ChromeOptions options = new ChromeOptions();
				if(!Environment.get("chrome_extension_file_path").trim().equalsIgnoreCase("")){
					File addonpath = new File(System.getProperty("user.dir") + Environment.get("chrome_extension_file_path"));
					options.addExtensions(addonpath);
				}
				options.addArguments("--kiosk"); //for full screen view
				options.addArguments("start-maximized");
				if(!Environment.get("chrome_profile_path").trim().equalsIgnoreCase("")){
					options.addArguments("user-data-dir=" + Environment.get("chrome_profile_path").trim());
				}
				Map<String, Object> prefs = new HashMap<String, Object>();
				prefs.put("profile.default_content_settings.popups", 0);
				options.setExperimentalOption("prefs", prefs);
				
				DesiredCapabilities capabilities = new DesiredCapabilities();
				capabilities.setCapability(ChromeOptions.CAPABILITY, options);
				driver = new ChromeDriver(capabilities);
				if(!Environment.get("preExecution").trim().equalsIgnoreCase("")){
					String[] words = Environment.get("preExecution").trim().split("\\.");
					String methodName = words[words.length - 1];
					String className = Environment.get("preExecution").trim().substring(0, Environment.get("preExecution").trim().indexOf("." + methodName));
					Object[] params = new Object[0];
					Class<?> thisClass;
					try {
						thisClass = Class.forName(className);
						Object busFunctions = thisClass.getConstructor(new Class[] { WebDriver.class, String.class, HashMapNew.class, HashMapNew.class, Reporting.class, Assert.class, SoftAssert.class }).newInstance(new Object[] { this.driver, this.driverType, this.Dictionary, this.Environment, this.Reporter, this.Assert, this.SoftAssert });
						Method method = thisClass.getDeclaredMethod(methodName, new Class[0]);
						method.invoke(busFunctions, params);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					}
				}
			}
			else{
				log.info("Invalid driver type " + driverType);
				throw new SkipException("Invalid driver type " + driverType);
			}
			driver.manage().timeouts().implicitlyWait(Integer.parseInt(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);      
			Reporter.driver = driver;
			Assert = new Assert(Reporter);
			SoftAssert = new SoftAssert(Reporter);
			BaseUtil = new BaseUtil(driver, driverType, Dictionary, Environment, Reporter, Assert, SoftAssert);
    	}
	    catch(Exception e){
	      log.info("Threw a Exception in Driver::initDriver, full stack trace follows:", e);
	      throw e;
	    }
		
		return driver;
	}
	
	@AfterMethod(alwaysRun=true)
	public void quitDriver(ITestResult tr, Object[] testData) throws Exception{
		Dictionary.put("STACKTRACE", "");
		int tcp = 0;
		int tcf = 0;
		if(Reporter != null){
			if(tr.getStatus() == 1){
				Reporter.log(tr.getName(), "Verify method status", "Method passed successfully", "Done");
				Reporter.fnCloseHtmlReport("Passed");
				log.info("########################" + Dictionary.get("TEST_NAME").trim() + " EXECUTION PASSED########################");
				if(Dictionary.get("TEST_NAME").trim().toLowerCase().contains("<br")){
					tcp = Dictionary.get("TEST_NAME").trim().split("<[Bb][rR]/?>").length;
				}
				else{
					tcp = 1;
				}
				totalPassedMtds += 1;
				writeToCSV("PASS", tcp, tcf);
			}
			else if(tr.getStatus() == 2){
				Throwable throwable = tr.getThrowable();
				if(throwable != null){
					Dictionary.put("STACKTRACE", Dictionary.get("STACKTRACE") + "<BR/>" + throwable.getMessage());
					if(throwable.getCause() != null){
						Dictionary.put("STACKTRACE", Dictionary.get("STACKTRACE") + "<BR/>" + throwable.getCause().toString());
					}
					StackTraceElement[] trace = throwable.getStackTrace();
					for(int i = 0 ; i < trace.length; i++){
						Dictionary.put("STACKTRACE", Dictionary.get("STACKTRACE") + "<BR/>" + trace[i].toString());
					}
				}
				driver = sDriver.get();
				Reporter.log(tr.getName(), "Check validation", "Some validation was not successfull, please check TestNG reports", "Fail");
				Reporter.fnCloseHtmlReport("Failed");
				log.info("########################" + Dictionary.get("TEST_NAME").trim() + " EXECUTION FAILED########################");
				if(Dictionary.get("TEST_NAME").trim().toLowerCase().contains("<br")){
					tcf = Dictionary.get("TEST_NAME").trim().split("<[Bb][rR]/?>").length;
					if(!Dictionary.get("TC_PASSED_COUNT").trim().equalsIgnoreCase("")){
						int passedCount = Integer.valueOf(Dictionary.get("TC_PASSED_COUNT").trim());
						tcp = passedCount;
						tcf -= passedCount;
					}
				}
				else{
					tcf = 1;
				}
				totalFailedMtds += 1;
				writeToCSV("FAIL", tcp, tcf);
			}
		}
		
		totalPassedTCs += tcp;
		totalFailedTCs += tcf;
		
		Dictionary.remove("TC_PASSED_COUNT");
		
		if(driverType != null && (driverType.trim().toUpperCase().contains("CHROME") || driverType.trim().toUpperCase().contains("API") || (driverType.trim().toUpperCase().contains("ANDROID") && Dictionary.get("TEST_BROWSER").trim().equalsIgnoreCase("true")) || (driverType.trim().toUpperCase().contains("IOS") && Dictionary.get("TEST_BROWSER").trim().equalsIgnoreCase("true")))){
			firstTCFlag.set(false);
		}
		else{
			if(driverType.trim().toUpperCase().contains("ANDROID") && !Dictionary.get("TEST_BROWSER").trim().equalsIgnoreCase("true")){
				if (testData != null && testData.length > 0) {
					String network = Dictionary.get("NETWORK_TYPE");
		            TestParameters testParams = null;
		            for (Object testParameter : testData) {
		                if (testParameter instanceof TestParameters) {
		                    if(((TestParameters)testParameter).getTestMethodName().trim().equalsIgnoreCase(tr.getName().trim())){
		                    	testParams = (TestParameters)testParameter;
		                    	break;
		                    }
		                }
		            }
		            if (testParams != null) {
		                network = testParams.network != null && !testParams.network.trim().equalsIgnoreCase("") ? testParams.network.trim() : network;
		            }
		            if(network.trim().equalsIgnoreCase("2g")){
		            	//Turn to 3G
		            	BaseUtil.setPreferredNetworkType(true);
		            } else if(network.trim().equalsIgnoreCase("airplane")){
		        		String deviceVersion = Dictionary.get(driverType.trim().toUpperCase() + "_VERSION").trim().toLowerCase().substring(0, 1);
		        		int dVersion = !deviceVersion.trim().equalsIgnoreCase("") ? Integer.valueOf(deviceVersion.trim()) : 4;
		        		if(dVersion > 4){
		        			BaseUtil.setAirplaneMode(false);
		        		}
		            }
				}
			}
			if(driver != null){
				driver.quit();
			}
		}
	}
	
	private void writeToCSV(String status, int tcp, int tcf) throws IOException{
		try{
			csvOutput = new CSVWriter(new FileWriter(resultPath, true));
			csvOutput.writeNext(new String[] {driverType, Environment.get("udid"), Dictionary.get("TEST_CLASS_NAME"), Dictionary.get("TEST_NAME").trim(), Dictionary.get("ACTION").trim(), Dictionary.get(driverType.trim().toUpperCase() + "_MANUFACTURER"), Dictionary.get(driverType.trim().toUpperCase() + "_MODEL"), Dictionary.get(driverType.trim().toUpperCase() + "_VERSION"), Dictionary.get(driverType.trim().toUpperCase() + "_OPERATOR"), Dictionary.get("TC_EXEC_TOTAL_DURATION"), System.getProperty("user.name"), Dictionary.get("TC_START_TIME"), status.trim().toUpperCase(), String.valueOf(tcp), String.valueOf(tcf), Dictionary.get("REL_REPORT_NAME"), Dictionary.get("REL_SNAPSHOTS_NAME"), Dictionary.get("REL_LOGS_NAME")});
		}
		finally{
			if(csvOutput != null)
				csvOutput.close();
		}
	}
	
	@AfterClass(alwaysRun = true)
	public void closeTestSummary() throws Exception{
		if(Reporter != null)
			Reporter.fnCloseTestSummary();
		c_EndTime = new Date();
		if(c_StartTime != null && c_EndTime != null){
			String strTimeDifference = fnTimeDiffference(c_StartTime.getTime(), c_EndTime.getTime());
			
			if(System.getProperty("classTo") != null && !System.getProperty("classTo").trim().equalsIgnoreCase("")){
				Environment.put("classTo", System.getProperty("classTo").trim());
				Environment.put("emailNotification", "true");
			}
			
			if(Environment.get("emailNotification").trim().equalsIgnoreCase("true") || Environment.get("emailNotification").trim().equalsIgnoreCase("yes")){
				String subject = Environment.get("env") + " : " + driverType.trim().toUpperCase() + " : " + Dictionary.get("TEST_CLASS_NAME").trim() + " : " + "Execution status";
				duration.put(driverType.trim().toUpperCase() + "_" + Dictionary.get("TEST_CLASS_NAME").trim().toUpperCase() + "_DURATION", strTimeDifference);
				
				if(!Environment.get("classTo").trim().equalsIgnoreCase("") || !Environment.get("classCc").trim().equalsIgnoreCase("") || !Environment.get("classBcc").trim().equalsIgnoreCase(""))
					classDraftReport(subject, strTimeDifference);
			}
		}
	}
	
	private void classDraftReport(String subject, String totalTime) throws Exception{
		if(new File(resultPath).exists()){
			CSVReader reader = null;
			try{
				reader = new CSVReader(new FileReader(resultPath));
				List<String[]> csv = reader.readAll();
				if(csv != null){
					String message = "";
				    message += "<HTML><BODY><FONT FACE=VERDANA COLOR=BLACK SIZE=2>Hi,<BR/><BR/>Please find the information below:</FONT>";
				    message += "<BR/><BR/><FONT FACE=VERDANA COLOR=BLACK SIZE=2>";
			        
			        int row = 1;
			        String sRowColor = "";
			        String sColor = "BLACK";
			        
			        String body = "";
			        body += "<TABLE BORDER=0 CELLPADDING=3 CELLSPACING=1 WIDTH=100% BGCOLOR=BLACK><TR><TD WIDTH=90% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=3><B>" + Environment.get("orgName") + "</B></FONT></TD></TR><TR><TD ALIGN=CENTER BGCOLOR=" + Environment.get("reportColor") + "><FONT FACE=VERDANA COLOR=WHITE SIZE=3><B>Automation Framework Reporting</B></FONT></TD></TR></TABLE><BR/>";
			        body += "<TABLE  CELLPADDING=3 CELLSPACING=1 WIDTH=100%>";           
			        body += "<TR COLS=6 BGCOLOR=" + Environment.get("reportColor") + "><TD WIDTH=5%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>S. No.</B></FONT></TD><TD  WIDTH=60%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Test Case Name</B></FONT></TD><TD  WIDTH=5%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Status</B></FONT></TD><TD  WIDTH=15%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Duration</B></FONT></TD><TD WIDTH=15%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Execution Start Time</B></FONT></TD></TR>";
					List<String> newFiles = new ArrayList<String>();
					int countF = 0;
					int countP = 0;
					int mCountF = 0;
					int mCountP = 0;
					String manufacturer = "";
					String model = "";
					String version = "";
					String operator = "";
					for(int i = 0 ; i < csv.size(); i++){
						String[] data = csv.get(i);
						if(data[0].trim().equalsIgnoreCase(driverType) && data[2].trim().equalsIgnoreCase(Dictionary.get("TEST_CLASS_NAME").trim())){
							String status = data[12];
							String testCaseName = data[3];
							String duration = data[9];
							String startTime = data[11];
							int tcPassed = Integer.valueOf(data[13]);
							int tcFailed = Integer.valueOf(data[14]);
							String reportPath = data[15];
							String snapshotPath = data[16];
							String logPath = data[17];
							manufacturer = data[5];
							model = data[6];
							version = data[7];
							operator = data[8];
							if (row % 2 == 0) {
								sRowColor = "#EEEEEE";
				      		} else {
					      		sRowColor = "#D3D3D3";
				      		}
							
							if(status.trim().contains("FAIL")){
								sColor = "RED";	
								mCountF += 1;
								newFiles.add(reportPath);
								newFiles.add(snapshotPath);
								newFiles.add(logPath);
							}
							else{
								sColor = "GREEN";
								mCountP += 1;
							}
							countP += tcPassed;
							countF += tcFailed;
							body += "<TR COLS=6 BGCOLOR=" + sRowColor + "><TD WIDTH=5%><FONT FACE=VERDANA COLOR=BLACK SIZE=2>" + row + "</FONT></TD><TD  WIDTH=60%><FONT FACE=VERDANA COLOR=BLACK SIZE=2>" + testCaseName + "</FONT></TD><TD  WIDTH=5%><FONT FACE=VERDANA COLOR=" + sColor + " SIZE=2><B>" + status + "</B></FONT></TD><TD WIDTH=15%><FONT FACE=VERDANA COLOR=BLACK SIZE=2>" + duration + "</FONT></TD><TD WIDTH=15%><FONT FACE=VERDANA COLOR=BLACK SIZE=2>" + startTime + "</FONT></TD></TR>";
					        row = row + 1;
						}
					}
					body += "</TABLE>";
					message += "Total execution time taken : " + totalTime + "<Br/>";
					message += "Environment : " + Environment.get("env") + "<Br/>";
					message += "Total test cases passed : " + countP + "<Br/>";
					message += "Total test cases failed : " + countF + "<Br/>";
					message += "Total methods passed : " + mCountP + "<Br/>";
					message += "Total methods failed : " + mCountF + "<Br/>";
					message += "Device on which test cases were run : " + manufacturer + " " + model + " (" + version + ") - " + operator + "<Br/><Br/>";
			        message += body;
			        message += "<FONT FACE=VERDANA COLOR=BLACK SIZE=2><BR/><BR/><I>Note: Please refer attached file for failed test cases</I><BR/><BR/><BR/>Thanks & Regards,<BR/>" + Environment.get("orgName") + " Automation Team</FONT></BODY></HTML>";
			        
			        if(mCountP + mCountF > 0)
			        	SendMail.sendMail(Environment.get("classTo"), Environment.get("classCc"), Environment.get("classBcc"), subject, message, ReportFilePath + OSValidator.delimiter + "FailedReports_" + driverType.trim() + "_" + Dictionary.get("TEST_CLASS_NAME").trim().replace(".", "_") + ".zip", newFiles, Boolean.valueOf(Environment.get("attachSSInEmail")), true);
				}
			}
			catch(Exception ex){
				throw ex;
			}
			finally{
				if(reader != null)
					reader.close();
			}
		}
	}
	
	private void suiteDraftReport(String subject, String totalTime) throws Exception{
		if(new File(resultPath).exists()){
			CSVReader reader = null;
			try{
				reader = new CSVReader(new FileReader(resultPath));
				List<String[]> csv = reader.readAll();
				if(csv != null){
					String message = "";
				    message += "<HTML><BODY><FONT FACE=VERDANA COLOR=BLACK SIZE=2>Hi,<BR/><BR/>Please find the information below:</FONT>";
				    message += "<BR/><BR/><FONT FACE=VERDANA COLOR=BLACK SIZE=2>";
			        
			        int row = 1;
			        String sRowColor = "";
			        String sColor = "BLACK";
			        
			        String body = "";
			        body += "<TABLE BORDER=0 CELLPADDING=3 CELLSPACING=1 WIDTH=100% BGCOLOR=BLACK><TR><TD WIDTH=90% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=3><B>" + Environment.get("orgName") + "</B></FONT></TD></TR><TR><TD ALIGN=CENTER BGCOLOR=" + Environment.get("reportColor") + "><FONT FACE=VERDANA COLOR=WHITE SIZE=3><B>Automation Framework Reporting</B></FONT></TD></TR></TABLE><BR/>";
			        body += "<TABLE  CELLPADDING=3 CELLSPACING=1 WIDTH=100%>";           
			        body += "<TR COLS=6 BGCOLOR=" + Environment.get("reportColor") + "><TD WIDTH=5%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>S. No.</B></FONT></TD><TD WIDTH=40%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Class Name</B></FONT></TD><TD  WIDTH=8%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>TC Passed</B></FONT></TD><TD  WIDTH=8%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>TC Failed</B></FONT></TD><TD  WIDTH=8%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>TM Passed</B></FONT></TD><TD  WIDTH=8%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>TM Failed</B></FONT></TD><TD  WIDTH=8%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Duration</B></FONT></TD><TD WIDTH=15%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Device Details</B></FONT></TD></TR>";
					String manufacturer = "";
					String model = "";
					String version = "";
					String operator = "";
					int count = 0;
					HashMapNew temp = new HashMapNew();
					for(int i = 0 ; i < csv.size(); i++){
						String[] data = csv.get(i);
						String status = data[12];
						String kCount = "COUNT";
						String kDeviceDetails = data[0].trim().toUpperCase() + "_" + data[2].trim().toUpperCase() + "_" + "DEVICE_DETAILS";
						if(!temp.containsKey(kDeviceDetails)){
							temp.put(kCount, String.valueOf(++count));
							temp.put("INDEX_" + count, data[0].trim().toUpperCase() + "_" + data[2].trim().toUpperCase() + "_");
							temp.put("INDEX_CLASSNAME_" + count, data[2].trim());
						}
						
						String kTCPassed = data[0].trim().toUpperCase() + "_" + data[2].trim().toUpperCase() + "_" + "TC_PASSED";
						String kTCFailed = data[0].trim().toUpperCase() + "_" + data[2].trim().toUpperCase() + "_" + "TC_FAILED";
						String kMTDPassed = data[0].trim().toUpperCase() + "_" + data[2].trim().toUpperCase() + "_" + "MTD_PASSED";
						String kMTDFailed = data[0].trim().toUpperCase() + "_" + data[2].trim().toUpperCase() + "_" + "MTD_FAILED";
						String kDuration = data[0].trim().toUpperCase() + "_" + data[2].trim().toUpperCase() + "_" + "DURATION";
						
						temp.put(kTCPassed, temp.get(kTCPassed).trim().equalsIgnoreCase("") ? String.valueOf(Integer.valueOf(data[13])) : String.valueOf(Integer.valueOf(temp.get(kTCPassed).trim()) + Integer.valueOf(data[13])));
						temp.put(kTCFailed, temp.get(kTCFailed).trim().equalsIgnoreCase("") ? String.valueOf(Integer.valueOf(data[14])) : String.valueOf(Integer.valueOf(temp.get(kTCFailed).trim()) + Integer.valueOf(data[14])));
						if(status.trim().contains("FAIL")){
							temp.put(kMTDFailed, temp.get(kMTDFailed).trim().equalsIgnoreCase("") ? "1" : String.valueOf(Integer.valueOf(temp.get(kMTDFailed).trim()) + 1));
						}
						else{
							temp.put(kMTDPassed, temp.get(kMTDPassed).trim().equalsIgnoreCase("") ? "1" : String.valueOf(Integer.valueOf(temp.get(kMTDPassed).trim()) + 1));
						}
						
						temp.put(kDuration, duration.get(kDuration));
						manufacturer = data[5];
						model = data[6];
						version = data[7];
						operator = data[8];
						temp.put(kDeviceDetails, manufacturer + " " + model + " (" + version + ") - " + operator);
					}
					
					for(int i = 0 ; i < Integer.valueOf(temp.get("COUNT").trim()); i++){
						if (row % 2 == 0) {	
							sRowColor = "#EEEEEE";
			      		} else {
				      		sRowColor = "#D3D3D3";
			      		}
						String className = temp.get("INDEX_CLASSNAME_" + (i + 1));
						String parentKey = temp.get("INDEX_" + (i + 1));
						String TCPASSED = temp.get(parentKey + "TC_PASSED");
						String TCFAILED = temp.get(parentKey + "TC_FAILED");
						String MTDPASSED = temp.get(parentKey + "MTD_PASSED").trim().equalsIgnoreCase("") ? "0" : temp.get(parentKey + "MTD_PASSED").trim();
						String MTDFAILED = temp.get(parentKey + "MTD_FAILED").trim().equalsIgnoreCase("") ? "0" : temp.get(parentKey + "MTD_FAILED").trim();
						String DURATION = temp.get(parentKey + "DURATION");
						String DEVICE_DETAILS = temp.get(parentKey + "DEVICE_DETAILS");
								
						body += "<TR COLS=6 BGCOLOR=" + sRowColor + "><TD WIDTH=5%><FONT FACE=VERDANA COLOR=BLACK SIZE=2>" + row + "</FONT></TD><TD  WIDTH=40%><FONT FACE=VERDANA COLOR=BLACK SIZE=2>" + className + "</FONT></TD><TD  WIDTH=8%><FONT FACE=VERDANA COLOR=" + sColor + " SIZE=2><B>" + TCPASSED + "</B></FONT></TD><TD  WIDTH=8%><FONT FACE=VERDANA COLOR=" + sColor + " SIZE=2><B>" + TCFAILED + "</B></FONT></TD><TD  WIDTH=8%><FONT FACE=VERDANA COLOR=" + sColor + " SIZE=2><B>" + MTDPASSED + "</B></FONT></TD><TD  WIDTH=8%><FONT FACE=VERDANA COLOR=" + sColor + " SIZE=2><B>" + MTDFAILED + "</B></FONT></TD><TD WIDTH=8%><FONT FACE=VERDANA COLOR=BLACK SIZE=2>" + DURATION + "</FONT></TD><TD WIDTH=15%><FONT FACE=VERDANA COLOR=BLACK SIZE=2>" + DEVICE_DETAILS + "</FONT></TD></TR>";
				        row = row + 1;
					}
					body += "</TABLE>";
					
					message += "Total execution time taken : " + totalTime + "<Br/>";
					message += "Environment : " + Environment.get("env") + "<Br/>";
					message += "Total test cases passed : " + totalPassedTCs + "<Br/>";
					message += "Total test cases failed : " + totalFailedTCs + "<Br/>";
					message += "Total methods passed : " + totalPassedMtds + "<Br/>";
					message += "Total methods failed : " + totalFailedMtds + "<Br/><Br/>";
			        message += body;
			        message += "<FONT FACE=VERDANA COLOR=BLACK SIZE=2><BR/><BR/><BR/>Thanks & Regards,<BR/>" + Environment.get("orgName") + " Automation Team</FONT></BODY></HTML>";
			        //target/surefire-reports/emailable-report.html
			        if(!temp.get("COUNT").trim().equalsIgnoreCase("")){
//			        	String emailableReport = ReportFilePath + OSValidator.delimiter + "SurefireReports.zip";
			        	SendMail.sendMail(Environment.get("suiteTo"), Environment.get("suiteCc"), Environment.get("suiteBcc"), subject, message, "", null /*Arrays.asList("target/surefire-reports/emailable-report.html")*/, Boolean.valueOf(Environment.get("attachSSInEmail")), false);
			        }
				}
			}
			catch(Exception ex){
				throw ex;
			}
			finally{
				if(reader != null)
					reader.close();
			}
		}
	}
	
	@AfterTest(alwaysRun = true)
	public void exitTest() throws Exception{
		if(driverType != null && (driverType.trim().toUpperCase().contains("CHROME") || driverType.trim().toUpperCase().contains("API") || (driverType.trim().toUpperCase().contains("ANDROID") && Dictionary.get("TEST_BROWSER").trim().equalsIgnoreCase("true")) || (driverType.trim().toUpperCase().contains("IOS") && Dictionary.get("TEST_BROWSER").trim().equalsIgnoreCase("true")))){
			if(driverType.trim().toUpperCase().contains("ANDROID") && Dictionary.get("TEST_BROWSER").trim().equalsIgnoreCase("true")){
				String network = Dictionary.get("NETWORK_TYPE");
	            if(network.trim().equalsIgnoreCase("2g")){
	            	//Turn to 3G
	            	BaseUtil.switchToContext("NATIVE_APP");
	            	BaseUtil.setPreferredNetworkType(true);
	            	BaseUtil.switchToContext("WEBVIEW_1");
	            }
			}
			if(driver != null){
				driver.quit();
			}
		}
	}
	
	@AfterSuite(alwaysRun=true)
	public void tearDown() throws Exception {
		Driver.g_EndTime = new Date();
		if(Driver.g_StartTime != null && Driver.g_EndTime != null){
			String strTimeDifference = fnTimeDiffference(Driver.g_StartTime.getTime(), Driver.g_EndTime.getTime());
			log.info("Total suite execution time : " + strTimeDifference);
			log.info("Total passed test cases : " + totalPassedTCs);
			log.info("Total failed test cases : " + totalFailedTCs);
			log.info("Total passed methods : " + totalPassedMtds);
			log.info("Total failed methods : " + totalFailedMtds);
			
			if(System.getProperty("suiteTo") != null && !System.getProperty("suiteTo").trim().equalsIgnoreCase("")){
				Environment.put("suiteTo", System.getProperty("suiteTo").trim());
				Environment.put("emailNotification", "true");
			}
			
			if(Environment.get("emailNotification").trim().equalsIgnoreCase("true") || Environment.get("emailNotification").trim().equalsIgnoreCase("yes")){
				String subject = "Test suite execution report";
				
				if(!Environment.get("suiteTo").trim().equalsIgnoreCase("") || !Environment.get("suiteCc").trim().equalsIgnoreCase("") || !Environment.get("suiteBcc").trim().equalsIgnoreCase(""))
					suiteDraftReport(subject, strTimeDifference);
			}
		}
		
		if(Environment.get("screenshotsFolder").trim().equalsIgnoreCase("")){
			Environment.put("screenshotsFolder", "/resources/Screenshots/");
		}
		
		String userdir = "";
		if(!Environment.get("user").trim().equalsIgnoreCase("")){
			userdir = System.getProperty("user.dir").replace(System.getProperty("user.name"), Environment.get("user").trim());
		}
		else
			userdir = System.getProperty("user.dir");
	
		File screenshotDir = new File(new File(userdir), Environment.get("screenshotsFolder").trim());
		if (screenshotDir.exists() && screenshotDir.isDirectory()) {
			FileUtils.cleanDirectory(screenshotDir);
		}
	}
		
	public String fnTimeDiffference(long startTime, long endTime) {
		long delta = endTime - startTime;
		int days = (int)delta / 86400000;
		delta = (int)delta % 86400000;
		int hrs = (int)delta / 3600000;
		delta = (int)delta % 3600000;
		int min = (int)delta / 60000;
		delta = (int)delta % 60000;
		int sec = (int)delta / 1000;
		
		String strTimeDifference = days + "d " + hrs + "h " + min + "m " + sec + "s";
		return strTimeDifference;
	}
	
	public class TestParameters {
	    private String testName = null;
	    private String methodName = null;
	    private String network = null;
	    private boolean autoLaunch = true;
	    private boolean noReset = true;
	    private String app = null;
	    private String appPackage = null;
	    private String appActivity = null;
	    private String appWaitActivity = null;
	    private List<String> lstParams;
	    private boolean testBrowser = Boolean.valueOf(Dictionary.get("TEST_BROWSER"));
	    private String automationName = Dictionary.get("AUTOMATION_NAME").trim();
	    private String version = System.getProperty("appVersion") != null && !System.getProperty("appVersion").trim().equalsIgnoreCase("") ? System.getProperty("appVersion").trim() : Environment.get("appVersion").trim();

	    public TestParameters(String name, String methodName, String network, boolean autoLaunch, boolean noReset, String app, String appPackage, String appActivity, String appWaitActivity, ArrayList<String> lstParams, String... others) {
	        this.testName = name;
	        this.methodName = methodName;
	        this.network = network;
	        this.autoLaunch = autoLaunch;
	        this.noReset = noReset;
	        this.app = app;
	        this.appPackage = appPackage;
	        this.appActivity = appActivity;
	        this.lstParams = lstParams;
	        this.appWaitActivity = appWaitActivity;
	        if(others.length > 0){
	        	this.testBrowser = Boolean.valueOf(others[0]);
	        	if(others.length > 1){
	        		this.automationName = others[1];
	        		if(others.length > 2){
	        			this.version = others[2];
	        		}
	        	}
	        }
	    }

	    public String getTestName() {
	        return testName;
	    }
	    public String getTestMethodName() {
	        return methodName;
	    }
	    public String getNetworkMode() {
	    	return network;
	    }
	    public boolean getAutoLaunchValue() {
	    	return autoLaunch;
	    }
	    public boolean getNoResetValue() {
	    	return noReset;
	    }
	    public String getApkName(){
	    	return app;
	    }
	    public String getAppPackage(){
	    	return appPackage;
	    }
	    public String getAppActivity(){
	    	return appActivity;
	    }
	    public String getAppWaitActivity(){
	    	return appWaitActivity;
	    }
	    public List<String> getParametersList(){
	    	return lstParams;
	    }
	    public boolean getTestBrowser(){
	    	return testBrowser;
	    }
	    public String getAutomationName(){
	    	return automationName;
	    }
	    public String getVersion(){
	    	return version;
	    }
	}
	
	public static class HashMapNew extends HashMap<String, String>{
		static final long serialVersionUID = 1L;
    
		public String get(Object key){
			String value = (String)super.get(key);
			if (value == null) {
				return "";
			}
			return value;
		}
	}

	@Override
	public void run() {
	}
	
	public HashMapNew GetXMLNodeValue(String path, String parentNode, int index){
		HashMapNew dict = new HashMapNew();
	    String RootPath = System.getProperty("user.dir");
	    try
	    {
	      String xmlPath = RootPath + path;
	      File fXmlFile = new File(xmlPath);
	      
	      if(!fXmlFile.exists())
	    	  return dict;
	      
	      DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
	      DocumentBuilder docBuilder = dbFac.newDocumentBuilder();
	      Document xmldoc = docBuilder.parse(fXmlFile);
	      
	      XPathFactory xPathfac = XPathFactory.newInstance();
	      XPath xpath = xPathfac.newXPath();

	      XPathExpression expr = xpath.compile(parentNode);
	      Object obj = expr.evaluate(xmldoc, XPathConstants.NODESET);
	      if(obj != null){
	    	  Node node = ((NodeList)obj).item(index);
	    	  if(node != null){
			      NodeList nl = node.getChildNodes();
			      for (int child = 0; child < nl.getLength(); child++) {
			    	  dict.put(nl.item(child).getNodeName(), nl.item(child).getTextContent());
			      }
	    	  }
	      }
	    }
	    catch (Exception excep){
	    	log.info("Threw a Exception in Driver::GetXMLNodeValue, full stack trace follows:", excep);
	    }
	    
	    return dict;
	}
	
	public void networkSetUp(String network, boolean autoLaunch, boolean testBrowser, String automationName) throws Exception{
		BaseUtil = new BaseUtil(driver, driverType, Dictionary, Environment, Reporter, Assert, SoftAssert);
		if(driverType.trim().toUpperCase().contains("ANDROID") && Dictionary.get("TEST_BROWSER").trim().equalsIgnoreCase("true")){
			BaseUtil.switchToContext("NATIVE_APP");
    		String output = "";
			if(network.trim().equalsIgnoreCase("wifi") || network.trim().equalsIgnoreCase("wi-fi")){
				//Check wifi is ON or OFF
				output = BaseUtil.getNetworkIPAddress(true);
				if(output.trim().equalsIgnoreCase("")){
					networkToggle(Arrays.asList("wifi", "data", "airplane"), Arrays.asList(true, false, false));
					//Check if Wifi is up or not
					String wifiIp = "";
					String dataIp = "";
					int counter = 10;
					do{
		    			wifiIp = BaseUtil.getNetworkIPAddress(true);
		    			dataIp = BaseUtil.getNetworkIPAddress(false);
		            	counter--;
					}while(wifiIp.trim().equalsIgnoreCase("") && !dataIp.trim().equalsIgnoreCase("") && counter > 0);
				}
			}
        	else if(!network.trim().equalsIgnoreCase("") && !network.trim().equalsIgnoreCase("airplane")){
        		//Check data is ON or OFF
            	output = BaseUtil.getNetworkIPAddress(false);
        		if(output.trim().equalsIgnoreCase("")){
        			networkToggle(Arrays.asList("wifi", "data", "airplane"), Arrays.asList(false, true, false));
        			//Check if Wifi is up or not
        			String wifiIp = "";
					String dataIp = "";
        			int counter = 10;
        			do{
            			wifiIp = BaseUtil.getNetworkIPAddress(true);
            			dataIp = BaseUtil.getNetworkIPAddress(false);
                    	counter--;
        			}while(!wifiIp.trim().equalsIgnoreCase("") && dataIp.trim().equalsIgnoreCase("") && counter > 0);
        		}
        	}
			if(network.trim().equalsIgnoreCase("3g")){
				BaseUtil.setPreferredNetworkType(true);
        	}
        	else if(network.trim().equalsIgnoreCase("2g")){
        		BaseUtil.setPreferredNetworkType(false);
        	}
			Dictionary.put("NETWORK_TYPE", network);
			BaseUtil.switchToContext("WEBVIEW_1");
		} 
		else if(Environment.get("browserTest").trim().equalsIgnoreCase("true") || Environment.get("browserTest").trim().equalsIgnoreCase("Y") || Environment.get("browserTest").trim().equalsIgnoreCase("Yes") || testBrowser || Environment.get("appPackage").trim().toLowerCase().contains("chrome") || Environment.get("app").trim().toLowerCase().contains("chrome") || Environment.get("appActivity").trim().toLowerCase().contains("chrome")){ 
			//Do Nothing
        }
        else{
	        if(!network.trim().equalsIgnoreCase("")){
        		String output = "";
				if(network.trim().equalsIgnoreCase("wifi") || network.trim().equalsIgnoreCase("wi-fi")){
					//Check wifi is ON or OFF
					output = BaseUtil.getNetworkIPAddress(true);
					if(output.trim().equalsIgnoreCase("")){
						networkToggle(Arrays.asList("wifi", "data", "airplane"), Arrays.asList(true, false, false));
						//Check if Wifi is up or not
						String wifiIp = "";
						String dataIp = "";
						int counter = 10;
						do{
			    			wifiIp = BaseUtil.getNetworkIPAddress(true);
			    			dataIp = BaseUtil.getNetworkIPAddress(false);
			            	counter--;
						}while(wifiIp.trim().equalsIgnoreCase("") && !dataIp.trim().equalsIgnoreCase("") && counter > 0);
					}
				}
	        	else if(!network.trim().equalsIgnoreCase("") && !network.trim().equalsIgnoreCase("airplane")){
	        		//Check data is ON or OFF
	            	output = BaseUtil.getNetworkIPAddress(false);
	        		if(output.trim().equalsIgnoreCase("")){
	        			networkToggle(Arrays.asList("wifi", "data", "airplane"), Arrays.asList(false, true, false));
	        			//Check if Wifi is up or not
	        			String wifiIp = "";
						String dataIp = "";
	        			int counter = 10;
	        			do{
	            			wifiIp = BaseUtil.getNetworkIPAddress(true);
	            			dataIp = BaseUtil.getNetworkIPAddress(false);
	                    	counter--;
	        			}while(!wifiIp.trim().equalsIgnoreCase("") && dataIp.trim().equalsIgnoreCase("") && counter > 0);
	        		}
	        	}
	        	else if(network.trim().equalsIgnoreCase("airplane")){
        			networkToggle(Arrays.asList("wifi", "data", "airplane"), Arrays.asList(false, false, true));
	        	}
        	}
	        if(autoLaunch){
        		try{
        			((AndroidDriver<?>)driver).launchApp();
        			if(network.trim().equalsIgnoreCase("3g")){
        				BaseUtil.setPreferredNetworkType(true);
		        	}
		        	else if(network.trim().equalsIgnoreCase("2g")){
		        		BaseUtil.setPreferredNetworkType(false);
		        	}
		        	else if(network.trim().equalsIgnoreCase("airplane")){
		        		String deviceVersion = Dictionary.get(driverType.trim().toUpperCase() + "_VERSION").trim().toLowerCase().substring(0, 1);
		        		int dVersion = !deviceVersion.trim().equalsIgnoreCase("") ? Integer.valueOf(deviceVersion.trim()) : 4;
		        		if(dVersion > 4){
		        			BaseUtil.setAirplaneMode(true);
		        		}
		        	}
        		}
        		catch(Exception ex){
        			ex.printStackTrace();
        		}
	        }
        }
	}
	
	private String checkAppVersion(String packageName) throws Exception{
		String output = BaseUtil.runADBCommand(new String[]{"-s", Environment.get("udid"), "shell", "dumpsys", "package", packageName, "|", "grep", "versionName"}, new Boolean[]{false, false, false, false, false, false, false, false, false}, 2000);
		String versionName = "";
		if(!output.trim().equalsIgnoreCase("")){
			output = output.trim().split("\n")[0].trim();
			if(output.contains("="))
				versionName = output.split("=")[1].trim();
		}
		return versionName;
	}
	
	private String[] getDeviceDetails(String udid) throws Exception{
		String model, manufacturer, operator, version; 
		if(driverType.trim().toUpperCase().contains("IOS")){
			model = BaseUtil.getIOSDeviceDetails("DeviceName", udid);
			manufacturer = BaseUtil.getIOSDeviceDetails("DeviceClass", udid);
			operator = "airtel";
			version = BaseUtil.getIOSDeviceDetails("ProductVersion", udid);
		}
		else{
			model = BaseUtil.runADBCommand(new String[]{"-s", udid, "shell", "getprop", "ro.product.model"}, new Boolean[]{false, false, false, false, false}, 2000);
			manufacturer = BaseUtil.runADBCommand(new String[]{"-s", udid, "shell", "getprop", "ro.product.manufacturer"}, new Boolean[]{false, false, false, false, false}, 2000);
			operator = BaseUtil.runADBCommand(new String[]{"-s", udid, "shell", "getprop", "gsm.sim.operator.alpha"}, new Boolean[]{false, false, false, false, false}, 2000);
			version = BaseUtil.runADBCommand(new String[]{"-s", udid, "shell", "getprop", "ro.build.version.release"}, new Boolean[]{false, false, false, false, false}, 2000);
			model = model.trim().split("\n")[0];
			manufacturer = manufacturer.trim().split("\n")[0];
			operator = operator.trim().split("\n")[0];
			version = version.trim().split("\n")[0];
		}
		
		return new String[]{operator, version, manufacturer, model};
	}
	
	private boolean isAppInstalled(String packageName) throws Exception{
		String output = BaseUtil.runADBCommand(new String[]{"-s", Environment.get("udid").trim(), "shell", "pm", "list", "packages", "|", "grep", packageName}, new Boolean[]{false, false, false, false, false, false, false, false, false}, 2000);
		return output.trim().contains("package:" + packageName);
	}
	
	private void install(String appPath) throws Exception{
		BaseUtil.runADBCommand(new String[]{"-s", Environment.get("udid"), "install", "-r", appPath}, new Boolean[]{false, false, false, false, false}, 6000, "Success");
	}
	
	private void installAppFromResources(String appName) throws Exception{
		File fappName = null;
		if(!Environment.get("apkFolder").trim().equalsIgnoreCase("")){
			fappName = new File(Environment.get("apkFolder").trim(), appName);
			if(!fappName.exists()){
				BaseUtil.downloadFromS3("QA-Builds/apps/", Environment.get("apkFolder").trim(), appName);
			}
		}
		else{
			String userdir = "";
			if(!Environment.get("user").trim().equalsIgnoreCase("")){
				userdir = System.getProperty("user.dir").replace(System.getProperty("user.name"), Environment.get("user").trim());
			}
			else
				userdir = System.getProperty("user.dir");
		
			File appDir = new File(new File(userdir), Environment.get("appsFolder").trim());
			fappName = new File(appDir, appName);
			if(!fappName.exists())
				BaseUtil.downloadFromS3("QA-Builds/apps/", appDir.getAbsolutePath(), appName);
		}
		install(fappName.getAbsolutePath());
	}
	
	private void networkToggle(List<String> networkType, List<Boolean> mode) throws Exception{
		
		//Check if appium settings app is installed in the device
		if(isAppInstalled("io.appium.networktoggle") == false){
			installAppFromResources("network_toggle.apk");
		}
    	
		String arguments = "";
		for(int i = 0 ; i < networkType.size(); i++){
			String network = networkType.get(i).trim();
			String m = mode.get(i) ? "on" : "off";
			arguments += "-e " + network + " " + m + " ";
		}
		
		//Run command
		launchApp("io.appium.networktoggle", "io.appium.networktoggle.MainActivity " + arguments);
	}
	
	public void launchApp(String packageName, String launchActivityName) throws Exception{
		String activityName = launchActivityName.trim().split("-e")[0].trim();
		if(launchActivityName.trim().contains("-e")){
			String[] values = launchActivityName.trim().split(activityName)[1].trim().split(" ");
			List<String> actual = new ArrayList<String>();
			List<Boolean> flag = new ArrayList<Boolean>();
			for(int i = 0 ; i < values.length; i++){
				if(!values[i].trim().equalsIgnoreCase("")){
					actual.add(values[i]);
					flag.add(false);
				}
			}
			BaseUtil.runADBCommand(ArrayUtils.addAll(new String[]{"-s", Environment.get("udid").trim(), "shell", "am", "start", "-S", "-n", packageName + "/" + activityName}, actual.toArray(new String[actual.size()])), ArrayUtils.addAll(new Boolean[]{false, false, false, false, false, false, false, false}, flag.toArray(new Boolean[flag.size()])), 2000);
		}
		else{
			BaseUtil.runADBCommand(new String[]{"-s", Environment.get("udid").trim(), "shell", "am", "start", "-S", "-n", packageName + "/" + activityName}, new Boolean[]{false, false, false, false, false, false, false, false}, 2000);
		}
	}
}