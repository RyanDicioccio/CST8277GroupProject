/********************************************************************************************************
 * File:  PublicSchool.java Course Materials CST 8277
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

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("1")
@JsonTypeName("PublicSchool")
public class PublicSchool extends MedicalSchool implements Serializable {
	private static final long serialVersionUID = 1L;

	public PublicSchool() {
		super(true);
	}
}
