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
import static org.hamcrest.Matchers.nullValue;


import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.time.LocalDateTime;
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
import acmemedical.entity.MedicalCertificate;
import jakarta.ws.rs.core.Response;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import acmemedical.entity.Physician;

@SuppressWarnings("unused")

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestACMEMedicalSystem {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;
    
    static final String MEDICAL_CERTIFICATE_RESOURCE_NAME = "medical_certificate";
    static final String MEDICAL_SCHOOL_RESOURCE_NAME = "medical_school";
    static final String MEDICAL_TRAINING_RESOURCE_NAME = "medical_training";

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
            //.register(userAuth)
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Physician> physicians = response.readEntity(new GenericType<List<Physician>>(){});
        assertThat(physicians, is(not(empty())));
        assertThat(physicians, hasSize(1));
    }
    
 
    @Test
    public void test02_medical_certificate_by_id_with_adminrole() {
        int certificateId = 1; // Change to a valid ID for your test environment
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .path(String.valueOf(certificateId))
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        MedicalCertificate certificate = response.readEntity(MedicalCertificate.class);
        assertThat(certificate, is(not(nullValue())));
        assertThat(certificate.getId(), is(certificateId));
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
        MedicalCertificate createdCertificate = response.readEntity(MedicalCertificate.class);
        assertThat(createdCertificate, is(not(nullValue())));
        assertThat(createdCertificate.getSigned(), is((byte) 1));
    }
    
    @Test
    public void test04_update_medical_certificate_with_adminrole() {
        int certificateId = 1; // Change to a valid ID for your test environment
        MedicalCertificate updatedCertificate = new MedicalCertificate();
        updatedCertificate.setSigned((byte) 0);

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .path(String.valueOf(certificateId))
            .request()
            .put(Entity.json(updatedCertificate));
        assertThat(response.getStatus(), is(200));
        MedicalCertificate certificate = response.readEntity(MedicalCertificate.class);
        assertThat(certificate.getSigned(), is((byte) 0));
    }
    
    @Test
    public void test05_delete_medical_certificate_with_adminrole() {
        int certificateId = 1; // Change to a valid ID for your test environment
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .path(String.valueOf(certificateId))
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
        List<MedicalSchool> medicalSchools = response.readEntity(new GenericType<List<MedicalSchool>>() {});
        assertThat(medicalSchools, is(not(empty())));
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
        MedicalSchool medicalSchool = response.readEntity(MedicalSchool.class);
        assertThat(medicalSchool, is(not(nullValue())));
        assertThat(medicalSchool.getId(), is(medicalSchoolId));
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
        MedicalSchool createdSchool = response.readEntity(MedicalSchool.class);
        assertThat(createdSchool, is(not(nullValue())));
        assertThat(createdSchool.getName(), is("New Medical School"));
    }
    
    @Test
    public void test09_update_medical_school_with_adminrole() {
        int medicalSchoolId = 1; // Replace with a valid ID
        MedicalSchool updatedSchool = new PrivateSchool();
        updatedSchool.setName("Updated Medical School");

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .path(String.valueOf(medicalSchoolId))
            .request()
            .put(Entity.json(updatedSchool));
        assertThat(response.getStatus(), is(200));
        MedicalSchool resultSchool = response.readEntity(MedicalSchool.class);
        assertThat(resultSchool.getName(), is("Updated Medical School"));
    }
    
    @Test
    public void test10_delete_medical_school_with_adminrole() {
        int medicalSchoolId = 1; // Replace with a valid ID
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

        MedicalSchool associatedSchool = new MedicalSchool() {
            {
                setId(medicalSchoolId);
            }
        };
        newTraining.setMedicalSchool(associatedSchool);

        DurationAndStatus durationAndStatus = new DurationAndStatus();
        durationAndStatus.setStartDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        durationAndStatus.setEndDate(LocalDateTime.of(2025, 1, 1, 0, 0));
        durationAndStatus.setActive((byte) 1); // Active status represented by 1
        newTraining.setDurationAndStatus(durationAndStatus);

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .path(String.valueOf(medicalSchoolId))
            .path("medicaltraining")
            .request()
            .post(Entity.json(newTraining));

        assertThat(response.getStatus(), is(200));
        MedicalTraining createdTraining = response.readEntity(MedicalTraining.class);
        assertThat(createdTraining, is(not(nullValue())));
        assertThat(createdTraining.getMedicalSchool().getId(), is(medicalSchoolId));
        assertThat(createdTraining.getDurationAndStatus().getStartDate(), is(LocalDateTime.of(2024, 1, 1, 0, 0)));
        assertThat(createdTraining.getDurationAndStatus().getEndDate(), is(LocalDateTime.of(2025, 1, 1, 0, 0)));
        assertThat(createdTraining.getDurationAndStatus().getActive(), is((byte) 1));
    }


    
    @Test
    public void test12_get_all_medical_trainings_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<MedicalTraining> trainings = response.readEntity(new GenericType<List<MedicalTraining>>() {});
        assertThat(trainings, is(not(empty())));
    }
    
    @Test
    public void test13_get_medical_training_by_id_with_adminrole() {
        int trainingId = 1; // Replace with a valid ID
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .path(String.valueOf(trainingId))
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        MedicalTraining training = response.readEntity(MedicalTraining.class);
        assertThat(training, is(not(nullValue())));
        assertThat(training.getId(), is(trainingId));
    }
    
    @Test
    public void test14_add_medical_training_with_adminrole() {
        MedicalTraining newTraining = new MedicalTraining();

        DurationAndStatus durationAndStatus = new DurationAndStatus();
        durationAndStatus.setStartDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        durationAndStatus.setEndDate(LocalDateTime.of(2025, 1, 1, 0, 0));
        durationAndStatus.setActive((byte) 1); // Active status represented by 1
        newTraining.setDurationAndStatus(durationAndStatus);

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .request()
            .post(Entity.json(newTraining));

        assertThat(response.getStatus(), is(200));
        MedicalTraining createdTraining = response.readEntity(MedicalTraining.class);
        assertThat(createdTraining, is(not(nullValue())));
        assertThat(createdTraining.getDurationAndStatus().getStartDate(), is(LocalDateTime.of(2024, 1, 1, 0, 0)));
        assertThat(createdTraining.getDurationAndStatus().getEndDate(), is(LocalDateTime.of(2025, 1, 1, 0, 0)));
        assertThat(createdTraining.getDurationAndStatus().getActive(), is((byte) 1));
    }

    @Test
    public void test15_update_medical_training_with_adminrole() {
        int trainingId = 1; // Replace with a valid ID
        MedicalTraining updatedTraining = new MedicalTraining();

        DurationAndStatus durationAndStatus = new DurationAndStatus();
        durationAndStatus.setStartDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        durationAndStatus.setEndDate(LocalDateTime.of(2025, 1, 1, 0, 0));
        durationAndStatus.setActive((byte) 0); // Active status set to 0 for inactive
        updatedTraining.setDurationAndStatus(durationAndStatus);

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .path(String.valueOf(trainingId))
            .request()
            .put(Entity.json(updatedTraining));

        assertThat(response.getStatus(), is(200));
        MedicalTraining resultTraining = response.readEntity(MedicalTraining.class);
        assertThat(resultTraining.getDurationAndStatus().getStartDate(), is(LocalDateTime.of(2024, 1, 1, 0, 0)));
        assertThat(resultTraining.getDurationAndStatus().getEndDate(), is(LocalDateTime.of(2025, 1, 1, 0, 0)));
        assertThat(resultTraining.getDurationAndStatus().getActive(), is((byte) 0)); // Expect inactive status
    }

    
    @Test
    public void test16_delete_medical_training_with_adminrole() {
        int trainingId = 1; // Replace with a valid ID
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .path(String.valueOf(trainingId))
            .request()
            .delete();
        assertThat(response.getStatus(), is(204)); 
    }
    
    @Test
    public void test17_get_all_medicines_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicine") // Use the correct resource name
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Medicine> medicines = response.readEntity(new GenericType<List<Medicine>>() {});
        assertThat(medicines, is(not(empty())));
    }

    @Test
    public void test18_get_medicine_by_id_with_adminrole() {
        int medicineId = 1; // Replace with a valid ID
        Response response = webTarget
            .register(adminAuth)
            .path("medicine") // Use the correct resource name
            .path(String.valueOf(medicineId))
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        Medicine medicine = response.readEntity(Medicine.class);
        assertThat(medicine, is(not(nullValue())));
        assertThat(medicine.getId(), is(medicineId));
    }

    @Test
    public void test19_add_medicine_with_adminrole() {
        Medicine newMedicine = new Medicine();
        newMedicine.setDrugName("New Medicine"); // Set appropriate fields
        newMedicine.setManufacturerName("Drugtopia");
        newMedicine.setDosageInformation("20mg every 6 hours.");

        Response response = webTarget
            .register(adminAuth)
            .path("medicine") // Use the correct resource name
            .request()
            .post(Entity.json(newMedicine));
        assertThat(response.getStatus(), is(200));
        Medicine createdMedicine = response.readEntity(Medicine.class);
        assertThat(createdMedicine, is(not(nullValue())));
        assertThat(createdMedicine.getDrugName(), is("New Medicine"));
    }

    @Test
    public void test20_update_medicine_with_adminrole() {
        int medicineId = 1; // Replace with a valid ID
        Medicine updatedMedicine = new Medicine();
        updatedMedicine.setDrugName("Updated Medicine"); // Set appropriate fields
        updatedMedicine.setManufacturerName("Drugopolis");
        updatedMedicine.setDosageInformation("60mg every 6 minutes.");

        Response response = webTarget
            .register(adminAuth)
            .path("medicine") // Use the correct resource name
            .path(String.valueOf(medicineId))
            .request()
            .put(Entity.json(updatedMedicine));
        assertThat(response.getStatus(), is(200));
        Medicine medicine = response.readEntity(Medicine.class);
        assertThat(medicine.getDrugName(), is("Updated Medicine"));
    }

    @Test
    public void test21_delete_medicine_with_adminrole() {
        int medicineId = 1; // Replace with a valid ID
        Response response = webTarget
            .register(adminAuth)
            .path("medicine") // Use the correct resource name
            .path(String.valueOf(medicineId))
            .request()
            .delete();
        assertThat(response.getStatus(), is(204));
    }

    
}