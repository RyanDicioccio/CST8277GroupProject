/********************************************************************************************************
 * File:  PojoListener.java Course Materials CST 8277
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

import java.time.LocalDateTime;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@SuppressWarnings("unused")

public class PojoListener {

	@PrePersist
	public void setCreatedOnDate(PojoBase pojoBase) {
		LocalDateTime now = LocalDateTime.now();
		pojoBase.setCreated(now);
	    pojoBase.setUpdated(now);
	}

	@PreUpdate
	public void setUpdatedDate(PojoBase pojoBase) {
		 LocalDateTime now = LocalDateTime.now();
		 pojoBase.setUpdated(now);
	}

}
