package com.sictiam.flux_pes.repository;
import java.awt.im.InputSubset;
import java.util.List;

import com.sictiam.flux_pes.model.PesObjet;
import com.sun.mail.imap.protocol.ID;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Id;

/**
 * Created by s.vergon on 24/03/2017.
 */
public interface PesObjRepositery extends CrudRepository<PesObjet,Long> {

}
