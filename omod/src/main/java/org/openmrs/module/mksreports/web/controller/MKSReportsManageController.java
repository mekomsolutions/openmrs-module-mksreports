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
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.mksreports.MKSReportsConstants;
import org.openmrs.module.mksreports.output.PatientEncountersPDFGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The main controller.
 */
@Controller
public class MKSReportsManageController {
	
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
		
		PatientEncountersPDFGenerator pdfGen = new PatientEncountersPDFGenerator();
		
		PrintWriter errorWriter = null;
		
		try {
			errorWriter = response.getWriter();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		
		byte[] pdfBytes = pdfGen.generatePatientEncountersPdfBytes(patientId, encounterUuidParam, errorWriter);
		
		response.setContentLength(pdfBytes.length);
		response.setContentType("application/pdf");
		
		// set the file as an attachment with the suggested filename if the GET params
		// did not indicate it's being opened in another tab
		if (StringUtils.isBlank(target)) {
			response.addHeader("Content-Disposition", "attachment;filename=patientHistory.pdf");
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
