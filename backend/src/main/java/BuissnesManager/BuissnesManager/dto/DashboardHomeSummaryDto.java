package BuissnesManager.BuissnesManager.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Lightweight payload for the home/admin dashboard.
 *
 * The dashboard only needs four counters and the five most-recent rows of each
 * resource. Returning this in a single call (with slim DTOs and per-enterprise
 * server-side aggregation) avoids the previous behaviour of fetching ALL
 * clients / fournisseurs / articles / stocks / commandes and shipping their full
 * entity graphs to the browser just so it could call {@code .length} and slice
 * the top 5.
 */
public class DashboardHomeSummaryDto {

    private long totalClients;
    private long totalFournisseurs;
    private long totalArticles;
    private long totalCommandes;

    private List<RecentClientDto> recentClients;
    private List<RecentFournisseurDto> recentFournisseurs;
    private List<RecentStockDto> recentStocks;
    private List<RecentCommandeDto> recentCommandes;

    public long getTotalClients() { return totalClients; }
    public void setTotalClients(long totalClients) { this.totalClients = totalClients; }

    public long getTotalFournisseurs() { return totalFournisseurs; }
    public void setTotalFournisseurs(long totalFournisseurs) { this.totalFournisseurs = totalFournisseurs; }

    public long getTotalArticles() { return totalArticles; }
    public void setTotalArticles(long totalArticles) { this.totalArticles = totalArticles; }

    public long getTotalCommandes() { return totalCommandes; }
    public void setTotalCommandes(long totalCommandes) { this.totalCommandes = totalCommandes; }

    public List<RecentClientDto> getRecentClients() { return recentClients; }
    public void setRecentClients(List<RecentClientDto> recentClients) { this.recentClients = recentClients; }

    public List<RecentFournisseurDto> getRecentFournisseurs() { return recentFournisseurs; }
    public void setRecentFournisseurs(List<RecentFournisseurDto> recentFournisseurs) { this.recentFournisseurs = recentFournisseurs; }

    public List<RecentStockDto> getRecentStocks() { return recentStocks; }
    public void setRecentStocks(List<RecentStockDto> recentStocks) { this.recentStocks = recentStocks; }

    public List<RecentCommandeDto> getRecentCommandes() { return recentCommandes; }
    public void setRecentCommandes(List<RecentCommandeDto> recentCommandes) { this.recentCommandes = recentCommandes; }

    public static class RecentClientDto {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String telephone;

        public RecentClientDto() {}
        public RecentClientDto(Long id, String nom, String prenom, String email, String telephone) {
            this.id = id; this.nom = nom; this.prenom = prenom; this.email = email; this.telephone = telephone;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getTelephone() { return telephone; }
        public void setTelephone(String telephone) { this.telephone = telephone; }
    }

    public static class RecentFournisseurDto {
        private Long id;
        private String nom;
        private String telephone;
        private String ville;

        public RecentFournisseurDto() {}
        public RecentFournisseurDto(Long id, String nom, String telephone, String ville) {
            this.id = id; this.nom = nom; this.telephone = telephone; this.ville = ville;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getTelephone() { return telephone; }
        public void setTelephone(String telephone) { this.telephone = telephone; }
        public String getVille() { return ville; }
        public void setVille(String ville) { this.ville = ville; }
    }

    public static class RecentStockDto {
        private Long id;
        private Long articleAchatId;
        private String articleNom;
        private Double quantiteDisponible;

        public RecentStockDto() {}
        public RecentStockDto(Long id, Long articleAchatId, String articleNom, Double quantiteDisponible) {
            this.id = id; this.articleAchatId = articleAchatId; this.articleNom = articleNom; this.quantiteDisponible = quantiteDisponible;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getArticleAchatId() { return articleAchatId; }
        public void setArticleAchatId(Long articleAchatId) { this.articleAchatId = articleAchatId; }
        public String getArticleNom() { return articleNom; }
        public void setArticleNom(String articleNom) { this.articleNom = articleNom; }
        public Double getQuantiteDisponible() { return quantiteDisponible; }
        public void setQuantiteDisponible(Double quantiteDisponible) { this.quantiteDisponible = quantiteDisponible; }
    }

    public static class RecentCommandeDto {
        private Long id;
        private LocalDate dateCommande;
        private Double montantTTC;
        private String statut;
        private String clientNom;

        public RecentCommandeDto() {}
        public RecentCommandeDto(Long id, LocalDate dateCommande, Double montantTTC, String statut, String clientNom) {
            this.id = id; this.dateCommande = dateCommande; this.montantTTC = montantTTC; this.statut = statut; this.clientNom = clientNom;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public LocalDate getDateCommande() { return dateCommande; }
        public void setDateCommande(LocalDate dateCommande) { this.dateCommande = dateCommande; }
        public Double getMontantTTC() { return montantTTC; }
        public void setMontantTTC(Double montantTTC) { this.montantTTC = montantTTC; }
        public String getStatut() { return statut; }
        public void setStatut(String statut) { this.statut = statut; }
        public String getClientNom() { return clientNom; }
        public void setClientNom(String clientNom) { this.clientNom = clientNom; }
    }
}
