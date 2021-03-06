/*
‰ * Copyright 2012 Rewardly Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.l2bq.logging.analysis.exporter.applog;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.RequestLogs;
import com.google.appengine.api.log.LogService.LogLevel;
import com.l2bq.logging.analysis.BigqueryFieldExporter;

/**
 * User defined log(aka App Log) will be processed with AppLogExporter
 * 
 * - App Log Format Exmaple
 * 
 * [l2bq-sample/v1].<stdout>: AppLog	{"data":{"time":1368673858791,"accountId":"t4","playerId":1111,"langType":1,"clientVer":0,"osType":0},"type":"login"}
 * 
 * @author Junki Kim(wishoping@gmail.com)
 * @author Wooseok Seo(wooseok.seo@gmail.com)
 * 
 * @date 2013. 4. 29.
 */
public class AppLogExporter implements BigqueryFieldExporter {
	
	/**
	 * App Log Line Index
	 */
	protected String LOG_DELIMETER = "\t";
	protected int logIndex = -1;
	protected String typeName = "";
	
	protected String [] fieldNames = null;
	protected String [] fieldTypes = null;
	protected List<String> logs = new ArrayList<String>();
	
	@Override
	public void processLog(RequestLogs log) {
		logIndex = -1;

		// Usually there are multiple app logs. 
		List<AppLogLine> logLines = log.getAppLogLines();
		if ( logLines == null || logLines.size() <= 0 )
			return;
		
		if ( logs.size() > 0 )
			logs.clear();
		
		for ( AppLogLine logLine : logLines )
		{
			String logMsg = logLine.getLogMessage();
			
			if ( logMsg == null || logMsg.isEmpty() )
				continue;
			if ( logLine.getLogLevel() != LogLevel.INFO )
				continue;

			String [] msgs = logLine.getLogMessage().split("^.*AppLog" + LOG_DELIMETER);
			
			// If Log Parsing result is not valid, pass log data 
			if ( msgs.length != 2 )
				continue;

			String msg = msgs[1];
			
			try
			{
				JSONObject data = new JSONObject( msg );
				if ( !data.has("type") )
					continue;
				if ( !data.getString("type").equals(typeName))
					continue;
				
			} catch (JSONException e)
			{
				continue;
			}
			
			logs.add( msg );
		}
	}

	@Override
	public String getFieldName(int i) {
		return fieldNames[i];
	}

	@Override
	public String getFieldType(int i) {
		return fieldTypes[i];
	}

	@Override
	public boolean isAppLogProcessed()
	{
		// App Log Processing Exporter. so return true 
		return true;
	}

	@Override
	public List<String> getAppLogLines()
	{
		return logs;
	}

	@Override
	public boolean nextAppLogLine()
	{
		if ( logIndex >= logs.size() - 1 )
			return false;
		
		logIndex++;
		
		return true;
	}
	
	/**
	 * All the Following methods are sample code to show how to process logs.
	 * You should implement it by yourself.
	 * 
	 * [ App Log Format Exmaple ]
	 * 
	 * [l2bq-sample/v1].<stdout>: AppLog	{"data":{"time":1368673858791,"accountId":"t4","playerId":1111,"langType":1,"clientVer":0,"osType":0},"type":"login"}
	 */
	protected Object getTime() {
		long time = 0;
		
		time = Long.parseLong( logs.get(logIndex).split(LOG_DELIMETER)[0] );
		
		return time;
	}
	
	protected Object getFieldLong(String fieldName) {
		long longValue = 0;
		String msg = logs.get(logIndex);
		try
		{
			JSONObject msgObj = new JSONObject(msg);
			if (!msgObj.has("data"))
				return null;
			longValue = msgObj.getJSONObject("data").getLong(fieldName);
		} catch (JSONException e)
		{
			return -1;
		}
		return longValue;
	}
	
	protected Object getFieldFloat(String fieldName) {
		double doubleValue = 0;
		String msg = logs.get(logIndex);
		try
		{
			JSONObject msgObj = new JSONObject(msg);
			if (!msgObj.has("data"))
				return null;
			doubleValue = msgObj.getJSONObject("data").getDouble(fieldName);
		} catch (JSONException e)
		{
			return -1;
		}
		return doubleValue;
		
	}
	
	protected Object getFieldString(String fieldName) {
		String stringValue = "";
		String msg = logs.get(logIndex);
		try
		{
			JSONObject msgObj = new JSONObject(msg);
			if (!msgObj.has("data"))
				return null;
			stringValue = msgObj.getJSONObject("data").getString(fieldName);
		} catch (JSONException e)
		{
			return null;
		}
		
		return stringValue;		
	}

	protected Object getFieldBoolean(String fieldName) {
		boolean boolValue = false;
		String msg = logs.get(logIndex);
		try
		{
			JSONObject msgObj = new JSONObject(msg);
			if (!msgObj.has("data"))
				return null;
			boolValue = msgObj.getJSONObject("data").getBoolean(fieldName);
		} catch (JSONException e)
		{
			return null;
		}
		
		return boolValue;
	}
	
	
	@Override
	public int getFieldCount() {
		return fieldNames.length;
	}

	@Override
	public Object getField(String name) {

		int index = 0;
		for(String fieldName:fieldNames) {
			if (fieldName.equals(name)) {
				if (fieldTypes[index].equals("integer")) {
					return getFieldLong(name);
				}
				else if (fieldTypes[index].equals("string")) {
					return getFieldString(name);
				}
				else if (fieldTypes[index].equals("float")) {
					return getFieldFloat(name);
				}
				else if (fieldTypes[index].equals("boolean")) {
					return getFieldBoolean(name);
				}
			}
			
			index++;
		}
		
		return null;
	}
	
}
