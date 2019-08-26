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
package org.openmrs.module.mksreports.api.impl;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.mksreports.api.MKSReportsService;
import org.openmrs.module.mksreports.patienthistory.PatientSummaryResult;
import org.openmrs.module.mksreports.patienthistory.PatientSummaryTemplate;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.ReportRenderer;
import org.openmrs.module.reporting.report.service.ReportService;

/**
 * It is a default implementation of {@link MKSReportsService}.
 */
public class MKSReportsServiceImpl extends BaseOpenmrsService implements MKSReportsService {

	protected final Log log = LogFactory.getLog(this.getClass());

	@Override
	public PatientSummaryTemplate getPatientSummaryTemplate(Integer id) {
		ReportDesign d = Context.getService(ReportService.class).getReportDesign(id);
		if (d != null) {
			return new PatientSummaryTemplate(d);
		}
		return null;
	}

	@Override
	public PatientSummaryResult evaluatePatientSummaryTemplate(PatientSummaryTemplate patientSummaryTemplate,
			Integer patientId, Map<String, Object> parameters) {
		PatientSummaryResult result = new PatientSummaryResult(patientSummaryTemplate, patientId, parameters);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			// Populate a new EvaluationContext with the patient and parameters passed in
			EvaluationContext context = new EvaluationContext();
			Cohort baseCohort = new Cohort();
			baseCohort.addMember(patientId);
			context.setBaseCohort(baseCohort);
			if (parameters != null) {
				for (Map.Entry<String, Object> paramEntry : parameters.entrySet()) {
					context.addParameterValue(paramEntry.getKey(), paramEntry.getValue());
				}
			}

			// Evaluate the PatientSummary with this context to produce the data to use to
			// populate the summary
			ReportDefinitionService rds = Context.getService(ReportDefinitionService.class);
			ReportData data = rds.evaluate(patientSummaryTemplate.getReportDesign().getReportDefinition(), context);

			// Render the template with this data to produce the raw data result
			Class<? extends ReportRenderer> rendererType = patientSummaryTemplate.getReportDesign().getRendererType();
			ReportRenderer renderer = rendererType.newInstance();
			String rendererArg = patientSummaryTemplate.getReportDesign().getUuid();
			renderer.render(data, rendererArg, baos);

			// Return a PatientSummaryResult which contains the raw output and contextual
			// data
			result.setContentType(patientSummaryTemplate.getContentType());
			result.setRawContents(baos.toByteArray());
		} catch (Throwable t) {
			log.error("An error occurred trying to evaluate a patient summary template", t);
			result.setErrorDetails(t);
		} finally {
			IOUtils.closeQuietly(baos);
		}
		return result;
	}
}