package com.dev.marchenko;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("All Parking System Tests")
@SelectPackages("com.dev.marchenko")
@IncludeClassNamePatterns({".*Test", ".*IT"})
public class AllTestsSuite {
}
