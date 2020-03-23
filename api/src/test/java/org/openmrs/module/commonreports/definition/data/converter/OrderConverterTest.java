package org.openmrs.module.commonreports.definition.data.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Order;
import org.openmrs.api.OrderService;
import org.openmrs.module.commonreports.data.converter.OrderConverter;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class OrderConverterTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private OrderService orderService;
	
	private List<Order> orders;
	
	@Before
	public void setUp() throws Exception {
		
		executeDataSet("org/openmrs/module/reporting/include/ReportTestDataset-openmrs-2.0.xml");
		
		orders = new ArrayList<Order>();
		orders.add(orderService.getOrder(1));
		orders.add(orderService.getOrder(2));
		orders.add(orderService.getOrder(3));
	}
	
	@Test
	public void convert_shouldReturnFormattedString() {
		
		OrderConverter converter = new OrderConverter();
		
		{
			String formattedOrders = (String) converter.convert(orders.get(0));
			assertEquals("Aspirin", formattedOrders);
		}
		
		// add a non drug order
		orders.add(orderService.getOrder(6));
		{
			String formattedOrders = (String) converter.convert(orders.get(3));
			assertEquals("CD4 COUNT", formattedOrders);
		}
	}
	
	@Test
	public void convert_shouldHandleNullInput() {
		
		OrderConverter converter = new OrderConverter();
		assertNull(converter.convert(null));
	}
}
