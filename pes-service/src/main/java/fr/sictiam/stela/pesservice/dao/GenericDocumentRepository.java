package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.GenericDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenericDocumentRepository extends JpaRepository<GenericDocument, Integer> {

}
