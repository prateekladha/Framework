package com.app.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.framework.Driver.HashMapNew;
import com.app.util.BaseUtil;

@SuppressWarnings("deprecation")
public class Reporting{
		private String g_strTestCaseReport;
		private String g_strSnapshotFolderName;
		private String g_strRelSnapshotFolderName;
		private String g_strLogFolderName;
		private String g_strRelLogFolderName;
		private String g_strScriptName;
		private int g_iSnapshotCount;
		private int g_OperationCount;
		private int g_iPassCount;
		private int g_iFailCount;
		private int g_iTCPassed;
		private int g_iTestCaseNo;
		private Date g_StartTime;
		private Date g_EndTime;
		private Date g_SummaryStartTime;
		private Date g_SummaryEndTime;
		public WebDriver driver;
		private String driverType;
		private HashMapNew Dictionary;
		private HashMapNew Environment;
		private BaseUtil BaseUtil;
		static Logger log = LoggerFactory.getLogger(Reporting.class);
	  
	public Reporting(WebDriver webDriver, String DT, HashMapNew Dict, HashMapNew Env, BaseUtil BaseUtil)
	  {
		this.driver = webDriver;
		this.Dictionary = Dict;
		this.Environment = Env;
		this.driverType = DT;
		this.BaseUtil = BaseUtil;
	  }
	  
	private FileOutputStream foutStrm = null;
	  
	public void fnCreateSummaryReport()
	  {
		this.g_iTCPassed = 0;
		this.g_iTestCaseNo = 0;
		this.g_SummaryStartTime = new Date();
		try
		{
		  this.foutStrm = new FileOutputStream((String)this.Environment.get("HTMLREPORTSPATH") + OSValidator.delimiter + "SummaryReport.html", true);

		  new PrintStream(this.foutStrm).println("<HTML><BODY><TABLE BORDER=0 CELLPADDING=3 CELLSPACING=1 WIDTH=100% BGCOLOR=BLACK>");
		  new PrintStream(this.foutStrm).println("<TR><TD WIDTH=90% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=3><B>" + Environment.get("orgName") + "</B></FONT></TD></TR><TR><TD ALIGN=CENTER BGCOLOR=" + Environment.get("reportColor") + "><FONT FACE=VERDANA COLOR=WHITE SIZE=3><B>Automation Framework Reporting [" + Dictionary.get("TEST_CLASS_NAME") + "]</B><FONT></TD></TR></TABLE><TABLE CELLPADDING=3 WIDTH=100%><TR height=30><TD WIDTH=100% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=//0073C5 SIZE=2><B>&nbsp; Automation Result : " + new Date() + " on Machine " + InetAddress.getLocalHost().getHostName() + " by user " + System.getProperty("user.name") + " on Device " + this.driverType + "</B></FONT></TD></TR><TR HEIGHT=5></TR></TABLE>");
		  new PrintStream(this.foutStrm).println("<TABLE  CELLPADDING=3 CELLSPACING=1 WIDTH=100%>");
		  new PrintStream(this.foutStrm).println("<TR COLS=6 BGCOLOR=" + Environment.get("reportColor") + "><TD WIDTH=10%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>TC No.</B></FONT></TD><TD  WIDTH=60%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Test Name</B></FONT></TD><TD BGCOLOR=" + Environment.get("reportColor") + " WIDTH=15%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Status</B></FONT></TD><TD  WIDTH=15%><FONT FACE=VERDANA COLOR=BLACK SIZE=2><B>Test Duration</B></FONT></TD></TR>");
		  

		  this.foutStrm.close();
		}
		catch (IOException io)
		{
			log.info("Threw a IOException in Reporting::fnCreateSummaryReport, full stack trace follows:", io);
		}
		this.foutStrm = null;
	  }
	  
