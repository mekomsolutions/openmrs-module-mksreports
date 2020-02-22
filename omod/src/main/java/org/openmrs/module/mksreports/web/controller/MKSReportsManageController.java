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
package org.openmrs.module.mksreports.web.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.module.mksreports.MKSReportsConstants;
import org.openmrs.module.mksreports.reports.PatientHistoryReportManager;
import org.openmrs.module.patientsummary.PatientSummaryResult;
import org.openmrs.module.patientsummary.PatientSummaryTemplate;
import org.openmrs.module.patientsummary.api.PatientSummaryService;
import org.openmrs.module.reporting.evaluation.context.EncounterEvaluationContext;
import org.openmrs.module.reporting.query.encounter.EncounterIdSet;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The main controller.
 */
@Controller
public class MKSReportsManageController {
	
	@Autowired
	private EncounterService encounterService;
	
	/**
	 * The path to the style sheet for Patient History reports.
	 */
	private static final String PATIENT_HISTORY_XSL_PATH = "patientHistoryFopStylesheet.xsl";
	
	@Autowired
	private PatientSummaryService patientSummaryService;
	
	@Autowired
	private ReportService reportService;
	
	/**
	 * Receives requests to run a patient summary.
	 * 
	 * @param patientId the id of patient whose summary you wish to view
	 * @param summaryId the id of the patientsummary you wish to view
	 * @param encounterId the id(s) of the encounters you wish to view, multiple encounters should be in
	 *            the form "<uuid>,<uuid>"
	 * @param target if used as an href URL, the intended target of the <a> (e.g. _self, _blank)
	 * @throws Exception
	 */
	@RequestMapping(value = MKSReportsConstants.CONTROLLER_PATIENTHISTORY_ROUTE)
	public void renderPatientHistory(ModelMap model, HttpServletRequest request, HttpServletResponse response,
	        @RequestParam("patientId") Integer patientId,
	        @RequestParam(value = "encounterUuid", required = false) String encounterUuidParam,
	        @RequestParam(value = "target", required = false) String target) {
		
		// User authenticatedUser = Context.getAuthenticatedUser();
		// String userRequestingReport = authenticatedUser.getDisplayString();
		
		ReportDesign reportDesign = null;
		for (ReportDesign rd : this.reportService.getAllReportDesigns(false)) {
			if (rd.getName().equals(PatientHistoryReportManager.REPORT_DESIGN_NAME)) {
				reportDesign = rd;
			}
		}
		
		PatientSummaryTemplate patientSummaryTemplate = this.patientSummaryService
		        .getPatientSummaryTemplate(reportDesign.getId());
		
		EncounterEvaluationContext context = new EncounterEvaluationContext();
		
		if (!StringUtils.isBlank(encounterUuidParam)) {
			
			// support csv style list of encounters
			List<String> encounterUuidList = Arrays.asList(encounterUuidParam.split(","));
			List<Integer> encounterIdList = new ArrayList<Integer>();
			
			for (String encounterUuid : encounterUuidList) {
				Encounter encounter = encounterService.getEncounterByUuid(encounterUuid.trim());
				encounterIdList.add(encounter.getEncounterId());
			}
			
			EncounterIdSet encIdSet = new EncounterIdSet(encounterIdList);
			
			context.addParameterValue("encounterIds", encIdSet);
			context.setBaseEncounters(encIdSet);
		}
		
		PatientSummaryResult patientSummaryResult = this.patientSummaryService
		        .evaluatePatientSummaryTemplate(patientSummaryTemplate, patientId, context);
		
		if (patientSummaryResult.getErrorDetails() != null) {
			try {
				patientSummaryResult.getErrorDetails().printStackTrace(response.getWriter());
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			
			StreamSource xmlSourceStream = new StreamSource(new ByteArrayInputStream(patientSummaryResult.getRawContents()));
			StreamSource xslTransformStream = new StreamSource(
			        OpenmrsClassLoader.getInstance().getResourceAsStream(PATIENT_HISTORY_XSL_PATH));
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			
			try {
				writeToOutputStream(xmlSourceStream, xslTransformStream, outStream);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			byte[] pdfBytes = outStream.toByteArray();
			response.setContentLength(pdfBytes.length);
			response.setContentType("application/pdf");
			
			// set the file as an attachment with the suggested filename if the GET params
			// did not indicate it's being opened in another tab
			if (StringUtils.isBlank(target)) {
				boolean useLetters = true;
				boolean useNumbers = true;
				
				String randomId = RandomStringUtils.random(10, useLetters, useNumbers);
				response.addHeader("Content-Disposition", "attachment;filename=" + randomId + ".pdf");
			}
			
			try {
				response.getOutputStream().write(pdfBytes);
				
				response.getOutputStream().flush();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * XML --> XSL --> output stream. This is the method processing the XML according to the style
	 * sheet.
	 * 
	 * @param xmlSourceStream A {@link StreamSource} built on the input XML.
	 * @param xslTransformStream A {@link StreamSource} built on the XSL style sheet.
	 * @param outStream
	 * @throws Exception
	 */
	protected void writeToOutputStream(StreamSource xmlSourceStream, StreamSource xslTransformStream, OutputStream outStream)
	        throws Exception {
		
		// Step 1: Construct a FopFactory
		FopFactory fopFactory = FopFactory.newInstance();
		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
		
		// Step 2: Construct fop with desired output format
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, outStream);
		
		// Step 3: Setup JAXP using identity transformer
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(xslTransformStream); // identity transformer
		// transformer.setParameter("imgPath", imgFileName);
		
		// Resulting SAX events (the generated FO) must be piped through to FOP
		Result res = new SAXResult(fop.getDefaultHandler());
		
		// Step 4: Start XSLT transformation and FOP processing
		transformer.transform(xmlSourceStream, res);
	}
	
}
