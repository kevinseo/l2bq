/*
 * Copyright 2012 Rewardly Inc.
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

package com.l2bq.logging.analysis;

import java.util.List;

import com.google.appengine.api.log.RequestLogs;
import com.google.apphosting.api.logservice.LogServicePb.RequestLog;

/**
 * A BigqueryFieldExporter converts a RequestLogs entry into a set of fields.
 * 
 * Initially {@link #getFieldCount() getFieldCount}, 
 * {@link #getFieldName(int) getFieldName}, 
 * and {@link #getFieldType(int)}
 * are used to create the schema.
 * Then, for each log entry, {@link #processLog(RequestLogs) processLog} is
 * called, followed by a {@link #getField(String) getField) call for each field.
 */
public interface BigqueryFieldExporter {
	
	/**
	 * Called for each RequestLogs to be processed. Should store necessary state
	 * to respond to later {@link #getField(String) getField} calls.
	 * 
	 * @param log entry to be processed
	 */
	public void processLog(RequestLogs log);
	
	/**
	 * Return the value of the given field for the last processed log.
	 * Any Object with a toString method that generates a BigQuery readable
	 * string is allowed.
	 * 
	 * The String is interned so that the reference can be compared directly.
	 * 
	 * @param name an intern'ed string returned from {@link #getFieldName(int) getFieldName}
	 * @return the field's value or null if the name is invalid
	 */
	public Object getField(String name);
	
	/**
	 * @return the number of fields generated by this exporter
	 */
	public int getFieldCount();
	
	/**
	 * Indexing must be consistent with {@link #getFieldType(int) getFieldType}.
	 * 
	 * @param i the field index
	 * @return the name of the i'th field
	 */
	public String getFieldName(int i);
	
	/**
	 * Indexing must be consistent with {@link #getFieldName(int) getFieldName}.
	 * 
	 * @param i the field index
	 * @return the BigQuery data type of the i'th field
	 */
	public String getFieldType(int i);
	
	/**
	 * Determine this processed {@link RequestLogs} is either a App Log or Request Header(or body) Log
	 * @return If this processed {@link RequestLogs} is a App Log, return true. Otherwise return false.
	 */
	public boolean isAppLogProcessed();
	
	/**
	 * Return processed log lines in {@link RequestLogs}
	 * @return List of Processed App Log Lines
	 */
	public List<String> getAppLogLines();
	
	/**
	 * Move iterator for app log lines list to next position
	 * @return If go to end line, return false
	 */
	public boolean nextAppLogLine();
	
}