	public void fnCreateHtmlReport(String strTestName) {
		this.g_OperationCount = 0;
		this.g_iPassCount = 0;
		this.g_iFailCount = 0;
		this.g_iSnapshotCount = 0;
		Date today = new Date();
	    Timestamp now = new Timestamp(today.getTime());
	    String[] tempNow = now.toString().split("\\.");
	    String timeStamp = tempNow[0].replaceAll(":", ".").replaceAll(" ", "T");

		this.g_strScriptName = strTestName + timeStamp;
		Dictionary.put("SCRIPT_NAME", this.g_strScriptName);
		
		this.g_strTestCaseReport = ((String)this.Environment.get("HTMLREPORTSPATH") + OSValidator.delimiter + "Report_" + this.g_strScriptName + ".html");
		Dictionary.put("REPORT_NAME", this.g_strTestCaseReport);
		Dictionary.put("REL_REPORT_NAME", ((String)this.Environment.get("RELHTMLREPORTSPATH") + OSValidator.delimiter + "Report_" + this.g_strScriptName + ".html"));

		this.g_strSnapshotFolderName = ((String)this.Environment.get("SNAPSHOTSFOLDER") + OSValidator.delimiter + this.g_strScriptName);
		this.g_strLogFolderName = ((String)this.Environment.get("LOGSFOLDER") + OSValidator.delimiter + this.g_strScriptName);
		this.g_strRelSnapshotFolderName = ((String)this.Environment.get("RELSNAPSHOTSFOLDER") + OSValidator.delimiter + this.g_strScriptName);
		this.g_strRelLogFolderName = ((String)this.Environment.get("RELLOGSFOLDER") + OSValidator.delimiter + this.g_strScriptName);
		Dictionary.put("REL_SNAPSHOTS_NAME", Environment.get("RELHTMLREPORTSPATH") + OSValidator.delimiter + g_strRelSnapshotFolderName);
		Dictionary.put("REL_LOGS_NAME", Environment.get("RELHTMLREPORTSPATH") + OSValidator.delimiter + g_strRelLogFolderName);
		
		File file = new File(this.g_strSnapshotFolderName);
		if (file.exists()) {
		  file.delete();
		}
		file.mkdir();
		file = new File(this.g_strLogFolderName);
		if (file.exists()) {
		  file.delete();
		}
		file.mkdir();
		try
		{
		  this.foutStrm = new FileOutputStream(this.g_strTestCaseReport);
		}
		catch (FileNotFoundException fe)
		{
			log.info("Threw a FileNotFoundException in Reporting::fnCreateHtmlReport, full stack trace follows:", fe);
		}
		try
		{
		  new PrintStream(this.foutStrm).println("<HTML><BODY><TABLE BORDER=0 CELLPADDING=3 CELLSPACING=1 WIDTH=100% BGCOLOR=" + Environment.get("reportColor") + ">");
		  new PrintStream(this.foutStrm).println("<TR><TD WIDTH=90% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=3><B>" + Environment.get("orgName") + "</B></FONT></TD></TR><TR><TD ALIGN=CENTER BGCOLOR=" + Environment.get("reportColor") + "><FONT FACE=VERDANA COLOR=WHITE SIZE=3><B>Automation Framework Reporting [" + Dictionary.get("TEST_CLASS_NAME") + "]</B></FONT></TD></TR></TABLE><TABLE CELLPADDING=3 WIDTH=100%><TR height=30><TD WIDTH=100% ALIGN=CENTER BGCOLOR=WHITE><FONT FACE=VERDANA COLOR=//0073C5 SIZE=2><B>&nbsp; Automation Result : " + new Date() + " on Machine " + InetAddress.getLocalHost().getHostName() + " by user " + System.getProperty("user.name") + " on Device " + this.driverType + "</B></FONT></TD></TR>");
		  new PrintStream(this.foutStrm).println("<TR HEIGHT=5></TR></TABLE>");
		  new PrintStream(this.foutStrm).println("<TABLE BORDER=0 BORDERCOLOR=WHITE CELLPADDING=3 CELLSPACING=1 WIDTH=100%>");
		  new PrintStream(this.foutStrm).println("<TR><TD BGCOLOR=BLACK WIDTH=20%><FONT FACE=VERDANA COLOR=WHITE SIZE=2><B>Test Name:</B></FONT></TD><TD COLSPAN=6 BGCOLOR=BLACK><FONT FACE=VERDANA COLOR=WHITE SIZE=2><B>" + Dictionary.get("TEST_NAME").trim() + "</B></FONT></TD></TR>");
		  
		  new PrintStream(this.foutStrm).println("</TABLE><BR/><TABLE WIDTH=100% CELLPADDING=3>");
		  new PrintStream(this.foutStrm).println("<TR WIDTH=100%><TH BGCOLOR=" + Environment.get("reportColor") + " WIDTH=5%><FONT FACE=VERDANA SIZE=2>Step No.</FONT></TH><TH BGCOLOR=" + Environment.get("reportColor") + " WIDTH=28%><FONT FACE=VERDANA SIZE=2>Step Description</FONT></TH><TH BGCOLOR=" + Environment.get("reportColor") + " WIDTH=25%><FONT FACE=VERDANA SIZE=2>Expected Value</FONT></TH><TH BGCOLOR=" + Environment.get("reportColor") + " WIDTH=25%><FONT FACE=VERDANA SIZE=2>Obtained Value</FONT></TH><TH BGCOLOR=" + Environment.get("reportColor") + " WIDTH=7%><FONT FACE=VERDANA SIZE=2>Result</FONT></TH></TR>");
		  
		  this.foutStrm.close();
		}
		catch (IOException io)
		{
			log.info("Threw a IOException in Reporting::fnCreateHtmlReport, full stack trace follows:", io);
		}
		this.foutStrm = null;
		this.g_StartTime = new Date();
	  }
	  
