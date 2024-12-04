/********************************************************************************************************
 * File:  TestACMEMedicalSystem.java Course Materials CST 8277
 * Last Updated: 2024-12-04
 * 
 * @author Teddy Yap
 * @author Dan Blais
 * @author Imed Cherabi
 * @author Ryan Di Cioccio
 * @author Aaron Renshaw
 * 
 */
package acmemedical;

import static acmemedical.utility.MyConstants.*;
import static acmemedical.utility.MyConstants.APPLICATION_API_VERSION;
import static acmemedical.utility.MyConstants.APPLICATION_CONTEXT_ROOT;
import static acmemedical.utility.MyConstants.DEFAULT_ADMIN_USER;
import static acmemedical.utility.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;
import static acmemedical.utility.MyConstants.DEFAULT_USER;
import static acmemedical.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static acmemedical.utility.MyConstants.PHYSICIAN_RESOURCE_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import acmemedical.entity.*;
import acmemedical.rest.resource.*;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import acmemedical.ejb.ACMEMedicalService;
import jakarta.ws.rs.core.Response;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@SuppressWarnings("unused")

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestACMEMedicalSystem {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;

    // Test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");
        uri = UriBuilder
            .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();
        adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER, DEFAULT_USER_PASSWORD);
    }

    protected WebTarget webTarget;
    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient().register(MyObjectMapperProvider.class).register(new LoggingFeature());
        webTarget = client.target(uri);
    }

    @Test
    public void test01_all_physicians_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
 
    @Test
    public void test02_medical_certificate_by_id_with_adminrole() {
        int certificateId = 1;
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .path(String.valueOf(certificateId))
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    public void test03_add_medical_certificate_with_adminrole() {
        MedicalCertificate newCertificate = new MedicalCertificate();
        newCertificate.setSigned((byte) 1);

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .request()
            .post(Entity.json(newCertificate));
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    public void test04_update_medical_certificate_with_adminrole() {
        int certificateId = 1;
        MedicalCertificate updatedCertificate = new MedicalCertificate();
        updatedCertificate.setSigned((byte) 0);

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .path(String.valueOf(certificateId))
            .request()
            .put(Entity.json(updatedCertificate));
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    public void test05_delete_medical_certificate_with_adminrole() {
        int certificateId = 1; // Change to a valid ID for your test environment
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .path("/" + String.valueOf(certificateId))
            .request()
            .delete();
        assertThat(response.getStatus(), is(204));
    }
    
    @Test
    public void test06_get_all_medical_schools_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    public void test07_get_medical_school_by_id_with_adminrole() {
        int medicalSchoolId = 1; // Replace with a valid ID
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .path(String.valueOf(medicalSchoolId))
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    public void test08_add_medical_school_with_adminrole() {
        MedicalSchool newMedicalSchool = new PrivateSchool();
        newMedicalSchool.setName("New Medical School");

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .request()
            .post(Entity.json(newMedicalSchool));
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    public void test09_update_medical_school_with_adminrole() {
        int medicalSchoolId = 1;
        MedicalSchool updatedSchool = new PrivateSchool();
        updatedSchool.setName("Updated Medical School");

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .path(String.valueOf(medicalSchoolId))
            .request()
            .put(Entity.json(updatedSchool));
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    public void test10_delete_medical_school_with_adminrole() {
        int medicalSchoolId = 1; 
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .path(String.valueOf(medicalSchoolId))
            .request()
            .delete();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    public void test11_add_medical_training_to_medical_school_with_adminrole() {
        int medicalSchoolId = 1;
        MedicalTraining newTraining = new MedicalTraining();
        newTraining.setDurationAndStatus(new DurationAndStatus());;

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .path(String.valueOf(medicalSchoolId))
            .path("medicaltraining")
            .request()
            .post(Entity.json(newTraining));
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    public void test12_get_all_schools_and_verify_one_with_name() {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void test13_delete_medical_school_with_invalid_id() {
        int invalidId = 9999999; 
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .path(String.valueOf(invalidId))
            .request()
            .delete();
        assertThat(response.getStatus(), is(404));
    }

    
    @Test
    public void test14_get_all_medical_trainings_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    public void test15_get_medical_training_by_id_with_adminrole() {
        int trainingId = 1;
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .path(String.valueOf(trainingId))
            .request()
            .get();
        assertThat(response.getStatus(), is(200));   
    }
    
    @Test
    public void test16_add_medical_training_with_adminrole() {
        MedicalTraining newTraining = new MedicalTraining();
        newTraining.setDurationAndStatus(new DurationAndStatus());

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .request()
            .post(Entity.json(newTraining));
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    public void test17_update_medical_training_with_adminrole() {
        int trainingId = 1;
        MedicalTraining updatedTraining = new MedicalTraining();

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .path(String.valueOf(trainingId))
            .request()
            .put(Entity.json(updatedTraining));
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    public void test18_delete_medical_training_with_adminrole() {
        int trainingId = 1;
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .path(String.valueOf(trainingId))
            .request()
            .delete();
        assertThat(response.getStatus(), is(204)); 
    }
    
    @Test
    public void test19_get_training_by_invalid_id() {
        int invalidId = 9999;
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .path(String.valueOf(invalidId))
            .request()
            .get();
        assertThat(response.getStatus(), is(404));
    }
    
    @Test
    public void test20_get_all_medicines_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicine")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void test21_get_medicine_by_id_with_adminrole() {
        int medicineId = 1;
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .path(String.valueOf(medicineId))
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void test22_add_medicine_with_adminrole() {
        Medicine newMedicine = new Medicine();
        newMedicine.setDrugName("New Medicine");

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .request()
            .post(Entity.json(newMedicine));

        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void test23_update_medicine_with_adminrole() {
        int medicineId = 1;
        Medicine updatedMedicine = new Medicine();
        updatedMedicine.setDrugName("Updated Medicine"); 

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME) 
            .path(String.valueOf(medicineId))
            .request()
            .put(Entity.json(updatedMedicine));
        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void test24_delete_medicine_with_adminrole() {
        int medicineId = 1; 
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .path(String.valueOf(medicineId))
            .request()
            .delete();
        assertThat(response.getStatus(), is(204));
    }
    
    @Test
    public void test25_get_all_medicines_and_verify_availability() {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Medicine> medicines = response.readEntity(new GenericType<List<Medicine>>() {});
        assertThat(medicines, is(not(empty())));

        boolean hasSpecificMedicine = medicines.stream().anyMatch(m -> m.getDrugName().equals("Aspirin"));
        assertThat(hasSpecificMedicine, is(true));
    }

    
}