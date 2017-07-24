package org.openmrs.module.mksreports.query.evaluator;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.VisitService;
import org.openmrs.module.mksreports.query.VisitWithinDateRangeQuery;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class VisitWithinDateRangeQueryEvaluatorTest extends BaseModuleContextSensitiveTest {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	EvaluationService evaluationService;
	
	@Autowired
	VisitService visitService;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet("reportingTestDataset.xml");
		
		visitService.endVisit(visitService.getVisit(243), DateUtil.getDateTime(2017, 7, 10));
		visitService.endVisit(visitService.getVisit(244), DateUtil.getDateTime(2017, 7, 10));
		visitService.endVisit(visitService.getVisit(242), DateUtil.getDateTime(2017, 7, 11));
	}
	
	@Test
	public void should_ReturnVisitsWithinDateRange() throws EvaluationException {
		
		EvaluationContext evalContext = new EvaluationContext();
		
		VisitWithinDateRangeQueryEvaluator queryEvaluator = new VisitWithinDateRangeQueryEvaluator();
		
		VisitWithinDateRangeQuery vq = new VisitWithinDateRangeQuery();
		vq.setStartDate(DateUtil.getDateTime(2017, 7, 01));
		vq.setEndDate(DateUtil.getDateTime(2017, 7, 31));
		
		List<Integer> visitIds = queryEvaluator.getAllVisitsWithinDateRange(vq, evalContext, evaluationService);
		
		assertEquals(3, visitIds.size());
	}
	
}
