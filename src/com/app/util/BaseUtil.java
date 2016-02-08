package com.app.util;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;
import io.appium.java_client.ios.IOSDriver;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Base64;
import org.jongo.MongoCollection;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.Location;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.app.framework.Assert;
import com.app.framework.Driver.HashMapNew;
import com.app.framework.DBActivities;
import com.app.framework.OSValidator;
import com.app.framework.RedisConfig;
import com.app.framework.RedisManager;
import com.app.framework.Reporting;
import com.app.framework.SoftAssert;
import com.google.common.io.Files;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.lept.PIX;
import org.bytedeco.javacpp.tesseract.TessBaseAPI;

import static org.bytedeco.javacpp.lept.*;

public class BaseUtil {
	private static long DEFAULT_FIND_ELEMENT_TIMEOUT;
	static File classpathRoot = new File(System.getProperty("user.dir"));
	static Logger log = LoggerFactory.getLogger(BaseUtil.class);
	public WebDriver driver;
	public String driverType;
	public HashMapNew Dictionary;
	public HashMapNew Environment;
	public Reporting Reporter;
	public Assert Assert;
	public SoftAssert SoftAssert;
	public AndroidDriver<?> android;
	public IOSDriver<?> ios;
	public AppiumDriver<?> appium;
	
	public BaseUtil(WebDriver driver, String driverType, HashMapNew Dictionary, HashMapNew Environment, Reporting Reporter, Assert Assert, SoftAssert SoftAssert){
		this.driver = driver;
		this.driverType = driverType;
		this.Dictionary = Dictionary;
		this.Environment = Environment;
		this.Reporter = Reporter;
		this.Assert = Assert;
		this.SoftAssert = SoftAssert;
		
		if(driverType != null && driverType.trim().toUpperCase().contains("IOS"))
			ios = (IOSDriver<?>)driver;
		if(driverType != null && driverType.trim().toUpperCase().contains("ANDROID"))
			android = (AndroidDriver<?>)driver;
		if(driverType != null && !driverType.trim().toUpperCase().contains("API") && !driverType.trim().toUpperCase().contains("CHROME"))
			appium = (AppiumDriver<?>)driver;
		DEFAULT_FIND_ELEMENT_TIMEOUT = Long.valueOf(Environment.get("implicitWait")) / 1000;
	}
	
	public void sendKeyEvent(int key){
		android.sendKeyEvent(key);
	}
	
	public void sendKeys(By locator, CharSequence... textToType) throws Exception{
		WebElement we = getElementWhenVisible(locator);
		switch(driverType.trim().toUpperCase().replaceAll("\\d", "")){
//			case "ANDROID":
//				if(Environment.get("browserTest").trim().equalsIgnoreCase("true") || Environment.get("browserTest").trim().equalsIgnoreCase("Y") || Environment.get("browserTest").trim().equalsIgnoreCase("Yes") || Environment.get("appPackage").trim().toLowerCase().contains("chrome") || Environment.get("app").trim().toLowerCase().contains("chrome") || Environment.get("appActivity").trim().toLowerCase().contains("chrome")){
//					we.sendKeys(textToType);
//				}
//				else{
//					we.click();
//					String text = (String)textToType[0];
//					text = text.replace(" ", "%s");
//					runADBCommand(new String[]{"-s", Environment.get("udid").trim(), "shell", "input", "text", "'" + text + "'"}, new Boolean[]{false, false, false, false, false, false}, 1000);
//				}
//				break;
			default:
				we.sendKeys(textToType);
		}
	}
	
	public void sendKeys(WebElement we, CharSequence... textToType) throws Exception{
		switch(driverType.trim().toUpperCase().replaceAll("\\d", "")){
//			case "ANDROID":
//				if(Environment.get("browserTest").trim().equalsIgnoreCase("true") || Environment.get("browserTest").trim().equalsIgnoreCase("Y") || Environment.get("browserTest").trim().equalsIgnoreCase("Yes") || Environment.get("appPackage").trim().toLowerCase().contains("chrome") || Environment.get("app").trim().toLowerCase().contains("chrome") || Environment.get("appActivity").trim().toLowerCase().contains("chrome")){
//					we.sendKeys(textToType);
//				}
//				else{
//					we.click();
//					String text = (String)textToType[0];
//					text = text.replace(" ", "%s");
//					runADBCommand(new String[]{"-s", Environment.get("udid").trim(), "shell", "input", "text", "'" + text + "'"}, new Boolean[]{false, false, false, false, false, false}, 1000);
//				}
//				break;
			default:
				we.sendKeys(textToType);
		}
	}
	
	/**
	 * Tap on element
	 * 
	 * @param webElmt
	 */
	public void tap(WebElement webElmt, String objName){
		TouchAction action = new TouchAction(appium);
		action.tap(webElmt).perform();
		Reporter.log("Verify " + objName + " is tapped", objName + " should be tapped", objName + "  is tapped successfully", "Done");
	}
	
	/**
	 * Tap on element - overloaded
	 * 
	 * @param webElmt
	 * @param x
	 * @param y
	 */
	public void tap(WebElement webElmt, String objName, int x, int y){
		TouchAction action = new TouchAction(appium);
		action.tap(webElmt, x, y).perform();
		Reporter.log("Verify " + objName + " is tapped", objName + " should be tapped", objName + "  is tapped successfully", "Done");
	}
	
	/**
	 * Tap on coordinates - x, y
	 * 
	 * @param webElmt
	 * @param x
	 * @param y
	 */
	public void tap(int x, int y){
		TouchAction action = new TouchAction(appium);
		action.tap(x, y).perform();
	}
	
	/**
	 * Tap on element
	 * 
	 * @param webElmt
	 */
	public void tap(By locator, String objName, long... waitSeconds){
		WebElement we = getElementWhenVisible(locator, waitSeconds);
		TouchAction action = new TouchAction(appium);
		action.tap(we).perform();
		Reporter.log("Verify " + objName + " is tapped", objName + " should be tapped", objName + "  is tapped successfully", "Done");
	}
	
	/**
	 * Tap on element - overloaded
	 * 
	 * @param webElmt
	 * @param x
	 * @param y
	 */
	public void tap(By locator, String objName, int x, int y, long... waitSeconds){
		WebElement we = getElementWhenVisible(locator, waitSeconds);
		TouchAction action = new TouchAction(appium);
		action.tap(we, x, y).perform();
		Reporter.log("Verify " + objName + " is tapped", objName + " should be tapped", objName + "  is tapped successfully", "Done");
	}
	
	/**
	 * Press on element
	 * 
	 * @param webElmt
	 */
	public void press(WebElement webElmt, String objName){
		TouchAction action = new TouchAction(appium);
		action.press(webElmt).perform();
		Reporter.log("Verify " + objName + " is pressed", objName + " should be pressed", objName + "  is pressed successfully", "Done");
	}
	
	/**
	 * Press on element - overloaded
	 * 
	 * @param webElmt
	 * @param x
	 * @param y
	 */
	public void press(WebElement webElmt, String objName, int x, int y){
		TouchAction action = new TouchAction(appium);
		action.press(webElmt, x, y).perform();
		Reporter.log("Verify " + objName + " is pressed", objName + " should be pressed", objName + "  is pressed successfully", "Done");
	}
	
	/** Press on element
	 * 
	 * @param webElmt
	 */
	public void press(By locator, String objName, long... waitSeconds){
		WebElement we = getElementWhenVisible(locator, waitSeconds);
		TouchAction action = new TouchAction(appium);
		if(we != null){
			action.press(we).perform();
			Reporter.log("Verify " + objName + " is pressed", objName + " should be pressed", objName + "  is pressed successfully", "Done");
		}
		else{
			Reporter.log("Verify " + objName + " is pressed", objName + " should be pressed", objName + "  is not pressed", "Fail");
		}
	}
	
	/**
	 * Press on element - overloaded
	 * 
	 * @param webElmt
	 * @param x
	 * @param y
	 */
	public void press(By locator, String objName, int x, int y, long... waitSeconds){
		WebElement we = getElementWhenVisible(locator, waitSeconds);
		
		TouchAction action = new TouchAction(appium);
		if(we != null){
			action.press(we, x, y).perform();
			Reporter.log("Verify " + objName + " is pressed", objName + " should be pressed", objName + "  is pressed successfully", "Done");
		}
		else{
			Reporter.log("Verify " + objName + " is pressed", objName + " should be pressed", objName + "  is not pressed", "Fail");
		}
	}
	
	/**
	 * Drag element
	 * 
	 * @param webElmtPropFrom
	 * @param webElmtPropTo
	 */
	public void drag(WebElement webElmtPropFrom, WebElement webElmtPropTo){
		TouchAction action = new TouchAction(appium);
		action.longPress(webElmtPropFrom).moveTo(webElmtPropTo).release().perform();
		Reporter.log("Verify drag happens", "Object should be dragged from one position to another", "Object is dragged from one position to another", "Pass");
	}
	
