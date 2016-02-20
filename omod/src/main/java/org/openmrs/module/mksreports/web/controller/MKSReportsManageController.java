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
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.openmrs.api.context.Context;
import org.openmrs.module.mksreports.patienthistory.PatientHistoryReportManager;
import org.openmrs.module.patientsummary.PatientSummaryResult;
import org.openmrs.module.patientsummary.PatientSummaryTemplate;
import org.openmrs.module.patientsummary.api.PatientSummaryService;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The main controller.
 */
@Controller
public class MKSReportsManageController {
	
	@RequestMapping(value = "/module/mksreports/manage", method = RequestMethod.GET)
	public void manage(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}
	
	/**
	 * The path to the style sheet for Patient History reports.
	 */
	public static final String PATIENT_HISTORY_XSL_PATH = "patientHistoryFopStylesheet.xsl";
	
	/**
	 * Receives requests to run a patient summary.
	 * 
	 * @param patientId the id of patient whose summary you wish to view
	 * @param summaryId the id of the patientsummary you wish to view
	 */
	@RequestMapping(value = "/module/mksreports/renderSummary")
	public void renderSummary(ModelMap model, HttpServletRequest request, HttpServletResponse response,
	                          @RequestParam("patientId") Integer patientId,
	                          @RequestParam(value = "download", required = false) boolean download,
	                          @RequestParam(value = "print", required = false) boolean print) throws IOException {
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
		
		try {
			
			PatientSummaryService patientSummaryService = Context.getService(PatientSummaryService.class);
			ReportService reportService = Context.getService(ReportService.class);

			ReportDesign reportDesign = null;
			for (ReportDesign rd : reportService.getAllReportDesigns(false)) {
				if (rd.getName().equals(PatientHistoryReportManager.REPORT_DESIGN_NAME)) {
					reportDesign = rd;
				}
			}
			
			PatientSummaryTemplate patientSummaryTemplate = patientSummaryService.getPatientSummaryTemplate(reportDesign.getId());
			
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("patientSummaryMode", print ? "print" : "download");
			PatientSummaryResult result = patientSummaryService.evaluatePatientSummaryTemplate(patientSummaryTemplate, patientId, parameters);
			if (result.getErrorDetails() != null) {
				result.getErrorDetails().printStackTrace(response.getWriter());
			}
			else
			{
				StreamSource xmlSourceStream = new StreamSource(new ByteArrayInputStream(result.getRawContents()));
				StreamSource xslTransformStream = new StreamSource(OpenmrsClassLoader.getInstance().getResourceAsStream(PATIENT_HISTORY_XSL_PATH));
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				
				writeToOutputStream(xmlSourceStream, xslTransformStream, outStream);
				
				byte[] pdfBytes = outStream.toByteArray();
				response.setContentLength(pdfBytes.length);
				response.setContentType("application/pdf");
				response.addHeader("Content-Disposition", "attachment;filename=patientHistory.pdf");
				response.getOutputStream().write(pdfBytes);
				response.getOutputStream().flush();
			}
		}
		catch (Exception e) {
			e.printStackTrace(response.getWriter());
		}
	}

	/**
	 * XML --> XSL --> output stream.
	 * This is the method processing the XML according to the style sheet.
	 * @param xmlSourceStream A {@link StreamSource} built on the input XML.
	 * @param xslTransformStream A {@link StreamSource} built on the XSL style sheet.
	 * @param outStream 
	 * @throws Exception
	 */
	protected void writeToOutputStream(StreamSource xmlSourceStream, StreamSource xslTransformStream, OutputStream outStream) throws Exception {
		
		// Step 1: Construct a FopFactory
		FopFactory fopFactory = FopFactory.newInstance();
		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
		
		// Step 2: Construct fop with desired output format
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, outStream);
		
		// Step 3: Setup JAXP using identity transformer
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(xslTransformStream); // identity transformer
        //transformer.setParameter("imgPath", imgFileName);
        
        // Resulting SAX events (the generated FO) must be piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        // Step 4: Start XSLT transformation and FOP processing
        transformer.transform(xmlSourceStream, res);
	}
	
}
