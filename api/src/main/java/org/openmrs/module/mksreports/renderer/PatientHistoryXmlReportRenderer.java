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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openmrs.annotation.Handler;
import org.openmrs.module.mksreports.patienthistory.PatientHistoryReportManager;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.renderer.RenderingException;
import org.openmrs.module.reporting.report.renderer.ReportDesignRenderer;
import org.openmrs.report.ReportRenderer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.thoughtworks.xstream.XStream;

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
	
	public void render(ReportData results, String argument, OutputStream out) throws IOException, RenderingException {
		
		BufferedWriter outWriter = null;
		try {
		  outWriter = new BufferedWriter(new FileWriter("/tmp/sampleReportData.xml"));
		  XStream xstream = new XStream();
		  xstream.toXML(results, outWriter);
		} catch (IOException e) {
		  System.out.println("IOException Occured" + e.getMessage());
		}
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RenderingException(e.getLocalizedMessage());
		}

		// Root element
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("patientHistory");
		doc.appendChild(rootElement);
		
		String dataSetKey = "";
		dataSetKey = PatientHistoryReportManager.DATASET_KEY_DEMOGRAPHICS;
		if(results.getDataSets().containsKey(dataSetKey)) {
			DataSet dataSet = results.getDataSets().get(dataSetKey);
			Element demographics = doc.createElement("demographics");
			rootElement.appendChild(demographics);
			
			for (DataSetRow row : dataSet) {
				for (DataSetColumn column : dataSet.getMetaData().getColumns()) {
					Object colValue = row.getColumnValue(column);
					if(colValue == null)
						continue;
					String strValue = colValue.toString();
					if(strValue.isEmpty())
						continue;
					
					Element demographicData = doc.createElement("demographic");
					demographics.appendChild(demographicData);
					demographicData.setAttribute("name", column.getLabel());
					demographicData.appendChild(doc.createTextNode(strValue));
				}
			}
		}
		
		dataSetKey = PatientHistoryReportManager.DATASET_KEY_ENCOUNTERS;
		if(results.getDataSets().containsKey(dataSetKey)) {
			DataSet dataSet = results.getDataSets().get(dataSetKey);
			Element encounters = doc.createElement("encounters");
			rootElement.appendChild(encounters);
			
			for (DataSetRow row : dataSet) {
				Element encounter = doc.createElement("encounter");
				encounters.appendChild(encounter);
				for (DataSetColumn column : dataSet.getMetaData().getColumns()) {
					Object colValue = row.getColumnValue(column);
					if(colValue == null)
						continue;
					String strValue = colValue.toString();
					if(strValue.isEmpty())
						continue;
					
					Element obs = doc.createElement("obs");
					encounter.appendChild(obs);
					obs.setAttribute("name", column.getLabel());
					obs.appendChild(doc.createTextNode(strValue));
				}
			}
		}
		
		// Write the content to the output stream
		Transformer transformer = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new RenderingException(e.getLocalizedMessage());
		} catch (TransformerFactoryConfigurationError e) {
			throw new RenderingException(e.getLocalizedMessage());
		}
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		try {
			transformer.transform(source, new StreamResult(out));
		} catch (TransformerException e) {
			throw new RenderingException(e.getLocalizedMessage());
		}
	}
}
