package org.openmrs.module.mksreports.web.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import javax.xml.transform.stream.StreamSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MKSReportsManageControllerTest {

	private static String SAMPLE_PDF_OUTPUT_PATH = "target/samplePatientHistory.pdf"; 
	
	private MKSReportsManageController ctrl;
	
	@Before
	public void setUp() {
		ctrl = new MKSReportsManageController();
		
		/* The below code deleting the output PDF should in fact even
			eventually moved in the tear down routine. */
		File file = new File(SAMPLE_PDF_OUTPUT_PATH);
		try {
			Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@After
	public void tearDown() {
	}
	
	@Test
	public void shouldProducePdf() throws Exception {
		
		StreamSource xmlSourceStream = new StreamSource(getClass().getClassLoader().getResourceAsStream("patientHistorySample.xml"));
		StreamSource xslTransformStream = new StreamSource(getClass().getClassLoader().getResourceAsStream(MKSReportsManageController.PATIENT_HISTORY_XSL_PATH));
		
		FileOutputStream outStream = new FileOutputStream(new File(SAMPLE_PDF_OUTPUT_PATH));
		ctrl.writeToOutputStream(xmlSourceStream, xslTransformStream, outStream);
		outStream.close();
		
		//TODO Actually check somehow that the PDF is ok.
	}
}
