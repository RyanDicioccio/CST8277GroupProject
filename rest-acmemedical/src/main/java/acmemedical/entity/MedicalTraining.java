/********************************************************************************************************
 * File:  MedicalTraining.java Course Materials CST 8277
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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@SuppressWarnings("unused")

/**
 * The persistent class for the medical_training database table.
 */
@Entity
@Table(name = "medical_training")
@NamedQuery(name = "MedicalTraining.findAll", query = "SELECT mt FROM MedicalTraining mt")
@NamedQuery(name = "MedicalTraining.findById", query = "SELECT mt FROM MedicalTraining mt WHERE mt.id = :param1")
@AttributeOverride(name = "id", column = @Column(name = "training_id"))
public class MedicalTraining extends PojoBase implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "school_id")
	private MedicalSchool school;

	@JsonBackReference("training")
	@OneToOne(mappedBy = "medicalTraining")
	private MedicalCertificate certificate;

	@Embedded
	private DurationAndStatus durationAndStatus;

	public MedicalTraining() {
		durationAndStatus = new DurationAndStatus();
	}

	public MedicalSchool getMedicalSchool() {
		return school;
	}

	public void setMedicalSchool(MedicalSchool school) {
		this.school = school;
	}

	public MedicalCertificate getCertificate() {
		return certificate;
	}
	
	public void setCertificate(MedicalCertificate certificate) {
		this.certificate = certificate;
	}
	
	public DurationAndStatus getDurationAndStatus() {
		return durationAndStatus;
	}

	public void setDurationAndStatus(DurationAndStatus durationAndStatus) {
		this.durationAndStatus = durationAndStatus;
	}
	
	//Inherited hashCode/equals NOT sufficient for this Entity class
	/**
	 * Very important:  Use getter's for member variables because JPA sometimes needs to intercept those calls<br/>
	 * and go to the database to retrieve the value
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		// Only include member variables that really contribute to an object's identity
		// i.e. if variables like version/updated/name/etc. change throughout an object's lifecycle,
		// they shouldn't be part of the hashCode calculation
		
		// include DurationAndStatus in identity
		return prime * result + Objects.hash(getId(), getDurationAndStatus());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj instanceof MedicalTraining otherMedicalTraining) {
			// See comment (above) in hashCode():  Compare using only member variables that are
			// truly part of an object's identity
			return Objects.equals(this.getId(), otherMedicalTraining.getId()) &&
				Objects.equals(this.getDurationAndStatus(), otherMedicalTraining.getDurationAndStatus());
		}
		return false;
	}
}
