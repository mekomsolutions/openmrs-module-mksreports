package org.openmrs.module.mksreports.renderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.module.mksreports.dataset.definition.PatientHistoryEncounterAndObsDataSetDefinition;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;

public class PatientHistoryXmlReportRendererTest {

	private static String OUTPUT_XML_OUTPUT_DIR = "target/test/";
	private static String OUTPUT_XML_OUTPUT_PATH = OUTPUT_XML_OUTPUT_DIR + "out_samplePatientHistory.xml";
	
	@Autowired
	private BuiltInPatientDataLibrary builtInPatientData;
	
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
	
	protected ReportDefinition getReportDefinition() {
		ReportDefinition rd = new ReportDefinition();
		rd.setName("Testing");
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		Map<String, Object> mappings = new HashMap<String, Object>();
		
		dsd.addColumn("ID", builtInPatientData.getPatientId(),mappings);
		dsd.addColumn("Given Name", builtInPatientData.getPreferredGivenName(),mappings);
		dsd.addColumn("Last Name", builtInPatientData.getPreferredFamilyName(),mappings);
		dsd.addColumn("Gender", builtInPatientData.getGender(),mappings);
		
		// Create new dataset definition 
		PatientHistoryEncounterAndObsDataSetDefinition dataSetDefinition = new PatientHistoryEncounterAndObsDataSetDefinition();
		dataSetDefinition.setName("Patient History data set");
		dataSetDefinition.addSortCriteria("encounterDate", SortCriteria.SortDirection.ASC);
		
		rd.addDataSetDefinition("demographics", dataSetDefinition, new HashMap<String, Object>());		
		rd.addDataSetDefinition("encounters", dataSetDefinition, new HashMap<String, Object>());
		return rd;
	}
	@Test
	public void shoudProduceValidXml() throws IOException {
		
		renderer.render(reportData, "", new FileOutputStream(file));
	}
}
