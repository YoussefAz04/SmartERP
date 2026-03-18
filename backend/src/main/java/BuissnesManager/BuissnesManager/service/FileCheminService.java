package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.FileChemin;
import BuissnesManager.BuissnesManager.repository.FileCheminRepo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileCheminService {

    @Autowired
    private FileCheminRepo fileCheminRepo ;

    public FileChemin addFileChemin(FileChemin fileChemin){
        return fileCheminRepo.save(fileChemin);
    }
}
