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

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientsummary.PatientSummaryResult;
import org.openmrs.module.patientsummary.PatientSummaryTemplate;
import org.openmrs.module.patientsummary.api.PatientSummaryService;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The main controller.
 */
@Controller
public class  MKSReportsReportsManageController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(value = "/module/mksreports/manage", method = RequestMethod.GET)
	public void manage(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}
	
	/**
	 * Receives requests to run a patient summary.
	 * @param patientId the id of patient whose summary you wish to view
	 * @param summaryId the id of the patientsummary you wish to view
	 */
	@RequestMapping(value = "/module/mksreports/renderSummary")
	public void renderSummary(ModelMap model, HttpServletRequest request, HttpServletResponse response,
							  @RequestParam("patientId") Integer patientId,
							  @RequestParam(value="download",required=false) boolean download,
							  @RequestParam(value="print",required=false) boolean print) throws IOException {		
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
			} 
			else {
				response.setContentType("text/xml");			      
				response.addHeader("Content-Disposition","attachment; filename=PatientHistory.xml" );
				response.getOutputStream().write(result.getRawContents());
			}
		}
		catch (Exception e) {
			e.printStackTrace(response.getWriter());
		}
	}
}