/********************************************************************************************************
 * File:  Prescription.java Course Materials CST 8277
 * Last Updated: 2024-12-02
 * 
 * @author Teddy Yap
 * @author Dan Blais
 * @author Imed Cherabi
 * @author Ryan Di Cioccio
 * @author Aaron Renshaw
 * 
 */
package acmemedical.entity;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@SuppressWarnings("unused")
/**
 * The persistent class for the prescription database table.
 */
@Entity
@Table(name = "prescription")
@Access(AccessType.FIELD)
@NamedQueries({
    @NamedQuery(name = "Prescription.findAll", query = "SELECT p FROM Prescription p"),
    @NamedQuery(name = "Prescription.findById", query = "SELECT p FROM Prescription p WHERE p.id = :param1"),
    @NamedQuery(name = "Prescription.findByPrescriptionPK", query = "SELECT p FROM Prescription p WHERE p.id = :param1")
})
public class Prescription extends PojoBaseCompositeKey<PrescriptionPK> implements Serializable {
	private static final long serialVersionUID = 1L;

	// Hint - What annotation is used for a composite primary key type?
	@EmbeddedId
	private PrescriptionPK id;

	// @MapsId is used to map a part of composite key to an entity.
	@JsonManagedReference
	@MapsId("physicianId")
    @ManyToOne(cascade = CascadeType.ALL, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "physician_id", referencedColumnName = "id", nullable = false)
	private Physician physician;

	@JsonManagedReference
	@MapsId("patientId")
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "patient_id", referencedColumnName = "patient_id", nullable = false)
	private Patient patient;

	@JsonManagedReference
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
	private Medicine medicine;

	@Column(name = "number_of_refills")
	private int numberOfRefills;

	@Column(length = 100, name = "prescription_information")
	private String prescriptionInformation;


	public Prescription() {
		id = new PrescriptionPK();
	}

	@Override
	public PrescriptionPK getId() {
		return id;
	}

	@Override
	public void setId(PrescriptionPK id) {
		this.id = id;
	}

	public Physician getPhysician() {
		return physician;
	}

	public void setPhysician(Physician physician) {
		id.setPhysicianId(physician.id);
		this.physician = physician;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		id.setPatientId(patient.id);
		this.patient = patient;
	}

	public Medicine getMedicine() {
		return medicine;
	}

	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}

	public int getNumberOfRefills() {
		return numberOfRefills;
	}
	
	public void setNumberOfRefills(int numberOfRefills) {
		this.numberOfRefills = numberOfRefills;
	}

	public String getPrescriptionInformation() {
		return prescriptionInformation;
	}

	public void setPrescriptionInformation(String prescriptionInformation) {
		this.prescriptionInformation = prescriptionInformation;
	}

	//Inherited hashCode/equals is sufficient for this entity class

}