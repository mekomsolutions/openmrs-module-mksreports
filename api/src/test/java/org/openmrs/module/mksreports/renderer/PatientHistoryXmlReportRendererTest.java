package org.openmrs.module.mksreports.renderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.contrib.testdata.builder.EncounterBuilder;
import org.openmrs.module.mksreports.dataset.definition.PatientHistoryEncounterAndObsDataSetDefinition;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class PatientHistoryXmlReportRendererTest extends BaseModuleContextSensitiveTest {
	
	private static String OUTPUT_XML_OUTPUT_DIR = "target/test/";
	
	private static String OUTPUT_XML_OUTPUT_PATH = OUTPUT_XML_OUTPUT_DIR + "out_samplePatientHistory.xml";
	
	@Autowired
	private BuiltInPatientDataLibrary builtInPatientData;
	
	@Autowired
	private ReportDefinitionService reportDefinitionService;
	
	@Autowired
	private TestDataManager data;
	
	private ReportData reportData = null;
	
	private File file = null;
	
	private Patient p1 =null;
	
	@Before
	public void setUp() throws Exception {
		
		/*Load the xml file with test concepts, locations, patient identifier types, ...
		TODO This should be improved once we get a metadata management strategy.
		A good strategy would be for example to use metadatadeploy (...see https://wiki.openmrs.org/display/docs/Metadata+Deploy+Module)
		to bundle metadata within a module and perhaps have another module that depends on it that can provide metadata lookup utilities*/
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
		
		file = new File(OUTPUT_XML_OUTPUT_DIR);
		file.mkdirs();
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
		PatientDataSetDefinition demographicsDSD = new PatientDataSetDefinition();
		Map<String, Object> mappings = new HashMap<String, Object>();
		
		demographicsDSD.addColumn("ID", builtInPatientData.getPatientId(), mappings);
		demographicsDSD.addColumn("Given Name", builtInPatientData.getPreferredGivenName(), mappings);
		demographicsDSD.addColumn("Last Name", builtInPatientData.getPreferredFamilyName(), mappings);
		demographicsDSD.addColumn("Gender", builtInPatientData.getGender(), mappings);
		
		// Create a new dataset definition to hold the patient's encounters and obs
		PatientHistoryEncounterAndObsDataSetDefinition encountersDSD = new PatientHistoryEncounterAndObsDataSetDefinition();
		encountersDSD.setName("Patient History data set");
		encountersDSD.addSortCriteria("encounterDate", SortCriteria.SortDirection.ASC);
		
		//Attaching the tow datasets to the report definition
		rd.addDataSetDefinition("demographics", demographicsDSD, new HashMap<String, Object>());
		rd.addDataSetDefinition("encounters", encountersDSD, new HashMap<String, Object>());
		return rd;
	}
	
	/**
	 * Gets the report definition and builds an evaluation context with one patient (...the test patient p1)
	 * Creates then a report design that uses PatientHistoryExcelTemplateRenderer and renders the report to xml.
	 * @throws IOException
	 * @throws EvaluationException error evaluating the report definition
	 */
	@Test
	public void shoudProduceValidXml() throws IOException, EvaluationException {
		
		ReportDefinition reportDefinition  = getReportDefinition();
		// Populate a new EvaluationContext with the test patient
		EvaluationContext context = new EvaluationContext();
		Cohort baseCohort = new Cohort();
		baseCohort.addMember(p1.getPatientId());
		context.setBaseCohort(baseCohort);
		
		// Evaluate the report with this context to produce the data to use to populate the summary
		reportData = reportDefinitionService.evaluate(reportDefinition, context);
		
		final ReportDesign design = new ReportDesign();
		design.setName("TestDesign");
		design.setReportDefinition(reportDefinition);
		design.setRendererType(PatientHistoryXmlReportRenderer.class);
		
		PatientHistoryXmlReportRenderer renderer = new PatientHistoryXmlReportRenderer() {
			public ReportDesign getDesign(String argument) {
				return design;
			}
		};
		
		//Outputting the generated xml to target/test
		FileOutputStream fos = new FileOutputStream(OUTPUT_XML_OUTPUT_PATH);
		renderer.render(reportData, " ", fos);
		fos.close();
	}
}
