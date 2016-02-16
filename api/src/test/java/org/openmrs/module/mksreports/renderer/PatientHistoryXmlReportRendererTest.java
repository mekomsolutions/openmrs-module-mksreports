package org.openmrs.module.mksreports.renderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.module.reporting.report.ReportData;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;

public class PatientHistoryXmlReportRendererTest {

	private static String OUTPUT_XML_OUTPUT_DIR = "target/test/";
	private static String OUTPUT_XML_OUTPUT_PATH = OUTPUT_XML_OUTPUT_DIR + "out_samplePatientHistory.xml";
	
	private PatientHistoryXmlReportRenderer renderer = new PatientHistoryXmlReportRenderer();
	private ReportData reportData = null;
	private File file = null;
	
	@Before
	public void setUp() throws IOException {
		InputStream inStream = getClass().getClassLoader().getResourceAsStream("sampleReportData.xml");
		String str = IOUtils.toString(inStream, "UTF-8");
		XStream xstream = new XStream(new Sun14ReflectionProvider());
		xstream.omitField(User.class, "log");
		xstream.omitField(Person.class, "log");
		xstream.omitField(Person.class, "deathdateEstimated");
		reportData = (ReportData) xstream.fromXML(str);
		
		file = new File(OUTPUT_XML_OUTPUT_PATH);
		file.mkdirs();
		
		/* The below code deleting the output XML should in fact even
		eventually moved in the tear down routine after tests are performed. */
		try {
			Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shoudProduceValidXml() throws IOException {
		
		renderer.render(reportData, "", new FileOutputStream(file));
	}
}
