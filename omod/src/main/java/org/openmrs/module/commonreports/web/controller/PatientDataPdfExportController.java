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
package org.openmrs.module.commonreports.web.controller;

import static org.openmrs.module.commonreports.CommonReportsConstants.PATIENTHISTORY_ID;
import static org.openmrs.module.commonreports.CommonReportsConstants.ROOT_URL;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.module.commonreports.reports.PatientHistoryPdfReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PatientDataPdfExportController {
	
	private final Log log = LogFactory.getLog(getClass());
	
	private PatientHistoryPdfReport pdfReport;
	
	private PatientService ps;
	
	private EncounterService es;
	
	@Autowired
	public PatientDataPdfExportController(@Qualifier("patientService") PatientService ps,
	    @Qualifier("encounterService") EncounterService es, PatientHistoryPdfReport pdfReport) {
		this.ps = ps;
		this.es = es;
		this.pdfReport = pdfReport;
	}
	
	private void writeReponse(Patient patient, Set<Encounter> encounters, String contentDisposition,
	        HttpServletResponse response) {
		response.setContentType("application/pdf");
		
		if (StringUtils.isBlank(contentDisposition)) {
			response.addHeader("Content-Disposition", "attachment;filename=patientHistory.pdf");
		} else {
			response.addHeader("Content-Disposition", contentDisposition);
		}
		
		try {
			byte[] pdfBytes = pdfReport.getBytes(patient, encounters);
			response.setContentLength(pdfBytes.length);
			response.getOutputStream().write(pdfBytes);
			response.getOutputStream().flush();
		}
		catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = ROOT_URL + "/" + PATIENTHISTORY_ID)
	public void getPatientHistory(ModelMap model, HttpServletRequest request, HttpServletResponse response,
	        @RequestParam(value = "patientUuid") String patientUuid,
	        @RequestParam(value = "contentDisposition", required = false) String contentDisposition) {
		
		Patient patient = ps.getPatientByUuid(patientUuid);
		
		writeReponse(patient, null, contentDisposition, response);
	}
	
	@RequestMapping(value = ROOT_URL + "/" + PATIENTHISTORY_ID + "/encounter")
	public void getSingleEncounter(ModelMap model, HttpServletRequest request, HttpServletResponse response,
	        @RequestParam(value = "encounterUuid") String encounterUuid,
	        @RequestParam(value = "contentDisposition", required = false) String contentDisposition) {
		
		Encounter encounter = es.getEncounterByUuid(encounterUuid);
		if (encounter == null) {
			log.error("'" + encounterUuid + "' does not point to a valid or existing encounter.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		Set<Encounter> encounters = new HashSet<Encounter>(Arrays.asList(encounter));
		
		writeReponse(null, encounters, contentDisposition, response);
	}
	
	@RequestMapping(value = ROOT_URL + "/" + PATIENTHISTORY_ID + "/encounters")
	public void getMultipleEncounters(ModelMap model, HttpServletRequest request, HttpServletResponse response,
	        @RequestParam(value = "encounterUuids") String encounterUuids,
	        @RequestParam(value = "contentDisposition", required = false) String contentDisposition) {
		
		Set<Encounter> encounters = Arrays.asList(StringUtils.split(encounterUuids, ",")).stream()
		        .map(uuid -> es.getEncounterByUuid(uuid.trim())).distinct().collect(Collectors.toSet());
		
		if (encounters.contains(null)) {
			log.error("Some UUIDs in '" + encounterUuids + "' do not point to valid or existing encounters.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		writeReponse(null, encounters, contentDisposition, response);
	}
}
