package org.openmrs.module.mksreports.web.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.xml.transform.stream.StreamSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MKSReportsManageControllerTest {

	private static String OUTPUT_PDF_OUTPUT_DIR = "target/test/";
	private static String OUTPUT_PDF_OUTPUT_PATH = OUTPUT_PDF_OUTPUT_DIR + "out_samplePatientHistory.pdf"; 
	
	private MKSReportsManageController ctrl;
	
	@Before
	public void setUp() {
		ctrl = new MKSReportsManageController();
		
		File file = new File(OUTPUT_PDF_OUTPUT_PATH);
		file.mkdirs();
		
		/* The below code deleting the output PDF should in fact even
		eventually moved in the tear down routine after tests are performed. */
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
		
		InputStream inStreamXml = getClass().getClassLoader().getResourceAsStream("samplePatientHistory.xml");
		StreamSource xmlSourceStream = new StreamSource(inStreamXml);
		
		InputStream inStreamXsl = getClass().getClassLoader().getResourceAsStream(MKSReportsManageController.PATIENT_HISTORY_XSL_PATH);
		StreamSource xslTransformStream = new StreamSource(inStreamXsl);
		
		FileOutputStream outStream = new FileOutputStream(new File(OUTPUT_PDF_OUTPUT_PATH));
		ctrl.writeToOutputStream(xmlSourceStream, xslTransformStream, outStream);
		outStream.close();
		
		//TODO Actually check somehow that the PDF is ok.
	}
}
