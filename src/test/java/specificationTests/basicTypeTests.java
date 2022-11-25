package specificationTests;

import nl.altindag.log.LogCaptor;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import crml.translator.Main;

 class specificationTests {
	 
	 static LogCaptor logCaptor;
	 static String testFolder,  outputFolder;
	 private static final Logger logger = LogManager.getLogger();
	 
	 @BeforeAll
	    public static void setupLogCaptor() {
	        logCaptor = LogCaptor.forClass(Main.class);
	        
	        testFolder = Thread.currentThread().getContextClassLoader().getResource("testFiles/specification").getPath(); 
	        
	       outputFolder = new File("output").getAbsolutePath();
	        
	    }
	 
	 	@Test
	 	@DisplayName("Check test suite is running")
		void testDummy() {
			
	    	assert(true);
		}	


	    @AfterEach
	    public void clearLogs() {
	        logCaptor.clearLogs();
	    }
	    
	    @AfterAll
	    public static void tearDown() {
	        logCaptor.close();
	    } 

	    @DisplayName("Testing primitive type translation")
	    @ParameterizedTest
	    @ValueSource(strings = {"PrimitiveDataTypes/IntegerPrimitives.crml", 
	    		"PrimitiveDataTypes/BooleanPrimitives.crml", 
	    		"PrimitiveDataTypes/RealPrimitives.crml"})
	    void testPrimitiveTypes(String fileName) {
	    	try {
	    		
				crml.translator.Main.parse_file(testFolder, fileName, outputFolder);
				
			} catch (IOException e) {
				fail("Unable to translate " + fileName);
			}
	    	
	}	
	
	

}