	/**
	 * Click on element
	 * 
	 * @param locator
	 * @param objName
	 * @param waitSeconds
	 */
	public void click(By locator, String objName, long... waitSeconds){
		WebElement we = getElementWhenVisible(locator, waitSeconds);
		if(we != null){
			we.click();
			Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "User clicked on " + objName.toLowerCase() + " successfully", "Pass");
		}
		else{
			Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "Not able to click on " + objName.toLowerCase(), "Fail");
		}
	}
	
	/**
	 * Click on element - overloaded
	 * 
	 * @param we
	 * @param objName
	 */
	public void click(WebElement we, String objName){
		we.click();
		Reporter.log("Verify user is able to click on " + objName.toLowerCase(), "User should able to click on " + objName.toLowerCase(), "User clicked on " + objName.toLowerCase() + " successfully", "Pass");
	}
	
	/**
	 * Send keys into textbox
	 * 
	 * @param locator
	 * @param objName
	 * @param textToType
	 * @param waitSeconds
	 */
	public void type(By locator, String objName, String textToType, long... waitSeconds){
		WebElement we = getElementWhenVisible(locator, waitSeconds);
		int intCount = 1;        
		while (intCount <= 4){
			try {	        		
				if(intCount == 1 && ((driverType.trim().toUpperCase().contains("IOS") && we.getAttribute("value").trim().equalsIgnoreCase("")) || we.getText().trim().equalsIgnoreCase("") || we.getAttribute("text").trim().equalsIgnoreCase("") || (driverType.trim().toUpperCase().contains("CHROME") && we.getAttribute("value").trim().equalsIgnoreCase("")))){
					//Do Nothing
				}
				else
					we.clear();
					sendKeys(we, textToType);
					hideKeyboard();
				if((driverType.trim().toUpperCase().contains("IOS") && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || (driverType.trim().toUpperCase().contains("CHROME") && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || we.getText().trim().equalsIgnoreCase(textToType.trim()) || we.getAttribute("text").trim().equalsIgnoreCase(textToType.trim()) || we.getAttribute("name").trim().equalsIgnoreCase(textToType.trim()))
					break;
			}catch (Exception e){	
				we = getElementWhenVisible(locator, waitSeconds);
			}
			if(intCount==4){
				Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Fail");
				return;
			}
			intCount++;
		}
		Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "User entered text - " + textToType + " into editbox - " + objName.toLowerCase(), "Pass");
	}
	
	/**
	 * Send keys into textbox
	 * 
	 * @param locator
	 * @param objName
	 * @param textToType
	 * @param waitSeconds
	 */
	public void type(By locator, String objName, String textToType, boolean skipValueCheck, long... waitSeconds){
		WebElement we = getElementWhenVisible(locator, waitSeconds);
		int intCount = 1;        
		while (intCount <= 4){
			try {	        		
				if(intCount == 1 && ((driverType.trim().toUpperCase().contains("IOS") && we.getAttribute("value").trim().equalsIgnoreCase("")) || we.getText().trim().equalsIgnoreCase("") || we.getAttribute("text").trim().equalsIgnoreCase("") || (driverType.trim().toUpperCase().contains("CHROME") && we.getAttribute("value").trim().equalsIgnoreCase("")))){
					//Do Nothing
				}
				else
					we.clear();
				sendKeys(we, textToType);
				hideKeyboard();
				if(skipValueCheck)
					break;
				if((driverType.trim().toUpperCase().contains("IOS") && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || (driverType.trim().toUpperCase().contains("CHROME") && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || we.getText().trim().equalsIgnoreCase(textToType.trim()) || we.getAttribute("text").trim().equalsIgnoreCase(textToType.trim()) || we.getAttribute("name").trim().equalsIgnoreCase(textToType.trim()))
					break;
			}catch (Exception e){	
				we = getElementWhenVisible(locator, waitSeconds);
			}
			if(intCount==4){
				Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Fail");
				return;
			}
			intCount++;
		}
		Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "User entered text - " + textToType + " into editbox - " + objName.toLowerCase(), "Pass");
	}
	
	/**
	 * Send keys into textbox - overloaded
	 * 
	 * @param we
	 * @param objName
	 * @param textToType
	 */
	public void type(WebElement we, String objName, String textToType){
		int intCount = 1;        
		while (intCount <= 4){
			try {	        		
				if(intCount == 1 && ((driverType.trim().toUpperCase().contains("IOS") && we.getAttribute("value").trim().equalsIgnoreCase("")) || we.getText().trim().equalsIgnoreCase("") || we.getAttribute("text").trim().equalsIgnoreCase("") || (driverType.trim().toUpperCase().contains("CHROME") && we.getAttribute("value").trim().equalsIgnoreCase("")))){
					//Do Nothing
				}
				else
					we.clear();
				sendKeys(we, textToType);
				hideKeyboard();
				if((driverType.trim().toUpperCase().contains("IOS") && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || (driverType.trim().toUpperCase().contains("CHROME") && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || we.getText().trim().equalsIgnoreCase(textToType.trim()) || we.getAttribute("text").trim().equalsIgnoreCase(textToType.trim()) || we.getAttribute("name").trim().equalsIgnoreCase(textToType.trim()))
					break;
			}catch (Exception e){	
				//Do Nothing
			}
			if(intCount==4){
				Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Fail");
				return;
			}
			intCount++;
		}
		Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "User entered text - " + textToType + " into editbox - " + objName.toLowerCase(), "Pass");
	}
	
	/**
	 * Send keys into textbox - overloaded
	 * 
	 * @param we
	 * @param objName
	 * @param textToType
	 */
	public void type(WebElement we, String objName, String textToType, boolean skipValueCheck){
		int intCount = 1;        
		while (intCount <= 4){
			try {	        		
				if(intCount == 1 && ((driverType.trim().toUpperCase().contains("IOS") && we.getAttribute("value").trim().equalsIgnoreCase("")) || we.getText().trim().equalsIgnoreCase("") || we.getAttribute("text").trim().equalsIgnoreCase("") || (driverType.trim().toUpperCase().contains("CHROME") && we.getAttribute("value").trim().equalsIgnoreCase("")))){
					//Do Nothing
				}
				else
					we.clear();
				sendKeys(we, textToType);
				hideKeyboard();
				if(skipValueCheck)
					break;
				if((driverType.trim().toUpperCase().contains("IOS") && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || (driverType.trim().toUpperCase().contains("CHROME") && we.getAttribute("value").trim().equalsIgnoreCase(textToType.trim())) || we.getText().trim().equalsIgnoreCase(textToType.trim()) || we.getAttribute("text").trim().equalsIgnoreCase(textToType.trim()) || we.getAttribute("name").trim().equalsIgnoreCase(textToType.trim()))
					break;
			}catch (Exception e){	
				//Do Nothing
			}
			if(intCount==4){
				Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "Not able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Fail");
				return;
			}
			intCount++;
		}
		Reporter.log("Validate user is able to enter text - " + textToType + " into editbox - " + objName.toLowerCase(), "Text - " + textToType + " should be entered into editbox - " + objName.toLowerCase(), "User entered text - " + textToType + " into editbox - " + objName.toLowerCase(), "Pass");
	}
	
	/**
	 * Clear textbox value
	 * 
	 * @param locator
	 * @param objName
	 * @param moveCursorToEnd
	 * @param waitSeconds
	 */
	public void clear(By locator, boolean moveCursorToEnd, long... waitSeconds){
			WebElement we =  getElementWhenVisible(locator, waitSeconds);
			if(we == null)
				return;
			
			String text;
			if(driverType.trim().toUpperCase().contains("IOS")){
				text = we.getAttribute("value");
			}
			else
				text = we.getAttribute("text");
			int maxChars = text.length();
			if(moveCursorToEnd){
				for (int i = 0; i < maxChars; i++)
					sendKeyEvent(22);
			}
	
			for (int i = 0; i < maxChars; i++)
				sendKeyEvent(67);
		
	}

	/**
	 * Get text attribute from element
	 * 
	 * @param locator
	 * @param objName
	 * @param waitSeconds
	 * @return
	 */
	public String getText(By locator, long... waitSeconds){
		WebElement we = getElementWhenVisible(locator, waitSeconds);
		if(we == null)
			return null;
		return we.getText();
	}
	
	/**
	 * Get text attribute from element
	 * 
	 * @param locator
	 * @param objName
	 * @param waitSeconds
	 * @return
	 */
	public String getText(WebElement we, long... waitSeconds){
		if(we == null)
			return null;
		return we.getText();
	}
	
	/**
	 * Get attribute from element
	 * 
	 * @param locator
	 * @param objName
	 * @param attribute
	 * @param waitSeconds
	 * @return
	 */
	public String getAttribute(By locator, String attribute, long... waitSeconds){
		WebElement we = getElementWhenVisible(locator, waitSeconds);
		if(we == null)
			return null;
		return we.getAttribute(attribute);
	}
	
	/**
	 * Get attribute from element - overloaded
	 * 
	 * @param we
	 * @param attribute
	 * @return
	 */
	public String getAttribute(WebElement we, String attribute){
		return we.getAttribute(attribute);
	}
	
	/**
	 * Long press on element
	 * 
	 * @param locator
	 * @param objName
	 * @param waitSeconds
	 */
	public void longPress(By locator, String objName, long... waitSeconds){
		WebElement we = getElementWhenVisible(locator, waitSeconds);
		if(we == null){
			return;
		}
		
		TouchAction action = new TouchAction(appium);
		action.longPress(we).perform();
		Reporter.log("Verify long press on object", "Long press should be done on '" + objName + "'", "Long press on '" + objName + "' completed" , "Pass");
	}
	
	/**
	 * Long press on element (Only in case of appium)
	 * 
	 * @param locator
	 * @param objName
	 * @param waitSeconds
	 */
	public void longPress(int x, int y){
		TouchAction action = new TouchAction(appium);
		action.longPress(x, y).perform();
	}
	
	/**
	 * Long press on element
	 * 
	 * @param locator
	 * @param objName
	 * @param waitSeconds
	 */
	public void longPress(WebElement we, String objName, long... waitSeconds){
		if(we == null)
			return;
		TouchAction action = new TouchAction(appium);
		action.longPress(we).perform();
		Reporter.log("Verify long press on object", "Long press should be done on '" + objName + "'", "Long press on '" + objName + "' completed" , "Pass");
	}

	/**
	 * Hide keyboard
	 */
	public void hideKeyboard(){
		try{
			(appium).hideKeyboard();
		}
		catch(Exception e){
			log.info("Softkeyboard not present");
		}
	}

	/**
	 * Open notifications
	 *  
	 * Only available for android
	 * @throws Exception 
	 */
	public void openNotifications() throws Exception{
		android.openNotifications();
		Reporter.log("Verify notification is opened", "Notification should be opened", "Notification is opened successfully" , "Pass");
	}

	/**
	 * Navigate back
	 */
	public void navigateBack(){
		driver.navigate().back();
	}
	
	/**
	 * Navigate back(in case of IOS)
	 * 
	 */
	public void pressBack(){
		click(By.id("Back"), "IOS back button");
	}
	
	/**
	 * Press android home key
	 */
	public void pressAndroidHomeKey(){
		sendKeyEvent(AndroidKeyCode.HOME);
		Reporter.log("Verify android home button is pressed", "Android home button should be pressed", "Android home button is pressed successfully" , "Pass");
	}
	
	/**
	 * Press android enter key
	 */
	public void pressAndroidEnterKey(){
		sendKeyEvent(AndroidKeyCode.ENTER);
	}
	
	/**
	 * Get element when visible
	 * 
	 * @param locater
	 * @param waitSeconds
	 * @return
	 */
	public WebElement getElementWhenVisible(By locater, long... waitSeconds){
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : DEFAULT_FIND_ELEMENT_TIMEOUT;
		WebElement element = null;
		
		driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
		
		Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
	       .withTimeout(seconds, TimeUnit.SECONDS)
	       .pollingEvery(100, TimeUnit.MILLISECONDS);
		
		try{
			element = wait.until(ExpectedConditions.visibilityOfElementLocated(locater));
		}
		catch(Exception ex){
			boolean flag = false;
			if(!Environment.get("methodHandleUnwantedPopups").trim().equalsIgnoreCase("")){
				String[] words = Environment.get("methodHandleUnwantedPopups").trim().split("\\.");
				String methodName = words[words.length - 1];
				String className = Environment.get("methodHandleUnwantedPopups").trim().substring(0, Environment.get("methodHandleUnwantedPopups").trim().indexOf("." + methodName));
				Object[] params = new Object[0];
				Class<?> thisClass;
				try {
					thisClass = Class.forName(className);
					Object busFunctions = thisClass.getConstructor(new Class[] { WebDriver.class, String.class, HashMapNew.class, HashMapNew.class, Reporting.class, Assert.class, SoftAssert.class }).newInstance(new Object[] { this.driver, this.driverType, this.Dictionary, this.Environment, this.Reporter, this.Assert, this.SoftAssert });
					Method method = thisClass.getDeclaredMethod(methodName, new Class[0]);
					Object objReturn = method.invoke(busFunctions, params);
					if (objReturn.equals(Boolean.valueOf(true))) {
						flag = true;
					}
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
			if(flag)
				element = wait.until(ExpectedConditions.visibilityOfElementLocated(locater));
			else
				throw ex;
		}
		finally{
			driver.manage().timeouts().implicitlyWait(Long.valueOf(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);
		}
		
		return element;
	}
	
	/**
	 * Get element when clickable
	 * 
	 * @param locator
	 * @param objName
	 * @param waitSeconds
	 * @return
	 */
	public WebElement getElementWhenClickable(By locator, long...waitSeconds){
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : DEFAULT_FIND_ELEMENT_TIMEOUT;

		WebElement element = null;
		driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
		WebDriverWait wait  = new WebDriverWait(driver, seconds);
		try{
			element = wait.until(ExpectedConditions.elementToBeClickable(locator));
		}
		catch(Exception ex){
			boolean flag = false;
			if(!Environment.get("methodHandleUnwantedPopups").trim().equalsIgnoreCase("")){
				String[] words = Environment.get("methodHandleUnwantedPopups").trim().split("\\.");
				String methodName = words[words.length - 1];
				String className = Environment.get("methodHandleUnwantedPopups").trim().substring(0, Environment.get("methodHandleUnwantedPopups").trim().indexOf("." + methodName));
				Object[] params = new Object[0];
				Class<?> thisClass;
				try {
					thisClass = Class.forName(className);
					Object busFunctions = thisClass.getConstructor(new Class[] { WebDriver.class, String.class, HashMapNew.class, HashMapNew.class, Reporting.class, Assert.class, SoftAssert.class }).newInstance(new Object[] { this.driver, this.driverType, this.Dictionary, this.Environment, this.Reporter, this.Assert, this.SoftAssert });
					Method method = thisClass.getDeclaredMethod(methodName, new Class[0]);
					Object objReturn = method.invoke(busFunctions, params);
					if (objReturn.equals(Boolean.valueOf(true))) {
						flag = true;
					}
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
			if(flag)
				element = wait.until(ExpectedConditions.elementToBeClickable(locator));
			else
				throw ex;
		}
		finally{
			driver.manage().timeouts().implicitlyWait(Long.valueOf(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);
		}
		return element;
	}

	/**
	 * Get element when text is present
	 * 
	 * @param locater
	 * @param objName
	 * @param text
	 * @param waitSeconds
	 * @return
	 */
	public WebElement getElementWhenTextIsPresent(By locater, String text, long... waitSeconds){
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : DEFAULT_FIND_ELEMENT_TIMEOUT;

		WebElement element =null;
		driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
		WebDriverWait wait  = new WebDriverWait(driver, seconds);
		try{
			boolean val = wait.until(ExpectedConditions.textToBePresentInElementLocated(locater, text));
			if(val){
				element = driver.findElement(locater);
			}
		}
		catch(Exception ex){
			boolean flag = false;
			if(!Environment.get("methodHandleUnwantedPopups").trim().equalsIgnoreCase("")){
				String[] words = Environment.get("methodHandleUnwantedPopups").trim().split("\\.");
				String methodName = words[words.length - 1];
				String className = Environment.get("methodHandleUnwantedPopups").trim().substring(0, Environment.get("methodHandleUnwantedPopups").trim().indexOf("." + methodName));
				Object[] params = new Object[0];
				Class<?> thisClass;
				try {
					thisClass = Class.forName(className);
					Object busFunctions = thisClass.getConstructor(new Class[] { WebDriver.class, String.class, HashMapNew.class, HashMapNew.class, Reporting.class, Assert.class, SoftAssert.class }).newInstance(new Object[] { this.driver, this.driverType, this.Dictionary, this.Environment, this.Reporter, this.Assert, this.SoftAssert });
					Method method = thisClass.getDeclaredMethod(methodName, new Class[0]);
					Object objReturn = method.invoke(busFunctions, params);
					if (objReturn.equals(Boolean.valueOf(true))) {
						flag = true;
					}
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
			if(flag){
				boolean val = wait.until(ExpectedConditions.textToBePresentInElementLocated(locater, text));
				if(val){
					element = driver.findElement(locater);
				}
			}
			else
				throw ex;
		}
		finally{
			driver.manage().timeouts().implicitlyWait(Long.valueOf(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);
		}
		return element;
	}
	
	/**
	 * Check if element is present
	 * 
	 * @param locator
	 * @param waitSeconds
	 * @return
	 */
	public boolean checkIfElementPresent(By locator, long... waitSeconds){
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : 5;
		try{
			driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
			if(driverType.trim().toUpperCase().contains("IOS")){
				Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
			       .withTimeout(seconds, TimeUnit.SECONDS)
			       .pollingEvery(100, TimeUnit.MILLISECONDS);
				
				try{
					wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
					return true;
				}
				catch(Exception ex){
					return false;
				}
				finally{
					driver.manage().timeouts().implicitlyWait(Long.valueOf(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);
				}
			}
			else{
				List<WebElement> obj = driver.findElements(locator);
				Boolean isPresent = obj.size() > 0;
				return isPresent;
			}
		}
		catch(Exception e){
			//Do Nothing
		}
		finally{
			driver.manage().timeouts().implicitlyWait(Long.valueOf(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);
		}
		return false;
	}
	
	/**
	 * Check if element is present
	 * 
	 * @param locator
	 * @param waitSeconds
	 * @return
	 */
	public boolean checkIfElementPresent(WebElement we, long... waitSeconds){
		assert waitSeconds.length <= 1;
		long seconds = waitSeconds.length > 0 ? waitSeconds[0] : 5;
		try{
			driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
			Boolean isPresent = we.isDisplayed();
			return isPresent;
		}
		catch(Exception e){
			//Do Nothing
		}
		finally{
			driver.manage().timeouts().implicitlyWait(Long.valueOf(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);
		}
		return false;
	}
	
	/**
	 * Verify is element displayed
	 * 
	 * @param locator
	 * @param objName
	 * @return
	 */
	public boolean isElementDisplayed(By locator, String objName, boolean screenPrint){
		boolean isDisplayed;
		WebElement we = getElementWhenVisible(locator);
		if(we == null){
			Reporter.log("Validate " + objName.toLowerCase(), objName + " should be displayed", objName + " is not displayed", "Fail");
			return false;
		}
		isDisplayed = we.isDisplayed();
		if(screenPrint){
			if(isDisplayed){
				Reporter.log("Validate " + objName.toLowerCase(), objName + " should be displayed", objName + " is displayed successfully", "Pass");
			}
			else{
				Reporter.log("Validate " + objName.toLowerCase(), objName + " should be displayed", objName + " is not displayed", "Fail");
			}
		}
		return isDisplayed;
	}
	
	/**
	 * Verify is element enabled
	 * 
	 * @param locator
	 * @param objName
	 * @return
	 */
	public boolean isElementEnabled(By locator, String objName, boolean... screenPrint){
		boolean isEnabled;
		WebElement we = getElementWhenVisible(locator);
		boolean print = screenPrint.length > 0 ? screenPrint[0] : false;
		if(we == null){
			if(print)
				Reporter.log("Validate " + objName.toLowerCase(), objName + " should be enabled", objName + " is not enabled", "Fail");
			return false;
		}
		isEnabled = we.isEnabled();
		if(isEnabled)
			if(print)
				Reporter.log("Validate " + objName.toLowerCase(), objName + " should be enabled", objName + " is enabled successfully", "Pass");
		else
			if(print)
				Reporter.log("Validate " + objName.toLowerCase(), objName + " should be enabled", objName + " is not enabled", "Fail");
		
		return isEnabled;
	}
	
	/**
	 * Verify is element selected
	 * 
	 * @param locator
	 * @param objName
	 * @return
	 */
	public boolean isElementSelected(By locator, String objName){
		WebElement we  = getElementWhenVisible(locator);
		if(we == null){
			Reporter.log("Validate " + objName.toLowerCase(), objName + " should be selected", objName + " is not selected", "Fail");
			return false;
		}
		boolean isSelected = we.isSelected();
		
		if(isSelected)
			Reporter.log("Validate " + objName.toLowerCase(), objName + " should be selected", objName + " is selected successfully", "Pass");
		else
			Reporter.log("Validate " + objName.toLowerCase(), objName + " should be selected", objName + " is not selected", "Fail");
		
		return isSelected;
	}
	
	/**
	 * Check if the object is checked or not
	 * 
	 * @param locator
	 * @param objName
	 * @return
	 */
	public boolean isChecked(By locator, String objName){
		WebElement we  = getElementWhenVisible(locator);
		if(we == null){
			Reporter.log("Validate " + objName.toLowerCase(), objName + " should be checked", objName + " is  unchecked", "Fail");
			return false;
		}
		boolean isChecked = Boolean.valueOf(getAttribute(locator, "checked"));
		
		if(isChecked)
			Reporter.log("Validate " + objName.toLowerCase(), objName + " should be checked", objName + " is checked successfully", "Pass");
		else
			Reporter.log("Validate " + objName.toLowerCase(), objName + " should be checked", objName + " is unchecked", "Fail");
		
		return isChecked;
	}
	
	/**
	 * Select by value
	 * 
	 * @param locator
	 * @param objName
	 * @param selText
	 */
	public void selectByValue(By locator, String objName, String selText){
		WebElement we = getElementWhenVisible(locator);
		if(we == null)
			return;
		
		Select select = new Select(we);
		select.selectByValue(selText);
		if(select.getFirstSelectedOption().getAttribute("value").equals(selText))
			Reporter.log("Validate " + selText + " is selected from the list - " + objName.toLowerCase(), selText + " should be selected from the list - " + objName.toLowerCase(), selText + " is selected from the list - " + objName.toLowerCase() + " successfully", "Pass");
		else
			Reporter.log("Validate " + selText + " is selected from the list - " + objName.toLowerCase(), selText + " should be selected from the list - " + objName.toLowerCase(), selText + " is not selected from the list - " + objName.toLowerCase(), "Fail");
	}
	
	/**
	 * Select by index
	 * 
	 * @param locator
	 * @param objName
	 * @param index
	 */
	public void selectByIndex(By locator, String objName, int index){
		WebElement we = getElementWhenVisible(locator);
		if(we == null)
			return;
		
		Select select = new Select(we);
		select.selectByIndex(index);
		Reporter.log("Validate value at index - " + index + " is selected from the list - " + objName.toLowerCase(), "Value at index - " + index + " should be selected from the list - " + objName.toLowerCase(), "Value at index - " + index + " is selected from the list - " + objName.toLowerCase() + " successfully", "Pass");
	}
	
	/**
	 * Select by visible text
	 * 
	 * @param locator
	 * @param objName
	 * @param selText
	 */
	public void selectByVisibleText(By locator, String objName, String selText){
		WebElement we = getElementWhenVisible(locator);
		if(we == null)
			return;
		
		Select select = new Select(we);
		select.selectByVisibleText(selText);
		if(select.getFirstSelectedOption().getAttribute("value").equals(selText))
			Reporter.log("Validate " + selText + " is selected from the list - " + objName.toLowerCase(), selText + " should be selected from the list - " + objName.toLowerCase(), selText + " is selected from the list - " + objName.toLowerCase() + " successfully", "Pass");
		else
			Reporter.log("Validate " + selText + " is selected from the list - " + objName.toLowerCase(), selText + " should be selected from the list - " + objName.toLowerCase(), selText + " is not selected from the list - " + objName.toLowerCase(), "Fail");
	}
	
	/**
	 * Get elements list
	 * 
	 * @param locator
	 * @param objName
	 * @return
	 */
	public List<WebElement> getWebElementsList(By locator){
		if(!driverType.trim().toUpperCase().contains("IOS")){
			getElementWhenVisible(locator);	//wait for locator to be visible.
		}
		return driver.findElements(locator);
	}
	
	/**
	 * Get text of all elements
	 * 
	 * @param locator
	 * @param objName
	 * @return
	 */
	public ArrayList<String> getTextOfAllWebElements(By locator, String objName){
		ArrayList<String> webElementsTextList = new ArrayList<String>();
		List<WebElement> webElementsList = getWebElementsList(locator);
		if(webElementsList != null){
			for(int i = 0; i< webElementsList.size(); i++){
				if(driverType.trim().toUpperCase().contains("IOS")){
					webElementsTextList.add(webElementsList.get(i).getText());
				}
				else{
					webElementsTextList.add(webElementsList.get(i).getAttribute("text"));
				}
			}
		}
		
		return webElementsTextList;
		
	}
	
	/**
	 * Get attributeValue of all elements
	 * 
	 * @param locator
	 * @param objName
	 * @return
	 */
	public ArrayList<String> getTextOfAllWebElements(By locator, String objName, String attributeName){
		ArrayList<String> webElementsTextList = new ArrayList<String>();
		List<WebElement> webElementsList = getWebElementsList(locator);
		if(webElementsList != null){
			for(int i = 0; i< webElementsList.size(); i++){
				webElementsTextList.add(webElementsList.get(i).getAttribute(attributeName));
			}
		}
		
		return webElementsTextList;
	}
	
	/**
	 * Scroll to exact text (Only in case of appium)
	 * 
	 * @param text
	 */
	public WebElement scrollToExactText(String text){
		WebElement we = null;
		try{
			 we = (appium).scrollToExact(text);
		}
		catch(Exception e){
			log.info("Scrollable view not found");
		}
		return we;
	}

	/**
	 * Scroll to text (Only in case of appium)
	 * 
	 * @param text
	 */
	public WebElement scrollToText(String text){
		WebElement we = null;
		try{
			we = (appium).scrollTo(text);
		}
		catch(Exception e){
			log.info("Scrollable view not found");
		}
		return we;
	}
	
	/**
	 * Launch app
	 * 
	 * @param appName
	 * @throws Exception 
	 */
	public void launchApp(String appName) throws Exception{
		switch(appName){
		case "Google Play Music":
			launchApp("com.google.android.music", "com.android.music.activitymanagement.TopLevelActivity");
			break;
		case "Google Play Store":
			launchApp("com.android.vending", "com.android.vending.AssetBrowserActivity");
			break;
		case "Message Classic":
			if(!isAppInstalled("com.thinkyeah.message")){
				installAppFromResources("Message_Classic.apk");
			}
			launchApp("com.thinkyeah.message", "com.android.mms.ui.ConversationList");
			break;
		case "TunnelBear":
			if(!isAppInstalled("com.tunnelbear.android")){
				installAppFromResources("tunnel_bear.apk");
			}
			sync(1000l);
			launchApp("com.tunnelbear.android", "com.tunnelbear.android.TbearMainActivity");
			break;
		}
	}
	
	/**
	 * Get promo code
	 * 
	 * @param promoCodeGeneraterUrl
	 * @return
	 * @throws IOException
	 */
	public String getPromoCode(String promoCodeGeneraterUrl) throws IOException{
		String response;
		response = getAPIResponse(promoCodeGeneraterUrl);
		int index = response.indexOf("http");
		String s3url = response.substring(index);
		response = getAPIResponse(s3url);
		String[] promoCode = response.split(",");
		return promoCode[0];
	}
	
	/**
	 * Get promo codes
	 * 
	 * @param promoCodeGeneraterUrl
	 * @return
	 * @throws IOException
	 */
	public List<List<String>> getPromoCodes(String promoCodeGeneraterUrl) throws IOException{
		String response;
		response = getAPIResponse(promoCodeGeneraterUrl);
		int index = response.indexOf("http");
		String s3url = response.substring(index);
		response = getAPIResponse(s3url);
		String[] result = response.split(",");
		List<List<String>> resp = new ArrayList<List<String>>();
		List<String> promoCode = new ArrayList<String>();
		int count = 0;
		for(int i = 0 ; i < result.length; i++){
			if(count == 3){
				if(i < result.length - 1)
					promoCode.add(result[i].substring(0, result[i].length() - 6));
				else
					promoCode.add(result[i]);
				resp.add(promoCode);
				promoCode = new ArrayList<String>();
				promoCode.add(result[i].substring(result[i].length() - 6, result[i].length()));
				count = 0;
			}
			else{
				promoCode.add(result[i]);
			}
			count++;
		}
		return resp;
	}
	
	/**
	 * Get API response
	 * 
	 * @param Url
	 * @return
	 * @throws IOException
	 */
	private String getAPIResponse(String Url) throws IOException{
		StringBuilder sb = new StringBuilder();
		URL url = new URL(Url.trim());
		String line;
		BufferedReader bufferedReader = null;
		InputStreamReader in = null;
		try
			{
					URLConnection connection = url.openConnection();
					in = new InputStreamReader(connection.getInputStream());
					bufferedReader = new BufferedReader(in);
					while ((line = bufferedReader.readLine())!=null)
						{
								sb.append(line);
						}
			}
		finally
			{
				if(bufferedReader != null)
					bufferedReader.close();
				if(in != null)
					in.close();
			}
		return sb.toString();
	}
	
	/**
	 * Generate hash
	 * 
	 * @param number
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public String generateHash(String number) throws NoSuchAlgorithmException{
	
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.reset();
		m.update(number.getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1,digest);
		String hashtext = bigInt.toString(16);
		
		// Now we need to zero pad it if you actually want the full 32 chars.
		while(hashtext.length() < 32 ){
		  hashtext = "0"+hashtext;
		}
	    
		return hashtext;
	}
	
	/**
	 * Set network settings
	 * 
	 * @param airplaneMode
	 * @param wifi
	 * @param data
	 * @throws Exception 
	 */
	public void setNetworkSettings(boolean airplaneMode, boolean wifi, boolean data) throws Exception{
		networkToggle(Arrays.asList("airplane", "wifi", "data"), Arrays.asList(airplaneMode, wifi, data));
		String wifiIp = "";
		String dataIp = "";
		if(wifi){
			int counter = 10;
			do{
    			wifiIp = getNetworkIPAddress(true);
    			dataIp = getNetworkIPAddress(false);
            	counter--;
			}while(wifiIp.trim().equalsIgnoreCase("") && !dataIp.trim().equalsIgnoreCase("") && counter > 0);
		}
		else if(data){
			int counter = 10;
			do{
    			wifiIp = getNetworkIPAddress(true);
    			dataIp = getNetworkIPAddress(false);
            	counter--;
			}while(!wifiIp.trim().equalsIgnoreCase("") && dataIp.trim().equalsIgnoreCase("") && counter > 0);
		}
	}

	/**
	 * Change string
	 * 
	 * @param s
	 * @return
	 */
	public String changeString(String s){
	   char[] characters = s.toCharArray();
	   int rand = (int)(Math.random() * s.length());
	   characters[rand] = '_';
	   return new String(characters);
	}
	
	/**
	 * Run command on terminal
	 * 
	 * @param _command
	 * @param arguments
	 * @param flagHandleQuoting
	 * @param wait
	 * @return
	 */
	public String runCommand(String _command, String[] arguments, boolean[] flagHandleQuoting, long wait){
		CommandLine command = new CommandLine(_command);
		for(int i = 0; i < arguments.length; i++){
			command.addArgument(arguments[i], flagHandleQuoting[i]);
		}
	  
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		PumpStreamHandler psh = new PumpStreamHandler(stdout);
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
		DefaultExecutor executor = new DefaultExecutor();
		executor.setStreamHandler(psh);
		try {
			executor.execute(command, resultHandler);
			while(stdout.toString().trim().equalsIgnoreCase("") && wait > 0){
				sync(1L);
				wait--;
			}
		} catch (IOException e1) {
			log.info("Threw a Exception in BaseUtil::runCommand, full stack trace follows:", e1);
		}
		
		return stdout.toString();
	}
	
	/**
	 * Get XML node value
	 * 
	 * @param path
	 * @param parentNode
	 * @param index
	 * @return
	 */
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
	    	log.info("Threw a Exception in BaseUtil::GetXMLNodeValue, full stack trace follows:", excep);
	    }
	    
	    return dict;
	}
	
	/**
	 * Run ADB command
	 * 
	 * @param arguments
	 * @param flagHandleQuoting
	 * @param wait
	 * @return
	 * @throws Exception 
	 */
	public String runADBCommand(String[] arguments, Boolean[] flagHandleQuoting, long wait, String... condition) throws Exception{
		if(Environment.get("user").trim().equalsIgnoreCase("")){
			String adbPath = "";
			if(Environment.get("autoDetectADBPath").trim().equalsIgnoreCase("true") || Environment.get("autoDetectADBPath").trim().equalsIgnoreCase("y") || Environment.get("autoDetectADBPath").trim().equalsIgnoreCase("yes")){
				if(OSValidator.shellType.trim().equalsIgnoreCase("cmd")){
					adbPath = runCommand(OSValidator.shellType, new String[]{"/c", "echo %ANDROID_HOME%"}, new boolean[]{false, false}, 2000);
					if(adbPath != null && !adbPath.trim().equalsIgnoreCase("")){
						adbPath += OSValidator.delimiter + "tools" + OSValidator.delimiter + "adb"; 
					}
				}
				else{
					adbPath = runCommand(OSValidator.shellType, new String[]{"-l", "-c", "which adb"}, new boolean[]{false, false, false}, 2000);
				}
			}
	  
			if(adbPath == null){
				return null;
			}
			else if(adbPath.trim().equalsIgnoreCase("")){
				if(!Environment.get("adbPath").trim().equalsIgnoreCase("")){
					adbPath = Environment.get("adbPath").trim();
				}
				else{
					log.info("ADB path not found");
					return null;
				}
			}
	  
			adbPath = adbPath.split("\n")[0].replace("//", "/");
	  
			CommandLine command = new CommandLine(adbPath);
			for(int i = 0; i < arguments.length; i++){
				command.addArgument(arguments[i], flagHandleQuoting[i]);
			}
	  
			ByteArrayOutputStream stdout = new ByteArrayOutputStream();
			PumpStreamHandler psh = new PumpStreamHandler(stdout);
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
			DefaultExecutor executor = new DefaultExecutor();
			executor.setStreamHandler(psh);
			try {
				executor.execute(command, resultHandler);
				assert condition.length <= 1;
				String finalvalue = condition.length > 0 ? condition[0] : "NOT NULL";
				switch(finalvalue.trim().toUpperCase()){
				case "NOT NULL":
					while(stdout.toString().trim().equalsIgnoreCase("") && wait > 0){
						sync(1L);
						wait--;
					} 
					break;
				case "WAIT":
					sync(wait);
					break;
				default:
					while(!stdout.toString().trim().contains(finalvalue.trim()) && wait > 0){
						sync(1L);
						wait--;
					}
				}
			} catch (IOException e1) {
				log.info("Threw a Exception in BaseUtil::runADBCommand, full stack trace follows:", e1);
			}
			
			return stdout.toString();
		}
		else{
			String args = "";
			boolean flag = false;
			for(int i = 0; i < arguments.length; i++){
				if(flag == false && arguments[i].trim().equalsIgnoreCase("shell")){
					flag = true;
					continue;
				}
				if(flag){
					if(arguments[i].trim().equalsIgnoreCase("|"))
						arguments[i] = "PIPE";
					args += arguments[i].trim() + " ";
				}
			}
			if(isAppInstalled("io.appium.networktoggle") == false){
				installAppFromResources("network_toggle.apk");
			}
			launchApp("io.appium.networktoggle", "io.appium.networktoggle.MainActivity -e run \"" + args.trim() + "\"");
			String result = getText(By.id("io.appium.networktoggle:id/notice_0"));
			return result;
		}
	}
	
	/**
	 * Function to read offline songs metadata currently stored in device
	 * 
	 * @return
	 * @throws Exception 
	 */
	public List<List<String>> getOfflineSongsMetaData() throws Exception{
		
		if(isAppInstalled("settings.appium.com.settings") == false){
			installAppFromResources("Appium_Settings.apk");
		}
		
		launchApp("settings.appium.com.settings", "settings.appium.com.settings.MainActivity");
		
		click(By.xpath(".//android.widget.ImageView[1]"), "Settings menu");
		
		click(By.xpath(".//android.widget.TextView[@text='View Offline Songs']"), "View offline songs menu");
		
		List<List<String>> SongsMetaData = new ArrayList<List<String>>();
		
		if(checkIfElementPresent(By.id("android:id/list"))){
			do{
				List<WebElement> lstSongs = getWebElementsList(By.id("android:id/text1"));
				List<String> nlstSongs = new ArrayList<String>();
				
				if(lstSongs == null){
					break;
				}
				
				for(int i = 0; i < lstSongs.size(); i++){
					nlstSongs.add(lstSongs.get(i).getText().trim());
					log.info(lstSongs.get(i).getText().trim());
					lstSongs.get(i).click();
					
					if(checkIfElementPresent(By.xpath(".//android.widget.TextView[@text='Song Detail']")) == false){
						continue;
					}
					
					swipe(By.xpath(".//android.widget.ScrollView"), "Up");
					
					List<String> songDetail = new ArrayList<String>();
					List<WebElement> lst = getWebElementsList(By.xpath(".//android.widget.ScrollView//android.widget.TextView"));
					if(lst == null){
						break;
					}
					
					for(int j = 0 ; j < lst.size(); j++){
						if(lst.get(j).getText().trim().contains("::")){
							songDetail.add(lst.get(j).getText().trim().split("::")[1].trim());
						}
						else{
							songDetail.add(lst.get(j).getText().trim());
						}
					}
					
					//Verify
					if(verifyContentinListofList(SongsMetaData, songDetail) == false){
						SongsMetaData.add(songDetail);
					}
					pressAndroidBackKey();
					getElementWhenVisible(By.id("android:id/list"), 2000L);
				}
				
				swipe(By.xpath(".//android.widget.ScrollView"), "Up");
				
				int flag = 0;
				
				if(nlstSongs.size() == lstSongs.size()){
					for(int i = 0, j = 0; i < lstSongs.size() || j < nlstSongs.size(); i++,j++){
						if(lstSongs.get(i).getText().trim().equals(nlstSongs.get(j).trim())){
							flag = 1;
						}
						else{
							flag = 0;
							break;
						}
					}
					
					if(flag == 1){
						break;
					}
				}
			}while(true);
		}
		
		//Close the settings app
		navigateBack();
		navigateBack();
		//forceStop("settings.appium.com.settings");
		
		return SongsMetaData;
	}
	
	/**
	 * Verify content in List<List<String>>
	 * 
	 * @param metaData
	 * @param detail
	 * @return
	 */
	public boolean verifyContentinListofList(List<List<String>> metaData, List<String> detail){
		boolean success = false;
		String console = "";
		
		int flag = 0;
		if(metaData != null){
			for(int i = 0; i < metaData.size(); i++){
				for(int j = 0; j < metaData.get(i).size(); j++){
					if(j < detail.size() && metaData.get(i).get(j).trim().equals(detail.get(j))){
						console += detail.get(j) + ";";
						flag = 1;
					}
					else{
						flag = 0;
						break;
					}
				}
				if(flag == 1){
					if(metaData.get(i).size() == detail.size())
						break;
					else
						flag = 0;
				}
			}
		}
		
		if(flag == 1){
			log.info(console.substring(0, console.length() - 1));
			success = true;
		}
		else{
			success = false;
		}
		
		return success;
	}
	
	/**
	 * Function to read jsonObject from given URL(Http(s))
	 * 
	 * @param url
	 * @param sslSecurity
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public JSONObject readJsonFromUrl(String url, boolean sslSecurity) throws IOException, JSONException {
		InputStream is = null;
		JSONObject json = null;
		
		try {            
	      final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	          public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
	          }
	          public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
	          }
	          public X509Certificate[] getAcceptedIssuers() {
	              return null;
	          }
	      } };
	      
	      final SSLContext sslContext = SSLContext.getInstance( "SSL" );
	      sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
	      final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();            
	      
	      final URLConnection urlCon = new URL(url).openConnection();            
	      urlCon.setRequestProperty("Request Method", "GET");
	      urlCon.setRequestProperty("Accept", "application/json"); 
	      
	      if(sslSecurity){
	    	  ( (HttpsURLConnection) urlCon ).setSSLSocketFactory( sslSocketFactory );
	      }
	      is = urlCon.getInputStream();
	              	
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	   	  String jsonText = readAll(rd);
		  json = new JSONObject(jsonText);  	    
	      
	  } catch(ConnectException e){
		  //Do Nothing
	  } catch ( final Exception e ) {
		  log.info("Threw a Exception in BaseUtil::readJsonFromUrl, full stack trace follows:", e);
	  } finally {
		  if(is != null)
			  is.close();
	  }
		return json;
	}
  
	/**
	 * Read text from file reader
	 * 
	 * @param rd
	 * @return
	 * @throws IOException
	 */
	public String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	}
	
	/**
	 * Press Android Back Key
	 */
	public void pressAndroidBackKey(){
		sendKeyEvent(AndroidKeyCode.BACK);
		Reporter.log("Verify android back button is pressed", "Android back button should be pressed", "Android back button is pressed successfully" , "Pass");
	}
	
	/**
	 * Scroll To
	 * 
	 * @param itemListLocator
	 * @param itemListObjName
	 * @param viewLocator
	 * @param fromText
	 * @param toText
	 * @return
	 */
	public WebElement scrollTo(By itemListLocator, String itemListObjName, By viewLocator, String fromText, String toText, Boolean screenPrint){
		WebElement webElement = getElementWhenVisible(viewLocator);
		WebElement tabBar = null;
		if(driverType.trim().toUpperCase().contains("IOS")){
			tabBar = getElementWhenVisible(By.className("UIATabBar"));
		}
		Dictionary.put("FROM_DIRECTION", "");
		Dictionary.put("TO_DIRECTION", "");
		WebElement element = null;
		String swipeDirection = "Up", first = "", last = "";
		int found = 0;
		do{
			found = 0;
			List<WebElement> lst = getWebElementsList(itemListLocator);
			if(lst == null || lst.size() == 0)
				break;
			if(last.equalsIgnoreCase(lst.get(lst.size() - 1).getText().trim()) && first.equalsIgnoreCase(lst.get(0).getText().trim())){
				if(driverType.trim().toUpperCase().contains("IOS")){
					break;
				}
				else{
					if(swipeDirection.trim().equalsIgnoreCase("UP"))
						swipeDirection = "Down";
					else
						break;
					swipe(webElement, swipeDirection);
					lst = getWebElementsList(itemListLocator);
					if(lst == null || lst.size() == 0)
						break;
					if(last.equalsIgnoreCase(lst.get(lst.size() - 1).getText().trim()) && first.equalsIgnoreCase(lst.get(0).getText().trim())){
						break;
					}
				}
			}
			
			for(int i = 0 ; i < lst.size(); i++){
				if(!toText.trim().equalsIgnoreCase("") && lst.get(i).getText().trim().equalsIgnoreCase(toText.trim())){
					Dictionary.put("TO_DIRECTION", swipeDirection.trim());
				}
				if(lst.get(i).getText().trim().equalsIgnoreCase(fromText.trim())){
					Dictionary.put("FROM_DIRECTION", swipeDirection.trim());
					element = lst.get(i); 
					found = 1;
					break;
				}
			}
			
			if(driverType.trim().toUpperCase().contains("IOS")){
				 if(found == 1){
					 while(!checkIfElementPresent(element, 1) && !checkIfElementPresent(lst.get(lst.size() - 1), 1)){
						 swipe(webElement, tabBar, swipeDirection);
					 }
					 if(checkIfElementPresent(element, 1))
						 break;
					 else{
						 if(swipeDirection.trim().equalsIgnoreCase("UP"))
							 swipeDirection = "Down";
						 else
							 break;
						 do{
							 swipe(webElement, tabBar, swipeDirection);
						 }while(!checkIfElementPresent(element, 1));
					 }
				 }
			 }
			
			if(found == 0){
				last = lst.get(lst.size() - 1) == null ? lst.get(lst.size() - 2).getText().trim() : lst.get(lst.size() - 1).getText().trim();
				first = lst.get(0).getText().trim();
				if(driverType.trim().toUpperCase().contains("IOS")){
					while(!checkIfElementPresent(lst.get(lst.size() - 1), 1)){
						 swipe(webElement, tabBar, swipeDirection);
					}
				}
				else{
					swipe(webElement, swipeDirection);
				}
			}
		}while(found == 0);
		
		if(screenPrint){
			if(element != null)
				Reporter.log("Verify element is found", "Element should be found in the given list", "Element - '" + fromText + "' is found in the given list successfully", "Pass");
			else{
				Reporter.log("Verify element is found", "Element should be found in the given list", "Element - '" + fromText + "' not found in the given list", "Fail");
			}
		}
		return element;
	}
	
	/**
	 * Launch app case of android
	 * 
	 * @param packageName
	 * @param launchActivityName
	 * @throws Exception
	 */
	public void launchApp(String packageName, String launchActivityName) throws Exception{
		android.startActivity(packageName, launchActivityName);
	}
	
	/**
	 * Launch app case of android
	 * 
	 * @param appPackage
	 * @param appActivity
	 * @param appWaitPackage
	 * @param appWaitActivity
	 * @throws Exception
	 */
	public void launchApp(String appPackage, String appActivity, String appWaitPackage, String appWaitActivity) throws Exception{
		android.startActivity(appPackage, appActivity, appWaitPackage, appWaitActivity);
	}
	
	/**
	 * Launch chrome app in case of android
	 */
	public void launchChrome(){
		try {
			launchApp("com.android.chrome", "com.google.android.apps.chrome.Main");
			Reporter.log("Verify launch chrome", "Chrome should be launched", "Chrome is launched successfully", "Pass");
		} catch (Exception e) {
			log.info("Threw a Exception in BaseUtil::launchChrome, full stack trace follows:", e);
		}
	}
	
	/**
	 * Launch env for any URL
	 * 
	 * @param strUrl
	 * @return
	 */
	public boolean launchUrl(String strUrl, boolean clearCookie){
		try {
			if(driverType.trim().toUpperCase().contains("IOS")){
				try{
					if(clearCookie)
						((JavascriptExecutor) driver).executeScript("window.sessionStorage.clear();");
				}catch(Exception e){
					log.info("Threw a Exception in BaseUtil::launchUrl, full stack trace follows:", e);
				}							
			}
			
			if(clearCookie)
				driver.manage().deleteAllCookies();
			
			//open env according to given URL
			driver.get(strUrl);
			        
			if(!driverType.trim().toUpperCase().contains("ANDROID") && !driverType.trim().toUpperCase().contains("IOS")){
				driver.manage().window().maximize();
			}		

			Reporter.log("Launch: "+strUrl, strUrl+" should be launched", strUrl+ " is launched successfully", "Pass");
			return true;

		} catch (Exception e) {
			log.info("Threw a Exception in BaseUtil::launchUrl, full stack trace follows:", e);
			Reporter.log("Launch: "+strUrl, "Exception occurred","Exception: " + e, "Fail");
			return false;
		}       
	}
	
	/**
	 * Function to validate the selected option in the list
	 * 
	 * @param webElmtProp
	 * @param strObjName
	 * @param expectedValue
	 * @return
	 */
	public boolean validateSelectedOptionFromList(By webElmtProp, String strObjName, String expectedValue){
		try{
			//Get WebElement
			WebElement objList = getElementWhenVisible(webElmtProp);

			//Set Select Element
			Select select = new Select(objList);
			//Get the selected value from the drop down
			String actualValue = select.getFirstSelectedOption().getText().trim();
			log.info("actual value: " + actualValue);
			log.info("expected value: " + expectedValue.trim());

			//Check if actual selected value is equal to expected value
			if(actualValue.trim().equalsIgnoreCase(expectedValue.trim())){
				Reporter.log("Validate option - " + expectedValue.toLowerCase() + " is selected from the list - " + strObjName.toLowerCase(), "Option - " + expectedValue.toLowerCase() + " should be selected from the list - " + strObjName.toLowerCase(), "Expected value matches actual value - " + actualValue.toLowerCase(), "Pass");
				return true;        		
			}else{
				Reporter.log("Validate option - " + expectedValue.toLowerCase() + " is selected from the list - " + strObjName.toLowerCase(), "Option - " + expectedValue.toLowerCase() + " should be selected from the list - " + strObjName.toLowerCase(), "Actual value selected is - " + actualValue.toLowerCase(), "Fail");
				return false;
			}
		} catch (Exception e){
			log.info("Threw a Exception in BaseUtil::validateSelectedOptionFromList, full stack trace follows:", e);
			Reporter.log("Weblist: "+strObjName, "Exception occurred","Exception: " + e, "Fail");
			return false;
		}
	}
	
	/**
	 * waiting the specified time
	 * 
	 * @param sTime
	 */
	public void sync(Long sTime)
	{
		try {
			Thread.sleep(sTime);
		} catch (InterruptedException e) {			
			log.info("Threw a Exception in BaseUtil::sync, full stack trace follows:", e);
		}
	}
	
	/**
	 * check if Alert popup is coming and click on OK (accept) button
	 * 
	 * @param sAction
	 */
	public void checkAlert(String sAction)
	{
		try{
			WebDriverWait wait = new WebDriverWait(driver,1);
			wait.until(ExpectedConditions.alertIsPresent());

			Alert alert = driver.switchTo().alert();
			if(sAction.equalsIgnoreCase("accept"))
				alert.accept();
			else if(sAction.equalsIgnoreCase("decline"))
				alert.dismiss();
		}
		catch (Exception e){
			log.info("Threw a Exception in BaseUtil::checkAlert, full stack trace follows:", e);
		}
	}	
	
	/**
	 * navigate back to previous page
	 */
	public void browserBackButton() {			
		driver.navigate().back();

	}
	
	/**
	 * switch to window based on index
	 * 
	 * @param iIndex
	 */
	public void switchToWindow(Integer iIndex){
		Set<String> collWindowHandles = driver.getWindowHandles();
		if(collWindowHandles.size() < iIndex + 1){
			Reporter.log("SwitchToWindow", "Specified index out of range.", "Available Windows: " + collWindowHandles.size() + "Specified Index: " + iIndex , "Fail");
		}
		else{
			Iterator<String> iter = collWindowHandles.iterator();
			for(int i=0;i<collWindowHandles.size();i++){    			
				String sWindowHandle = iter.next();
				if(i == iIndex){
					driver.switchTo().window(sWindowHandle);
					break;
				}
			}
		}
	}
	
	/**
	 * switch to window based on window name
	 * 
	 * @param windowName
	 */
	public void switchToWindow(String windowName){
		Set<String> collWindowHandles = driver.getWindowHandles();
		boolean flag = false;
		Iterator<String> iter = collWindowHandles.iterator();
		for(int i=0;i<collWindowHandles.size();i++){    			
			String sWindowHandle = iter.next();
			if(sWindowHandle.trim().equalsIgnoreCase(windowName) || sWindowHandle.trim().toLowerCase().contains(windowName.trim().toLowerCase())){
				flag = true;
				driver.switchTo().window(sWindowHandle);
				break;
			}
		}
		
		if(flag == false){
			Reporter.log("Switch to window", "Specified window should be found", "Specified window not found", "Fail");
		}
	}
	
	/**
	 * switch to window based on url
	 * 
	 * @param windowName
	 */
	public void switchToWindowBasedOnUrl(String url){
		Set<String> collWindowHandles = driver.getWindowHandles();
		boolean flag = false;
		Iterator<String> iter = collWindowHandles.iterator();
		String currentUrl = driver.getCurrentUrl();
		for(int i=0; i<collWindowHandles.size(); i++){    			
			String sWindowHandle = iter.next();
			driver.switchTo().window(sWindowHandle);
			if(driver.getCurrentUrl().trim().equalsIgnoreCase(url.trim())){
				flag = true;
				break;
			}
		}
		
		if(flag == false){
			switchToWindowBasedOnUrl(currentUrl);
			Reporter.log("Switch to window", "Specified window should be found", "Specified window not found", "Fail");
		}
	}
	
	/**
	 * Function to read SMS based on address and body
	 * 
	 * @param SMSAddress
	 * @param SMSBody
	 * @param unread
	 * @return
	 * @throws Exception 
	 */
	public String getSMSBody(String SMSAddress, String SMSBody, Boolean unread) throws Exception{
		
		if(isAppInstalled("settings.appium.com.settings") == false){
			installAppFromResources("Appium_Settings.apk");
		}
		
		launchApp("settings.appium.com.settings", "settings.appium.com.settings.MainActivity");
		
		click(By.xpath(".//android.widget.ImageView[1]"), "Settings menu");
		
		click(By.xpath(".//android.widget.TextView[@text='View Messages']"), "View messages menu");
		
		if(!SMSAddress.trim().equalsIgnoreCase("")){
			type(By.id("settings.appium.com.settings:id/editText1"), "SMS address", SMSAddress.trim());
		}
		
		if(!SMSBody.trim().equalsIgnoreCase("")){
			type(By.id("settings.appium.com.settings:id/editText2"), "SMS body", SMSBody.trim());
		}
		
		if(unread){
			click(By.id("settings.appium.com.settings:id/checkBox"), "Unread messages checkbox");
		}
		
		click(By.id("settings.appium.com.settings:id/search1"), "Search button");
		
		String message = null;
		
		if(checkIfElementPresent(By.id("android:id/list"))){
			List<WebElement> lstMessages = getWebElementsList(By.id("android:id/text1"));
			java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd-MMM-yyyy");
            String dateString = formatter.format(new java.util.Date());
            
			for(int i = 0; i < lstMessages.size(); i++){
				if(SMSAddress.trim().equalsIgnoreCase("")){
					if(lstMessages.get(i).getText().trim().contains(" : " + dateString)){
						lstMessages.get(i).click();
						getElementWhenVisible(By.xpath(".//android.view.View[@resource-id='settings.appium.com.settings:id/action_bar']/android.widget.TextView"), 2000L);
						message = getAttribute(By.id("settings.appium.com.settings:id/messageslist_detail"), "text");
						break;
					}
				}
				else{
					if(lstMessages.get(i).getText().trim().toLowerCase().contains(SMSAddress.trim().toLowerCase()) && lstMessages.get(i).getText().trim().toLowerCase().contains(" : " + dateString.trim().toLowerCase())){
						lstMessages.get(i).click();
						getElementWhenVisible(By.xpath(".//android.view.View[@resource-id='settings.appium.com.settings:id/action_bar']/android.widget.TextView"), 2000L);
						message = getAttribute(By.id("settings.appium.com.settings:id/messageslist_detail"), "text");
						break;
					}
				}
			}
		}
		
		//Close the settings app
		navigateBack();
		navigateBack();
		navigateBack();
		navigateBack();
		//forceStop("settings.appium.com.settings");
		
		if(message != null)
			Reporter.log("Verify SMS text is read", "SMS text should be read", "SMS text is read successfully - <br/>'" + message + "'", "Done");
		else{
			Reporter.log("Verify SMS text is read", "SMS text should be read", "SMS text not found", "Fail");
		}
		return message;
	}
	
	/**
	 * Toggle swipe direction
	 * 
	 * @param direction
	 * @return
	 */
	public String toggle(String direction){
		
		String toggleDirection = "";
		
		switch(direction.trim().toUpperCase()){
			case "UP":
				toggleDirection = "DOWN";
				break;
			case "DOWN":
				toggleDirection = "UP";
				break;
			case "LEFT":
				toggleDirection = "RIGHT";
				break;
			case "RIGHT":
				toggleDirection = "LEFT";
				break;
		}
		
		return toggleDirection;
	}
	
	/**
	 * Swipe function for swiping the entire page
	 * 
	 * @param SwipeDirection
	 */
	public void swipe(String SwipeDirection) { 
		
		Dimension size = driver.manage().window().getSize();
		if(driverType.trim().toUpperCase().contains("IOS")){
			if (SwipeDirection.equalsIgnoreCase("Up")){
				appium.swipe((int) (size.width*0.8), (int) (size.height*0.8), (int) (size.width*0.8), (int) (size.height*0.04), 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Down")) {
				appium.swipe((int) (size.width*0.8), (int) (size.height*0.04), (int) (size.width*0.8), (int) (size.height*0.8), 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Right")) {
				appium.swipe((int) (size.width*0.1), (int) (size.height*0.8), (int) (size.width*0.8), (int) (size.height*0.8), 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Left")) {
				appium.swipe((int) (size.width*0.8), (int) (size.height*0.8), (int) (size.width*0.1), (int) (size.height*0.8), 2000);
			}
			else {
				log.info("Not a valid direction passed");
			}
		}
		else{
			if (SwipeDirection.equalsIgnoreCase("Up")){
				appium.swipe((int) (size.width*0.8), (int) (size.height*0.8), (int) (size.width*0.8), (int) (size.height*0.04), 4000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Down")) {
				appium.swipe((int) (size.width*0.8), (int) (size.height*0.04), (int) (size.width*0.8), (int) (size.height*0.8), 4000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Right")) {
				appium.swipe((int) (size.width*0.1), (int) (size.height*0.8), (int) (size.width*0.8), (int) (size.height*0.8), 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Left")) {
				appium.swipe((int) (size.width*0.8), (int) (size.height*0.8), (int) (size.width*0.1), (int) (size.height*0.8), 2000);
			}
			else {
				log.info("Not a valid direction passed");
			}
		}
	}
	
	/**
	 * Swipe based on webelement
	 * 
	 * @param driver
	 * @param we
	 * @param SwipeDirection
	 */
	public void swipe(WebElement we, String SwipeDirection) { 
		Dimension size = we.getSize();
		
		MobileElement me = ((MobileElement) we);
		
		if(driverType.trim().toUpperCase().contains("IOS")){
			if (SwipeDirection.trim().equalsIgnoreCase("Up")){
				appium.swipe(me.getCenter().getX(), me.getLocation().getY() + size.height - 1, me.getCenter().getX(), me.getLocation().getY() + 1, 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Down")) {
				appium.swipe(me.getCenter().getX(), me.getLocation().getY() + 1, me.getCenter().getX(), me.getLocation().getY() + size.height - 1, 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Right")) {
				appium.swipe(me.getLocation().getX() + 1, me.getCenter().getY(), me.getLocation().getX() + size.width - 1, me.getCenter().getY(), 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Left")) {
				appium.swipe(me.getLocation().getX() + size.width - 1, me.getCenter().getY(), me.getLocation().getX() + 1, me.getCenter().getY(), 2000);
			}
			else {
				log.info("Not a valid direction passed");
			}
		}
		else{
			if (SwipeDirection.trim().equalsIgnoreCase("Up")){
				appium.swipe(me.getCenter().getX(), me.getLocation().getY() + size.height - 1, me.getCenter().getX(), me.getLocation().getY() + 1, 4000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Down")) {
				appium.swipe(me.getCenter().getX(), me.getLocation().getY() + 1, me.getCenter().getX(), me.getLocation().getY() + size.height - 1, 4000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Right")) {
				appium.swipe(me.getLocation().getX() + 1, me.getCenter().getY(), me.getLocation().getX() + size.width - 1, me.getCenter().getY(), 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Left")) {
				appium.swipe(me.getLocation().getX() + size.width - 1, me.getCenter().getY(), me.getLocation().getX() + 1, me.getCenter().getY(), 2000);
			}
			else {
				log.info("Not a valid direction passed");
			}
		}
	}
	
	/**
	 * Swipe based on webelement
	 * 
	 * @param driver
	 * @param we
	 * @param SwipeDirection
	 */
	public void swipe(By webElemProp, String SwipeDirection) { 
		WebElement we = getElementWhenVisible(webElemProp);
		Dimension size = we.getSize();
		
		MobileElement me = ((MobileElement) we);
		
		if(driverType.trim().toUpperCase().contains("IOS")){
			if (SwipeDirection.trim().equalsIgnoreCase("Up")){
				appium.swipe(me.getCenter().getX(), me.getLocation().getY() + size.height - 1, me.getCenter().getX(), me.getLocation().getY() + 1, 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Down")) {
				appium.swipe(me.getCenter().getX(), me.getLocation().getY() + 1, me.getCenter().getX(), me.getLocation().getY() + size.height - 1, 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Right")) {
				appium.swipe(me.getLocation().getX() + 1, me.getCenter().getY(), me.getLocation().getX() + size.width - 1, me.getCenter().getY(), 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Left")) {
				appium.swipe(me.getLocation().getX() + size.width - 1, me.getCenter().getY(), me.getLocation().getX() + 1, me.getCenter().getY(), 2000);
			}
			else {
				log.info("Not a valid direction passed");
			}
		}
		else{
			if (SwipeDirection.trim().equalsIgnoreCase("Up")){
				appium.swipe(me.getCenter().getX(), me.getLocation().getY() + size.height - 1, me.getCenter().getX(), me.getLocation().getY() + 1, 4000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Down")) {
				appium.swipe(me.getCenter().getX(), me.getLocation().getY() + 1, me.getCenter().getX(), me.getLocation().getY() + size.height - 1, 4000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Right")) {
				appium.swipe(me.getLocation().getX() + 1, me.getCenter().getY(), me.getLocation().getX() + size.width - 1, me.getCenter().getY(), 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Left")) {
				appium.swipe(me.getLocation().getX() + size.width - 1, me.getCenter().getY(), me.getLocation().getX() + 1, me.getCenter().getY(), 2000);
			}
			else {
				log.info("Not a valid direction passed");
			}
		}
	}
	
	/**
	 * Swipe based on selected first and second webelement
	 * 
	 * @param driver
	 * @param first
	 * @param second
	 * @param SwipeDirection
	 */
	public void swipe(WebElement first, WebElement second, String SwipeDirection) {
		MobileElement meFirst = (MobileElement)first;
		int firstX = meFirst.getLocation().getX();
		int firstY = meFirst.getLocation().getY();
		
		MobileElement meSecond = (MobileElement)second;
		int secondX = meSecond.getLocation().getX();
		int secondY = meSecond.getLocation().getY();
		
		if(driverType.trim().toUpperCase().contains("IOS")){
			if (SwipeDirection.trim().equalsIgnoreCase("Up")){
				appium.swipe(meFirst.getCenter().getX(), secondY - 10, meFirst.getCenter().getX(), firstY + 1, 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Down")) {
				appium.swipe(meFirst.getCenter().getX(), secondY + meSecond.getSize().height + 10, meFirst.getCenter().getX(), firstY + meFirst.getSize().height - 1, 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Right")) {
				appium.swipe(secondX + meSecond.getSize().width + 10, meFirst.getCenter().getY(), firstX + meFirst.getSize().width - 1, meFirst.getCenter().getY(), 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Left")) {
				appium.swipe(secondX - 10, meFirst.getCenter().getY(), firstX + 1, meFirst.getCenter().getY(), 2000);
			}
			else {
				log.info("Not a valid direction passed");
			}
		}
		else{
			if (SwipeDirection.trim().equalsIgnoreCase("Up")){
				appium.swipe(meFirst.getCenter().getX(), secondY - 1, meFirst.getCenter().getX(), firstY + 1, 4000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Down")) {
				appium.swipe(meFirst.getCenter().getX(), secondY + meSecond.getSize().height + 1, meFirst.getCenter().getX(), firstY + meFirst.getSize().height - 1, 4000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Right")) {
				appium.swipe(secondX + meSecond.getSize().width + 1, meFirst.getCenter().getY(), firstX + meFirst.getSize().width - 1, meFirst.getCenter().getY(), 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Left")) {
				appium.swipe(secondX - 1, meFirst.getCenter().getY(), firstX + 1, meFirst.getCenter().getY(), 2000);
			}
			else {
				log.info("Not a valid direction passed");
			}
		}
	}
	
	/**
	 * Swipe based on selected first and second webelement
	 * 
	 * @param driver
	 * @param first
	 * @param second
	 * @param SwipeDirection
	 */
	public void swipe(By firstElemProp, By secondElemProp, String SwipeDirection) { 
		WebElement first = getElementWhenVisible(firstElemProp);
		WebElement second = getElementWhenVisible(secondElemProp);
		
		MobileElement meFirst = (MobileElement)first;
		int firstX = meFirst.getLocation().getX();
		int firstY = meFirst.getLocation().getY();
		
		MobileElement meSecond = (MobileElement)second;
		int secondX = meSecond.getLocation().getX();
		int secondY = meSecond.getLocation().getY();
		
		if(driverType.trim().toUpperCase().contains("IOS")){
			if (SwipeDirection.trim().equalsIgnoreCase("Up")){
				appium.swipe(meFirst.getCenter().getX(), secondY - 10, meFirst.getCenter().getX(), firstY + 1, 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Down")) {
				appium.swipe(meFirst.getCenter().getX(), secondY + meSecond.getSize().height + 10, meFirst.getCenter().getX(), firstY + meFirst.getSize().height - 1, 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Right")) {
				appium.swipe(secondX + meSecond.getSize().width + 10, meFirst.getCenter().getY(), firstX + meFirst.getSize().width - 1, meFirst.getCenter().getY(), 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Left")) {
				appium.swipe(secondX - 10, meFirst.getCenter().getY(), firstX + 1, meFirst.getCenter().getY(), 2000);
			}
			else {
				log.info("Not a valid direction passed");
			}
		}
		else{
			if (SwipeDirection.trim().equalsIgnoreCase("Up")){
				appium.swipe(meFirst.getCenter().getX(), secondY - 1, meFirst.getCenter().getX(), firstY + 1, 4000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Down")) {
				appium.swipe(meFirst.getCenter().getX(), secondY + meSecond.getSize().height + 1, meFirst.getCenter().getX(), firstY + meFirst.getSize().height - 1, 4000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Right")) {
				appium.swipe(secondX + meSecond.getSize().width + 1, meFirst.getCenter().getY(), firstX + meFirst.getSize().width - 1, meFirst.getCenter().getY(), 2000);
			}
			else if (SwipeDirection.equalsIgnoreCase("Left")) {
				appium.swipe(secondX - 1, meFirst.getCenter().getY(), firstX + 1, meFirst.getCenter().getY(), 2000);
			}
			else {
				log.info("Not a valid direction passed");
			}
		}
	}
	
	/**
	 * Function to call USSD number and get output
	 * 
	 * @param number
	 * @return
	 * @throws Exception 
	 */
	public String callUSSDNumber(String number) throws Exception{
		
		if(Environment.get("user").trim().equalsIgnoreCase("")){
			runADBCommand(new String[]{"-s", Environment.get("udid"), "shell", "am", "start", "-a", "android.intent.action.CALL", "-d", "tel://" + number.trim().replace("#", "%23")}, new Boolean[]{false, false, false, false, false, false, false, false, false}, 2000);
			String message = "";
			if(checkIfElementPresent(By.id("android:id/message"))){
				message = getAttribute(By.id("android:id/message"), "text");
				Reporter.log("Verify USSD message is displayed", "USSD message should be displayed", "USSD message is displayed successfully", "Pass");
				click(By.id("android:id/button1"), "Ok button");
			}
		
			if(message == null){
				Reporter.log("Verify USSD message is displayed", "USSD message should be displayed", "USSD message is not displayed", "Fail");
			}
		
			return message;
		}
		else{
			if(isAppInstalled("settings.appium.com.settings") == false){
				installAppFromResources("Appium_Settings.apk");
			}
			
			launchApp("settings.appium.com.settings", "settings.appium.com.settings.MainActivity");
			
			click(By.xpath(".//android.widget.ImageView[1]"), "Settings menu");
			
			click(By.xpath(".//android.widget.TextView[@text='Call USSD Number']"), "Call USSD number menu");
			
			String message = null;
			
			if(!number.trim().equalsIgnoreCase("")){
				type(By.id("settings.appium.com.settings:id/editText1"), "USSD number", number.trim());
				click(By.id("settings.appium.com.settings:id/search2"), "Call button");
				
				if(checkIfElementPresent(By.id("android:id/message"))){
					message = getAttribute(By.id("android:id/message"), "text");
					Reporter.log("Verify USSD message is displayed", "USSD message should be displayed", "USSD message is displayed successfully", "Pass");
					click(By.id("android:id/button1"), "Ok button");
				}
			}
			
			//Close the settings app
			navigateBack();
			navigateBack();
			//forceStop("settings.appium.com.settings");
			
			if(message == null){
				Reporter.log("Verify USSD message is displayed", "USSD message should be displayed", "USSD message is not displayed", "Fail");
			}
			
			return message;
		}
	}
	
	/**
	 * Drag element - overloaded
	 * 
	 * @param fromText
	 * @param toText
	 * @param dragLocator
	 * @param objType
	 * @param objName
	 * @param itemListLocator
	 * @param itemListObjName
	 * @param viewLocator
	 * @param parentlevel
	 * @param swipeDirection
	 * @return
	 */
	public boolean drag(String fromText, String toText, By dragLocator, String objType, String objName, By itemListLocator, String itemListObjName, By viewLocator, int parentlevel, String swipeDirection) {
		String className;
		if(driverType.trim().toUpperCase().contains("ANDROID")){
			className = "android.widget.TextView";
		}
		else{
			className = "UIAStaticText";
		}
		WebElement dragFrom = null, dragTo = null;
		int found = 0; String last = "", first = "";
		
		if(scrollTo(itemListLocator, itemListObjName, viewLocator, fromText, toText, false) == null){
			Reporter.log("Verify drag happens from one element to another", "Drag should be done from one element to another element", "One of the element found null" ,"Fail");
			return false;
		}
		
		if(Dictionary.get("TO_DIRECTION").trim().equalsIgnoreCase(Dictionary.get("FROM_DIRECTION"))){
			swipeDirection = toggle(Dictionary.get("TO_DIRECTION").trim());
		}
		else{
			if(!Dictionary.get("TO_DIRECTION").trim().equalsIgnoreCase("")){
				swipeDirection = Dictionary.get("TO_DIRECTION").trim();
			}
		}
		
		String currentDirection = swipeDirection.trim();
		
		do{
			found = 0;
			dragFrom = null;
			dragTo = null;
			List<WebElement> lst = getWebElementsList(itemListLocator);
			if(lst == null || lst.size() == 0)
				break;
			if(last.equalsIgnoreCase(lst.get(lst.size() - 1).getText().trim()) && first.equalsIgnoreCase(lst.get(0).getText().trim())){
				if(swipeDirection.trim().equalsIgnoreCase(currentDirection.trim()))
					swipeDirection = toggle(swipeDirection);
				else{
					break;
				}
				String parent = getParentElement(itemListLocator, className, "Item", parentlevel, 1);
				dragTo = getSingleChildObject(parent, dragLocator, objType, objName);
				parent = getParentElement(itemListLocator, className, "Item", parentlevel, lst.size() - 1);
				
				if(driver.findElement(By.xpath(getXpath(itemListLocator, className, "Item", lst.size() - 1))).getText().trim().equalsIgnoreCase(fromText)){
					dragFrom = getSingleChildObject(parent, dragLocator, objType, objName);
				}
				else{
					if(scrollTo(itemListLocator, itemListObjName, viewLocator, fromText, toText, false) == null){
						Reporter.log("Verify drag happens from one element to another", "Drag should be done from one element to another element", "One of the element found null" ,"Fail");
						return false;
					}
					
					if(Dictionary.get("TO_DIRECTION").trim().equalsIgnoreCase(Dictionary.get("FROM_DIRECTION"))){
						swipeDirection = toggle(Dictionary.get("TO_DIRECTION").trim());
					}
					else{
						if(!Dictionary.get("TO_DIRECTION").trim().equalsIgnoreCase("")){
							swipeDirection = Dictionary.get("TO_DIRECTION").trim();
						}
					}
					continue;
				}
				
				if(dragFrom != null && dragTo != null){
					drag(dragFrom, dragTo);
				}
				else{
					Reporter.log("Verify drag happens from one element to another", "Drag should be done from one element to another element", "One of the element found null" ,"Fail");
					return false;
				}
				parent = getParentElement(itemListLocator, className, "Item", parentlevel, 2);
				swipe(viewLocator, By.xpath(parent), swipeDirection);
				lst = getWebElementsList(itemListLocator);
				if(lst == null || lst.size() == 0)
					break;
				if(last.equalsIgnoreCase(lst.get(lst.size() - 1).getText().trim()) && first.equalsIgnoreCase(lst.get(0).getText().trim())){
					break;
				}
			}
			for(int i = 0 ; i < lst.size(); i++){
				if(found == 2)
					break;
				if(lst.get(i).getText().trim().equalsIgnoreCase(toText)){
					String parent = getParentElement(itemListLocator, className, "Item", parentlevel, i + 1);
					dragTo = getSingleChildObject(parent, dragLocator, objType, objName);
					if(found == 0)
						found = 1;
					else if(found == 1){
						found = 2;
						break;
					}
				}
				if(lst.get(i).getText().trim().equalsIgnoreCase(fromText)){
					String parent = getParentElement(itemListLocator, className, "Item", parentlevel, i + 1);
					dragFrom = getSingleChildObject(parent, dragLocator, objType, objName);
					if(found == 0)
						found = 1;
					else if(found == 1){
						found = 2;
						break;
					}
				}
			}
			if(dragFrom == null || found == 0){
				if(scrollTo(itemListLocator, itemListObjName, viewLocator, fromText, toText, false) == null){
					Reporter.log("Verify drag happens from one element to another", "Drag should be done from one element to another element", "One of the element found null" ,"Fail");
					return false;
				}
				if(Dictionary.get("TO_DIRECTION").trim().equalsIgnoreCase(Dictionary.get("FROM_DIRECTION"))){
					swipeDirection = toggle(Dictionary.get("TO_DIRECTION").trim());
				}
				else{
					if(!Dictionary.get("TO_DIRECTION").trim().equalsIgnoreCase("")){
						swipeDirection = Dictionary.get("TO_DIRECTION").trim();
					}
				}
				continue;
			}
			if(found == 1){
				String parent = "";
				if(swipeDirection.trim().equalsIgnoreCase("UP") || swipeDirection.trim().equalsIgnoreCase("LEFT")){
					last = lst.get(lst.size() - 1).getText().trim();
					first = lst.get(0).getText().trim();
					parent = getParentElement(itemListLocator, className, "Item", parentlevel, lst.size());
				}
				else{
					first = lst.get(0).getText().trim();
					last = lst.get(lst.size() - 1).getText().trim();
					parent = getParentElement(itemListLocator, className, "Item", parentlevel, 1);
				}
				
				dragTo = getSingleChildObject(parent, dragLocator, objType, objName);
				if(dragFrom != null && dragTo != null){
					drag(dragFrom, dragTo);
				}
				else{
					Reporter.log("Verify drag happens from one element to another", "Drag should be done from one element to another element", "One of the element found null" ,"Fail");
					return false;
				}
				if(swipeDirection.trim().equalsIgnoreCase("UP") || swipeDirection.trim().equalsIgnoreCase("LEFT"))
					parent = getParentElement(itemListLocator, className, "Item", parentlevel, lst.size() - 1);
				else
					parent = getParentElement(itemListLocator, className, "Item", parentlevel, 2);
				swipe(viewLocator, By.xpath(parent), swipeDirection);
			}
		}while(found != 2);
		
		if(dragFrom != null && dragTo != null && found == 2){
			drag(dragFrom, dragTo);
			Reporter.log("Verify drag happens from one element to another", "Drag should be done from one element to another element", "Drag successfull" ,"Done");
		}
		else{
			Reporter.log("Verify drag happens from one element to another", "Drag should be done from one element to another element", "One of the element found null" ,"Fail");
			return false;
		}
		
		return true;
	}
	
	public String getLocatorValue(By element){
		
		String val = null;
		
		if (element instanceof By.ByXPath){
			val = element.toString().replaceFirst("By.xpath: ", "");
		}
		else if (element instanceof By.ByName){
			val = element.toString().replaceFirst("By.name: ", "");
		}
		else if (element instanceof By.ById){
			val = element.toString().replaceFirst("By.id: ", "");
		}
		else if (element instanceof By.ByClassName){
			val = element.toString().replaceFirst("By.className: ", "");
		}
		else if (element instanceof By.ByTagName){
			val = element.toString().replaceFirst("By.tagName: ", "");
		}
		else if(element instanceof By.ByCssSelector){
			val = element.toString().replaceFirst("By.selector: ", "");
		}
		else if(element instanceof By.ByLinkText){
			val = element.toString().replaceFirst("By.linkText: ", "");
		}
		else{
			val = element.toString().replaceFirst("By.partialLinkText: ", "");
		}
		
		return val;
	}
	
	public String getXpath(By childElement, String objName, String objType, int index){
		
		String Xpath = null;
		String FindBy = "";
		String val = getLocatorValue(childElement);
		if (childElement instanceof By.ByXPath){
			FindBy = "Xpath";
			if(index > 0){
				Xpath = "(" + val + ")[" + index + "]";
			}
			else{
				Xpath = val;
			}
		}
		else if (childElement instanceof By.ByName){
			FindBy = "Name";
			if(index > 0){
				Xpath = "(//" + objType + "[@name='" + val + "'])[" + index + "]";
			}
			else{
				Xpath = "//" + objType + "[@name='" + val + "']";
			}
		}
		else if (childElement instanceof By.ById){
			FindBy = "Id";
			if(index > 0){
				Xpath = "(//" + objType + "[@resource-id='" + val + "'])[" + index + "]";
			}
			else{
				Xpath = "//" + objType + "[@resource-id='" + val + "']";
			}
		}
		else if (childElement instanceof By.ByClassName){
			FindBy = "ClassName";
			if(index > 0){
				Xpath = "(//" + objType + "[@class='" + val + "'])[" + index + "]";
			}
			else{
				Xpath = "//" + objType + "[@class='" + val + "']";
			}
		}
		else if (childElement instanceof By.ByTagName){
			FindBy = "TagName";
			if(index > 0){
				Xpath = "(//" + val + ")[" + index + "]";
			}
			else{
				Xpath = "//" + val;
			}
		}
		else{
			Reporter.log("Object Identification", "Property name :" + FindBy,"Property name specified for object " + objName + " is invalid", "Fail");
			return null;
		}
		
		return Xpath;
	}
	
	/**
	 * Function to get Parent Web Element
	 * 
	 * @param childElement
	 * @param objType
	 * @param objName
	 * @param ParentLevel
	 * @param index
	 * @return
	 */
	public String getParentElement(By childElement, String objType, String objName, int ParentLevel, int index){
		try{
			String Xpath = getXpath(childElement, objName, objType, index);
			//Define Parent xpath
			String strParentXpath = "";
			if(ParentLevel > 0){
				for(int count=1; count<=ParentLevel; count++){
					strParentXpath = strParentXpath + "/..";
				}	
			}
			//Get Parent WebElement
			return Xpath + strParentXpath;			
		}catch (Exception e) {
			log.info("Threw a Exception in BaseUtil::getParentElement, full stack trace follows:", e);
			Reporter.log(objName, "Exception occurred","Exception :" + e, "Fail");
			return null;
		} 
	}
	
	/**
	 * Function to get Parent Web Element - Overloaded function
	 * 
	 * @param childElement
	 * @param objName
	 * @param ParentLevel
	 * @return
	 */
	public WebElement getParentElement(By childElement, String objName, int ParentLevel){
		try{
			//get the object 
			WebElement childObject = getElementWhenVisible(childElement);
			if(childObject==null)
				return null;
			
			//Define Parent xpath
			String strParentXpath = "..";
			if(ParentLevel > 1){
				for(int count=2; count<=ParentLevel; count++){
					strParentXpath = strParentXpath + "/..";
				}	
			}
			//Get Parent WebElement
			WebElement parentElement = childObject.findElement(By.xpath(strParentXpath));   		
			return parentElement;			
		}catch (Exception e) {
			log.info("Threw a Exception in BaseUtil::getParentElement, full stack trace follows:", e);
			Reporter.log(objName, "Exception occurred","Exception :" + e, "Fail");
			return null;
		} 
	}
	
	/**
	 * Function to get Sibling Web Element
	 * 
	 * @param strChildElement
	 * @param siblingDesc
	 * @return
	 */
	public List<WebElement> getSiblingElements(By childElement,String objName){
		try{

			WebElement child = getElementWhenVisible(childElement);
			WebElement parent = child.findElement(By.xpath(".."));
			List<WebElement> list= getMultipleChildObjects(parent, childElement, objName);
			return list;
		}
		catch (Exception e) {
			log.info("Threw a Exception in BaseUtil::getSiblingElements, full stack trace follows:", e);
			Reporter.log(objName, "Exception occurred","Exception :" + e, "Fail");
			return null;
		}
	}
	
	/**
	 * Get child webelements under parent webelement
	 * 
	 * @param Parent
	 * @param childElmtProp
	 * @param childName
	 * @return
	 */
	public List<WebElement> getChildWebElementsList(WebElement Parent, By childElmtProp, String childName){
		try{
			//Get WebElement    		
			List<WebElement> childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName);

			//Check if the WebElement is enabled or displayed    		
			boolean bIsDisplayed = false;
			boolean bIsEnabled = false;

			int intCount = 1;        
			while (!(bIsDisplayed || bIsEnabled) && (intCount <=3)){
				try {	        					
					if(childWebElements.size() != 0){
						bIsDisplayed = childWebElements.get(0).isDisplayed();
						bIsEnabled = childWebElements.get(0).isEnabled();
						for(int i = 0; i < childWebElements.size(); i++){
							if(!childWebElements.get(i).isDisplayed()){
								childWebElements.remove(i);
							}
						}
					}					
				}catch (StaleElementReferenceException e){	
					childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName);
				}catch (WebDriverException e){	    
					childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName);
				}catch (NullPointerException e){	    
					childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName);
					if(childWebElements == null){
						break;
					}
				}	    	    
				intCount++;			
			}

			//Validate if the element is displayed
			if (!(bIsDisplayed || bIsEnabled)){	        	
				return null;
			}	        
			return childWebElements;
		}catch(Exception e){
			log.info("Threw a Exception in Baseutil::getChildWebElementsList, full stack trace follows:", e);
			Reporter.log(childName, "Exception occurred","Exception: " + e, "Fail");			
			return null;    		
		}
	}
	
	/**
	 * Get child webelements under parent webelement
	 * 
	 * @param Parent
	 * @param childElmtProp
	 * @param childName
	 * @param objType
	 * @return
	 */
	public List<WebElement> getChildWebElementsList(By Parent, By childElmtProp, String childName, String objType){
		try{
			//Get WebElement    		
			List<WebElement> childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName, objType);
			
			 if(!driverType.trim().toUpperCase().contains("IOS")){
				//Check if the WebElement is enabled or displayed    		
				boolean bIsDisplayed = false;
				boolean bIsEnabled = false;
	
				int intCount = 1;        
				while (!(bIsDisplayed || bIsEnabled) && (intCount <=3)){
					try {	        					
						if(childWebElements.size() != 0){
							bIsDisplayed = childWebElements.get(0).isDisplayed();
							bIsEnabled = childWebElements.get(0).isEnabled();
							for(int i = 0; i < childWebElements.size(); i++){
								if(!childWebElements.get(i).isDisplayed()){
									childWebElements.remove(i);
								}
							}
						}					
					}catch (StaleElementReferenceException e){	
						childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName, objType);
					}catch (WebDriverException e){	    
						childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName, objType);
					}catch (NullPointerException e){	    
						childWebElements = getMultipleChildObjects(Parent, childElmtProp, childName, objType);
						if(childWebElements == null){
							break;
						}
					}	    	    
					intCount++;			
				}
	
				//Validate if the element is displayed
				if (!(bIsDisplayed || bIsEnabled)){	        	
					return null;
				}	 
			 }
			return childWebElements;
		}catch(Exception e){
			log.info("Threw a Exception in BaseUtil::getChildWebElementsList, full stack trace follows:", e);
			Reporter.log(childName, "Exception occurred","Exception: " + e, "Fail");			
			return null;    		
		}
	}
	
	/**
	 * Method to get single child object under a parent object
	 * 
	 * @param parent
	 * @param objDesc
	 * @param objType
	 * @param objName
	 * @return
	 */
	public WebElement getSingleChildObject(String parent, By objDesc, String objType, String objName){
		//Verify parent element
		if(parent == null){
			return null;
		}
		
		driver.manage().timeouts().implicitlyWait(2000, TimeUnit.MILLISECONDS);
		
		String Xpath = getXpath(objDesc, objName, objType, -1);
		List<WebElement> obj = null;

		int intcount = 1;	            
		while (intcount <= 1){	            	
			try{
				obj = driver.findElements(By.xpath(parent + Xpath));
				boolean isPresent = obj.size() > 0;
				if(isPresent){
					driver.manage().timeouts().implicitlyWait(Long.valueOf(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);
					return driver.findElement(By.xpath(parent + Xpath));
				}		            	
			}
			catch(Exception e){		            	
				if (intcount == 2){
					log.info("Threw a Exception in BaseUtil::getSingleChildObject, full stack trace follows:", e);
					driver.manage().timeouts().implicitlyWait(Long.valueOf(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);
					Reporter.log("Object :"+objName, objName+" is not identified", "Exception :" + e.toString(), "Fail");
					return null;
				}
			}		            
			intcount = intcount + 1;
		}
		
		driver.manage().timeouts().implicitlyWait(Long.valueOf(Environment.get("implicitWait")), TimeUnit.MILLISECONDS);
		return null;	           
	}
	
	/**
	 * Method to get multiple child objects under a parent object
	 * 
	 * @param parent
	 * @param objDesc
	 * @param objName
	 * @return
	 */
	public List<WebElement> getMultipleChildObjects(WebElement parent, By objDesc, String objName){
		//Verify parent element
		if(parent == null){ 
			return null;
		}
		
		String val = getLocatorValue(objDesc);	            
		String FindBy = "";
		int intcount = 1;	            
		while (intcount <= 2){	            	
			try{
				//Handle all FindBy cases
				if (objDesc instanceof By.ByLinkText){
					FindBy = "LinkText";
					return parent.findElements(By.linkText(val));
				}
				else if (objDesc instanceof By.ByXPath){
					FindBy = "Xpath";
					return parent.findElements(By.xpath(val));
				}
				else if (objDesc instanceof By.ByName){
					FindBy = "Name";
					return parent.findElements(By.name(val));
				}
				else if (objDesc instanceof By.ById){
					FindBy = "Id";
					return parent.findElements(By.id(val));
				}
				else if (objDesc instanceof By.ByClassName){
					FindBy = "Classname";
					return parent.findElements(By.className(val));
				}
				else if (objDesc instanceof By.ByCssSelector){
					FindBy = "CssSelector";
					return parent.findElements(By.cssSelector(val));
				}
				else if (objDesc instanceof By.ByTagName){
					FindBy = "TagName";
					return parent.findElements(By.tagName(val));
				}
				else{
					Reporter.log("Object Identification", "Property name :" + FindBy,"Property name specified for object " + objName + " is invalid", "Fail");
					return null;
				}		            	
			}
			catch(Exception e){		            	
				if (intcount == 2){
					log.info("Threw a Exception in BaseUtil::getMultipleChildObjects, full stack trace follows:", e);
					Reporter.log("Object : " + objName, objName + " is not identified", "Exception :" + e.toString(), "Fail");
					return null;
				}		            	
				intcount = intcount + 1;
			}		            
		}
		return null;	           
	}
	
	/**
	 * Method to get multiple child objects under a parent object
	 * 
	 * @param parent
	 * @param objDesc
	 * @param objName
	 * @param objType
	 * @return
	 */
	public List<WebElement> getMultipleChildObjects(By parent, By objDesc, String objName, String objType){
		//Verify parent element
		if(parent == null){ 
			return null;
		}
		
		String Xpath = getXpath(objDesc, objName, objType, -1);
		String parentXpath = getXpath(parent, "Parent", objType, -1);
		int intcount = 1;	            
		while (intcount <= 2){	            	
			try{
				return driver.findElements(By.xpath(parentXpath + Xpath));
			}
			catch(Exception e){		            	
				if (intcount == 2){
					log.info("Threw a Exception in BaseUtil::getMultipleChildObjects, full stack trace follows:", e);
					Reporter.log("Object :" + objName, objName + " is not identified", "Exception :" + e.toString(), "Fail");
					return null;
				}		            	
				intcount = intcount + 1;
			}		            
		}
		return null;	           
	}
	
	/**
	 * Connects to unix box and returns connection object
	 * 
	 * @param sUnixServer
	 * @param sUser
	 * @param sPassword
	 * @return
	 */
	public Connection connectToUnixBox(String sUnixServer, String sUser, String sPassword){
		try{
			//Create a Connection object
			Connection conn = new Connection( sUnixServer );
			conn.connect();

			//Checks if credentials are valid
			boolean bIsAuthenticated = conn.authenticateWithPassword(sUser, sPassword);
			if(bIsAuthenticated){
				return conn;				
			}else{
				Reporter.log("ConnectToUnixBox", "Connection should be authenticated", "Authentication failed", "Fail");
				return null;
			}   		

		}catch(Exception e){
			log.info("Threw a Exception in BaseUtil::ConnectToUnixBox, full stack trace follows:", e);
			Reporter.log("ConnectToUnixBox", "Exception occurred","Exception :" + e, "Fail");			
			return null;		
		}    	
	}
	
	/**
	 * Retrieve File from remote server
	 * 
	 * @param sshConn
	 * @param sUnixFilePath
	 * @param sUnixFileName
	 * @param sFileType
	 * @return
	 */
	public File getRemoteFile(Connection sshConn, String sUnixFilePath, String sUnixFileName, String sFileType){
		try{    
			File objFile = null;
			String sFilename = "";

			//Create a session
			Session sess = sshConn.openSession();

			//Execute unix command to list all the files
			sess.execCommand("cd " +sUnixFilePath+ ";ls -t *" +sUnixFileName+ "*");

			//Code to check unix box stacktrace
			InputStream stderr = new StreamGobbler(sess.getStderr());
			InputStream stdout = new StreamGobbler(sess.getStdout());

			BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
			BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdout));

			//Check for any errors while executing the command
			String strErrorMessage = "";
			while (true){
				String line = stderrReader.readLine();
				if (line == null){
					break;
				}else{
					strErrorMessage = strErrorMessage + line;
				}
			}
			if(!strErrorMessage.equals("")){
				Reporter.log("GetRemoteFile", "Error Message", strErrorMessage, "Fail");
				return null;
			}

			//Get Response xml File name present in unix box
			while (true){
				String line = stdoutReader.readLine();
				if (line == null){
					Reporter.log("GetRemoteFile", "File should be found", "File not found", "Fail");
					return null;

				}else if(line.contains( sUnixFileName )){         	
					sFilename = line;
					Reporter.log("Unix file", "File should be found", "File is found :" + sFilename, "Done");
					break;
				}                   
			}            

			//Create new session object
			sess.close();
			sess = sshConn.openSession();

			//Execute unix command to read xml file
			sess.execCommand("cd " + sUnixFilePath + ";cat "+ sFilename);

			//Code to check unix box stacktrace
			stderr = new StreamGobbler(sess.getStderr());
			stdout = new StreamGobbler(sess.getStdout());

			stderrReader = new BufferedReader(new InputStreamReader(stderr));
			stdoutReader = new BufferedReader(new InputStreamReader(stdout));

			//Check for any errors while executing the command
			strErrorMessage = "";
			while (true){
				String line = stderrReader.readLine();
				if (line == null){
					break;
				}else{
					strErrorMessage = strErrorMessage + line;
				}
			}
			if(!strErrorMessage.equals("")){
				Reporter.log("GetRemoteFile", "Error Message", strErrorMessage, "Fail");
				return null;
			}

			//Create a file in localhost
			objFile = new File(System.getProperty("user.dir")+ OSValidator.delimiter + sFilename);

			//Copy file content from Unix box to windows
			while (true){
				String line = stdoutReader.readLine();
				if (line == null){
					break;
				}                    
				FileUtils.writeStringToFile(objFile, line, "UTF-8", true);           
			}

			//Close session object
			sess.close();
			sess = null;
			return objFile;    		

		}catch(Exception e){
			log.info("Threw a Exception in BaseUtil::GetRemoteFile, full stack trace follows:", e);
			Reporter.log("GetRemoteFile", "Exception occurred","Exception :" + e, "Fail");			
			return null;		
		}    	
	}
	
	/**
	 * Connects to unix box and returns file object
	 * 
	 * @param sUnixServer
	 * @param sUser
	 * @param sPassword
	 * @param sFilePath
	 * @param sFileName
	 * @param sFileType
	 * @return
	 */
	public File retrieveUnixBoxFile(String sUnixServer, String sUser, String sPassword, String sFilePath, String sFileName, String sFileType){
		try{
			File objRecentFile = null;
			//Get File from each server
			String[] arrUnixServers = sUnixServer.split(";");
			String[] arrUnixFilePaths = sFilePath.split(";");

			for(int serverCount=0; serverCount<arrUnixServers.length; serverCount++){    			

				//Get SSH connection object
				Connection sshConn = connectToUnixBox(arrUnixServers[serverCount], sUser, sPassword); 		
				if(sshConn == null){
					Reporter.log("RetrieveUnixBoxFile", "Unix Box connection should be done", "Connection is not done", "Fail");			
					return null;
				}

				//Get Remote File
				File objFile = getRemoteFile(sshConn, arrUnixFilePaths[serverCount], sFileName, sFileType);
				if(objFile != null){
					if(objRecentFile == null){
						objRecentFile = objFile;

					}else{
						String objFileDateTime = objFile.getName().split("_")[0] + objFile.getName().split("_")[1];
						String objRecentFileDateTime = objRecentFile.getName().split("_")[0] + objRecentFile.getName().split("_")[1];

						SimpleDateFormat df = new SimpleDateFormat("ddMMMyyyyHHmmss");	    				
						Date dFileDate = df.parse(objFileDateTime);
						Date dRecentFileDate = df.parse(objRecentFileDateTime);

						//Checks most recent file
						if(dFileDate.after(dRecentFileDate)){
							//Delete previous xml file object
							deleteFolder(objRecentFile);	    					
							objRecentFile = objFile;

						}else{
							//Delete new xml file object
							deleteFolder(objFile);
						}
					}
				}
				sshConn.close();
				sshConn = null;
				objFile = null;
			}
			//Check file object
			if(objRecentFile == null){
				Reporter.log("RetrieveUnixBoxFile", "File should be retrieved", "File is not retrieved", "Fail");
			}
			return objRecentFile;

		}catch(Exception e){
			log.info("Threw a Exception in BaseUtil::RetrieveUnixBoxFile, full stack trace follows:", e);
			Reporter.log("RetrieveUnixBoxFile", "Exception occurred","Exception :" + e, "Fail");			
			return null;		
		}  	
	}
	
	/**
	 * Retrives all xml nodes for a xml path
	 * 
	 * @param xmlFile
	 * @param strXPath
	 * @return
	 */
	public NodeList getXMLNodes(File xmlFile, String strXPath){
		try{
			//Create Document Object
			DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbFac.newDocumentBuilder();
			Document xmldoc = docBuilder.parse( xmlFile );

			//Create xPath object
			XPathFactory xPathfac = XPathFactory.newInstance();
			XPath objXpath = xPathfac.newXPath();			
			XPathExpression xpathExpr = objXpath.compile( strXPath );

			//Get List of nodes
			NodeList objNodeList = (NodeList)xpathExpr.evaluate(xmldoc, XPathConstants.NODESET);

			return objNodeList;    		
		}catch(Exception e){
			log.info("Threw a Exception in BaseUtil::getXMLNodes, full stack trace follows:", e);
			Reporter.log("getXMLNodes", "Exception occurred","Exception : " + e, "Fail");			
			return null;		
		}  	
	}
	
	/**
     * Click on the webelement
     * 
     * @param webElement
     * @param strObjName
     * @return
     */
    public boolean javascriptClick(WebElement webElement, String strObjName){   	 		
        //Click on the WebElement    		
        int intCount = 1;        
        while (intCount<=4){
        	try {
        		if(driverType.trim().toUpperCase().contains("ANDROID") || driverType.trim().toUpperCase().contains("IOS")){
	        		webElement.click();
	        	}else{
        			((JavascriptExecutor) driver).executeScript("return arguments[0].click()", webElement);
	        	}
        		break;
	        }catch (Exception e){    
	        	if(intCount==4){
	        		log.info("Threw a Exception in BaseUtil::javascriptClick, full stack trace follows:", e);
	    	    	Reporter.log("Click: " + strObjName, "Exception occurred","Exception: " + e, "Fail");
	    			return false;
	        	}
    	    }  	    
    	    intCount++;
        }	        
        return true;    	       
    }
    
    /**
	 * Verify content in list
	 * 
	 * @param list
	 * @param content
	 * @return
	 */
	public boolean verifyContentinList(String[] list, String content){
		boolean success = false;
		for(int i = 0; i < list.length; i++){
			if(list[i].trim().equals(content)){
				success = true;
				break;
			}
			else{
				success = false;
			}
		}
		
		log.info(content + " : returned " + success);
		
		return success;
	}
	
	/**
	 * Verify content in list - overloaded
	 * 
	 * @param list
	 * @param content
	 * @return
	 */
	public boolean verifyContentinList(List<String> list, String content){
		boolean success = false;
		for(int i = 0; i < list.size(); i++){
			if(list.get(i).trim().equals(content)){
				success = true;
				break;
			}
			else{
				success = false;
			}
		}
		
		log.info(content + " : returned " + success);
		
		return success;
	}
	
	/**
	 * Deletes a folder after deleting all its sub-folders and files
	 * 
	 * @param FolderPath
	 * @return
	 */
	public boolean deleteFolder(File FolderPath) {
		try{		
			if (FolderPath.isDirectory()) {
				String[] arrChildNodes = FolderPath.list();
				for (int i=0; i<arrChildNodes.length; i++) {
					deleteFolder(new File(FolderPath, arrChildNodes[i]));
				}
			}
			FolderPath.delete();
			return true;

		}catch(Exception e){
			log.info("Threw a Exception in BaseUtil::DeleteFolder, full stack trace follows:", e);
			Reporter.log("DeleteFolder", "Exception occurred" ,"Exception :" + e, "Fail");
			return false;
		}
	}
	
	/**
	 * Close app
	 * @throws Exception 
	 */
	public void closeApp() throws Exception{
		forceStop(Environment.get("appPackage"));
	}
	
	/**
	 * Install app
	 * 
	 * @param appPath
	 * @throws Exception 
	 */
	public void install(String appPath) throws Exception{
		(appium).installApp(appPath);
	}
	
	/**
	 * Install app from resources folder
	 * 
	 * @param appPath
	 * @throws Exception 
	 */
	public void installAppFromResources(String appName, String version) throws Exception{
		File fappName = null;
		if(!Environment.get("apkFolder").trim().equalsIgnoreCase("")){
			fappName = new File(Environment.get("apkFolder").trim() + version + "/", appName);
			if(!fappName.exists()){
				downloadFromS3("QA-Builds/apps/" + version.replace(" ", ".") + "/", Environment.get("apkFolder").trim() + version + "/", appName);
			}
		}
		else{
			String userdir = "";
			if(!Environment.get("user").trim().equalsIgnoreCase("")){
				userdir = System.getProperty("user.dir").replace(System.getProperty("user.name"), Environment.get("user").trim());
			}
			else
				userdir = System.getProperty("user.dir");
		
			File appDir = new File(new File(userdir), Environment.get("appsFolder").trim() + version + "/");
			fappName = new File(appDir, appName);
			if(!fappName.exists())
				downloadFromS3("QA-Builds/apps/" + version.replace(" ", ".") + "/", appDir.getAbsolutePath(), appName);
		}
		install(fappName.getAbsolutePath().replace(" ", "\\ "));
	}
	
	/**
	 * Install app from resources folder
	 * 
	 * @param appPath
	 * @throws Exception 
	 */
	public void installAppFromResources(String appName) throws Exception{
		File fappName = null;
		if(!Environment.get("apkFolder").trim().equalsIgnoreCase("")){
			fappName = new File(Environment.get("apkFolder").trim(), appName);
			if(!fappName.exists()){
				downloadFromS3("QA-Builds/apps/", Environment.get("apkFolder").trim(), appName);
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
				downloadFromS3("QA-Builds/apps/", appDir.getAbsolutePath(), appName);
		}
		install(fappName.getAbsolutePath());
	}
	
	/**
	 * Uninstall app in case of IOS device
	 * 
	 * @param bundleId
	 * @throws Exception 
	 */
	public void uninstall(String bundleId) throws Exception{
		(appium).removeApp(bundleId);
	}
	
	/**
	 * Launch app
	 * 
	 * @throws Exception
	 */
	public void launchApp() throws Exception{
		launchApp(Environment.get("appPackage"), Environment.get("appActivity"));
		Reporter.log("Verify launch app", "App should be launched", "App is launched successfully", "Pass");
	}
	
	/**
	 * Clear cache
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean clearCache() throws Exception{
		boolean success = false;
		String output = runADBCommand(new String[]{"-s", Environment.get("udid").trim(), "shell", "pm", "clear", Environment.get("appPackage")}, new Boolean[]{false, false, false, false, false, false, false}, 2000);
		if(!output.trim().equals("")){
			output = output.trim().split("\n")[0].trim();
			if(output.equalsIgnoreCase("success") || output.equalsIgnoreCase("failed")){
				success = true;
			}
		}
		
		log.info("App - " + Environment.get("appPackage").trim() + " reset done");
		
		return success;
	}
	
	/**
	 * Reset app
	 * 
	 * @throws Exception
	 */
	public void resetApp() throws Exception{
		closeApp();
		
		if(clearCache()){
			Reporter.log("Verify app is reset and launched", "App should be reset and launched", "App is reset and launched successfully", "Done");
			launchApp();
		}
	}
	
	/**
	 * Get specified item from list of items on the screen by swiping up(if needed)
	 * 
	 * @param itemListLocator
	 * @param itemListObjName
	 * @param itemName
	 * @param itemObjName
	 * @param screenName
	 * @param parentlevel
	 * @param index
	 * @param viewLocator
	 * @param swipeDirection
	 * @return
	 */
	@SuppressWarnings("unused")
	public String getItemByName(By itemListLocator, String itemName, String itemObjName, int parentlevel, String index, By viewLocator, String swipeDirection){
		String className;
		if(driverType.trim().toUpperCase().contains("ANDROID")){
			className = "android.widget.TextView";
		}
		else{
			className = "UIAStaticText";
		}
		
		String item = null, last = "", first = "", currentDirection = swipeDirection.trim();
		WebElement webElement = getElementWhenVisible(viewLocator);
		
		int check = 0, count = 0;
		int position = 1;
		if(!index.trim().equalsIgnoreCase("")){
			position = Integer.valueOf(index.trim());
		}
		
		do{
			boolean flag = false;
			List<WebElement> listItems = getWebElementsList(itemListLocator);
			List<String> nlst = new ArrayList<String>();
			if(listItems == null || listItems.size() == 0){
				break;
			}
			check = 0;
			
			if(last.equalsIgnoreCase(listItems.get(listItems.size() - 1).getText().trim()) && first.equalsIgnoreCase(listItems.get(0).getText().trim())){
				if(driverType.trim().toUpperCase().contains("IOS")){
					break;
				}
				else{
					String collectionItemText;
					if(driverType.trim().toUpperCase().contains("IOS")){
						collectionItemText = listItems.get(listItems.size() - 1).getText();
					}
					else{
						collectionItemText = listItems.get(listItems.size() - 1).getAttribute("text");
					}
					if(collectionItemText.trim().equalsIgnoreCase(itemName.trim())){
						count++;
						if(count == position){
							check = 1;
							Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " - " + itemName.trim() + " is displayed", itemObjName.trim() + " - " + itemName.trim() + " should be displayed", itemObjName.trim() + " - " + itemName.trim() + " found", "Pass");
							item = getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size());
							break;
						}
						else{
							check = 0;
						}
					}
					else{
						check = 0;
					}
					break;
				}
			}
			
			for(int i = 0; i < listItems.size(); i++){
				if(driverType.trim().toUpperCase().contains("IOS")){
					nlst.add(listItems.get(i).getText());
				}
				else{
					nlst.add(listItems.get(i).getAttribute("text"));
				}
				if(i == listItems.size() - 1 && !driverType.trim().toUpperCase().contains("IOS")){
					flag = true;
					check = 0;
					break;
				}
				String collectionItemText;
				if(driverType.trim().toUpperCase().contains("IOS")){
					collectionItemText = listItems.get(i).getText();
				}
				else{
					collectionItemText = listItems.get(i).getAttribute("text");
				}
				if(collectionItemText.trim().equalsIgnoreCase(itemName.trim())){
					count++;
					if(count == position){
						check = 1;
						Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " - " + itemName.trim() + " is displayed", itemObjName.trim() + " - " + itemName.trim() + " should be displayed", itemObjName.trim() + " - " + itemName.trim() + " found", "Pass");
						item = getParentElement(itemListLocator, className, "Item", parentlevel, i + 1);
						break;
					}
					else{
						check = 0;
					}
				}
				else{
					check = 0;
				}
			}
			
			 if(driverType.trim().toUpperCase().contains("IOS")){
				 if(check == 1){
					 while(!checkIfElementPresent(By.xpath(item), 1)){
						 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
					 }
					 break;
				 }
			 }
			
			if(check == 0){
				last = listItems.get(listItems.size() - 1) == null ? listItems.get(listItems.size() - 2).getText().trim() : listItems.get(listItems.size() - 1).getText().trim();
				first = listItems.get(0).getText().trim();
				if(driverType.trim().toUpperCase().contains("IOS")){
					String lastElement = getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size());
					while(!checkIfElementPresent(By.xpath(lastElement), 1)){
						 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
					}
				}
				else{
					if(flag)
						swipe(webElement, driver.findElement(By.xpath(getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size()))), swipeDirection);
					else
						swipe(webElement, swipeDirection);
				}
			}
		}while(check == 0);
		
		if(check == 0){
			Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " - " + itemName.trim() + " is displayed", itemObjName.trim() + " - " + itemName.trim() + " should be displayed", itemObjName.trim() + " - " + itemName.trim() + " not found", "Fail");
			return item;
		}
		
		return item;
	}
	
	/**
	 * Get specified item from list of items on the screen by swiping up(if needed) based on category
	 * 
	 * @param itemListLocator
	 * @param itemName
	 * @param itemObjName
	 * @param parentlevel
	 * @param index
	 * @param viewLocator
	 * @param swipeDirection
	 * @param category
	 * @return
	 */
	@SuppressWarnings("unused")
	public String getItemByNameBasedOnCategory(By itemListLocator, By checkItemListLocator, String itemName, String itemObjName, int parentlevel, String index, By viewLocator, String swipeDirection, String category){
		String className;
		if(driverType.trim().toUpperCase().contains("ANDROID")){
			className = "android.widget.TextView";
		}
		else{
			className = "UIAStaticText";
		}
		
		String item = null, last = "", first = "", currentDirection = swipeDirection.trim();
		WebElement webElement = getElementWhenVisible(viewLocator);
		String list_header_name = category.trim();
		int check = 0, count = 0;
		int position = 1, seemore = 0;
		int cflag = 0;
		if(!index.trim().equalsIgnoreCase("")){
			position = Integer.valueOf(index.trim());
		}
		
		do{
			if(cflag == 1){
				//Do Nothing
			}
			else{
				cflag = 0;
			}
			boolean flag = false;
			List<WebElement> listItems = getWebElementsList(itemListLocator);
			List<String> nlst = new ArrayList<String>();
			if(listItems == null || listItems.size() == 0){
				break;
			}
			check = 0;
			
			if(last.equalsIgnoreCase(listItems.get(listItems.size() - 1).getText().trim()) && first.equalsIgnoreCase(listItems.get(0).getText().trim())){
				if(driverType.trim().toUpperCase().contains("IOS")){
					break;
				}
				else{
					String collectionItemText;
					if(driverType.trim().toUpperCase().contains("IOS")){
						collectionItemText = listItems.get(listItems.size() - 1).getText();
					}
					else{
						collectionItemText = listItems.get(listItems.size() - 1).getAttribute("text");
					}
					if((collectionItemText.equalsIgnoreCase(list_header_name) || collectionItemText.equalsIgnoreCase(list_header_name.substring(0, list_header_name.length() - 1))) && cflag == 0){
						if(swipeDirection.trim().equalsIgnoreCase("DOWN") || swipeDirection.trim().equalsIgnoreCase("RIGHT")){
							currentDirection = swipeDirection.trim();
							swipeDirection = toggle(swipeDirection.trim());
						}
						cflag = 1;
						continue;
					}
					if(cflag == 1){
						String parent = getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size());
						String child = parent + getXpath(checkItemListLocator, className, "Item", -1);
						String childValue = getText(By.xpath(child));
						if(childValue.trim().equalsIgnoreCase(itemName.trim())){
							count++;
							if(count == position){
								check = 1;
								Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " - " + itemName.trim() + " is displayed", itemObjName.trim() + " - " + itemName.trim() + " should be displayed", itemObjName.trim() + " - " + itemName.trim() + " found", "Pass");
								item = parent;
								break;
							}
							else{
								check = 0;
								if(collectionItemText.equalsIgnoreCase("See more " + list_header_name.trim().toLowerCase())){
									click(listItems.get(listItems.size() - 1), "See more " + list_header_name.trim().toLowerCase() + " link");
									seemore = 1;
									break;
								}
							}
						}
						else{
							check = 0;
							if(collectionItemText.equalsIgnoreCase("See more " + list_header_name.trim().toLowerCase())){
								click(listItems.get(listItems.size() - 1), "See more " + list_header_name.trim().toLowerCase() + " link");
								seemore = 1;
								break;
							}
						}
					}
					break;
				}
			}
			
			for(int i = 0; i < listItems.size(); i++){
				if(driverType.trim().toUpperCase().contains("IOS")){
					nlst.add(listItems.get(i).getText());
				}
				else{
					nlst.add(listItems.get(i).getAttribute("text"));
				}
				if(i == listItems.size() - 1 && !driverType.trim().toUpperCase().contains("IOS")){
					flag = true;
					check = 0;
					break;
				}
				String collectionItemText;
				if(driverType.trim().toUpperCase().contains("IOS")){
					collectionItemText = listItems.get(i).getText();
				}
				else{
					collectionItemText = listItems.get(i).getAttribute("text");
				}
				if((collectionItemText.equalsIgnoreCase(list_header_name) || collectionItemText.equalsIgnoreCase(list_header_name.substring(0, list_header_name.length() - 1))) && cflag == 0){
					if(swipeDirection.trim().equalsIgnoreCase("DOWN") || swipeDirection.trim().equalsIgnoreCase("RIGHT")){
						currentDirection = swipeDirection.trim();
						swipeDirection = toggle(swipeDirection.trim());
					}
					cflag = 1;
					continue;
				}
				if(cflag == 1){
					String parent = getParentElement(itemListLocator, className, "Item", parentlevel, i + 1);
					String child = parent + getXpath(checkItemListLocator, "Item", className, -1);
					if(driverType.trim().toUpperCase().contains("IOS")){
						while(!checkIfElementPresent(By.xpath(child), 1)){
							 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
						}
					}
					String childValue = getText(By.xpath(child));
					if(childValue.trim().equalsIgnoreCase(itemName.trim())){
						count++;
						if(count == position){
							check = 1;
							Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " - " + itemName.trim() + " is displayed", itemObjName.trim() + " - " + itemName.trim() + " should be displayed", itemObjName.trim() + " - " + itemName.trim() + " found", "Pass");
							item = parent;
							break;
						}
						else{
							check = 0;
							if(collectionItemText.equalsIgnoreCase("See more " + list_header_name.trim().toLowerCase())){
								if(driverType.trim().toUpperCase().contains("IOS")){
									while(!checkIfElementPresent(listItems.get(i), 1)){
										 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
									}
								}
								click(listItems.get(i), "See more " + list_header_name.trim().toLowerCase() + " link");
								seemore = 1;
								break;
							}
						}
					}
					else{
						check = 0;
						if(collectionItemText.equalsIgnoreCase("See more " + list_header_name.trim().toLowerCase())){
							if(driverType.trim().toUpperCase().contains("IOS")){
								while(!checkIfElementPresent(listItems.get(i), 1)){
									 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
								}
							}
							click(listItems.get(i), "See more " + list_header_name.trim().toLowerCase() + " link");
							seemore = 1;
							break;
						}
					}
				}
			}
			
			 if(driverType.trim().toUpperCase().contains("IOS")){
				 if(check == 1){
					 while(!checkIfElementPresent(By.xpath(item), 1)){
						 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
					 }
					 break;
				 }
			 }
			
			if(check == 1 || seemore == 1)
				break;
			
			if(check == 0){
				last = listItems.get(listItems.size() - 1) == null ? listItems.get(listItems.size() - 2).getText().trim() : listItems.get(listItems.size() - 1).getText().trim();
				first = listItems.get(0).getText().trim();
				if(driverType.trim().toUpperCase().contains("IOS")){
					String lastElement = getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size());
					while(!checkIfElementPresent(By.xpath(lastElement), 1)){
						 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
					}
				}
				else{
					if(flag)
						swipe(webElement, driver.findElement(By.xpath(getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size()))), swipeDirection);
					else
						swipe(webElement, swipeDirection);
				}
			}
		}while(check == 0);
		
		if(seemore == 1){
			Dictionary.put("SEE_MORE", "true");
		}
		else{
			Dictionary.put("SEE_MORE", "false");
			if(check == 0){
				Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " - " + itemName.trim() + " is displayed", itemObjName.trim() + " - " + itemName.trim() + " should be displayed", itemObjName.trim() + " - " + itemName.trim() + " not found", "Fail");
				return item;
			}
		}
		
		return item;
	}
	
	/**
	 * Get specified item from list of items on the screen by swiping up(if needed)
	 * 
	 * @param itemListLocator
	 * @param itemListObjName
	 * @param index
	 * @param itemObjName
	 * @param screenName
	 * @param parentlevel
	 * @param viewLocator
	 * @param swipeDirection
	 * @return
	 */
	public String getItemByIndex(By itemListLocator, int index, String itemObjName, int parentlevel, By viewLocator, String swipeDirection){
		String className;
		if(driverType.trim().toUpperCase().contains("ANDROID")){
			className = "android.widget.TextView";
		}
		else{
			className = "UIAStaticText";
		}
		
		WebElement webElement = getElementWhenVisible(viewLocator);
		
		String item = null, last = "", first = "";
		List<List<String>> oldlst = new ArrayList<List<String>>();
		List<List<String>> newlst = null;
		int cflag = 0;
		int check = 0;
		int k = 0;
		
		do{
			boolean flag = false;
			List<WebElement> listItems = getWebElementsList(itemListLocator);
			List<String> nlst = new ArrayList<String>();
			if(cflag == 1){
				oldlst.addAll(newlst);
			}
			newlst = new ArrayList<List<String>>();
			
			if(listItems == null || listItems.size() == 0){
				break;
			}
			check = 0;
			
			if(last.equalsIgnoreCase(listItems.get(listItems.size() - 1).getText().trim()) && first.equalsIgnoreCase(listItems.get(0).getText().trim())){
				if(driverType.trim().toUpperCase().contains("IOS")){
					break;
				}
				else{
					String parent = getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size());
					List<WebElement> childItems;
					if(parentlevel > 0){
						childItems = getChildWebElementsList(By.xpath(parent), By.xpath("//" + className), "Textview", className);
					}
					else{
						childItems = new ArrayList<WebElement>();
						childItems.add(driver.findElement(By.xpath(parent)));
					}
					if(childItems == null || childItems.size() == 0)
						break;
					List<String>childItemsText= new ArrayList<String>();
					for(WebElement we:childItems){
						if(driverType.trim().toUpperCase().contains("IOS")){
							childItemsText.add(we.getText().trim());
						}
						else{
							childItemsText.add(we.getAttribute("text").trim());
						}
					}
					
					newlst.add(childItemsText);
					
					String collectionItemText;
					if(driverType.trim().toUpperCase().contains("IOS")){
						collectionItemText = listItems.get(listItems.size() - 1).getText();
					}
					else
						collectionItemText = listItems.get(listItems.size() - 1).getAttribute("text");
					if(verifyContentinListofList(oldlst, childItemsText)){
						//Do Nothing
					}
					else{
						k++;
					}
					if(k == index){
						check = 1;
						Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " - " + collectionItemText.trim() + " is displayed", itemObjName.trim() + " - " + collectionItemText.trim() + " should be displayed", itemObjName.trim() + " - " + collectionItemText.trim() + " found", "Pass");
						item = getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size());
						break;
					}
					else{
						check = 0;
					}
					break;
				}
			}
			
			for(int i = 0; i < listItems.size(); i++){
				if(driverType.trim().toUpperCase().contains("IOS")){
					nlst.add(listItems.get(i).getText());
				}
				else
					nlst.add(listItems.get(i).getAttribute("text"));
				
				if(i == listItems.size() - 1 && !driverType.trim().toUpperCase().contains("IOS")){
					flag = true;
					check = 0;
					break;
				}
				String collectionItemText;
				if(driverType.trim().toUpperCase().contains("IOS")){
					collectionItemText = listItems.get(i).getText();
					if((i + 1) == index){
						check = 1;
						Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " - " + collectionItemText.trim() + " is displayed", itemObjName.trim() + " - " + collectionItemText.trim() + " should be displayed", itemObjName.trim() + " - " + collectionItemText.trim() + " found", "Pass");
						item = getParentElement(itemListLocator, className, "Item", parentlevel, i + 1);
						break;
					}
					else{
						check = 0;
					}
				}
				else{
					String parent = getParentElement(itemListLocator, className, "Item", parentlevel, i + 1);
					List<WebElement> childItems;
					if(parentlevel > 0){
						childItems = getChildWebElementsList(By.xpath(parent), By.xpath("//" + className), "Textview", className);
					}
					else{
						childItems = new ArrayList<WebElement>();
						childItems.add(driver.findElement(By.xpath(parent)));
					}
					if(childItems == null || childItems.size() == 0)
						continue;
					List<String>childItemsText= new ArrayList<String>();
					for(WebElement we:childItems){
						if(driverType.trim().toUpperCase().contains("IOS")){
							childItemsText.add(we.getText().trim());
						}
						else
							childItemsText.add(we.getAttribute("text").trim());
					}
					
					newlst.add(childItemsText);
					if(driverType.trim().toUpperCase().contains("IOS")){
						collectionItemText = listItems.get(i).getText();
					}
					else
						collectionItemText = listItems.get(i).getAttribute("text");
					if(verifyContentinListofList(oldlst, childItemsText)){
						//Do Nothing
					}
					else{
						k++;
					}
					if(k == index){
						check = 1;
						Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " - " + collectionItemText.trim() + " is displayed", itemObjName.trim() + " - " + collectionItemText.trim() + " should be displayed", itemObjName.trim() + " - " + collectionItemText.trim() + " found", "Pass");
						item = getParentElement(itemListLocator, className, "Item", parentlevel, i + 1);
						break;
					}
					else{
						check = 0;
					}
				}
			}
			
		 	if(driverType.trim().toUpperCase().contains("IOS")){
				 if(check == 1){
					 while(!checkIfElementPresent(By.xpath(item), 1)){
						 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
					 }
					 break;
				 }
			 }
			
			if(check == 0){
				if(cflag == 0)
					cflag = 1;
				last = listItems.get(listItems.size() - 1) == null ? listItems.get(listItems.size() - 2).getText().trim() : listItems.get(listItems.size() - 1).getText().trim();
				first = listItems.get(0).getText().trim();
				if(driverType.trim().toUpperCase().contains("IOS")){
					String lastElement = getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size());
					while(!checkIfElementPresent(By.xpath(lastElement), 1)){
						 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
					}
				}
				else{
					if(flag)
						swipe(webElement, driver.findElement(By.xpath(getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size()))), swipeDirection);
					else
						swipe(webElement, swipeDirection);
				}
			}
		}while(check == 0);
		
		if(check == 0){
			Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " at index - " + index + " is displayed", itemObjName.trim() + " at index - " + index + " should be displayed", itemObjName.trim() + " at index - " + index + " not found", "Fail");
			return item;
		}
		
		return item;
	}
	
	/**
	 * Get item list on the screen by swiping up(if needed)
	 * 
	 * @param itemListLocator
	 * @param itemListObjName
	 * @param numSwipes
	 * @param parentlevel
	 * @param viewLocator
	 * @param swipeDirection
	 * @return
	 */
	public List<List<String>> getItemList(By itemListLocator, int numSwipes, int parentlevel, By viewLocator, String swipeDirection){
		String className;
		if(driverType.trim().toUpperCase().contains("ANDROID")){
			className = "android.widget.TextView";
		}
		else{
			className = "UIAStaticText";
		}
		
		WebElement webElement = null;
		
		if(!driverType.trim().toUpperCase().contains("IOS")){
			webElement = getElementWhenVisible(viewLocator);
		}

		String last = "", first = "";
		
		List<List<String>> itemList = new ArrayList<List<String>>();
		List<List<String>> oldlst = null;
		List<List<String>> newlst = null;
		int cflag = 0;
		int toggle = 0, checkCountofElements = 1;
		
		do{
			boolean flag = false;
			List<WebElement> listItems = getWebElementsList(itemListLocator);
			List<String> nlst = new ArrayList<String>();
			if(cflag == 1){
				oldlst = new ArrayList<List<String>>();
				oldlst.addAll(newlst);
			}
			newlst = new ArrayList<List<String>>();
			
			if(listItems == null || listItems.size() == 0){
				break;
			}
			
			if(last.equalsIgnoreCase(listItems.get(listItems.size() - 1).getText().trim()) && first.equalsIgnoreCase(listItems.get(0).getText().trim())){
				if(driverType.trim().toUpperCase().contains("IOS")){
					break;
				}
				else{
					String parent = getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size());
					List<WebElement> childItems;
					if(parentlevel > 0){
						childItems = getChildWebElementsList(By.xpath(parent), By.xpath("//" + className), "Textview", className);
					}
					else{
						childItems = new ArrayList<WebElement>();
						childItems.add(driver.findElement(By.xpath(parent)));
					}
					if(childItems == null || childItems.size() == 0)
						break;
					List<String>childItemsText= new ArrayList<String>();
					for(WebElement we:childItems){
						if(driverType.trim().toUpperCase().contains("IOS")){
							childItemsText.add(we.getText().trim());
						}
						else
							childItemsText.add(we.getAttribute("text").trim());
					}
					newlst.add(childItemsText);
					if(verifyContentinListofList(oldlst, childItemsText)){
						//Do Nothing
					}
					else{
						itemList.add(childItemsText);
					}
					break;
				}
			}
			
			for(int i = 0; i < listItems.size() && toggle == 0; i++){
				if(driverType.trim().toUpperCase().contains("IOS")){
					nlst.add(listItems.get(i).getText());
				}
				else
					nlst.add(listItems.get(i).getAttribute("text"));
				
				if(i == listItems.size() - 1 && !driverType.trim().toUpperCase().contains("IOS")){
					flag = true;
					break;
				}
				String parent = getParentElement(itemListLocator, className, "Item", parentlevel, i + 1);
				List<WebElement> childItems;
				if(parentlevel > 0){
					childItems = getChildWebElementsList(By.xpath(parent), By.xpath("//" + className), "Textview", className);
				}
				else{
					childItems = new ArrayList<WebElement>();
					childItems.add(listItems.get(i));
				}
				if(childItems == null || childItems.size() == 0)
					continue;
				List<String>childItemsText= new ArrayList<String>();
				for(WebElement we:childItems){
					if(driverType.trim().toUpperCase().contains("IOS")){
						childItemsText.add(we.getText().trim());
					}
					else
						childItemsText.add(we.getAttribute("text").trim());
				}
				if(checkCountofElements < childItemsText.size())
					checkCountofElements = childItemsText.size();
				
				newlst.add(childItemsText);
				if(driverType.trim().toUpperCase().contains("IOS")){
					itemList.add(childItemsText);
				}
				else{
					if(verifyContentinListofList(oldlst, childItemsText)){
						//Do Nothing
					}
					else{
						itemList.add(childItemsText);
					}
				}
			}
			
			if(cflag == 0)
				cflag = 1;
			
			if(driverType.trim().toUpperCase().contains("IOS")){
				break;
			}
			
			if(numSwipes > 0){
				last = listItems.get(listItems.size() - 1) == null ? listItems.get(listItems.size() - 2).getText().trim() : listItems.get(listItems.size() - 1).getText().trim();
				first = listItems.get(0).getText().trim();
				if(flag)
					swipe(webElement, driver.findElement(By.xpath(getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size()))) , swipeDirection);
				else
					swipe(webElement, swipeDirection);
				numSwipes--;
			}
			else{
				break;
			}
			
		}while(true);
		
		return itemList;
	}
	
	/**
	 * Get item list on the screen by swiping up(if needed) - overloaded
	 * 
	 * @param itemListLocator
	 * @param childElementLocator
	 * @param childElementObjType
	 * @param childAttributeName
	 * @param numSwipes
	 * @param parentlevel
	 * @param viewLocator
	 * @param swipeDirection
	 * @return
	 */
	public List<List<String>> getItemList(By itemListLocator, String childAttributeName, int numSwipes, int parentlevel, By viewLocator, String swipeDirection){
		String className;
		if(driverType.trim().toUpperCase().contains("ANDROID")){
			className = "android.widget.TextView";
		}
		else{
			className = "UIAStaticText";
		}
		
		WebElement webElement = null;
		if(!driverType.trim().toUpperCase().contains("IOS")){
			webElement = getElementWhenVisible(viewLocator);
		}
		
		String last = "", first = "";
		
		List<List<String>> itemList = new ArrayList<List<String>>();
		List<List<String>> oldlst = null;
		List<List<String>> newlst = null;
		int cflag = 0;
		int toggle = 0, checkCountofElements = 1;
		
		do{
			boolean flag = false;
			List<WebElement> listItems = getWebElementsList(itemListLocator);
			List<String> nlst = new ArrayList<String>();
			if(cflag == 1){
				oldlst = new ArrayList<List<String>>();
				oldlst.addAll(newlst);
			}
			newlst = new ArrayList<List<String>>();
			
			if(listItems == null || listItems.size() == 0){
				break;
			}
			
			if(last.equalsIgnoreCase(listItems.get(listItems.size() - 1).getText().trim()) && first.equalsIgnoreCase(listItems.get(0).getText().trim())){
				if(driverType.trim().toUpperCase().contains("IOS")){
					break;
				}
				else{
					String parent = getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size());
					List<WebElement> childItems;
					if(parentlevel > 0){
						childItems = getChildWebElementsList(By.xpath(parent), By.xpath("//*"), "All elements", "*");
					}
					else{
						childItems = new ArrayList<WebElement>();
						childItems.add(driver.findElement(By.xpath(parent)));
					}
					if(childItems == null || childItems.size() == 0)
						break;
					List<String>childItemsText = new ArrayList<String>();
					List<String>childItemsPropValue = new ArrayList<String>();
					for(WebElement we:childItems){
						if(!we.getAttribute(childAttributeName).trim().equalsIgnoreCase(""))
							childItemsPropValue.add(we.getAttribute(childAttributeName).trim());
						if(driverType.trim().toUpperCase().contains("IOS")){
							if(!we.getText().trim().equalsIgnoreCase("")){
								childItemsPropValue.add(we.getText().trim());
								childItemsText.add(we.getText().trim());
							}
						}
						else{
							if(!we.getAttribute("text").trim().equalsIgnoreCase("")){
								childItemsPropValue.add(we.getAttribute("text").trim());
								childItemsText.add(we.getAttribute("text").trim());
							}
						}
					}
					newlst.add(childItemsText);
					if(verifyContentinListofList(oldlst, childItemsText)){
						//Do Nothing
					}
					else{
						itemList.add(childItemsPropValue);
					}
					break;
				}
			}
			
			for(int i = 0; i < listItems.size() && toggle == 0; i++){
				if(driverType.trim().toUpperCase().contains("IOS")){
					nlst.add(listItems.get(i).getText());
				}
				else
					nlst.add(listItems.get(i).getAttribute("text"));
				
				if(i == listItems.size() - 1 && !driverType.trim().toUpperCase().contains("IOS")){
					if(newlst.size() > 0){
						flag = true;
						break;
					}
				}
				
				String parent = getParentElement(itemListLocator, className, "Item", parentlevel, i + 1);
				List<WebElement> childItems;
				if(parentlevel > 0){
					childItems = getChildWebElementsList(By.xpath(parent), By.xpath("//*"), "All elements", "*");
				}
				else{
					childItems = new ArrayList<WebElement>();
					childItems.add(listItems.get(i));
				}
				if(childItems == null || childItems.size() == 0)
					continue;
				List<String>childItemsText = new ArrayList<String>();
				List<String>childItemsPropValue = new ArrayList<String>();
				for(WebElement we:childItems){
					if(!we.getAttribute(childAttributeName).trim().equalsIgnoreCase(""))
						childItemsPropValue.add(we.getAttribute(childAttributeName).trim());
					if(driverType.trim().toUpperCase().contains("IOS")){
						if(!we.getText().trim().equalsIgnoreCase("")){
							childItemsPropValue.add(we.getText().trim());
							childItemsText.add(we.getText().trim());
						}
					}
					else{
						if(!we.getAttribute("text").trim().equalsIgnoreCase("")){
							childItemsPropValue.add(we.getAttribute("text").trim());
							childItemsText.add(we.getAttribute("text").trim());
						}
					}
				}
				if(checkCountofElements < childItemsText.size())
					checkCountofElements = childItemsText.size();
				
				newlst.add(childItemsText);
				if(driverType.trim().toUpperCase().contains("IOS")){
					itemList.add(childItemsPropValue);
				}
				else{
					if(verifyContentinListofList(oldlst, childItemsText)){
						//Do Nothing
					}
					else{
						itemList.add(childItemsPropValue);
					}
				}
			}
			
			if(cflag == 0)
				cflag = 1;
			
			 if(driverType.trim().toUpperCase().contains("IOS")){
				 break;
			 }
			
			if(numSwipes > 0){
				last = listItems.get(listItems.size() - 1) == null ? listItems.get(listItems.size() - 2).getText().trim() : listItems.get(listItems.size() - 1).getText().trim();
				first = listItems.get(0).getText().trim();
				if(flag)
					swipe(webElement, driver.findElement(By.xpath(getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size()))) , swipeDirection);
				else
					swipe(webElement, swipeDirection);
				numSwipes--;
			}
			else{
				break;
			}
			
		}while(true);
		
		return itemList;
	}
	
	/**
	 * Get selected item list on the screen by swiping up(if needed)
	 * Working only in case of android
	 * 
	 * @param itemListLocator
	 * @param itemListObjName
	 * @param numSwipes
	 * @param parentlevel
	 * @param checkBoxLocator
	 * @param viewLocator
	 * @param swipeDirection
	 * @return
	 */
	public String getSelectedItemList(By itemListLocator, String itemListObjName, int numSwipes, int parentlevel, By checkBoxLocator, By viewLocator, String swipeDirection){
		String className;
		if(driverType.trim().toUpperCase().contains("ANDROID")){
			className = "android.widget.TextView";
		}
		else{
			className = "UIAStaticText";
		}
		
		WebElement webElement = null;
		if(!driverType.trim().toUpperCase().contains("IOS")){
			webElement = getElementWhenVisible(viewLocator);
		}
		
		String itemList = "", last = "", first = "";
		
		List<List<String>> oldlst = new ArrayList<List<String>>();
		List<List<String>> newlst = null;
		int cflag = 0, checkCountofElements = 1;
		
		do{
			boolean flag = false;
			List<WebElement> listItems = getWebElementsList(itemListLocator);
			List<String> nlst = new ArrayList<String>();
			if(cflag == 1){
				oldlst.addAll(newlst);
			}
			newlst = new ArrayList<List<String>>();
			
			if(listItems == null || listItems.size() == 0){
				break;
			}
			
			if(last.equalsIgnoreCase(listItems.get(listItems.size() - 1).getText().trim()) && first.equalsIgnoreCase(listItems.get(0).getText().trim())){
				break;
			}
			
			for(int i = 0; i < listItems.size(); i++){
				if(driverType.trim().toUpperCase().contains("IOS")){
					nlst.add(listItems.get(i).getText());
				}
				else
					nlst.add(listItems.get(i).getAttribute("text"));
				
				String parent = getParentElement(itemListLocator, className, "Item", parentlevel, i + 1);
				WebElement checkbox = getSingleChildObject(parent, checkBoxLocator, "android.widget.CheckBox", "Item checkbox");
				if(checkbox == null)
					continue;
				if(checkbox.isSelected()){
					List<WebElement> childItems = getChildWebElementsList(By.xpath(parent), By.xpath("//" + className), "Textview", className);
					List<String>childItemsText= new ArrayList<String>();
					for(WebElement we:childItems){
						if(driverType.trim().toUpperCase().contains("IOS")){
							childItemsText.add(we.getText().trim());
						}
						else
							childItemsText.add(we.getAttribute("text").trim());
					}
					if(i == listItems.size() - 1){
						if(newlst.size() > 0){
							if(checkCountofElements > childItemsText.size()){
								flag = true;
								break;
							}
						}
					}
					
					if(checkCountofElements < childItemsText.size())
						checkCountofElements = childItemsText.size();
					
					newlst.add(childItemsText);
					if(verifyContentinListofList(oldlst, childItemsText)){
						//Do Nothing
					}
					else{
						int k = 0;
						for(; k < childItemsText.size() - 1; k++){
							itemList += childItemsText.get(k) + "::";
						}
						itemList += childItemsText.get(k) + ";";
					}
				}	
			}
			
			if(cflag == 0)
				cflag = 1;
			
			if(numSwipes > 0){
				last = listItems.get(listItems.size() - 1) == null ? listItems.get(listItems.size() - 2).getText().trim() : listItems.get(listItems.size() - 1).getText().trim();
				first = listItems.get(0).getText().trim();
				if(flag)
					swipe(webElement, driver.findElement(By.xpath(getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size()))) , swipeDirection);
				else
					swipe(webElement, swipeDirection);
				numSwipes--;
			}
			else{
				break;
			}
		}while(true);
		
		return itemList;
	}
	
	/**
	 * Retrieve logs such adb logcat logs etc.
	 * 
	 * @param logType
	 * @return
	 */
	public List<LogEntry> getLogs(String logType, Level level){
		LogEntries _log = driver.manage().logs().get(logType);
		return _log.filter(level);
	}
	
	/**
	 * Show quick settings
	 * @throws Exception 
	 * 
	 */
	public void showQuickSettings() throws Exception{
		openNotifications();
		runADBCommand(new String[]{"-s", Environment.get("udid").trim(), "shell", "input", "swipe", "10", "10", "10", "1000"}, new Boolean[]{false, false, false, false, false, false, false, false, false}, 2000);
	}
	
	/**
	 * Close notifications
	 * @throws Exception 
	 * 
	 */
	public void closeNotifications() throws Exception{
		runADBCommand(new String[]{"-s", Environment.get("udid").trim(), "shell", "input", "swipe", "10", "1000", "10", "10"}, new Boolean[]{false, false, false, false, false, false, false, false, false}, 2000);
		if(checkIfElementPresent(By.id("com.android.systemui:id/dismiss_text"), 1)){
			runADBCommand(new String[]{"-s", Environment.get("udid").trim(), "shell", "input", "swipe", "10", "1000", "10", "10"}, new Boolean[]{false, false, false, false, false, false, false, false, false}, 2000);
		}
	}
	
	/**
	 * Hide quick settings
	 * @throws Exception 
	 * 
	 */
	public void hideQuickSettings() throws Exception{
		runADBCommand(new String[]{"-s", Environment.get("udid").trim(), "shell", "input", "swipe", "10", "1000", "10", "10"}, new Boolean[]{false, false, false, false, false, false, false, false, false}, 2000);
	}
	
	/**
	 * Run app in background
	 * 
	 * @param seconds
	 */
	public void runAppinBackground(int seconds){
		(appium).runAppInBackground(seconds);
	}
	
	/**
	 * Run app in background using ADB
	 * 
	 * @param milliseconds
	 */
	public void runAppinBackgroundUsingADB(long milliseconds){
		sendKeyEvent(187);
		sync(Long.valueOf(milliseconds));
		sendKeyEvent(187);
	}
	
	/**
	 * Lock screen
	 * 
	 * @param seconds
	 */
	public void lockScreen(int seconds){
		if(!android.isLocked()){
			android.lockScreen(seconds);
		}
	}
	
	/**
	 * Is screen locked
	 * 
	 * @return
	 */
	public boolean isLocked(){
		return android.isLocked();
	}
	
	/**
	 * Unlock screen
	 * 
	 */
	public void unlockScreen(){
		sendKeyEvent(26);
	}
	
	/**
	 * Unlock phone
	 * 
	 * @throws Exception 
	 */
	public void unlockPhone() throws Exception{
		launchApp("io.appium.unlock", "io.appium.unlock.Unlock");
	}
	
	/**
	 * Force stop any app (In case of android)
	 * 
	 * @param packageName
	 * @throws Exception 
	 */
	public void forceStop(String packageName) throws Exception{
		if(Environment.get("user").trim().equalsIgnoreCase(""))
			runADBCommand(new String[]{"-s", Environment.get("udid"), "shell", "am", "force-stop", packageName}, new Boolean[]{false, false, false, false, false, false}, 2000);
		else{
			if(isAppInstalled("io.appium.networktoggle") == false){
				installAppFromResources("network_toggle.apk");
			}
			launchApp("io.appium.networktoggle", "io.appium.networktoggle.MainActivity -e stop " + packageName);
		}
	}
	
	/**
	 * Epoch time converter
	 * 
	 * @param value
	 * @param type : EPOCH or any value
	 * @return
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	public String epochTimeConverter(String value, String type) throws ParseException{
		String output = null;
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        
		if(type.trim().equalsIgnoreCase("EPOCH")){
	        output = formatter.format(new java.util.Date(Long.parseLong(value)));
		}
		else{
			Date _date = null;
			if(!value.trim().equalsIgnoreCase(""))
				_date = formatter.parse((formatter.format(new Date(value))));
			else
				_date = formatter.parse((formatter.format(new Date())));
				output = String.valueOf(_date.getTime());
		}
		
		return output;
	}
	
	/**
	 * Check if app (Android/IOS) is installed on device
	 * 
	 * @param packageName
	 * @return
	 * @throws Exception 
	 */
	public boolean isAppInstalled(String packageName) throws Exception{
		return (appium).isAppInstalled(packageName);
	}
	
	
	/**
	 * Add songs to the android device
	 * 
	 * @param files
	 * @param remotePath
	 * @throws Exception
	 */
	public void addSongs(String[] files, String remotePath) throws Exception{
		String[] folderNames = files;
		for(int i = 0 ; i < folderNames.length; i++){
			pushFile(folderNames[i], remotePath);
		}
		broadcastIntent("android.intent.action.MEDIA_MOUNTED", null);
	}
	
	/**
	 * Delete songs from device storage
	 * 
	 * @param SONGS_NAME_WITH_PATH
	 * @param FOLDER_NAMES
	 * @throws Exception 
	 */
	public void removeSongs(String[] SONGS_NAME_WITH_PATH, String[] FOLDER_NAMES) throws Exception{
		if(SONGS_NAME_WITH_PATH != null && SONGS_NAME_WITH_PATH.length > 0){
			String[] songsPath = SONGS_NAME_WITH_PATH;
			for(int i = 0; i < songsPath.length; i++){
				runADBCommand(new String[]{"-s", Environment.get("udid"), "shell", "rm", "sdcard/" + songsPath[i]}, new Boolean[]{false, false, false, false, false}, 2000);
				String[] folders = songsPath[i].split("/");
				String newPath = "";
				for(int j = 0 ; j < folders.length - 1 ; j++){
					if(!folders[j].trim().equalsIgnoreCase("")){
						newPath = newPath + folders[j] + "/";
					}
				}
				if(Environment.get("user").trim().equalsIgnoreCase(""))
					runADBCommand(new String[]{"-s", Environment.get("udid"), "shell", "am" , "broadcast", "-a", "android.intent.action.MEDIA_MOUNTED", "-d", "file:///mnt/sdcard/" + newPath}, new Boolean[]{false, false, false, false, false, false, false, false, false}, 4000);
			}
		}
		
		//adb -s 06c10888003ba315 shell rm -r sdcard/Music
		String[] folderNames = FOLDER_NAMES;
		for(int i = 0 ; i < folderNames.length; i++){
			runADBCommand(new String[]{"-s", Environment.get("udid"), "shell", "rm" , "-r", "sdcard/" + folderNames[i]}, new Boolean[]{false, false, false, false, false, false}, 2000);
			String[] folders = folderNames[i].split("/");
			String newPath = "";
			for(int j = 0 ; j < folders.length - 1 ; j++){
				if(!folders[j].trim().equalsIgnoreCase("")){
					newPath = newPath + folders[j] + "/";
				}
			}
			//adb shell am broadcast -a android.intent.action.MEDIA_MOUNTED -d file:///mnt/sdcard
			if(Environment.get("user").trim().equalsIgnoreCase(""))
				runADBCommand(new String[]{"-s", Environment.get("udid"), "shell", "am" , "broadcast", "-a", "android.intent.action.MEDIA_MOUNTED", "-d", "file:///mnt/sdcard/" + newPath}, new Boolean[]{false, false, false, false, false, false, false, false, false}, 4000);
		}
		
		if(!Environment.get("user").trim().equalsIgnoreCase(""))
			broadcastIntent("android.intent.action.MEDIA_MOUNTED", null);
	}
	
	/**
	 * Context switching
	 * 
	 * @param contextName
	 */
	public void switchToContext(String contextName){
		(appium).context(contextName);
	}
	
	/**
	 * Get current context
	 * 
	 * @return
	 */
	public String getCurrentContext(){
		return (appium).getContext();
	}
	
	/**
	 * Get list of available context handles
	 * 
	 * @return
	 */
	public Set<String> getContextHandles(){
		return (appium).getContextHandles();
	}
	
	/**
	 * Get current screen orientation - PORTRAIT or LANDSCAPE
	 * @return
	 */
	public ScreenOrientation getOrientation(){
		return (appium).getOrientation();
	}
	
	/**
	 * Get physical location of device - altitude, latitude and longitude (Only in case of appium)
	 * 
	 * @return
	 */
	public double[] getLocation(){
		Location location = (appium).location();
		double altitude = location.getAltitude();
		double longitude = location.getLongitude();
		double latitude = location.getLatitude();
		return new double[]{altitude, latitude, longitude};
	}
	
	/**
	 * Pull file from device storage
	 * 
	 * @param remotePath
	 * @param path
	 * @return
	 * @throws Exception 
	 */
	public String pullFile(String remotePath, String path) throws Exception{
		byte[] bytes = (appium).pullFile(remotePath);
		Files.write(bytes, new File(path));
		return path;
	}
	
	/**
	 * Pull folder from device storage
	 * 
	 * @param remotePath
	 * @param path
	 * @return
	 * @throws Exception 
	 */
	public String pullFolder(String remotePath, String path) throws Exception{
		byte[] bytes = (appium).pullFolder(remotePath);
		Files.write(bytes, new File(path));
		return path;
	}
	
	/**
	 * Rotate device screen
	 * 
	 * @param orientation
	 * @return
	 */
	public void rotateScreen(String orientation){
		if(!getOrientation().value().trim().equalsIgnoreCase(orientation)){
			(appium).rotate(ScreenOrientation.valueOf(orientation));
		}
	}
	
	/**
	 * Get android app current activity
	 * 
	 * @return
	 */
	public String getCurrentActivity(){
		return android.currentActivity();
	}
	
	/**
	 * Broadcast intent (with path)
	 * 
	 * @param intent
	 * @param path
	 * @throws Exception 
	 */
	public void broadcastIntent(String intent, String path) throws Exception{
		String arguments = "-e broadcast \"" + intent;
		if(path != null && !path.trim().equalsIgnoreCase("")){
			arguments += "," + path;
		}
		arguments += "\"";
		if(isAppInstalled("io.appium.networktoggle") == false){
			installAppFromResources("network_toggle.apk");
		}
		launchApp("io.appium.networktoggle", "io.appium.networktoggle.MainActivity " + arguments);	
	}
	
	/**
	 * Push file to device local storage
	 * 
	 * @param localfilePath
	 * @param remotePath
	 * @throws Exception 
	 */
	public void pushFile(String localfilePath, String remotePath) throws Exception{
		FileInputStream fileInputStream = null;
        File file = new File(localfilePath);
        byte[] bFile = new byte[(int) file.length()];
        try {
            //convert file into array of bytes
        	fileInputStream = new FileInputStream(file);
        	fileInputStream.read(bFile);
        	fileInputStream.close();
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        android.pushFile(remotePath, Base64.encode(bFile));
	}
	
	/**
	 * Toggle location services
	 * @throws Exception 
	 * 
	 */
	public void toggleLocationServices() throws Exception{
		android.toggleLocationServices();
	}
	
	/**
	 * Set network settings
	 * 
	 * @param networkType
	 * @param mode
	 * @throws Exception 
	 */
	public void networkToggle(List<String> networkType, List<Boolean> mode) throws Exception{
		
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
	
	/**
	 * Get list of toast messages generated latest based on package name
	 * 
	 * @param packageName
	 * @param last_no_of_messages
	 * @return
	 * @throws Exception
	 */
	public List<String> getToastMessages(String packageName, int last_no_of_messages) throws Exception{
		List<String> msgs = new ArrayList<String>();
		String output = "";
		output = runADBCommand(new String[]{"-s", Environment.get("udid"), "shell", "logcat", "|", "grep", "Captured message.*" + packageName}, new Boolean[]{false, false, false, false, false, false, false}, 2000);
		System.out.println(output);
		if(!output.trim().equalsIgnoreCase("")){
			String[] logs = output.trim().split("\n");
			int n = 0;
			for(int i = logs.length - 1; i >= 0 && n < last_no_of_messages; i--, n++){
				String toastMessage = logs[i].split("Captured message")[1].trim().split("for source")[0].trim();
				msgs.add(toastMessage.substring(1, toastMessage.length() - 1));
			}
		}
		return msgs;
	}
	
	/**
	 * Get ADB logcat logs based on search string
	 * 
	 * @param searchString
	 * @return
	 * @throws Exception
	 */
	public String[] getADBLogsOnSearchString(String searchString, boolean caseSensitive, long... wait) throws Exception{
		String[] logs = null;
		String output = "";
		assert wait.length <= 1;
		long seconds = wait.length > 0 ? (wait[0] * 1000) : 2000;
		if(!caseSensitive){
			output = runADBCommand(new String[]{"-s", Environment.get("udid"), "shell", "logcat", "|", "grep", "-i", searchString}, new Boolean[]{false, false, false, false, false, false, false, false}, seconds);
		}
		else{
			output = runADBCommand(new String[]{"-s", Environment.get("udid"), "shell", "logcat", "|", "grep", searchString}, new Boolean[]{false, false, false, false, false, false, false}, seconds);
		}
		System.out.println(output);
		if(!output.trim().equalsIgnoreCase("")){
			logs = output.trim().split("\n");
		}
		return logs;
	}
	
	/**
	 * Get ADB logcat logs based on search string
	 * 
	 * @param searchString
	 * @return
	 * @throws Exception
	 */
	public String getADBLogsOutputOnSearchString(String searchString, boolean caseSensitive, long... wait) throws Exception{
		String output = "";
		assert wait.length <= 1;
		long seconds = wait.length > 0 ? (wait[0] * 1000) : 2000;
		if(!caseSensitive){
			output = runADBCommand(new String[]{"-s", Environment.get("udid"), "shell", "logcat", "|", "grep", "-i", searchString}, new Boolean[]{false, false, false, false, false, false, false, false}, seconds);
		}
		else{
			output = runADBCommand(new String[]{"-s", Environment.get("udid"), "shell", "logcat", "|", "grep", searchString}, new Boolean[]{false, false, false, false, false, false, false}, seconds);
		}
		System.out.println(output);
		return output;
	}
	
	/**
	 * Run perl script
	 * 
	 * @param arguments
	 * @return
	 * @throws Exception
	 */
	public String runPerlScript(String condition, String... arguments) throws Exception{
		String perlPath = "";
		if(OSValidator.shellType.trim().equalsIgnoreCase("cmd")){
			perlPath = runCommand(OSValidator.shellType, new String[]{"/c", "which perl"}, new boolean[]{false, false}, 2000);
		}
		else{
			perlPath = runCommand(OSValidator.shellType, new String[]{"-l", "-c", "which perl"}, new boolean[]{false, false, false}, 2000);
		}
  
		if(perlPath == null){
			log.info("PERL path not found");
			return null;
		}
  
		perlPath = perlPath.split("\n")[0].replace("//", "/");
		
		CommandLine command = new CommandLine(perlPath);
		for(int i = 0; i < arguments.length; i++){
			command.addArgument(arguments[i], false);
		}
  
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		PumpStreamHandler psh = new PumpStreamHandler(stdout);
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
		DefaultExecutor executor = new DefaultExecutor();
		executor.setStreamHandler(psh);
		try {
			executor.execute(command, resultHandler);
			long wait = 30000L;
			switch(condition.trim().toUpperCase()){
				case "NOT NULL":
					while(stdout.toString().trim().equalsIgnoreCase("") && wait > 0){
						sync(1L);
						wait--;
					} 
					break;
				default:
					while(!stdout.toString().trim().contains(condition.trim()) && wait > 0){
						sync(1L);
						wait--;
					}
			}
			
		} catch (IOException e1) {
			log.info("Threw a Exception in BaseUtil::runADBCommand, full stack trace follows:", e1);
		}
		
		return stdout.toString();
	}
	
	/**
	 * Fetch file from remote server and store it locally
	 * 
	 * @param host
	 * @param username
	 * @param remotePath
	 * @param remoteFileName
	 * @param localPath
	 * @param localFileName
	 * @return
	 * @throws Exception
	 */
	public String fetchLogFile(String host, String username, String remotePath, String remoteFileName, String localPath, String localFileName) throws Exception{
		if(localPath == null || localPath.trim().equalsIgnoreCase("")){
	        if(!new File(Environment.get("CURRENTEXECUTIONFOLDER") + OSValidator.delimiter + "ANALYTICS LOGS").exists()){
	        	new File(Environment.get("CURRENTEXECUTIONFOLDER") + OSValidator.delimiter + "ANALYTICS LOGS").mkdirs();	                
	        }
	        
	        localPath = Environment.get("CURRENTEXECUTIONFOLDER") + OSValidator.delimiter + "ANALYTICS LOGS" + OSValidator.delimiter;
		}
		
		if(localFileName == null || localFileName.trim().equalsIgnoreCase("")){
			java.util.Date today = new java.util.Date();
			Timestamp now = new java.sql.Timestamp(today.getTime());
			String tempNow[] = now.toString().split("\\.");
			final String sStartTime = tempNow[0].replaceAll(":", ".").replaceAll(" ", "T");
			localFileName = localPath + "Analytics_" + sStartTime + ".log";
		}
		else{
			localFileName = localPath + localFileName + ".log";
		}
		
		String output = "";
		output = runPerlScript("copied from " + host + " successfully!", System.getProperty("user.dir") + Environment.get("perlScriptPath") + "FetchLogFile.pl", host, username, remotePath, remoteFileName, localFileName);
		if(output.trim().contains("copied from " + host + " successfully!")){
			log.info("Log file fetched successfully and stored in " + localFileName);
			return localFileName;
		}
		else{
			log.info("Log file not found");
			return null;
		}
	}
	
	/**
	 * Run command on remote server
	 * 
	 * @param host
	 * @param username
	 * @param command
	 * @throws Exception 
	 */
	public String runRemoteCommand(String host, String username, String command) throws Exception{
		String output = runPerlScript("NOT NULL", System.getProperty("user.dir") + Environment.get("perlScriptPath") + "RunRemoteCommand.pl", host, username, command);
		return output;
	}
	
	/**
	 * Get current time in seconds on android device
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getCurrentTimeInSecondsOnAndroidDevice() throws Exception{
		return runADBCommand(new String[]{"-s", Environment.get("udid").trim(), "shell", "date", "+%s"}, new Boolean[]{false, false, false, false, false}, 2000);
	}

	/**
	 * Get current time in milliseconds on android device
	 * 
	 * @return
	 * @throws Exception
	 */
	public long getCurrentTimeInMillisecondsOnAndroidDevice() throws Exception{
		String timeinsecondsOnDevice = getCurrentTimeInSecondsOnAndroidDevice();
		long timeinMillisecondsOnDeviceL = Long.valueOf(timeinsecondsOnDevice.trim())*1000;
		return timeinMillisecondsOnDeviceL;
	}
	
	/**
	 * Convert locators based on automation tool - appium or selendroid
	 * By default "Appium" is the automation name
	 * 
	 * @return
	 */
	public By convertLocator(By locator){
		String val = null;
		
		if(Environment.get("automationName").trim().equalsIgnoreCase("Selendroid")){
			if (locator instanceof By.ByXPath){
				val = locator.toString().replaceFirst("By.xpath: ", "").trim();
				boolean flag = true;
				while(flag){
					if(val.trim().contains("android.view.View")){
						val = val.replace("android.view.View", "Toolbar");
					}
					else if(val.trim().contains("resource-id")){
						val = val.replace("resource-id", "id");
					}
					else if(val.trim().contains("android.widget.TextView")){
						val = val.replace("android.widget.TextView", "TextView");
					}
					else{
						flag = false;
					}
				}
				
				val = val.replace(Environment.get("appPackage") + ":id/", "");
				return By.xpath(val);
			}
			else if (locator instanceof By.ById){
				val = locator.toString().replaceFirst("By.id: ", "");
				String[] split = val.split(":id/");
				if(split.length > 1){
					return By.id(split[1]);
				}
				else{
					return locator;
				}
			}
			else if (locator instanceof By.ByClassName){
				//TBD
			}
			else{
				return locator;
			}
		}
		
		return locator;
	}
	
	/**
	 * Download app from amazon cloud s3
	 * 
	 * @param remotePath
	 * @param downloadPath
	 * @param appName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public boolean downloadFromS3(String remotePath, String downloadPath, String appName) throws FileNotFoundException, IOException{
		AmazonS3 s3 = new AmazonS3Client(new BasicAWSCredentials(Environment.get("accessKey"), Environment.get("secretKey")));
    	Region usWest2 = Region.getRegion(Regions.US_EAST_1);
    	s3.setRegion(usWest2);

    	String bucketName = "twanganalytics-dev";
    	//"QA-Builds/apps/" + version.replace(" ", ".") + "/" + appName;
    	String key = "";
    	if(driverType.trim().toUpperCase().contains("ANDROID"))
    		key = remotePath + appName;
    	else if(driverType.trim().toUpperCase().contains("IOS"))
    		key = remotePath + appName + "/";
    	
    	System.out.println("===========================================");
    	System.out.println("Getting Started with Amazon S3");
    	System.out.println("===========================================\n");

    	downloadPath = downloadPath.trim().endsWith(OSValidator.delimiter) ? downloadPath.trim() : downloadPath.trim() + OSValidator.delimiter;
    	System.out.println("Downloading APP - " + downloadPath + appName + " from S3");
    	//System.getProperty("user.dir") + "/resources/apps/" + version + "/" + appName
    	try{
    		S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
			S3ObjectInputStream objectData = object.getObjectContent();
			if(!new java.io.File(downloadPath).exists()){
	    		new java.io.File(downloadPath).mkdirs();
	    	}
			
			IOUtils.copy(objectData, new FileOutputStream(downloadPath + appName));
			System.out.println("Downloading completed");
			return true;
    	}
    	catch(Exception ex){
    		//Do Nothing
    	}
    	return false;
	}
	
	/**
	 * Check app version
	 * 
	 * @param packageName
	 * @return
	 * @throws Exception
	 */
	public String checkAppVersion(String packageName) throws Exception{
		String output = runADBCommand(new String[]{"-s", Environment.get("udid"), "shell", "dumpsys", "package", packageName, "|", "grep", "versionName"}, new Boolean[]{false, false, false, false, false, false, false, false, false}, 2000);
		String versionName = "";
		if(!output.trim().equalsIgnoreCase("")){
			output = output.trim().split("\n")[0].trim();
			if(output.contains("="))
				versionName = output.split("=")[1].trim();
		}
		return versionName;
	}
	
	/**
	 * Get location of mobile element
	 * 
	 * @param locator
	 * @return
	 */
	public int[] getLocation(By locator){
		WebElement we = getElementWhenVisible(locator);
		MobileElement me = (MobileElement) we;
		Point pt = me.getLocation();
		return new int[]{pt.x, pt.y};
	}
	
	/**
	 * Get center of mobile element
	 * 
	 * @param locator
	 * @return
	 */
	public int[] getCenter(By locator){
		WebElement we = getElementWhenVisible(locator);
		MobileElement me = (MobileElement) we;
		Point pt = me.getCenter();
		return new int[]{pt.x, pt.y};
	}
	
	/**
	 * Get width and height of mobile element
	 * 
	 * @param locator
	 * @return
	 */
	public int[] getSize(By locator){
		WebElement we = getElementWhenVisible(locator);
		MobileElement me = (MobileElement) we;
		Dimension di = me.getSize();
		return new int[]{di.getWidth(), di.getHeight()};
	}
	
	/**
	 * Get specified item from list of items on the screen by swiping up(if needed) based on category
	 * 
	 * @param itemListLocator
	 * @param itemName
	 * @param itemObjName
	 * @param parentlevel
	 * @param index
	 * @param viewLocator
	 * @param swipeDirection
	 * @param category
	 * @return
	 */
	@SuppressWarnings("unused")
	public String getItemByNameBasedOnCategory(By itemListLocator, String itemName, String itemObjName, int parentlevel, String index, By viewLocator, String swipeDirection, String category){
		String className;
		if(driverType.trim().toUpperCase().contains("ANDROID")){
			className = "android.widget.TextView";
		}
		else{
			className = "UIAStaticText";
		}
		
		String item = null, last = "", first = "", currentDirection = swipeDirection.trim();
		WebElement webElement = getElementWhenVisible(viewLocator);
		String list_header_name = category.trim();
		int check = 0, count = 0;
		int position = 1, seemore = 0;
		int cflag = 0;
		if(!index.trim().equalsIgnoreCase("")){
			position = Integer.valueOf(index.trim());
		}
		
		do{
			if(cflag == 1){
				//Do Nothing
			}
			else{
				cflag = 0;
			}
			boolean flag = false;
			List<WebElement> listItems = getWebElementsList(itemListLocator);
			List<String> nlst = new ArrayList<String>();
			if(listItems == null || listItems.size() == 0){
				break;
			}
			check = 0;
			
			if(last.equalsIgnoreCase(listItems.get(listItems.size() - 1).getText().trim()) && first.equalsIgnoreCase(listItems.get(0).getText().trim())){
				if(driverType.trim().toUpperCase().contains("IOS")){
					break;
				}
				else{
					String collectionItemText;
					if(driverType.trim().toUpperCase().contains("IOS")){
						collectionItemText = listItems.get(listItems.size() - 1).getText();
					}
					else
						collectionItemText = listItems.get(listItems.size() - 1).getAttribute("text");
					if((collectionItemText.equalsIgnoreCase(list_header_name) || collectionItemText.equalsIgnoreCase(list_header_name.substring(0, list_header_name.length() - 1))) && cflag == 0){
						if(swipeDirection.trim().equalsIgnoreCase("DOWN") || swipeDirection.trim().equalsIgnoreCase("RIGHT")){
							currentDirection = swipeDirection.trim();
							swipeDirection = toggle(swipeDirection.trim());
						}
						cflag = 1;
						continue;
					}
					if(cflag == 1){
						if(collectionItemText.trim().equalsIgnoreCase(itemName.trim())){
							count++;
							if(count == position){
								check = 1;
								Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " - " + itemName.trim() + " is displayed", itemObjName.trim() + " - " + itemName.trim() + " should be displayed", itemObjName.trim() + " - " + itemName.trim() + " found", "Pass");
								item = getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size());
								break;
							}
							else{
								check = 0;
								if(collectionItemText.equalsIgnoreCase("See more " + list_header_name.trim().toLowerCase())){
									click(listItems.get(listItems.size() - 1), "See more " + list_header_name.trim().toLowerCase() + " link");
									seemore = 1;
									break;
								}
							}
						}
						else{
							check = 0;
							if(collectionItemText.equalsIgnoreCase("See more " + list_header_name.trim().toLowerCase())){
								click(listItems.get(listItems.size() - 1), "See more " + list_header_name.trim().toLowerCase() + " link");
								seemore = 1;
								break;
							}
						}
					}
					break;
				}
			}
			
			for(int i = 0; i < listItems.size(); i++){
				if(driverType.trim().toUpperCase().contains("IOS")){
					nlst.add(listItems.get(i).getText());
				}
				else
					nlst.add(listItems.get(i).getAttribute("text"));
				
				if(i == listItems.size() - 1 && !driverType.trim().toUpperCase().contains("IOS")){
					flag = true;
					check = 0;
					break;
				}
				
				String collectionItemText;
				if(driverType.trim().toUpperCase().contains("IOS")){
					collectionItemText = listItems.get(i).getText();
				}
				else
					collectionItemText = listItems.get(i).getAttribute("text");
				if((collectionItemText.equalsIgnoreCase(list_header_name) || collectionItemText.equalsIgnoreCase(list_header_name.substring(0, list_header_name.length() - 1))) && cflag == 0){
					if(swipeDirection.trim().equalsIgnoreCase("DOWN") || swipeDirection.trim().equalsIgnoreCase("RIGHT")){
						currentDirection = swipeDirection.trim();
						swipeDirection = toggle(swipeDirection.trim());
					}
					cflag = 1;
					continue;
				}
				if(cflag == 1){
					if(collectionItemText.trim().equalsIgnoreCase(itemName.trim())){
						count++;
						if(count == position){
							check = 1;
							Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " - " + itemName.trim() + " is displayed", itemObjName.trim() + " - " + itemName.trim() + " should be displayed", itemObjName.trim() + " - " + itemName.trim() + " found", "Pass");
							item = getParentElement(itemListLocator, className, "Item", parentlevel, i + 1);
							break;
						}
						else{
							check = 0;
							if(collectionItemText.equalsIgnoreCase("See more " + list_header_name.trim().toLowerCase())){
								if(driverType.trim().toUpperCase().contains("IOS")){
									while(!checkIfElementPresent(listItems.get(i), 1)){
										 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
									}
								}
								click(listItems.get(i), "See more " + list_header_name.trim().toLowerCase() + " link");
								seemore = 1;
								break;
							}
						}
					}
					else{
						check = 0;
						if(collectionItemText.equalsIgnoreCase("See more " + list_header_name.trim().toLowerCase())){
							if(driverType.trim().toUpperCase().contains("IOS")){
								while(!checkIfElementPresent(listItems.get(i), 1)){
									 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
								}
							}
							click(listItems.get(i), "See more " + list_header_name.trim().toLowerCase() + " link");
							seemore = 1;
							break;
						}
					}
				}
			}
			
			if(driverType.trim().toUpperCase().contains("IOS")){
				 if(check == 1){
					 while(!checkIfElementPresent(By.xpath(item), 1)){
						 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
					 }
					 break;
				 }
			 }
			
			if(check == 1 || seemore == 1)
				break;
			
			if(check == 0){
				last = listItems.get(listItems.size() - 1) == null ? listItems.get(listItems.size() - 2).getText().trim() : listItems.get(listItems.size() - 1).getText().trim();
				first = listItems.get(0).getText().trim();
				if(driverType.trim().toUpperCase().contains("IOS")){
					String lastElement = getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size());
					while(!checkIfElementPresent(By.xpath(lastElement), 1)){
						 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
					}
				}
				else{
					if(flag)
						swipe(webElement, driver.findElement(By.xpath(getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size()))), swipeDirection);
					else
						swipe(webElement, swipeDirection);
				}
			}
		}while(check == 0);
		
		if(seemore == 1){
			Dictionary.put("SEE_MORE", "true");
		}
		else{
			Dictionary.put("SEE_MORE", "false");
			if(check == 0){
				Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " - " + itemName.trim() + " is displayed", itemObjName.trim() + " - " + itemName.trim() + " should be displayed", itemObjName.trim() + " - " + itemName.trim() + " not found", "Fail");
				return item;
			}
		}
		
		return item;
	}
	
	/**
	 * Get specified item from list of items on the screen by swiping up(if needed) based on category
	 * 
	 * @param itemListLocator
	 * @param index
	 * @param itemObjName
	 * @param parentlevel
	 * @param viewLocator
	 * @param swipeDirection
	 * @param category
	 * @return
	 */
	public String getItemByIndexBasedOnCategory(By itemListLocator, int index, String itemObjName, int parentlevel, By viewLocator, String swipeDirection, String category){
		String className;
		if(driverType.trim().toUpperCase().contains("ANDROID")){
			className = "android.widget.TextView";
		}
		else{
			className = "UIAStaticText";
		}
		
		WebElement webElement = getElementWhenVisible(viewLocator);
		String list_header_name = category.trim();
		String item = null, last = "", first = "";
		List<List<String>> oldlst = new ArrayList<List<String>>();
		List<List<String>> newlst = null;
		int cflag = 0;
		int check = 0, seemore = 0;
		int bflag = 0;
		int k = 0;
		
		do{
			if(bflag == 1){
				//Do Nothing
			}
			else{
				bflag = 0;
			}
			boolean flag = false;
			List<WebElement> listItems = getWebElementsList(itemListLocator);
			List<String> nlst = new ArrayList<String>();
			if(cflag == 1){
				oldlst.addAll(newlst);
			}
			newlst = new ArrayList<List<String>>();
			
			if(listItems == null || listItems.size() == 0){
				break;
			}
			check = 0;
			
			if(last.equalsIgnoreCase(listItems.get(listItems.size() - 1).getText().trim()) && first.equalsIgnoreCase(listItems.get(0).getText().trim())){
				if(driverType.trim().toUpperCase().contains("IOS")){
					break;
				}
				else{
					String collectionItemText;
					if(driverType.trim().toUpperCase().contains("IOS")){
						collectionItemText = listItems.get(listItems.size() - 1).getText();
					}
					else
						collectionItemText = listItems.get(listItems.size() - 1).getAttribute("text");
					if((collectionItemText.equalsIgnoreCase(list_header_name) || collectionItemText.equalsIgnoreCase(list_header_name.substring(0, list_header_name.length() - 1))) && bflag == 0){
						bflag = 1;
						continue;
					}
					if(bflag == 1){
						String parent = getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size());
						List<WebElement> childItems;
						if(parentlevel > 0){
							childItems = getChildWebElementsList(By.xpath(parent), By.xpath("//" + className), "Textview", className);
						}
						else{
							childItems = new ArrayList<WebElement>();
							childItems.add(driver.findElement(By.xpath(parent)));
						}
						if(childItems == null || childItems.size() == 0)
							break;
						List<String>childItemsText= new ArrayList<String>();
						for(WebElement we:childItems){
							if(driverType.trim().toUpperCase().contains("IOS")){
								childItemsText.add(we.getText().trim());
							}
							else
								childItemsText.add(we.getAttribute("text").trim());
						}
						
						newlst.add(childItemsText);
						
						if(driverType.trim().toUpperCase().contains("IOS")){
							collectionItemText = listItems.get(listItems.size() - 1).getText();
						}
						else
							collectionItemText = listItems.get(listItems.size() - 1).getAttribute("text");
						if(verifyContentinListofList(oldlst, childItemsText)){
							//Do Nothing
						}
						else{
							k++;
						}
						if(k == index){
							check = 1;
							Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " - " + collectionItemText.trim() + " is displayed", itemObjName.trim() + " - " + collectionItemText.trim() + " should be displayed", itemObjName.trim() + " - " + collectionItemText.trim() + " found", "Pass");
							item = getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size());
							break;
						}
						else{
							check = 0;
							if(collectionItemText.equalsIgnoreCase("See more " + list_header_name.trim().toLowerCase())){
								click(listItems.get(listItems.size() - 1), "See more " + list_header_name.trim().toLowerCase() + " link");
								seemore = 1;
								break;
							}
						}
					}
					break;
				}
			}
			
			for(int i = 0; i < listItems.size(); i++){
				if(driverType.trim().toUpperCase().contains("IOS")){
					nlst.add(listItems.get(i).getText());
				}
				else
					nlst.add(listItems.get(i).getAttribute("text"));
				
				if(i == listItems.size() - 1 && !driverType.trim().toUpperCase().contains("IOS")){
					flag = true;
					check = 0;
					break;
				}
				
				String collectionItemText;
				if(driverType.trim().toUpperCase().contains("IOS")){
					collectionItemText = listItems.get(i).getText();
				}
				else
					collectionItemText = listItems.get(i).getAttribute("text");
				if((collectionItemText.equalsIgnoreCase(list_header_name) || collectionItemText.equalsIgnoreCase(list_header_name.substring(0, list_header_name.length() - 1))) && bflag == 0){
					bflag = 1;
					continue;
				}
				if(bflag == 1){
					if(driverType.trim().toUpperCase().contains("IOS")){
						if((k + 1) == index){
							check = 1;
							Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " - " + collectionItemText.trim() + " is displayed", itemObjName.trim() + " - " + collectionItemText.trim() + " should be displayed", itemObjName.trim() + " - " + collectionItemText.trim() + " found", "Pass");
							item = getParentElement(itemListLocator, className, "Item", parentlevel, i + 1);
							break;
						}
						else{
							check = 0;
							if(collectionItemText.equalsIgnoreCase("See more " + list_header_name.trim().toLowerCase())){
								if(driverType.trim().toUpperCase().contains("IOS")){
									while(!checkIfElementPresent(listItems.get(i), 1)){
										 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
									}
								}
								click(listItems.get(i), "See more " + list_header_name.trim().toLowerCase() + " link");
								seemore = 1;
								break;
							}
							k++;
						}
					}
					else{
						String parent = getParentElement(itemListLocator, className, "Item", parentlevel, i + 1);
						List<WebElement> childItems;
						if(parentlevel > 0){
							childItems = getChildWebElementsList(By.xpath(parent), By.xpath("//" + className), "Textview", className);
						}
						else{
							childItems = new ArrayList<WebElement>();
							childItems.add(driver.findElement(By.xpath(parent)));
						}
						if(childItems == null || childItems.size() == 0)
							continue;
						List<String>childItemsText= new ArrayList<String>();
						for(WebElement we:childItems){
							if(driverType.trim().toUpperCase().contains("IOS")){
								childItemsText.add(we.getText().trim());
							}
							else
								childItemsText.add(we.getAttribute("text").trim());
						}
						
						newlst.add(childItemsText);
						
						if(driverType.trim().toUpperCase().contains("IOS")){
							collectionItemText = listItems.get(i).getText();
						}
						else
							collectionItemText = listItems.get(i).getAttribute("text");
						if(verifyContentinListofList(oldlst, childItemsText)){
							//Do Nothing
						}
						else{
							k++;
						}
						if(k == index){
							check = 1;
							Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " - " + collectionItemText.trim() + " is displayed", itemObjName.trim() + " - " + collectionItemText.trim() + " should be displayed", itemObjName.trim() + " - " + collectionItemText.trim() + " found", "Pass");
							item = getParentElement(itemListLocator, className, "Item", parentlevel, i + 1);
							break;
						}
						else{
							check = 0;
							if(collectionItemText.equalsIgnoreCase("See more " + list_header_name.trim().toLowerCase())){
								click(listItems.get(i), "See more " + list_header_name.trim().toLowerCase() + " link");
								seemore = 1;
								break;
							}
						}
					}
				}
			}
			
			if(seemore == 1)
				break;
			
			if(driverType.trim().toUpperCase().contains("IOS")){
				 if(check == 1){
					 while(!checkIfElementPresent(By.xpath(item), 1)){
						 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
					 }
					 break;
				 }
			 }
			
			if(check == 0){
				if(bflag == 1){
					if(cflag == 0)
						cflag = 1;
				}
				last = listItems.get(listItems.size() - 1) == null ? listItems.get(listItems.size() - 2).getText().trim() : listItems.get(listItems.size() - 1).getText().trim();
				first = listItems.get(0).getText().trim();
				if(driverType.trim().toUpperCase().contains("IOS")){
					String lastElement = getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size());
					while(!checkIfElementPresent(By.xpath(lastElement), 1)){
						 swipe(webElement, driver.findElement(By.className("UIATabBar")), swipeDirection);
					}
				}
				else{
					if(flag)
						swipe(webElement, driver.findElement(By.xpath(getParentElement(itemListLocator, className, "Item", parentlevel, listItems.size()))), swipeDirection);
					else
						swipe(webElement, swipeDirection);
				}
			}
		}while(check == 0);
		
		if(seemore == 1){
			Dictionary.put("SEE_MORE", "true");
		}
		else{
			Dictionary.put("SEE_MORE", "false");
			if(check == 0){
				Reporter.log("Verify " + itemObjName.trim().toLowerCase() + " at index - " + index + " is displayed", itemObjName.trim() + " at index - " + index + " should be displayed", itemObjName.trim() + " at index - " + index + " not found", "Fail");
				return item;
			}
		}
		
		return item;
	}
	
	/**
	 * Swipe tile by tile
	 * 
	 * @param SwipeDirection
	 */
	public void swipeTile(String SwipeDirection) {

		Dimension size = driver.manage().window().getSize();

		if (SwipeDirection.trim().equalsIgnoreCase("Up")) {
			appium.swipe((int) (size.width * 0.8), (int) (size.height * 0.8), (int) (size.width * 0.8), (int) (size.height * 0.6), 4000);
		}
		else if (SwipeDirection.trim().equalsIgnoreCase("Down")) {
			appium.swipe((int) (size.width * 0.8), (int) (size.height * 0.6), (int) (size.width * 0.8), (int) (size.height * 0.8), 4000);
		}
		else if (SwipeDirection.trim().equalsIgnoreCase("Right")) {
			appium.swipe((int) (size.width * 0.8), (int) (size.height * 0.8), (int) (size.width * 0.6), (int) (size.height * 0.8), 4000);
		}
		else if (SwipeDirection.trim().equalsIgnoreCase("Left")) {
			appium.swipe((int) (size.width * 0.6), (int) (size.height * 0.8), (int) (size.width * 0.8), (int) (size.height * 0.8), 4000);
		}
		else {
			log.info("Not a valid direction passed");
		}
	}
	
	public void setFUPCount(String uid, String value) throws Exception{
		RedisManager redisManager;
		redisManager = new RedisManager(new RedisConfig(Environment.get("REDIS_HOSTNAME"), Integer.valueOf(Environment.get("REDIS_PORT"))));
		try{
			String json = redisManager.get("uid:" + uid);
			if(json != null && !json.trim().equalsIgnoreCase("")){
				JSONObject jsonObject = new JSONObject(json);
				if(jsonObject.has("packs")){
					JSONObject packs = jsonObject.getJSONObject("packs");
					if(packs.has("FUPPack")){
						JSONObject FUPPack = packs.getJSONObject("FUPPack");
						FUPPack.put("streamedCount", value);
						redisManager.set("uid:" + uid, jsonObject.toString());
					}
				}
			}
			DBActivities objDB = new DBActivities(driverType, Dictionary, Environment);
			MongoCollection users = objDB.GetMongoCollection("USER", "users");
			users.update("{uid : \"" + uid + "\"}").with("{$set:{\"packs.FUPPack.streamedCount\" :" + value + "}}");
		}
		catch(Exception ex){
			//Do Nothing
		}
		finally{
			if(redisManager != null)
				redisManager.quit();
		}
		log.info("FUP count resetted for uid : " + uid);
	}
	
	public void scrollingToElementofAPage(By locator) {
		WebElement webElement = getElementWhenVisible(locator);		
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", webElement);
	}
	
	public void scrollingByCoordinatesofAPage(int x, int y) {
		((JavascriptExecutor) driver).executeScript("window.scrollBy(" + x + "," + y + ")");
	}
	
	public void scrollingToBottomofAPage() {
		 ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
	}
	
	public void uninstallIOSAppUsingCLI(String bundleId, String udid){
		String ideviceinstaller = "";
		ideviceinstaller = runCommand(OSValidator.shellType, new String[]{"-l", "-c", "which ideviceinstaller"}, new boolean[]{false, false, false}, 2000);
  
		if(ideviceinstaller == null){
			return;
		}
		else if(ideviceinstaller.trim().equalsIgnoreCase("")){
			ideviceinstaller = "/usr/local/bin/ideviceinstaller";
		}
  
		ideviceinstaller = ideviceinstaller.split("\n")[0].replace("//", "/");
  
		CommandLine command = new CommandLine(ideviceinstaller);
		if(!udid.trim().equalsIgnoreCase("")){
			command.addArgument("-u", false);
			command.addArgument(udid, false);
		}
		command.addArgument("-U", false);
		command.addArgument(bundleId, false);
  
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		PumpStreamHandler psh = new PumpStreamHandler(stdout);
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
		DefaultExecutor executor = new DefaultExecutor();
		executor.setStreamHandler(psh);
		try {
			executor.execute(command, resultHandler);
			long wait = 20000L;
			while(!stdout.toString().trim().contains("Complete") && wait > 0){
				sync(1L);
				wait--;
			}
		} catch (IOException e1) {
			log.info("Threw a Exception in BaseUtil::uninstallIOSAppUsingCLI, full stack trace follows:", e1);
		}
	}
	
	@SuppressWarnings("deprecation")
	public int getIOSDeviceDateTime(String udid){
		String idevicedate = "";
		idevicedate = runCommand(OSValidator.shellType, new String[]{"-l", "-c", "which idevicedate"}, new boolean[]{false, false, false}, 2000);
  
		if(idevicedate == null){
			return -1;
		}
		else if(idevicedate.trim().equalsIgnoreCase("")){
			idevicedate = "/usr/local/bin/idevicedate";
		}
  
		idevicedate = idevicedate.split("\n")[0].replace("//", "/");
  
		CommandLine command = new CommandLine(idevicedate);
		if(!udid.trim().equalsIgnoreCase("")){
			command.addArgument("-u", false);
			command.addArgument(udid, false);
		}
  
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		PumpStreamHandler psh = new PumpStreamHandler(stdout);
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
		DefaultExecutor executor = new DefaultExecutor();
		executor.setStreamHandler(psh);
		try {
			executor.execute(command, resultHandler);
			long wait = 2000L;
			while(stdout.toString().trim().equalsIgnoreCase("") && wait > 0){
				sync(1L);
				wait--;
			}
		} catch (IOException e1) {
			log.info("Threw a Exception in BaseUtil::getIOSDeviceDateTime, full stack trace follows:", e1);
		}
		
		Date date = new Date(stdout.toString().trim());
		return date.getSeconds() * 1000;
	}
	
	public String getIOSDeviceDetails(String prop, String udid){
		String ideviceinfo = "";
		ideviceinfo = runCommand(OSValidator.shellType, new String[]{"-l", "-c", "which ideviceinfo"}, new boolean[]{false, false, false}, 2000);
  
		if(ideviceinfo == null){
			return null;
		}
		else if(ideviceinfo.trim().equalsIgnoreCase("")){
			ideviceinfo = "/usr/local/bin/ideviceinfo";
		}
  
		ideviceinfo = ideviceinfo.split("\n")[0].replace("//", "/");
  
		CommandLine command = new CommandLine(ideviceinfo);
		if(!udid.trim().equalsIgnoreCase("")){
			command.addArgument("-u", false);
			command.addArgument(udid, false);
		}
		command.addArgument("-k", false);
		command.addArgument(prop, false);
  
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		PumpStreamHandler psh = new PumpStreamHandler(stdout);
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
		DefaultExecutor executor = new DefaultExecutor();
		executor.setStreamHandler(psh);
		try {
			executor.execute(command, resultHandler);
			long wait = 2000L;
			while(stdout.toString().trim().equalsIgnoreCase("") && wait > 0){
				sync(1L);
				wait--;
			}
		} catch (IOException e1) {
			log.info("Threw a Exception in BaseUtil::getIOSDeviceDetails, full stack trace follows:", e1);
		}
		
		return stdout.toString().trim();
	}
	
	public String readImageText(ArrayList<String> imagesPath){
		BytePointer outText;

        TessBaseAPI api = new TessBaseAPI();
        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(null, "eng") != 0) {
            System.err.println("Could not initialize tesseract.");
            return null;
        }

        // Open input image with leptonica library
        System.out.println("Screenshot OCR output: \n" + imagesPath.get(0));
        PIX image = pixRead(imagesPath.get(0));
        api.SetImage(image);
        // Get OCR result
        outText = api.GetUTF8Text();
        String myText = outText.getString();

        // Destroy used object and release memory
        try {
			api.close();
			//api.End();
		} catch (Exception e) {
			e.printStackTrace();
		}
        outText.deallocate();
        pixDestroy(image);
        
        System.out.println(myText);
        
        return myText;
    }
	
	public String captureScreenshot(){
		String userdir = "";
		if(!Environment.get("user").trim().equalsIgnoreCase("")){
			userdir = System.getProperty("user.dir").replace(System.getProperty("user.name"), Environment.get("user").trim());
		}
		else
			userdir = System.getProperty("user.dir");
	
		if(Environment.get("screenshotsFolder").trim().equalsIgnoreCase("")){
			Environment.put("screenshotsFolder", "/resources/Screenshots/");
		}
		String screenshotPath = userdir + Environment.get("screenshotsFolder").trim();
		System.out.println("Capturing the snapshot of the page ");
		File srcFiler=((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		
		try {
			FileUtils.copyFile(srcFiler, new File(screenshotPath + srcFiler.getName()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String fileName = srcFiler.getName();
		srcFiler = null;
		return screenshotPath + fileName;
		
	}
	
	public String getIOSDeviceSysLogs(String searchString, String udid){
		String idevicesyslog = "";
		idevicesyslog = runCommand(OSValidator.shellType, new String[]{"-l", "-c", "which idevicesyslog"}, new boolean[]{false, false, false}, 2000);
  
		if(idevicesyslog == null){
			return null;
		}
		else if(idevicesyslog.trim().equalsIgnoreCase("")){
			idevicesyslog = "/usr/local/bin/idevicesyslog";
		}
  
		idevicesyslog = idevicesyslog.split("\n")[0].replace("//", "/");
  
		CommandLine command = new CommandLine(idevicesyslog);
		if(!udid.trim().equalsIgnoreCase("")){
			command.addArgument("-u", false);
			command.addArgument(udid, false);
		}
		
		CommandLine _command = new CommandLine(command);
		_command.addArgument("|", false);
		_command.addArgument("grep", false);
		_command.addArgument(searchString, false);
  
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		PumpStreamHandler psh = new PumpStreamHandler(stdout);
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
		DefaultExecutor executor = new DefaultExecutor();
		executor.setStreamHandler(psh);
		try {
			executor.execute(command, resultHandler);
			long wait = 3000L;
			sync(wait);
		} catch (IOException e1) {
			log.info("Threw a Exception in BaseUtil::getIOSDeviceSysLogs, full stack trace follows:", e1);
		}
		
		return stdout.toString().trim();
	}
	
	public void setAirplaneMode(boolean on) throws Exception{
		String manufacturer = Dictionary.get(driverType.trim().toUpperCase() + "_MANUFACTURER");
		By locater;
		if(manufacturer.trim().toUpperCase().equals("SAMSUNG")){
			launchApp("'com.android.settings", ".Settings'");
			locater = By.xpath(".//android.widget.TextView[@text='Airplane mode' or @text='Aeroplane mode' or @text='Flight mode']/../..//android.widget.CheckBox[1]");
		}
		else{
			launchApp("'com.android.settings", ".Settings\\$WirelessSettingsActivity'");
			locater = By.xpath(".//android.widget.TextView[@text='Airplane mode' or @text='Aeroplane mode' or @text='Flight mode']/../..//android.widget.LinearLayout/*[1]");
		}
		
		WebElement we = null;
		try{
			we = getElementWhenVisible(locater, 2);
		}
		catch(Exception ex){
			if(manufacturer.trim().toUpperCase().equals("SAMSUNG")){
				launchApp("'com.android.settings", ".Settings'");
				locater = By.xpath(".//android.widget.TextView[@text='Airplane mode' or @text='Aeroplane mode' or @text='Flight mode']/../..//android.widget.CheckBox[1]");
			}
			else{
				launchApp("'com.android.settings", ".Settings\\$WirelessSettingsActivity'");
				locater = By.xpath(".//android.widget.TextView[@text='Airplane mode' or @text='Aeroplane mode' or @text='Flight mode']/../..//android.widget.LinearLayout/*[1]");
			}
			we = getElementWhenVisible(locater, 2);
		}
		if(on && we.getAttribute("checked").trim().equalsIgnoreCase("false")){
			we.click();
			if(manufacturer.trim().toUpperCase().equals("SAMSUNG")){
				driver.findElement(By.name("Enable")).click();
			}
		} else if(!on && we.getAttribute("checked").trim().equalsIgnoreCase("true")){
			we.click();
			if(manufacturer.trim().toUpperCase().equals("SAMSUNG")){
				driver.findElement(By.name("Disable")).click();
			}
		}
		navigateBack();
	}
	
	public void setPreferredNetworkType(boolean _3g) throws Exception{
		String manufacturer = Dictionary.get(driverType.trim().toUpperCase() + "_MANUFACTURER");
		if(manufacturer.trim().toUpperCase().equals("ONEPLUS"))
			launchApp("'com.android.settings", ".Settings'");
		else
			launchApp("'com.android.settings", ".Settings\\$WirelessSettingsActivity'");
		switch(manufacturer.trim().toUpperCase()){
			case "ONEPLUS":
			case "LG":
			case "MOTOROLA":
			case "SAMSUNG":
				WebElement we = null;
				try{
					we = getElementWhenVisible(By.xpath(".//android.widget.TextView[@text = 'Mobile networks' or @text = 'Cellular networks']"), 2);
				}
				catch(Exception ex){
					if(manufacturer.trim().toUpperCase().equals("ONEPLUS"))
						launchApp("'com.android.settings", ".Settings'");
					else
						launchApp("'com.android.settings", ".Settings\\$WirelessSettingsActivity'");
					we = getElementWhenVisible(By.xpath(".//android.widget.TextView[@text = 'Mobile networks' or @text = 'Cellular networks']"), 2);
				}
				we.click();
				we = getElementWhenVisible(By.xpath(".//android.widget.TextView[@text='Preferred network type' or @text='Network mode']"), 2);
				we.click();
				List<WebElement> checkBox = getWebElementsList(By.className("android.widget.CheckedTextView"));
				if(_3g){
					checkBox.get(0).click();
				}else{
					checkBox.get(checkBox.size() - 1).click();
				}
				if(manufacturer.trim().toUpperCase().equals("SAMSUNG")){
					if(checkIfElementPresent(By.name("OK"), 2))
						driver.findElement(By.name("OK")).click();
				}
				break;
			default:
				log.info("Manufacturer not added in the list - " + manufacturer);
				navigateBack();
				return;
		}
		navigateBack();
		navigateBack();
		String deviceId = Environment.get("udid").trim();
		String output = "";
		int counter = 10;
		//Check if 3G/2G ON
		do{
			output = runADBCommand(new String[]{"-s", deviceId, "shell", "netcfg", "|", "grep", "rmnet0", "|", "grep", "-v", "rev"}, new Boolean[]{false, false, false, false, false, false, false, false, false, false, false}, 2000);
        	String[] arrOutput = output.split(" ");
        	ArrayList<String> finalOutput = new ArrayList<String>();
        	for(int i = 0 ; i < arrOutput.length; i++){
        		if(!arrOutput[i].trim().equalsIgnoreCase("")){
        			finalOutput.add(arrOutput[i].trim());
        		}
        	}
        	output = finalOutput.size() > 2 ? finalOutput.get(2).trim() : "";
        	counter--;
		}while(output.equalsIgnoreCase("0.0.0.0/0") && counter > 0);
	}
	
	/**
	 * Make call
	 * 
	 * @param number
	 * @throws Exception
	 */
	public void makeCall(String number) throws Exception{
		runADBCommand(new String[]{"-s", Environment.get("udid"), "shell", "am", "start", "-a", "android.intent.action.CALL", "-d", "tel://" + number.trim().replace("#", "%23")}, new Boolean[]{false, false, false, false, false, false, false, false, false}, 2000);
		sync(2000L);
		Reporter.log("Verify call happened", "Call should be initiated", "Call is initiated successfully", "Pass");
	}
	
	/**
	 * End call
	 *
	 */
	public void endCall(){
		sendKeyEvent(AndroidKeyCode.KEYCODE_ENDCALL);
	}
	
	public String getNetworkIPAddress(boolean wifi){
		String deviceId = Environment.get("udid").trim();
		String port = "wlan";
		if(!wifi)
			port = "rmnet";
		String adbPath = "";
		if(Environment.get("autoDetectADBPath").trim().equalsIgnoreCase("true") || Environment.get("autoDetectADBPath").trim().equalsIgnoreCase("y") || Environment.get("autoDetectADBPath").trim().equalsIgnoreCase("yes")){
			if(OSValidator.shellType.trim().equalsIgnoreCase("cmd")){
				adbPath = runCommand(OSValidator.shellType, new String[]{"/c", "echo %ANDROID_HOME%"}, new boolean[]{false, false}, 2000);
				if(adbPath != null && !adbPath.trim().equalsIgnoreCase("")){
					adbPath += OSValidator.delimiter + "tools" + OSValidator.delimiter + "adb"; 
				}
			}
			else{
				adbPath = runCommand(OSValidator.shellType, new String[]{"-l", "-c", "which adb"}, new boolean[]{false, false, false}, 2000);
			}
		}
  
		if(adbPath == null){
			return null;
		}
		else if(adbPath.trim().equalsIgnoreCase("")){
			if(!Environment.get("adbPath").trim().equalsIgnoreCase("")){
				adbPath = Environment.get("adbPath").trim();
			}
			else{
				log.info("ADB path not found");
				return null;
			}
		}
  
		adbPath = adbPath.split("\n")[0].replace("//", "/");
  
		CommandLine command = new CommandLine(adbPath);
		String[] arguments = new String[]{"-s", deviceId, "shell", "ip", "route", "|", "grep", "'" + port + ".*link.*src'"};
		for(int i = 0; i < arguments.length; i++){
			command.addArgument(arguments[i], false);
		}
		
		CommandLine _command = new CommandLine(command);
		_command.addArgument("|", false);
		_command.addArgument("awk", false);
		_command.addArgument("'{print $9}'", false);
  
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		PumpStreamHandler psh = new PumpStreamHandler(stdout);
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
		DefaultExecutor executor = new DefaultExecutor();
		executor.setStreamHandler(psh);
		try {
			executor.execute(command, resultHandler);
			long wait = 500L;
			sync(wait);
		} catch (IOException e1) {
			log.info("Threw a Exception in BaseUtil::getNetworkIPAddress, full stack trace follows:", e1);
		}
		
		return stdout.toString().trim().split("\n")[0];
	}
}