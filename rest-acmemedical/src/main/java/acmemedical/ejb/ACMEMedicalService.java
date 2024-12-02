/********************************************************************************************************
 * File:  ACMEMedicalService.java Course Materials CST 8277
 * Last Updated 2024-12-02
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @author Dan Blais
 * @author Ryan Di Cioccio
 * @author Imed Cherabi
 * @author Aaron Renshaw
 * 
 */
package acmemedical.ejb;

import static acmemedical.utility.MyConstants.DEFAULT_KEY_SIZE;
import static acmemedical.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static acmemedical.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static acmemedical.utility.MyConstants.DEFAULT_SALT_SIZE;
import static acmemedical.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static acmemedical.utility.MyConstants.DEFAULT_USER_PREFIX;
import static acmemedical.utility.MyConstants.PARAM1;
import static acmemedical.utility.MyConstants.PROPERTY_ALGORITHM;
import static acmemedical.utility.MyConstants.PROPERTY_ITERATIONS;
import static acmemedical.utility.MyConstants.PROPERTY_KEY_SIZE;
import static acmemedical.utility.MyConstants.PROPERTY_SALT_SIZE;
import static acmemedical.utility.MyConstants.PU_NAME;
import static acmemedical.utility.MyConstants.USER_ROLE;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmemedical.entity.MedicalTraining;
import acmemedical.entity.Patient;
import acmemedical.entity.MedicalCertificate;
import acmemedical.entity.Medicine;
import acmemedical.entity.Prescription;
import acmemedical.entity.PrescriptionPK;
import acmemedical.entity.SecurityRole;
import acmemedical.entity.SecurityUser;
import acmemedical.entity.Physician;
import acmemedical.entity.MedicalSchool;

@SuppressWarnings("unused")

/**
 * Stateless Singleton EJB Bean - ACMEMedicalService
 */
