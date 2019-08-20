package eu.europa.esig.dss.web.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.InternalServerErrorException;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.junit.Before;
import org.junit.Test;

import eu.europa.esig.dss.diagnostic.jaxb.XmlCertificate;
import eu.europa.esig.dss.diagnostic.jaxb.XmlDiagnosticData;
import eu.europa.esig.dss.simplecertificatereport.jaxb.XmlChainItem;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.web.config.CXFConfig;
import eu.europa.esig.dss.ws.cert.validation.dto.CertificateReportsDTO;
import eu.europa.esig.dss.ws.cert.validation.dto.CertificateToValidateDTO;
import eu.europa.esig.dss.ws.cert.validation.rest.client.RestCertificateValidationService;
import eu.europa.esig.dss.ws.converter.RemoteCertificateConverter;
import eu.europa.esig.dss.ws.dto.RemoteCertificate;

public class RestCertificateValidationIT extends AbstractRestIT {

	private RestCertificateValidationService validationService;

	@Before
	public void init() {
		JAXRSClientFactoryBean factory = new JAXRSClientFactoryBean();

		factory.setAddress(getBaseCxf() + CXFConfig.REST_CERTIFICATE_VALIDATION);
		factory.setServiceClass(RestCertificateValidationService.class);
		factory.setProviders(Arrays.asList(jacksonJsonProvider()));

		LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		validationService = factory.create(RestCertificateValidationService.class);
	}
	
	@Test
	public void testWithCertificateChainAndValdiationTime() {
		RemoteCertificate remoteCertificate = RemoteCertificateConverter.toRemoteCertificate(
				DSSUtils.loadCertificate(new File("src/test/resources/good-user.cer")));
		RemoteCertificate issuerCertificate = RemoteCertificateConverter.toRemoteCertificate(
				DSSUtils.loadCertificate(new File("src/test/resources/good-ca.cer")));
		Calendar calendar = Calendar.getInstance();
		calendar.set(2018, 12, 31);
		Date validationDate = calendar.getTime();
		validationDate.setTime((validationDate.getTime() / 1000) * 1000); // clean millis
		CertificateToValidateDTO certificateToValidateDTO = new CertificateToValidateDTO(remoteCertificate, 
				Arrays.asList(issuerCertificate), validationDate);
		
		CertificateReportsDTO reportsDTO = validationService.validateCertificate(certificateToValidateDTO);

		assertNotNull(reportsDTO.getDiagnosticData());
		assertNotNull(reportsDTO.getSimpleCertificateReport());
		assertNotNull(reportsDTO.getDetailedReport());
		
		XmlDiagnosticData diagnosticData = reportsDTO.getDiagnosticData();
		List<XmlCertificate> usedCertificates = diagnosticData.getUsedCertificates();
		assertEquals(3, usedCertificates.size());
		List<XmlChainItem> chain = reportsDTO.getSimpleCertificateReport().getChain();
		assertEquals(3, chain.size());
		for (XmlCertificate certificate : usedCertificates) {
			if (chain.get(0).getId().equals(certificate.getId())) {
				assertEquals(2, certificate.getCertificateChain().size());
			}
		}
		assertTrue(validationDate.compareTo(diagnosticData.getValidationDate()) == 0);
	}
	
	@Test
	public void testWithNoValidationTime() {
		RemoteCertificate remoteCertificate = RemoteCertificateConverter.toRemoteCertificate(
				DSSUtils.loadCertificate(new File("src/test/resources/good-user.cer")));
		RemoteCertificate issuerCertificate = RemoteCertificateConverter.toRemoteCertificate(
				DSSUtils.loadCertificate(new File("src/test/resources/good-ca.cer")));
		
		CertificateToValidateDTO certificateToValidateDTO = new CertificateToValidateDTO(remoteCertificate, 
				Arrays.asList(issuerCertificate), null);
		
		CertificateReportsDTO reportsDTO = validationService.validateCertificate(certificateToValidateDTO);

		assertNotNull(reportsDTO.getDiagnosticData());
		assertNotNull(reportsDTO.getSimpleCertificateReport());
		assertNotNull(reportsDTO.getDetailedReport());
		
		XmlDiagnosticData diagnosticData = reportsDTO.getDiagnosticData();
		List<XmlCertificate> usedCertificates = diagnosticData.getUsedCertificates();
		assertEquals(3, usedCertificates.size());
		List<XmlChainItem> chain = reportsDTO.getSimpleCertificateReport().getChain();
		assertEquals(3, chain.size());
		for (XmlCertificate certificate : usedCertificates) {
			if (chain.get(0).getId().equals(certificate.getId())) {
				assertEquals(2, certificate.getCertificateChain().size());
			}
		}
		assertNotNull(diagnosticData.getValidationDate());
	}
	
	@Test
	public void testWithNoCertificateChain() {
		RemoteCertificate remoteCertificate = RemoteCertificateConverter.toRemoteCertificate(
				DSSUtils.loadCertificate(new File("src/test/resources/good-user.cer")));
		CertificateToValidateDTO certificateToValidateDTO = new CertificateToValidateDTO(remoteCertificate);
		
		CertificateReportsDTO reportsDTO = validationService.validateCertificate(certificateToValidateDTO);

		assertNotNull(reportsDTO.getDiagnosticData());
		assertNotNull(reportsDTO.getSimpleCertificateReport());
		assertNotNull(reportsDTO.getDetailedReport());
		
		XmlDiagnosticData diagnosticData = reportsDTO.getDiagnosticData();
		List<XmlCertificate> usedCertificates = diagnosticData.getUsedCertificates();
		assertEquals(3, usedCertificates.size());
		List<XmlChainItem> chain = reportsDTO.getSimpleCertificateReport().getChain();
		assertEquals(3, chain.size());
		for (XmlCertificate certificate : usedCertificates) {
			if (chain.get(0).getId().equals(certificate.getId())) {
				assertEquals(2, certificate.getCertificateChain().size());
			}
		}
		assertNotNull(diagnosticData.getValidationDate());
	}
	
	@Test(expected = InternalServerErrorException.class)
	public void testWithNoCertificateProvided() {
		CertificateToValidateDTO certificateToValidateDTO = new CertificateToValidateDTO(null);
		
		CertificateReportsDTO reportsDTO = validationService.validateCertificate(certificateToValidateDTO);

		assertNotNull(reportsDTO.getDiagnosticData());
		assertNotNull(reportsDTO.getSimpleCertificateReport());
		assertNotNull(reportsDTO.getDetailedReport());
		
		XmlDiagnosticData diagnosticData = reportsDTO.getDiagnosticData();
		List<XmlCertificate> usedCertificates = diagnosticData.getUsedCertificates();
		assertEquals(0, usedCertificates);
		List<XmlChainItem> chain = reportsDTO.getSimpleCertificateReport().getChain();
		assertEquals(0, chain.size());
		assertNotNull(diagnosticData.getValidationDate());
	}

}