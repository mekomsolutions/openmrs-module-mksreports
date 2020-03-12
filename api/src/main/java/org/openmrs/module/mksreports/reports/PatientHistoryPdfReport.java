package org.openmrs.module.mksreports.reports;

import static org.openmrs.module.mksreports.reports.PatientHistoryReportManager.REPORT_DESIGN_UUID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.function.Function;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.openmrs.api.EncounterService;
import org.openmrs.module.mksreports.MKSReportsConstants;
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
public class PatientHistoryPdfReport {
	
	@Autowired
	@Qualifier("patientsummaryPatientSummaryService")
	private PatientSummaryService pss;
	
	@Autowired
	@Qualifier("reportingReportService")
	private ReportService rs;
	
	@Autowired
	@Qualifier("encounterService")
	private EncounterService es;
	
	private Integer verifyEncounterIdSetPatientId(Integer firstEncPatientId, Set<Integer> encounterIds) {
		
		// a mismatch between patientIds should never return info for
		// other patients' encounter
		// (THIS *SHOULD* ALREADY OCCUR IN OTHER CODE, BUT IS INCLUDED HERE FOR DUE
		// DILIGENCE)
		
		// map-reduce all encounters
		return encounterIds.stream().map(new Function<Integer, Integer>() {
			
			@Override
			public Integer apply(Integer encId) {
				return es.getEncounter(encId).getPatient().getId();
			}
			
		}).reduce(firstEncPatientId, (a, b) -> a == b ? a : null);
		
	}
	
	/**
	 * Create "application/pdf" byte array based on Reporting XML renderer and transform XSL via Apache
	 * FOP, one of patientId or encounterIds must not be null/empty, if both are supplied they must
	 * match, i.e. all encounters must be for the patient specified
	 * 
	 * @param patientId If creating a complete patient history, the Patient.getId() id of the patient
	 *            (may be null)
	 * @param encounterIds If creating a specific report with only the specified encounters, the set of
	 *            all encounter ids (i.e. Encounter.getId()), they must all be from the same patient
	 * @return byte [] of generated PDF bytes
	 * @throws Exception if neither patientId nor encounterIds were provided
	 */
	public byte[] getBytes(Integer patientId, Set<Integer> encounterIds) throws Exception {
		
		EncounterEvaluationContext context = new EncounterEvaluationContext();
		
		// don't create an empty set of base encounters if no encounterIds are provided
		if (encounterIds != null && !encounterIds.isEmpty()) {
			EncounterIdSet encIdSet = new EncounterIdSet(encounterIds);
			
			// patientId might be null if encounters were provided instead
			// retrieve the patientId of an encounter
			Integer firstEncPatientId = es.getEncounter(encIdSet.getMemberIds().toArray(new Integer[1])[0]).getPatient()
			        .getId();
			
			// verify all encounters are for the same patient
			firstEncPatientId = verifyEncounterIdSetPatientId(firstEncPatientId, encounterIds);
			
			// null is returned if there was mismatch in encounters
			if (firstEncPatientId == null) {
				throw new Exception("Encounters are from different patients");
				// if patient id was null, but the encounter ids matched, use that patient id
			} else if (patientId == null) {
				patientId = firstEncPatientId;
				// if the patient ids are mismatched throw an error
			} else if (patientId != firstEncPatientId) {
				throw new Exception("Encounters do not match patient");
			}
			
			context.addParameterValue("encounterIds", encIdSet);
			context.setBaseEncounters(encIdSet);
			
		} else if (patientId == null) {
			throw new Exception("Neither patientId nor encounterUuid(s) were provided to generate a report");
		}
		
		ReportDesign reportDesign = rs.getReportDesignByUuid(REPORT_DESIGN_UUID);
		PatientSummaryTemplate template = pss.getPatientSummaryTemplate(reportDesign.getId());
		PatientSummaryResult result = pss.evaluatePatientSummaryTemplate(template, patientId, context);
		
		StreamSource xmlSourceStream = new StreamSource(new ByteArrayInputStream(result.getRawContents()));
		StreamSource xslTransformStream = new StreamSource(
		        OpenmrsClassLoader.getInstance().getResourceAsStream(MKSReportsConstants.PATIENT_HISTORY_XSL_PATH));
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		writeToOutputStream(xmlSourceStream, xslTransformStream, outStream);
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
