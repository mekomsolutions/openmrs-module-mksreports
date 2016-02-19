/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.mksreports.renderer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.renderer.RenderingException;
import org.openmrs.module.reporting.report.renderer.ReportDesignRenderer;
import org.openmrs.report.ReportRenderer;

/**
 * ReportRenderer that renders to a default XML format
 */
@Handler
@Localized("reporting.XmlReportRenderer")
public class PatientHistoryXmlReportRenderer extends ReportDesignRenderer {
	
	/**
	 * @see ReportRenderer#getFilename(org.openmrs.module.reporting.report.ReportRequest)
	 */
	@Override
	public String getFilename(ReportRequest request) {
		return getFilenameBase(request) + ".xml";
	}
	
	/**
	 * @see ReportRenderer#getRenderedContentType(org.openmrs.module.reporting.report.ReportRequest)
	 */
	public String getRenderedContentType(ReportRequest request) {
		return "text/xml";
	}
	
	/**
	 * @see ReportRenderer#render(ReportData, String, OutputStream)
	 */
	public void render(ReportData results, String argument, OutputStream out) throws IOException, RenderingException {
				
		Writer w = new OutputStreamWriter(out, "UTF-8");
		
		w.write("<?xml version=\"1.0\"?>\n");
		w.write("<report name=\"" + results.getDefinition().getName() + "\">\n");
		for (String dsKey : results.getDataSets().keySet()) {
			DataSet dataset = results.getDataSets().get(dsKey);
			List<DataSetColumn> columns = dataset.getMetaData().getColumns();
			w.write("<dataset name=\"" + dsKey + "\">\n");
			w.write("\t<rows>\n");
			for (DataSetRow row : dataset) {
				w.write("\t\t<row>");
				for (DataSetColumn column : columns) {
					Object colValue = row.getColumnValue(column);
					String label = toCamelCase(column.getLabel());
					if (dsKey.equalsIgnoreCase("encounters")) {
						w.write("<obs key=\"" + label + "\" name=\"" + column.getLabel().replaceAll("_", " ") + "\">");
					} else {
						w.write("<" + label + ">");
					}
					if (colValue != null) {
						if (colValue instanceof Cohort) {
							w.write(Integer.toString(((Cohort) colValue).size()));
						} else {
							w.write(colValue.toString());
						}
					}
					if (dsKey.equalsIgnoreCase("encounters")) {
						w.write("</obs>");
					} else {
						w.write("</" + label + ">");
					}
				}
				w.write("</row>\n");
			}
			w.write("\t</rows>\n");
			w.write("</dataset>\n");
		}
		w.write("</report>\n");
		w.flush();
	}
	
	/**
	 * @param s the string to conver to camelcase
	 * @return should return the passed in string in the camelcase format
	 */
	public static String toCamelCase(String s) {
		StringBuffer sb = new StringBuffer();
		String[] words = s.replaceAll("[^A-Za-z]", " ").replaceAll("\\s+", " ").trim().split(" ");
		
		for (int i = 0; i < words.length; i++) {
			if (i == 0)
				words[i] = words[i].toLowerCase();
			else
				words[i] = String.valueOf(words[i].charAt(0)).toUpperCase() + words[i].substring(1);
			
			sb.append(words[i]);
		}
		return sb.toString();
		
	}
}