	public void fnWriteTestSummary(String strTestCaseName, String strResult, String strDuration)
	  {
		try
		{
		  this.foutStrm = new FileOutputStream((String)this.Environment.get("HTMLREPORTSPATH") + OSValidator.delimiter + "SummaryReport.html", true);
		  String sColor;
		  if ((strResult.toUpperCase().equals("PASSED")) || (strResult.toUpperCase().equals("PASS")))
		  {
			sColor = "GREEN";
			this.g_iTCPassed += 1;
		  }
		  else
		  {        
			if ((strResult.toUpperCase().equals("FAILED")) || (strResult.toUpperCase().equals("FAIL"))) {
			  sColor = "RED";
			} else {
			  sColor = "" + Environment.get("reportColor") + "";
			}
		  }
		  this.g_iTestCaseNo += 1;
		  String sRowColor;
		  
		  if (this.g_iTestCaseNo % 2 == 0) {
			sRowColor = "#EEEEEE";
		  } else {
			sRowColor = "#D3D3D3";
		  }
		  new PrintStream(this.foutStrm).println("<TR COLS=3 BGCOLOR=" + sRowColor + "><TD  WIDTH=10%><FONT FACE=VERDANA SIZE=2>" + this.g_iTestCaseNo + "</FONT></TD><TD  WIDTH=60%><FONT FACE=VERDANA SIZE=2>" + strTestCaseName + "</FONT></TD><TD  WIDTH=15%><A HREF='" + "Report_" + Dictionary.get("SCRIPT_NAME").trim() + ".html'><FONT FACE=VERDANA SIZE=2 COLOR=" + sColor + "><B>" + strResult + "</B></FONT></A></TD><TD  WIDTH=15%><FONT FACE=VERDANA SIZE=2>" + strDuration + "</FONT></TD></TR>");
		
		  this.foutStrm.close();
		}
		catch (IOException io)
		{
			log.info("Threw a IOException in Reporting::fnWriteTestSummary, full stack trace follows:", io);
		}
		this.foutStrm = null;
	  }
	  
