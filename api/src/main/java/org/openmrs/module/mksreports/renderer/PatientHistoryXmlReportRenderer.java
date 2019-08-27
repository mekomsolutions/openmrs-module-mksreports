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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

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

import org.apache.commons.lang.StringUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.module.mksreports.patientsummary.patienthistory.PatientHistoryReportManager;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.renderer.RenderingException;
import org.openmrs.module.reporting.report.renderer.ReportDesignRenderer;
import org.openmrs.module.reporting.report.renderer.ReportRenderer;
import org.openmrs.module.reporting.serializer.ReportingSerializer;
import org.openmrs.serialization.SerializationException;
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

	protected String getStringValue(Object value) {
		String strVal = "";
		if (value != null)
			return strVal = value.toString();
		return strVal;
	}

	protected String getStringValue(DataSetRow row, String columnName) {
		Object value = row.getColumnValue(columnName);
		return getStringValue(value);
	}

	protected String getStringValue(DataSetRow row, DataSetColumn column) {
		return getStringValue(row, column.getName());
	}

	public void render(ReportData results, String argument, OutputStream out) throws IOException, RenderingException {
		// - - - - - - - - - - - - - - - - - - - - - - - -
		// TODO This should go eventually.
		// - - - - - - - - - - - - - - - - - - - - - - - -
		if (false == StringUtils.equals(argument, "in_tests")) {

			// Marhsalling using Xstream directly
			try {
				File xmlFile = File.createTempFile("sampleReportData_Xstream_", ".xml");
				BufferedWriter outWriter = new BufferedWriter(new FileWriter(xmlFile));
				XStream xstream = new XStream();
				xstream.toXML(results, outWriter);
			} catch (IOException e) {
				System.out.println("IOException Occured" + e.getMessage());
			}

			// Marhsalling using ReportingSerializer
			try {
				File xmlFile = File.createTempFile("sampleReportData_ReportingSerializer_", ".xml");
				ReportingSerializer serializer = new ReportingSerializer();
				serializer.serializeToStream(results, new FileOutputStream(xmlFile));
			} catch (SerializationException e) {
				System.out.println("SerializationException Occured" + e.getMessage());
			}
		}
		// - - - - - - - - - - - - - - - - - - - - - - - -
		//
		// - - - - - - - - - - - - - - - - - - - - - - - -

		final String ATTR_TYPE = "type";
		final String ATTR_LABEL = "label";
		final String ATTR_TIME = "time";
		final String ATTR_UUID = "uuid";
		final String ATTR_LOC = "location";
		final String ATTR_PROV = "provider";

		final String DATETIME_FORMAT = "dd MMM yyyy @ HH:mm";
		final String TIME_FORMAT = "HH:mm:ss";

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
		if (results.getDataSets().containsKey(dataSetKey)) {
			DataSet dataSet = results.getDataSets().get(dataSetKey);
			Element demographics = doc.createElement("demographics");
			rootElement.appendChild(demographics);

			for (DataSetRow row : dataSet) {
				for (DataSetColumn column : dataSet.getMetaData().getColumns()) {
					Element demographicData = doc.createElement("demographic");
					demographics.appendChild(demographicData);
					demographicData.setAttribute(ATTR_LABEL, column.getLabel());
					String strValue = getStringValue(row, column);
					demographicData.appendChild(doc.createTextNode(strValue));
				}
			}
		}

		dataSetKey = PatientHistoryReportManager.DATASET_KEY_ENCOUNTERS;
		if (results.getDataSets().containsKey(dataSetKey)) {
			DataSet dataSet = results.getDataSets().get(dataSetKey);

			for (DataSetRow row : dataSet) {
				String visitUuid = row.getColumnValue(PatientHistoryReportManager.VISIT_UUID_LABEL).toString();
				Element visit = doc.getElementById(visitUuid);
				if (visit == null) { // If the visit node doesn't exist, we create it.
					visit = doc.createElement("visit");
					visit.setAttribute(ATTR_UUID, visitUuid);
					visit.setIdAttribute(ATTR_UUID, true);
					rootElement.appendChild(visit);

					// TODO: Add the visit location and the visit type
					String visitType = getStringValue(row, PatientHistoryReportManager.VISIT_TYPE_LABEL);
					visit.setAttribute(ATTR_TYPE, visitType);
					String visitLocation = getStringValue(row, PatientHistoryReportManager.VISIT_LOCATION_LABEL);
					visit.setAttribute(ATTR_LOC, visitLocation);
				}

				// Adding the encounter.
				String encounterUuid = row.getColumnValue(PatientHistoryReportManager.ENCOUNTER_UUID_LABEL).toString();
				Element encounter = doc.createElement("encounter");
				encounter.setAttribute(ATTR_UUID, encounterUuid);
				encounter.setIdAttribute(ATTR_UUID, true);

				String encounterName = getStringValue(row, PatientHistoryReportManager.ENCOUNTERTYPE_NAME_LABEL);
				encounter.setAttribute(ATTR_LABEL, encounterName);

				Object value = row.getColumnValue(PatientHistoryReportManager.ENCOUNTER_DATETIME_LABEL);
				String encounterDatetime = (new SimpleDateFormat(DATETIME_FORMAT)).format(value);
				encounter.setAttribute(ATTR_TIME, encounterDatetime);

				String encounterProvider = getStringValue(row, PatientHistoryReportManager.ENCOUNTER_PROVIDER_LABEL);
				encounter.setAttribute(ATTR_PROV, encounterProvider);

				visit.appendChild(encounter);
			}
		}

		dataSetKey = PatientHistoryReportManager.DATASET_KEY_OBS;
		if (results.getDataSets().containsKey(dataSetKey)) {
			DataSet dataSet = results.getDataSets().get(dataSetKey);

			for (DataSetRow row : dataSet) {
				Element obs = doc.createElement("obs");

				String encounterUuid = row.getColumnValue(PatientHistoryReportManager.ENCOUNTER_UUID_LABEL).toString();
				Element encounter = doc.getElementById(encounterUuid);
				if (encounter == null) {
					// TODO: At least log this.
				}

				List<DataSetColumn> columns = dataSet.getMetaData().getColumns();
				for (DataSetColumn column : columns) {
					String colName = column.getName();
					Object value = row.getColumnValue(column);
					String strValue = getStringValue(value);
					if (StringUtils.equals(colName, PatientHistoryReportManager.ENCOUNTER_UUID_LABEL)) {
						continue;
					}
					if (StringUtils.equals(column.getName(), PatientHistoryReportManager.OBS_NAME_LABEL)) {
						obs.setAttribute(ATTR_LABEL, strValue);
						continue;
					}
					if (StringUtils.equals(column.getName(), PatientHistoryReportManager.OBS_DATATYPE_LABEL)) {
						obs.setAttribute(ATTR_TYPE, strValue);
						continue;
					}
					if (StringUtils.equals(column.getName(), PatientHistoryReportManager.OBS_DATETIME_LABEL)) {
						String obsDateTime = (new SimpleDateFormat(TIME_FORMAT)).format(value);
						obs.setAttribute(ATTR_TIME, obsDateTime);
						continue;
					}
					if (StringUtils.equals(column.getName(), PatientHistoryReportManager.OBS_PROVIDER_LABEL)) {
						obs.setAttribute(ATTR_PROV, strValue);
						continue;
					}
					if (StringUtils.equals(column.getName(), PatientHistoryReportManager.OBS_VALUE_LABEL)) {
						obs.appendChild(doc.createTextNode(strValue));
						continue;
					}
				}

				encounter.appendChild(obs);
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
