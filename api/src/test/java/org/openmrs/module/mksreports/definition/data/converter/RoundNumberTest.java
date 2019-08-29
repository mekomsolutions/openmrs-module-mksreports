package org.openmrs.module.mksreports.definition.data.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.openmrs.module.mksreports.data.converter.RoundNumber;

public class RoundNumberTest {
	
	@Test
	public void convert_shouldReturnRoundedNumber() {
		
		RoundNumber rounder2 = new RoundNumber();
		
		// Default rounding is to 2 decimals
		assertEquals(new Double(1234.05), (Double) rounder2.convert("1234.0517609876987"));
		
		assertEquals(new Double(-9.08), (Double) rounder2.convert(new Double(-9.079754)));
		
		assertEquals(new Integer(5), rounder2.convert(new Integer(5)));
		
		Object o = new Object();
		assertEquals(o, rounder2.convert(o));
		
		// Force rounding to 5 decimals
		RoundNumber rounder5 = new RoundNumber();
		rounder5.setDecimals(5);
		
		assertEquals(new Double(1234.05176), (Double) rounder5.convert("1234.0517609876987"));
	}
	
	@Test
	public void convert_shouldHandleNullInput() {
		
		RoundNumber rounder = new RoundNumber();
		assertNull(rounder.convert(null));
	}
}