	public void fnCloseHtmlReport(String status) throws Exception
	  {
		String strTestCaseResult = null;
		try
		{
		  this.foutStrm = new FileOutputStream(this.g_strTestCaseReport, true);
		}
		catch (FileNotFoundException fe)
		{
			log.info("Threw a FileNotFoundException in Reporting::fnCloseHtmlReport, full stack trace follows:", fe);
		}
		this.g_EndTime = new Date();
		
		Timestamp now = new Timestamp(this.g_StartTime.getTime());
		String[] tempNow = now.toString().split("\\.");
		String timeStamp = tempNow[0].replaceAll(" ", "T");
		
		Dictionary.put("TC_START_TIME", timeStamp.split("T")[1]);
		String strTimeDifference = fnTimeDiffference(this.g_StartTime.getTime(), this.g_EndTime.getTime());
		Dictionary.put("TC_EXEC_TOTAL_DURATION", strTimeDifference);
		try
		{
		  new PrintStream(this.foutStrm).println("<TR></TR><TR><TD BGCOLOR=BLACK WIDTH=5%></TD><TD BGCOLOR=BLACK WIDTH=28%><FONT FACE=VERDANA COLOR=WHITE SIZE=2><B>Time Taken : " + strTimeDifference + "</B></FONT></TD><TD BGCOLOR=BLACK WIDTH=25%><FONT FACE=VERDANA COLOR=WHITE SIZE=2><B>Pass Count : " + this.g_iPassCount + "</B></FONT></TD><TD BGCOLOR=BLACK WIDTH=25%><FONT FACE=VERDANA COLOR=WHITE SIZE=2><B>Fail Count : " + this.g_iFailCount + "</b></FONT></TD><TD BGCOLOR=Black WIDTH=7%></TD></TR></TABLE><TABLE WIDTH=100%>");
		  if (status.equals("Failed")) {
			  if(driverType.trim().toUpperCase().contains("ANDROID") && !Dictionary.get("TEST_BROWSER").trim().equalsIgnoreCase("true") && !Environment.get("appPackage").trim().equalsIgnoreCase("")){
				  String logcatLogFilePath = this.g_strLogFolderName + OSValidator.delimiter + "logcat.txt";
				  String relLogcatLogFilePath = this.g_strRelLogFolderName + OSValidator.delimiter + "logcat.txt";
				  String output = BaseUtil.runADBCommand(new String[]{"-s", Environment.get("udid"), "-d", "shell", "logcat", Environment.get("appPackage")}, new Boolean[]{false, false, false, false, false, false}, 2000, "WAIT");
				  try{
					  Runtime.getRuntime().exec("ps -ef| grep -i 'adb -s " + Environment.get("udid") + " -d shell logcat " + Environment.get("appPackage") + "' | grep -v grep | awk '{print $2}' | xargs kill -9");
				  }
				  catch(Exception ex){
					  //Do Nothing
				  }
				  if(output != null && !output.trim().equalsIgnoreCase("")){
					  String logcat = output;
					  FileOutputStream fout = new FileOutputStream(logcatLogFilePath, true);
					  new PrintStream(fout).println(logcat);
					  fout.close();
					  fout = null;
					  new PrintStream(this.foutStrm).println("<TR><TD ALIGN=CENTER><A HREF='" + relLogcatLogFilePath + "'><FONT FACE=VERDANA COLOR=BLACK SIZE=2>ADB LOGS</FONT></A></TD></TR><Br/>");
				  }
			  }
			  else if(driverType.trim().toUpperCase().contains("IOS") && !Dictionary.get("TEST_BROWSER").trim().equalsIgnoreCase("true") && !Environment.get("iosAppName").trim().equalsIgnoreCase("")){
				  String logcatLogFilePath = this.g_strLogFolderName + OSValidator.delimiter + "logcat.txt";
				  String relLogcatLogFilePath = this.g_strRelLogFolderName + OSValidator.delimiter + "logcat.txt";
				  String output = BaseUtil.getIOSDeviceSysLogs(Environment.get("iosAppName").trim(), Environment.get("udid").trim());
				  try{
					  Runtime.getRuntime().exec("ps -ef| grep -i idevicesyslog | grep -v grep | awk '{print $2}' | xargs kill -9");
				  }
				  catch(Exception ex){
					  //Do Nothing
				  }
				  if(output != null && !output.trim().equalsIgnoreCase("")){
					  String logcat = output;
					  FileOutputStream fout = new FileOutputStream(logcatLogFilePath, true);
					  new PrintStream(fout).println(logcat);
					  fout.close();
					  fout = null;
					  new PrintStream(this.foutStrm).println("<TR><TD ALIGN=CENTER><A HREF='" + relLogcatLogFilePath + "'><FONT FACE=VERDANA COLOR=BLACK SIZE=2>IOS DEVICE SYSLOGS</FONT></A></TD></TR><Br/>");
				  }
			  }
		  }
		  new PrintStream(this.foutStrm).println("<TR><TD ALIGN=RIGHT><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=1>&copy; " + Environment.get("orgName") + "</FONT></TD></TR></TABLE></BODY></HTML>");
		  this.foutStrm.close();
		}
		catch (IOException io)
		{
			log.info("Threw a IOException in Reporting::fnCloseHtmlReport, full stack trace follows:", io);
		}
		this.foutStrm = null;
		if (this.g_iFailCount != 0) {
		  strTestCaseResult = "Fail";
		} else {
		  strTestCaseResult = "Pass";
		}
		
		if (status.equals("Passed")) {
			strTestCaseResult = "Pass";
		} else if (status.equals("Failed")) {
		  strTestCaseResult = "Fail";
		}
		
		fnWriteTestSummary(this.Dictionary.get("TEST_NAME"), strTestCaseResult, strTimeDifference);
	}
	  