@Singleton
public class ACMEMedicalService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = LogManager.getLogger();
    
    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;
    
    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    public List<Physician> getAllPhysicians() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Physician> cq = cb.createQuery(Physician.class);
        cq.select(cq.from(Physician.class));
        return em.createQuery(cq).getResultList();
    }
    
    public Physician getPhysicianById(int id) {
        return em.find(Physician.class, id);
    }

    @Transactional
    public Physician persistPhysician(Physician newPhysician) {
        em.persist(newPhysician);
        return newPhysician;
    }
    
    @Transactional
    public Patient persistPatient(Patient newPatient) {
        em.persist(newPatient);
        return newPatient;
    }

    @Transactional
    public void buildUserForNewPhysician(Physician newPhysician) {
        SecurityUser userForNewPhysician = new SecurityUser();
        userForNewPhysician.setUsername(
            DEFAULT_USER_PREFIX + "_" + newPhysician.getFirstName() + "." + newPhysician.getLastName());
        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALT_SIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEY_SIZE, DEFAULT_KEY_SIZE);
        pbAndjPasswordHash.initialize(pbAndjProperties);
        String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
        userForNewPhysician.setPwHash(pwHash);
        userForNewPhysician.setPhysician(newPhysician);
        TypedQuery<SecurityRole> query = em.createNamedQuery("SecurityRole.findRoleByName", SecurityRole.class);
        query.setParameter(PARAM1, "USER_ROLE");
        SecurityRole userRole = query.getSingleResult();
        userForNewPhysician.getRoles().add(userRole);
        userRole.getUsers().add(userForNewPhysician);
        em.persist(userForNewPhysician);
    }

    @Transactional
    public Medicine setMedicineForPhysicianPatient(int physicianId, int patientId, Medicine newMedicine) {
        Physician physicianToBeUpdated = em.find(Physician.class, physicianId);
        if (physicianToBeUpdated != null) { // Physician exists
            Set<Prescription> prescriptions = physicianToBeUpdated.getPrescriptions();
            prescriptions.forEach(p -> {
                if (p.getPatient().getId() == patientId) {
                    if (p.getMedicine() != null) { // Medicine exists
                        Medicine medicine = em.find(Medicine.class, p.getMedicine().getId());
                        medicine.setMedicine(newMedicine.getDrugName(),
                        				  newMedicine.getManufacturerName(),
                        				  newMedicine.getDosageInformation());
                        em.merge(medicine);
                    }
                    else { // Medicine does not exist
                        p.setMedicine(newMedicine);
                        em.merge(physicianToBeUpdated);
                    }
                }
            });
            return newMedicine;
        }
        else return null;  // Physician doesn't exists
    }

    /**
     * To update a physician
     * 
     * @param id - id of entity to update
     * @param physicianWithUpdates - entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public Physician updatePhysicianById(int id, Physician physicianWithUpdates) {
    	Physician physicianToBeUpdated = getPhysicianById(id);
        if (physicianToBeUpdated != null) {
            em.refresh(physicianToBeUpdated);
            em.merge(physicianWithUpdates);
            em.flush();
        }
        return physicianToBeUpdated;
    }
    
    /**
     * To update a patient
     * 
     * @param id - id of entity to update
     * @param patientWithUpdates - entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public Patient updatePatientById(int id, Patient patientWithUpdates) {
    	Patient patientToBeUpdated = getById(Patient.class, "Patient.findById", id);
        if (patientToBeUpdated != null) {
            em.refresh(patientToBeUpdated);
            em.merge(patientWithUpdates);
            em.flush();
        }
        return patientToBeUpdated;
    }

    /**
     * To delete a physician by id
     * 
     * @param id - physician id to delete
     */
    @Transactional
    public void deletePhysicianById(int id) {
        Physician physician = getPhysicianById(id);
        if (physician != null) {
            em.refresh(physician);
            TypedQuery<SecurityUser> findUser = em.createNamedQuery("SecurityUser.findByPhysicianId", SecurityUser.class);
            findUser.setParameter(PARAM1, id);
            SecurityUser sUser = findUser.getSingleResult();
            em.remove(sUser);
            em.remove(physician);
        }
    }
    
    /**
     * To delete a patient by id
     * 
     * @param id - patient id to delete
     */
    @Transactional
    public void deletePatientById(int id) {
        Patient patient = getById(Patient.class, "Patient.findById", id);
        if (patient != null) {
            em.remove(patient);
        } 
    }
    
    public List<MedicalSchool> getAllMedicalSchools() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MedicalSchool> cq = cb.createQuery(MedicalSchool.class);
        cq.select(cq.from(MedicalSchool.class));
        return em.createQuery(cq).getResultList();
    }
    
    public Prescription getPrescriptionById(PrescriptionPK id) {
        TypedQuery<Prescription> query = em.createNamedQuery("Prescription.findByPrescriptionPK", Prescription.class);
        query.setParameter(PARAM1, id);
        return query.getSingleResult();
    }

    // Why not use the build-in em.find?  The named query SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME
    // includes JOIN FETCH that we cannot add to the above API
    public MedicalSchool getMedicalSchoolById(int id) {
        TypedQuery<MedicalSchool> specificMedicalSchoolQuery = em.createNamedQuery("MedicalSchool.findById", MedicalSchool.class);
        specificMedicalSchoolQuery.setParameter(PARAM1, id);
        return specificMedicalSchoolQuery.getSingleResult();
    }
    
    // These methods are more generic.

    public <T> List<T> getAll(Class<T> entity, String namedQuery) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        return allQuery.getResultList();
    }
    
    public <T> T getById(Class<T> entity, String namedQuery, int id) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        allQuery.setParameter(PARAM1, id);
        return allQuery.getSingleResult();
    }

    @Transactional
    public MedicalSchool deleteMedicalSchool(int id) {
        //MedicalSchool ms = getMedicalSchoolById(id);
    	MedicalSchool ms = getById(MedicalSchool.class, "MedicalSchool.findById", id);
        if (ms != null) {
            Set<MedicalTraining> medicalTrainings = ms.getMedicalTrainings();
            List<MedicalTraining> list = new LinkedList<>();
            medicalTrainings.forEach(list::add);
            list.forEach(mt -> {
                if (mt.getCertificate() != null) {
                    MedicalCertificate mc = getById(MedicalCertificate.class, "MedicalCertificate.findById", mt.getCertificate().getId());
                    mc.setMedicalTraining(null);
                }
                mt.setCertificate(null);
                em.merge(mt);
            });
            em.remove(ms);
            return ms;
        }
        return null;
    }
    
    // Please study & use the methods below in your test suites
    
    public boolean isDuplicated(MedicalSchool newMedicalSchool) {
        TypedQuery<Long> allMedicalSchoolsQuery = em.createNamedQuery("MedicalSchool.isDuplicate", Long.class);
        allMedicalSchoolsQuery.setParameter(PARAM1, newMedicalSchool.getName());
        return (allMedicalSchoolsQuery.getSingleResult() >= 1);
    }

    @Transactional
    public MedicalSchool persistMedicalSchool(MedicalSchool newMedicalSchool) {
        em.persist(newMedicalSchool);
        return newMedicalSchool;
    }

    @Transactional
    public MedicalSchool updateMedicalSchool(int id, MedicalSchool updatingMedicalSchool) {
    	MedicalSchool medicalSchoolToBeUpdated = getMedicalSchoolById(id);
        if (medicalSchoolToBeUpdated != null) {
            em.refresh(medicalSchoolToBeUpdated);
            medicalSchoolToBeUpdated.setName(updatingMedicalSchool.getName());
            em.merge(medicalSchoolToBeUpdated);
            em.flush();
        }
        return medicalSchoolToBeUpdated;
    }
    
    @Transactional
    public MedicalTraining persistMedicalTraining(MedicalTraining newMedicalTraining) {
        em.persist(newMedicalTraining);
        return newMedicalTraining;
    }
    
    public MedicalTraining getMedicalTrainingById(int mtId) {
        TypedQuery<MedicalTraining> allMedicalTrainingQuery = em.createNamedQuery("MedicalTraining.findById", MedicalTraining.class);
        allMedicalTrainingQuery.setParameter(PARAM1, mtId);
        return allMedicalTrainingQuery.getSingleResult();
    }

    @Transactional
    public MedicalTraining updateMedicalTraining(int id, MedicalTraining medicalTrainingWithUpdates) {
    	MedicalTraining medicalTrainingToBeUpdated = getMedicalTrainingById(id);
        if (medicalTrainingToBeUpdated != null) {
            em.refresh(medicalTrainingToBeUpdated);
            em.merge(medicalTrainingWithUpdates);
            em.flush();
        }
        return medicalTrainingToBeUpdated;
    }
    
    /**
     * To update a medical training
     * 
     * @param id - ID of the entity to update
     * @param trainingWithUpdates - Entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public MedicalTraining updateMedicalTrainingById(int id, MedicalTraining trainingWithUpdates) {
        MedicalTraining trainingToBeUpdated = getById(MedicalTraining.class, "MedicalTraining.findById", id);
        if (trainingToBeUpdated != null) {
            em.refresh(trainingToBeUpdated);
            em.merge(trainingWithUpdates);
            em.flush();
        }
        return trainingToBeUpdated;
    }
    
    /**
     * To delete a medical training by ID
     * 
     * @param id - ID of the medical training to delete
     */
    @Transactional
    public void deleteMedicalTrainingById(int id) {
        MedicalTraining training = getById(MedicalTraining.class, "MedicalTraining.findById", id);
        if (training != null) {
            em.refresh(training);
            em.remove(training);
        }
    }
    
    /**
     * To persist a new medical certificate
     * 
     * @param newCertificate - The new MedicalCertificate entity to persist
     * @return The persisted MedicalCertificate entity with generated ID and timestamps
     */
    @Transactional
    public MedicalCertificate persistMedicalCertificate(MedicalCertificate newCertificate) {
        em.persist(newCertificate);
        em.flush(); // Ensure the persistence is immediately reflected in the database
        return newCertificate;
    }
    
    /**
     * To update a medical certificate
     * 
     * @param id - ID of the entity to update
     * @param certificateWithUpdates - Entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public MedicalCertificate updateMedicalCertificateById(int id, MedicalCertificate certificateWithUpdates) {
        MedicalCertificate certificateToBeUpdated = getById(MedicalCertificate.class, "MedicalCertificate.findById", id);
        if (certificateToBeUpdated != null) {
            em.refresh(certificateToBeUpdated);
            em.merge(certificateWithUpdates);
            em.flush();
        }
        return certificateToBeUpdated;
    }

    /**
     * To delete a medical certificate by ID
     * 
     * @param id - ID of the medical certificate to delete
     */
    @Transactional
    public void deleteMedicalCertificateById(int id) {
        MedicalCertificate certificate = getById(MedicalCertificate.class, "MedicalCertificate.findById", id);
        if (certificate != null) {
            em.refresh(certificate);
            em.remove(certificate);
        }
    }
    
    /**
     * To persist a new prescription
     * 
     * @param newPrescription - The new Prescription entity to persist
     * @return The persisted Prescription entity with generated values
     */
    @Transactional
    public Prescription persistPrescription(Prescription newPrescription) {
        em.persist(newPrescription);
        em.flush(); // Ensure immediate synchronization with the database
        return newPrescription;
    }

    
    /**
     * To update a prescription
     * 
     * @param id - ID of the entity to update
     * @param prescriptionWithUpdates - Entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public Prescription updatePrescriptionById(PrescriptionPK id, Prescription prescriptionWithUpdates) {
        TypedQuery<Prescription> query = em.createNamedQuery("Prescription.findById", Prescription.class);
        query.setParameter(PARAM1, id);
        Prescription prescriptionToBeUpdated = query.getSingleResult();
        
        if (prescriptionToBeUpdated != null) {
            em.refresh(prescriptionToBeUpdated);
            em.merge(prescriptionWithUpdates);
            em.flush();
        }
        return prescriptionToBeUpdated;
    }
    
    /**
     * To delete a prescription by ID
     * 
     * @param id - Composite key (PrescriptionPK) of the prescription to delete
     */
    @Transactional
    public void deletePrescriptionById(PrescriptionPK id) {
        TypedQuery<Prescription> query = em.createNamedQuery("Prescription.findById", Prescription.class);
        query.setParameter(PARAM1, id);

        Prescription prescription = query.getSingleResult();
        
        if (prescription != null) {
            em.refresh(prescription);
            em.remove(prescription);
        }
    }
    
    /**
     * Persist a new Medicine entity.
     * 
     * @param newMedicine - Medicine entity to persist.
     * @return Persisted Medicine entity.
     */
    @Transactional
    public Medicine persistMedicine(Medicine newMedicine) {
        em.persist(newMedicine);
        em.flush();
        return newMedicine;
    }

    /**
     * Update an existing Medicine entity by its ID.
     * 
     * @param id - ID of the Medicine to update.
     * @param updatedMedicine - Medicine entity with updated details.
     * @return Updated Medicine entity or null if not found.
     */
    @Transactional
    public Medicine updateMedicineById(int id, Medicine updatedMedicine) {
        Medicine medicineToBeUpdated = em.find(Medicine.class, id);
        if (medicineToBeUpdated != null) {
            updatedMedicine.setId(id); // Ensure the ID remains consistent
            em.merge(updatedMedicine);
            em.flush();
            return updatedMedicine;
        }
        return null; // Return null if the Medicine entity with the specified ID does not exist
    }

    /**
     * Delete a Medicine entity by its ID.
     * 
     * @param id - ID of the Medicine to delete.
     */
    @Transactional
    public void deleteMedicineById(int id) {
        Medicine medicine = em.find(Medicine.class, id);
        if (medicine != null) {
            em.remove(medicine);
        }
    }

    
  


}