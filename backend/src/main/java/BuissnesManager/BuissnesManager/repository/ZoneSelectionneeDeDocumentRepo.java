package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.ZoneSelectionneeDeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZoneSelectionneeDeDocumentRepo extends JpaRepository<ZoneSelectionneeDeDocument,Long> {


    List<ZoneSelectionneeDeDocument> findAllByTypeDeDocument(String typeDeDocument);
}