	public void fnCloseTestSummary()
	  {
		this.g_SummaryEndTime = new Date();
		
		if(this.g_SummaryStartTime == null)
			return;
		
		String strTimeDifference = fnTimeDiffference(this.g_SummaryStartTime.getTime(), this.g_SummaryEndTime.getTime());
		try
		{
		  this.foutStrm = new FileOutputStream((String)this.Environment.get("HTMLREPORTSPATH") + OSValidator.delimiter + "SummaryReport.html", true);
		  
		  new PrintStream(this.foutStrm).println("</TABLE><TABLE WIDTH=100%><TR>");
		  new PrintStream(this.foutStrm).println("<TD BGCOLOR=BLACK WIDTH=10%></TD><TD BGCOLOR=BLACK WIDTH=60%><FONT FACE=VERDANA SIZE=2 COLOR=WHITE><B></B></FONT></TD><TD BGCOLOR=BLACK WIDTH=15%><FONT FACE=WINGDINGS SIZE=4>2</FONT><FONT FACE=VERDANA SIZE=2 COLOR=WHITE><B>Total Passed: " + this.g_iTCPassed + "</B></FONT></TD><TD BGCOLOR=BLACK WIDTH=15%><FONT FACE=VERDANA SIZE=2 COLOR=WHITE><B>" + strTimeDifference + "</B></FONT></TD>");
		  new PrintStream(this.foutStrm).println("</TR></TABLE>");
		  new PrintStream(this.foutStrm).println("<TABLE WIDTH=100%><TR><TD ALIGN=RIGHT><FONT FACE=VERDANA COLOR=" + Environment.get("reportColor") + " SIZE=1>&copy; " + Environment.get("orgName") + "</FONT></TD></TR></TABLE></BODY></HTML>");
		  

		  this.foutStrm.close();
		}
		catch (IOException io)
		{
			log.info("Threw a IOException in Reporting::fnCloseTestSummary, full stack trace follows:", io);
		}
		this.foutStrm = null;
	  }
	  
