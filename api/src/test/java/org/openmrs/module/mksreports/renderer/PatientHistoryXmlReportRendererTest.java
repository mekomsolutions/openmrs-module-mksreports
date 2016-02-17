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
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.contrib.testdata.builder.EncounterBuilder;
import org.openmrs.module.mksreports.dataset.definition.PatientHistoryEncounterAndObsDataSetDefinition;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;

public class PatientHistoryXmlReportRendererTest extends BaseModuleContextSensitiveTest {
	
	private static String OUTPUT_XML_OUTPUT_DIR = "target/test/";
	
	private static String OUTPUT_XML_OUTPUT_PATH = OUTPUT_XML_OUTPUT_DIR + "out_samplePatientHistory.xml";
	
	@Autowired
	private BuiltInPatientDataLibrary builtInPatientData;
	
	@Autowired
	private TestDataManager data;
	
	private PatientHistoryXmlReportRenderer renderer = new PatientHistoryXmlReportRenderer();
	
	private ReportData reportData = null;
	
	private File file = null;
	
	private Patient p1 =null;
	
	@Before
	public void setUp() throws Exception {
		
		/*Load the xml file with test concepts, locations, patient identifier types, ...
		TODO This should be improved once we get a metadata management strategy.
		A good strategy would be for example to use metadatadeploy (...see https://wiki.openmrs.org/display/docs/Metadata+Deploy+Module)
		to bundle metadata within a module and perhaps have another module that depends on it that can provide metada lookups utilities*/
		executeDataSet("org/openmrs/module/mksreports/include/ReportTestDataset.xml");
		
		PatientIdentifierType testIdentifierType = data.getPatientService().getPatientIdentifierType(4); //Social Security Number
		Location testLocation = data.getLocationService().getLocation(1); //Unknown Location
		
		//Build a test patient
		p1 = data.patient().name("Alice", "MKS Test").gender("F").birthdate("1975-01-02", false).dateCreated("2013-10-01").identifier(testIdentifierType, "Y2ATDN", testLocation).save();
		
		//Build a sample a test encounter
		EncounterBuilder eb = data.randomEncounter().patient(p1).encounterType(6).form(2); //Laboratory Encouter Type
		Concept wt = data.getConceptService().getConcept(5089);
		Concept civilStatus = data.getConceptService().getConcept(4);
		Concept single = data.getConceptService().getConcept(5);
		
		//Add some obs to the encounter
		eb.obs(wt, 77);
		eb.obs(civilStatus, single);
		
		//Save the encounter
		eb.save();
		
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
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a new report definition and adds a PatientDataSetDefinition and
	 * PatientHistoryEncounterAndObsDataSetDefinition datasets to it
	 * 
	 * @return rd The new report definition
	 */
	protected ReportDefinition getReportDefinition() {
		ReportDefinition rd = new ReportDefinition();
		rd.setName("Testing Renderer");
		
		//Create a new dataset definition to hold the patient's demographics
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		Map<String, Object> mappings = new HashMap<String, Object>();
		
		dsd.addColumn("ID", builtInPatientData.getPatientId(), mappings);
		dsd.addColumn("Given Name", builtInPatientData.getPreferredGivenName(), mappings);
		dsd.addColumn("Last Name", builtInPatientData.getPreferredFamilyName(), mappings);
		dsd.addColumn("Gender", builtInPatientData.getGender(), mappings);
		
		// Create a new dataset definition to hold the patient's encounters and obs
		PatientHistoryEncounterAndObsDataSetDefinition dataSetDefinition = new PatientHistoryEncounterAndObsDataSetDefinition();
		dataSetDefinition.setName("Patient History data set");
		dataSetDefinition.addSortCriteria("encounterDate", SortCriteria.SortDirection.ASC);
		
		//Attaching the tow datasets to the report definition
		rd.addDataSetDefinition("demographics", dataSetDefinition, new HashMap<String, Object>());
		rd.addDataSetDefinition("encounters", dataSetDefinition, new HashMap<String, Object>());
		return rd;
	}
	
	@Test
	public void shoudProduceValidXml() throws IOException {
		
		renderer.render(reportData, "", new FileOutputStream(file));
	}
}
