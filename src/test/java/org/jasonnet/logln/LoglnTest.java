package org.jasonnet.logln;

import org.junit.Test;
import static org.jasonnet.logln.Logln.logln;

public class LoglnTest {

	@Test
	public void veryveryverylongMethodNameTest() {
		logln("mm");
	}

	@Test
	public void shortMNameTest() {
		logln("mm");
	}

	@Test
	public void testClassNameLength() {
		ShortClassName.method1();
		VeryVeryVeryVeryLongClassName.method1();
	}
}
