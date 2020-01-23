package org.openmrs.module.mksreports.web.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.hamcrest.text.StringContainsInOrder;
import org.hibernate.cfg.Environment;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.mksreports.MKSReportManager;
import org.openmrs.module.mksreports.MKSReportsConstants;
import org.openmrs.module.patientsummary.api.PatientSummaryService;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

public class MKSReportsManageControllerTest extends BaseModuleWebContextSensitiveTest {
	
	@Autowired
	private MKSReportsManageController ctrl;
	
	@Autowired
	VisitService visitService;
	
	@Autowired
	ReportService reportService;
	
	@Autowired
	PatientSummaryService patientSummaryService;
	
	@Autowired
	MessageSourceService messageSourceService;
	
	@Autowired
	@Qualifier(MKSReportsConstants.COMPONENT_REPORTMANAGER_PATIENTHISTORY)
	private MKSReportManager reportManager;
	
	@Override
	public Properties getRuntimeProperties() {
		Properties props = super.getRuntimeProperties();
		
		String dbUrl = props.getProperty(Environment.URL);
		
		dbUrl += ";DB_CLOSE_ON_EXIT=FALSE";
		
		props.setProperty(Environment.URL, dbUrl);
		
		return props;
	}
	
	@Before
	public void setup() throws Exception {
		executeDataSet("testDataset.xml");
		ReportManagerUtil.setupReport(this.reportManager);
	}
	
	@Test
	public void renderPatientHistory_shouldProducePDFWithEncounterTranslations_en() throws IOException {
		// setup
		ModelMap model = new ModelMap();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		Context.setLocale(Locale.ENGLISH);
		
		Integer patientId = 100000;
		
		// replay
		ctrl.renderPatientHistory(model, request, response, patientId, null);
		
		// verify // insure unknown patients with minimal info do not cause any NPEs
		
		byte[] pdfData = response.getContentAsByteArray();
		
		assertNotNull(pdfData);
		
		PdfReader reader = new PdfReader(pdfData);
		PdfTextExtractor extractor = new PdfTextExtractor(reader, true);
		
		String allText = "";
		
		for (Integer pageNum = 1; pageNum < reader.getNumberOfPages() + 1; pageNum++) {
			allText += extractor.getTextFromPage(pageNum) + "\n\r";
		}
		
		List<String> encounterValues = Arrays.asList("Encounter", "Type", "Name:", "English", "Translation");
		List<String> visitValues = Arrays.asList("Visit", "Type", "Name:", "English", "Translation");
		
		assertThat(allText, StringContainsInOrder.stringContainsInOrder(encounterValues));
		assertThat(allText, StringContainsInOrder.stringContainsInOrder(visitValues));
		
		reader.close();
	}
	
	@Test
	public void renderPatientHistory_shouldProducePDFWithEncounterTranslations_es() throws IOException {
		// setup
		ModelMap model = new ModelMap();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		Context.setLocale(new Locale("es", "ES"));
		
		Integer patientId = 100000;
		
		// replay
		ctrl.renderPatientHistory(model, request, response, patientId, null);
		
		// verify // insure unknown patients with minimal info do not cause any NPEs
		
		byte[] pdfData = response.getContentAsByteArray();
		
		assertNotNull(pdfData);
		
		PdfReader reader = new PdfReader(pdfData);
		PdfTextExtractor extractor = new PdfTextExtractor(reader, true);
		
		String allText = "";
		
		for (Integer pageNum = 1; pageNum < reader.getNumberOfPages() + 1; pageNum++) {
			allText += extractor.getTextFromPage(pageNum) + "\n\r";
		}
		
		List<String> encValues = Arrays.asList("Nombre", "del", "tipo", "de", "encuentro:", "traducción", "al", "español");
		List<String> visitValues = Arrays.asList("Nombre", "del", "tipo", "de", "visita:", "Traducción", "al", "español");
		
		assertThat(allText, StringContainsInOrder.stringContainsInOrder(encValues));
		assertThat(allText, StringContainsInOrder.stringContainsInOrder(visitValues));
		
		reader.close();
	}
}
