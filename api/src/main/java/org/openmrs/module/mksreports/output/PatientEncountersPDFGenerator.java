package org.openmrs.module.mksreports.output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PatientEncountersPDFGenerator {
	
	@Autowired
	@Qualifier("patientSummaryService")
	private PatientSummaryService patientSummaryService;// = Context.getService(PatientSummaryService.class);
	
	@Autowired
	@Qualifier("reportService")
	private ReportService reportService;// = Context.getService(ReportService.class);
	
	@Autowired
	@Qualifier("encounterService")
	private EncounterService encounterService;
	
	public byte[] generatePatientEncountersPdfBytes(Integer patientId, String encounterUuidParam, PrintWriter errorWriter) {
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		
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
		
		if (patientSummaryResult.getErrorDetails() != null && errorWriter != null) {
			
			patientSummaryResult.getErrorDetails().printStackTrace(errorWriter);
		} else {
			StreamSource xmlSourceStream = new StreamSource(new ByteArrayInputStream(patientSummaryResult.getRawContents()));
			StreamSource xslTransformStream = new StreamSource(
			        OpenmrsClassLoader.getInstance().getResourceAsStream(MKSReportsConstants.PATIENT_HISTORY_XSL_PATH));
			
			try {
				writeToOutputStream(xmlSourceStream, xslTransformStream, outStream);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return outStream.toByteArray();
		
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