	public void log(String strDescription, String strExpectedValue, String strObtainedValue, String strResult)
	  {
		String sStep;
		if (this.Dictionary.containsKey("STEP"))
		{
		  sStep = this.Dictionary.get("STEP") + "<NS>" + strDescription + "<ND>" + strExpectedValue + "<ND>" + strObtainedValue + "<ND>" + strResult;
		  this.Dictionary.remove("STEP");
		}
		else
		{
		  sStep = strDescription + "<ND>" + strExpectedValue + "<ND>" + strObtainedValue + "<ND>" + strResult;
		}
		this.Dictionary.put("STEP", sStep);
		try
		{
		  this.foutStrm = new FileOutputStream(this.g_strTestCaseReport, true);
		}
		catch (FileNotFoundException fe)
		{
			log.info("Threw a FileNotFoundException in Reporting::log, full stack trace follows:", fe);
		}
		this.g_OperationCount += 1;
		String sRowColor;
		
		if (this.g_OperationCount % 2 == 0) {
		  sRowColor = "#EEEEEE";
		} else {
		  sRowColor = "#D3D3D3";
		}    
		
		if(System.getProperty("takeScreenshot") != null && !System.getProperty("takeScreenshot").trim().equalsIgnoreCase("")){
			Environment.put("takeScreenshot", System.getProperty("takeScreenshot").trim());
		}
		
		if(Environment.get("takeScreenshot").trim().equalsIgnoreCase("false") || Environment.get("takeScreenshot").trim().equalsIgnoreCase("n") || Environment.get("takeScreenshot").trim().equalsIgnoreCase("no")){
			if(strResult.trim().equalsIgnoreCase("pass")){
				strResult = "Done";
			}
		}
			
		if (strResult.toUpperCase().equals("PASS")) {
		  this.g_iPassCount += 1;
		  this.g_iSnapshotCount += 1;
		  
		  String snapshotFilePath = this.g_strSnapshotFolderName + OSValidator.delimiter + "SS_" + this.g_iSnapshotCount + ".gif";
		  String relSnapshotFilePath = this.g_strRelSnapshotFolderName + OSValidator.delimiter + "SS_" + this.g_iSnapshotCount + ".gif";
		  snapshotFilePath = fTakeScreenshot(snapshotFilePath);
		  if(snapshotFilePath.trim().endsWith(".html")){
			  relSnapshotFilePath = relSnapshotFilePath.replace(".gif", ".html");
		  }
		  String path = relSnapshotFilePath;
		  new PrintStream(this.foutStrm).println("<TR WIDTH=100%><TD BGCOLOR=" + sRowColor + " WIDTH=5% ALIGN=CENTER><FONT FACE=VERDANA SIZE=2 ><B>" + this.g_OperationCount + "</B></FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=28% STYLE=\"max-width:28%\"><FONT FACE=VERDANA SIZE=2>" + strDescription + " </FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=25% STYLE=\"max-width:25%\"><FONT FACE=VERDANA SIZE=2>" + strExpectedValue + " </FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=25% STYLE=\"max-width:25%\"><FONT FACE=VERDANA SIZE=2>" + strObtainedValue + " </FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=7% ALIGN=CENTER><A HREF='" + path + "'><FONT FACE=VERDANA SIZE=2 COLOR=GREEN><B>" + "Pass" + " </B></FONT></A></TD></TR>");
		  
		}
		else if (strResult.toUpperCase().equals("FAIL")) {
		  this.g_iSnapshotCount += 1;
		  this.g_iFailCount += 1;     

		  String snapshotFilePath = this.g_strSnapshotFolderName + OSValidator.delimiter + "SS_" + this.g_iSnapshotCount + ".gif";
		  String relSnapshotFilePath = this.g_strRelSnapshotFolderName + OSValidator.delimiter + "SS_" + this.g_iSnapshotCount + ".gif";
		  snapshotFilePath = fTakeScreenshot(snapshotFilePath);
		  if(snapshotFilePath.trim().endsWith(".html")){
			  relSnapshotFilePath = relSnapshotFilePath.replace(".gif", ".html");
		  }
		  String path = relSnapshotFilePath;
		  new PrintStream(this.foutStrm).println("<TR WIDTH=100%><TD BGCOLOR=" + sRowColor + " WIDTH=5% ALIGN=CENTER><FONT FACE=VERDANA SIZE=2 ><B>" + this.g_OperationCount + "</B></FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=28% STYLE=\"max-width:28%\"><FONT FACE=VERDANA SIZE=2>" + strDescription + " </FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=25% STYLE=\"max-width:25%\"><FONT FACE=VERDANA SIZE=2>" + strExpectedValue + " </FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=25% STYLE=\"max-width:25%\"><FONT FACE=VERDANA SIZE=2>" + strObtainedValue + " </FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=7% ALIGN=CENTER><A HREF='" + path + "'><FONT FACE=VERDANA SIZE=2 COLOR=RED><B>" + "Fail" + " </B></FONT></A></TD></TR>");
		  
		}
		else if (strResult.toUpperCase().equals("DONE")) {
		  strResult = "Pass";
		  new PrintStream(this.foutStrm).println("<TR WIDTH=100%><TD BGCOLOR=" + sRowColor + " WIDTH=5% ALIGN=CENTER><FONT FACE=VERDANA SIZE=2><B>" + this.g_OperationCount + "</B></FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=28% STYLE=\"max-width:28%\"><FONT FACE=VERDANA SIZE=2>" + strDescription + "</FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=25% STYLE=\"max-width:25%\"><FONT FACE=VERDANA SIZE=2>" + strExpectedValue + "</FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=25% STYLE=\"max-width:25%\"><FONT FACE=VERDANA SIZE=2>" + strObtainedValue + "</FONT></TD><TD BGCOLOR=" + sRowColor + " WIDTH=7% ALIGN=CENTER><FONT FACE=VERDANA SIZE=2 COLOR=LimeGreen><B>" + strResult + "</B></FONT></TD></TR>");
		}
		try {
		  this.foutStrm.close();
		}
		catch (IOException io) {
			log.info("Threw a IOException in Reporting::log, full stack trace follows:", io);
		}
	}
	  
