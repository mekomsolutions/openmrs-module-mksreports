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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.openmrs.api.context.Context;
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
public class MKSReportsReportsManageController {
	
	@RequestMapping(value = "/module/mksreports/manage", method = RequestMethod.GET)
	public void manage(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}
	
	public Transformer getTransformer(StreamSource streamSource) {
		// setup the xslt transformer
		net.sf.saxon.TransformerFactoryImpl impl = new net.sf.saxon.TransformerFactoryImpl();
		
		try {
			return impl.newTransformer(streamSource);
			
		}
		catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
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
		
		FopFactory fopFactory = FopFactory.newInstance();
		TransformerFactory tFactory = TransformerFactory.newInstance();
		
		try {
			PatientSummaryService pss = Context.getService(PatientSummaryService.class);
			
			ReportService rs = Context.getService(ReportService.class);
			ReportDesign psrd = null;
			for (ReportDesign rd : rs.getAllReportDesigns(false)) {
				if ("mekomPatientSummary.xml_".equals(rd.getName())) {
					psrd = rd;
				}
			}
			
			PatientSummaryTemplate ps = pss.getPatientSummaryTemplate(psrd.getId());
			
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("patientSummaryMode", print ? "print" : "download");
			PatientSummaryResult result = pss.evaluatePatientSummaryTemplate(ps, patientId, parameters);
			if (result.getErrorDetails() != null) {
				result.getErrorDetails().printStackTrace(response.getWriter());
			} else {

				// the XML file from which we take the name
				//StreamSource src = new StreamSource(new ByteArrayInputStream(result.getRawContents()));
				StreamSource source = new StreamSource(OpenmrsClassLoader.getInstance().getResourceAsStream("sample.xml"));
				// creation of transform source
				StreamSource transformSource = new StreamSource(OpenmrsClassLoader.getInstance().getResourceAsStream("sampleStylesheet.xsl"));
				// create an instance of fop factory
				// FopFactory fopFactory = FopFactory.newInstance();
				// a user agent is needed for transformation
				FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
				// to store output
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				
				Transformer xslfoTransformer;
				try {
					xslfoTransformer = getTransformer(transformSource);
					// Construct fop with desired output format
					Fop fop;
					try {
						fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, outStream);
						// Resulting SAX events (the generated FO)
						// must be piped through to FOP
						Result res = new SAXResult(fop.getDefaultHandler());
						
						// Start XSLT transformation and FOP processing
						try {
							// everything will happen here..
							xslfoTransformer.transform(source, res);
										
							// to write the content to out put stream
							byte[] pdfBytes = outStream.toByteArray();
							response.setContentLength(pdfBytes.length);
							response.setContentType("application/pdf");
							response.addHeader("Content-Disposition", "attachment;filename=patientHistory.pdf");
							response.getOutputStream().write(pdfBytes);
							response.getOutputStream().flush();
						}
						catch (TransformerException e) {
							throw e;
						}
					}
					catch (FOPException e) {
						throw e;
					}
				}
				catch (TransformerConfigurationException e) {
					throw e;
				}
				catch (TransformerFactoryConfigurationError e) {
					throw e;
				}
				
			}
		}
		catch (Exception e) {
			e.printStackTrace(response.getWriter());
		}
	}
}
