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

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.EncounterService;
import org.openmrs.module.mksreports.MKSReportsConstants;
import org.openmrs.module.mksreports.reports.PatientHistoryPdfReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MKSReportsManageController {
	
	private PatientHistoryPdfReport pdfReport;
	
	private EncounterService es;
	
	@Autowired
	public MKSReportsManageController(@Qualifier("encounterService") EncounterService es,
	    PatientHistoryPdfReport pdfReport) {
		this.es = es;
		this.pdfReport = pdfReport;
	}
	
	/**
	 * Receives requests to run a patient summary.
	 * 
	 * @param patientId the id of patient whose summary you wish to view
	 * @param encounterUuids the UUID(s) of the encounters you wish to view, CSV format is supported for
	 *            multiple encounters
	 * @param target if used as an href URL, the intended target of the <a> (e.g. _self, _blank)
	 * @throws Exception
	 */
	@RequestMapping(value = MKSReportsConstants.CONTROLLER_PATIENTHISTORY_ROUTE)
	public void renderPatientHistory(ModelMap model, HttpServletRequest request, HttpServletResponse response,
	        @RequestParam(value = "patientId", required = false) Integer patientId,
	        @RequestParam(value = "encounterUuid", required = false) String encounterUuids,
	        @RequestParam(value = "target", required = false) String target) {
		
		Set<Integer> encounterIds = StringUtils.isBlank(encounterUuids) ? Collections.emptySet()
		        : Arrays.asList(StringUtils.split(encounterUuids, ",")).stream()
		                .map(uuid -> es.getEncounterByUuid(uuid.trim()).getId()).distinct().collect(Collectors.toSet());
		
		response.setContentType("application/pdf");
		// set the file as an attachment with the suggested filename if the GET params
		// did not indicate it's being opened in another tab
		if (StringUtils.isBlank(target)) {
			response.addHeader("Content-Disposition", "attachment;filename=patientHistory.pdf");
		}
		
		byte[] pdfBytes;
		try {
			pdfBytes = pdfReport.getBytes(patientId, encounterIds);
			response.setContentLength(pdfBytes.length);
			response.getOutputStream().write(pdfBytes);
			response.getOutputStream().flush();
		}
		catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
