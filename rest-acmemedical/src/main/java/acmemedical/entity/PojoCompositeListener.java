/********************************************************************************************************
 * File:  PojoCompositeListener.java Course Materials CST 8277
 * Last Updated: 2024-12-02
 * 
 * @author Teddy Yap
 * @author Dan Blais
 * @author Imed Cherabi
 * @author Ryan Di Cioccio
 * @author Aaron Renshaw
 * 
 */ acmemedical.entity;

import java.time.LocalDateTime;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@SuppressWarnings("unused")

public class PojoCompositeListener {

	// TODO PCL01 - What annotation is used when we want to do something just before object is INSERT'd into database?
	@PrePersist
	public void setCreatedOnDate(PojoBaseCompositeKey<?> pojoBaseComposite) {
		LocalDateTime now = LocalDateTime.now();
		// TODO PCL02 - What member field(s) do we wish to alter just before object is INSERT'd in the database?
		pojoBaseComposite.setCreatedDate(now);
        pojoBaseComposite.setUpdatedDate(now);
	}

	// TODO PCL03 - What annotation is used when we want to do something just before object is UPDATE'd into database?
	 @PreUpdate
	public void setUpdatedDate(PojoBaseCompositeKey<?> pojoBaseComposite) {
		 LocalDateTime now = LocalDateTime.now();
	        // PCL04 - Altering 'updatedDate' before UPDATE
	        pojoBaseComposite.setUpdatedDate(now);
	}

}