	public String fTakeScreenshot(String SSPath) {
		try
		{
		  WebDriver screenDriver;
		  if ((this.driverType.contains("ANDROID")) || (this.driverType.contains("IOS")) || (this.driverType.contains("API"))) {
			screenDriver = this.driver;
		  } else {
			screenDriver = new Augmenter().augment(this.driver);
		  }
		  if(screenDriver != null){
			  String HTMLPath = null;
			  if(this.driverType.contains("API") && Environment.containsKey("RAW_RESPONSE") && !Environment.get("RAW_RESPONSE").trim().equalsIgnoreCase("")){
				  HTMLPath = SSPath.replace(".gif", ".html");
				  FileOutputStream fout = new FileOutputStream(HTMLPath, true);
				  new PrintStream(fout).println("<HTML><BODY><TABLE ALIGN=CENTER WIDTH=100% BORDER=1><THEAD><TR><TH WIDTH=50% ALIGN=LEFT>REQUEST</TH><TH WIDTH=50% ALIGN=LEFT>RESPONSE</TH></TR></THEAD><TR VALIGN=TOP><TD WIDTH=50% ALIGN=LEFT>");
				  new PrintStream(fout).println(Environment.get("RAW_REQUEST") + "</TD><TD WIDTH=50% ALIGN=LEFT>");
				  new PrintStream(fout).println(Environment.get("RAW_RESPONSE") + "</TD></TR></TABLE>");
				  new PrintStream(fout).println("</BODY></HTML>"); 
				  fout.close();
				  fout = null;
				  Environment.remove("RAW_RESPONSE");
				  Environment.remove("RAW_REQUEST");
			  }
			  else if(!Dictionary.get("STACKTRACE").trim().equalsIgnoreCase("")){
				  HTMLPath = SSPath.replace(".gif", ".html");
				  FileOutputStream fout = new FileOutputStream(HTMLPath, true);
				  new PrintStream(fout).println("<HTML><BODY><A HREF='" + SSPath.split(OSValidator.delimiter)[SSPath.split(OSValidator.delimiter).length - 1] + "'>SCREENSHOT</A><BR/>");
				  new PrintStream(fout).println(Dictionary.get("STACKTRACE"));
				  new PrintStream(fout).println("</BODY></HTML>"); 
				  Dictionary.remove("STACKTRACE");
				  fout.close();
				  fout = null;
			  }
			  if(!this.driverType.contains("API")){
				  if(screenDriver != null){
					  File scrFile = (File)((TakesScreenshot)screenDriver).getScreenshotAs(OutputType.FILE);
					  FileUtils.copyFile(scrFile, new File(SSPath));
					  FileUtils.deleteQuietly(scrFile);
					  scrFile = null;
					  try{
						Thread.sleep(1L);
					  }
					  catch (InterruptedException e){
						  log.info("Threw a InterruptedException in Reporting::fTakeScreenshot, full stack trace follows:", e);
					  }
				  }
			  }
			  screenDriver = null;
			  if(HTMLPath != null){
				  SSPath = HTMLPath;
			  }
		  }
		  else{
			  SSPath = SSPath.replace(".gif", ".html");
			  FileOutputStream fout = new FileOutputStream(SSPath, true);
			  if(!Dictionary.get("STACKTRACE").trim().equalsIgnoreCase("")){
				  new PrintStream(fout).println("<HTML><BODY>" + Dictionary.get("STACKTRACE") + "</BODY></HTML>");
				  Dictionary.remove("STACKTRACE");
			  }
			  else{
				  new PrintStream(fout).println("<HTML><BODY>"); 
				  for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
					  new PrintStream(fout).println(ste);
				  }
				  new PrintStream(fout).println("</BODY></HTML>"); 
			  }
			  fout.close();
			  fout = null;
		  }
		}
		catch (Exception e)
		{
			log.info("Threw a Exception in Reporting::fTakeScreenshot, full stack trace follows:", e);
		}
		
