package BuissnesManager.BuissnesManager.dto;

public class CategorieClientDto {

    private Long id;
    private String nom;
    private String description;
    private long clientCount;

    public CategorieClientDto() {}

    public CategorieClientDto(Long id, String nom, String description, long clientCount) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.clientCount = clientCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getClientCount() { return clientCount; }
    public void setClientCount(long clientCount) { this.clientCount = clientCount; }
}
