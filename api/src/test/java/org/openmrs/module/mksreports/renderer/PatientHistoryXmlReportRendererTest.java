package org.openmrs.module.mksreports.renderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptNumeric;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.serialization.SerializationException;

import com.thoughtworks.xstream.XStream;

public class PatientHistoryXmlReportRendererTest {
	
	private static String OUTPUT_XML_OUTPUT_DIR = "target/test/";
	
	private static String OUTPUT_XML_OUTPUT_PATH = OUTPUT_XML_OUTPUT_DIR + "out_samplePatientHistory.xml";
	
	private PatientHistoryXmlReportRenderer renderer = new PatientHistoryXmlReportRenderer();
	
	private ReportData reportData = null;
	
	private File file = null;
	
	@Before
	public void setUp() throws IOException, SerializationException {
		InputStream inStream = getClass().getClassLoader().getResourceAsStream("sampleReportData.xml");
		String str = IOUtils.toString(inStream, "UTF-8");
		
		XStream xstream = new XStream();
		xstream.alias("org.openmrs.User_$$_jvstd85_41", User.class);
		xstream.alias("org.openmrs.ConceptDatatype_$$_jvstd85_40", ConceptDatatype.class);
		xstream.alias("org.openmrs.ConceptClass_$$_jvstd85_16", ConceptClass.class);
		xstream.alias("org.openmrs.Concept_$$_jvstd85_10", Concept.class);
		xstream.omitField(User.class, "log");
		xstream.omitField(Person.class, "log");
		xstream.omitField(Person.class, "deathdateEstimated");
		xstream.omitField(ConceptNumeric.class, "allowDecimal");
		reportData = (ReportData) xstream.fromXML(str);
		
		file = new File(OUTPUT_XML_OUTPUT_PATH);
		file.mkdirs();
		
		/*
		 * The below code deleting the output XML should in fact even eventually moved
		 * in the tear down routine after tests are performed.
		 */
		try {
			Files.deleteIfExists(file.toPath());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shoudProduceValidXml() throws Exception {
		
		renderer.render(reportData, "in_tests", new FileOutputStream(file));
		
	}
}
