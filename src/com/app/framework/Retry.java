package com.app.framework;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.app.framework.Driver.HashMapNew;

public class Retry implements IRetryAnalyzer {
    private int retryCount = 0;
    HashMapNew env = new Driver().getEnvValues();
    private int maxRetryCount = env.containsKey("retryCount") ? Integer.valueOf(env.get("retryCount").trim()) : 0;

    public boolean retry(ITestResult result) {
    	String className = result.getClass().getName();
    	String methodName = result.getName();
    	if(env.get("retryNames").trim().contains(methodName) || env.get("retryNames").trim().contains(className) || env.get("retryNames").trim().equalsIgnoreCase("") || env.get("retryNames").trim().equalsIgnoreCase("all")){
	        if (retryCount < maxRetryCount) {
	            System.out.println("Retrying test " + result.getName() + " with status " + getResultStatusName(result.getStatus()) + " for the " + (retryCount+1) + " time(s).");
	            retryCount++;
	            return true;
	        }
    	}
        return false;
    }
    
    public String getResultStatusName(int status) {
    	String resultName = null;
    	if(status==1)
    		resultName = "SUCCESS";
    	if(status==2)
    		resultName = "FAILURE";
    	if(status==3)
    		resultName = "SKIP";
		return resultName;
    }
}
