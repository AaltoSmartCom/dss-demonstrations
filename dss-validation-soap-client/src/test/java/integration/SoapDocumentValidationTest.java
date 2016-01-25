package integration;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europa.esig.dss.FileDocument;
import eu.europa.esig.dss.RemoteDocument;
import eu.europa.esig.dss.validation.SoapDocumentValidationService;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.validation.reports.dto.DataToValidateDTO;
import eu.europa.esig.dss.validation.reports.dto.ReportsDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-context.xml")
public class SoapDocumentValidationTest {
	
	@Autowired
	private SoapDocumentValidationService validationService;

	@Test
	public void test() {
		RemoteDocument signedFile = new RemoteDocument(new FileDocument("src/test/resources/XAdESLTA.xml"));
		
		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, null, null);
		
		ReportsDTO result = validationService.validateSignature(toValidate);
		
		Assert.assertNotNull(result.getDiagnosticData());
		Assert.assertNotNull(result.getDetailedReport());
		Assert.assertNotNull(result.getSimpleReport());
		
		Assert.assertEquals(1, result.getSimpleReport().getSignature().size());
		Assert.assertEquals(2, result.getDiagnosticData().getSignature().get(0).getTimestamps().getTimestamp().size());
		Assert.assertTrue(result.getSimpleReport().getSignature().get(0).getIndication().equals(Indication.VALID));
		
		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport());
		reports.print();
	}
}