		return SSPath;
	  }
	  
	public String fnTimeDiffference(long startTime, long endTime)
	  {
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
	  
	public void fnWriteThreadReport(int iThreadCount, String sReportFile, String sCalendar, String sSummaryFile)
	  {
		try
		{
		  this.foutStrm = new FileOutputStream(sReportFile, true);
		  String sRowColor;
		  
		  if ((iThreadCount) % 2 == 0) {
			sRowColor = "#EEEEEE";
		  } else {
			sRowColor = "#D3D3D3";
		  }
		  new PrintStream(this.foutStrm).println("<TR COLS=3 BGCOLOR=" + sRowColor + "><TD  WIDTH=10%><FONT FACE=VERDANA SIZE=2>" + iThreadCount + "</FONT></TD><TD  WIDTH=35%><FONT FACE=VERDANA SIZE=2>" + this.driverType + "</FONT></TD><TD  WIDTH=35%><FONT FACE=VERDANA SIZE=2>" + sCalendar + "</FONT></TD><TD  WIDTH=20%><A HREF='" + sSummaryFile + "'><FONT FACE=VERDANA SIZE=2 COLOR=GREEN><B>Report</B></FONT></A></TD></TR>");
		  
		  this.foutStrm.close();
		}
		catch (IOException io)
		{
			log.info("Threw a IOException in Reporting::fnWriteThreadReport, full stack trace follows:", io);
		}
		this.foutStrm = null;
	  }
		
	public static void copyFolder(File src, File dest) throws IOException{
	 
		if(src.isDirectory()){
 
			//if directory not exists, create it
			if(!dest.exists()){
			   dest.mkdir();
			   log.info("Directory copied from " 
							  + src + "  to " + dest);
			}
 
			//list all the directory contents
			String files[] = src.list();
 
			for (String file : files) {
			   //construct the src and dest file structure
			   File srcFile = new File(src, file);
			   File destFile = new File(dest, file);
			   //recursive copy
			   copyFolder(srcFile,destFile);
			}
 
		}else{
			//if file, then copy it
			//Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest); 
 
			byte[] buffer = new byte[1024];
 
			int length;
			//copy the file content in bytes 
			while ((length = in.read(buffer)) > 0){
			   out.write(buffer, 0, length);
			}
 
			in.close();
			out.close();
			log.info("File copied from " + src + " to " + dest);
		}
	}

	@SuppressWarnings({ "unused" })
	private static String getIPOfNode(RemoteWebDriver remoteDriver) {
		String hostFound = null;
		try {
		  HttpCommandExecutor ce = (HttpCommandExecutor) remoteDriver.getCommandExecutor();
		  String hostName = ce.getAddressOfRemoteServer().getHost();
		  int port = ce.getAddressOfRemoteServer().getPort();
		  HttpHost host = new HttpHost(hostName, port);
		  DefaultHttpClient client = new DefaultHttpClient();
		  URL sessionURL = new URL("http://" + hostName + ":" + port
			+ "/grid/api/testsession?session=" + remoteDriver.getSessionId());
		  BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest(
			  "POST", sessionURL.toExternalForm());
		  HttpResponse response = client.execute(host, r);
		  JSONObject object = extractObject(response);
		  URL myURL = new URL(object.getString("proxyId"));
		  if ((myURL.getHost() != null) && (myURL.getPort() != -1)) {
			hostFound = myURL.getHost();
		  }
		} catch (Exception e) {
			log.info("Threw a Exception in Reporting::getIPOfNode, full stack trace follows:", e);
		}
		return hostFound;
	  }

	private static JSONObject extractObject(HttpResponse resp) throws IOException, JSONException {
		InputStream contents = resp.getEntity().getContent();
		StringWriter writer = new StringWriter();
		IOUtils.copy(contents, writer, "UTF8");
		JSONObject objToReturn = new JSONObject(writer.toString());
		return objToReturn;
	}
}