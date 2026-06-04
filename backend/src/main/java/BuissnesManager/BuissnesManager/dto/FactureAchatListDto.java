package BuissnesManager.BuissnesManager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FactureAchatListDto {

    private Long id;
    private String dateAchat;
    private String referance;
    private Double montantTotal;
    private Double montantHT;
    private Double montantTVA;
    private Double montantTTC;
    private Double remise;
    private String modePaiement;
    private String statut;
    private String description;
    private String cheminFichier;
    private FournisseurSummary fournisseur;
    private CommandeAchatSummary commandeAchat;

    public FactureAchatListDto(
            Long id,
            String dateAchat,
            String referance,
            Double montantTotal,
            Double montantHT,
            Double montantTVA,
            Double montantTTC,
            Double remise,
            String modePaiement,
            String statut,
            String description,
            String cheminFichier,
            Long fournisseurId,
            String fournisseurNom,
            String fournisseurPrenom,
            String fournisseurEmail,
            String fournisseurTelephone,
            String fournisseurAdresse,
            String fournisseurVille,
            String fournisseurPays,
            String fournisseurNumeroIdentificationFiscale,
            String fournisseurRegistreCommerce,
            Long commandeAchatId,
            String commandeAchatReference
    ) {
        this.id = id;
        this.dateAchat = dateAchat;
        this.referance = referance;
        this.montantTotal = montantTotal;
        this.montantHT = montantHT;
        this.montantTVA = montantTVA;
        this.montantTTC = montantTTC;
        this.remise = remise;
        this.modePaiement = modePaiement;
        this.statut = statut;
        this.description = description;
        this.cheminFichier = cheminFichier;

        if (fournisseurId != null) {
            this.fournisseur = new FournisseurSummary(
                    fournisseurId,
                    fournisseurNom,
                    fournisseurPrenom,
                    fournisseurEmail,
                    fournisseurTelephone,
                    fournisseurAdresse,
                    fournisseurVille,
                    fournisseurPays,
                    fournisseurNumeroIdentificationFiscale,
                    fournisseurRegistreCommerce
            );
        }

        if (commandeAchatId != null) {
            this.commandeAchat = new CommandeAchatSummary(commandeAchatId, commandeAchatReference);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FournisseurSummary {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
        private String adresse;
        private String ville;
        private String pays;
        private String numeroIdentificationFiscale;
        private String registreCommerce;

        public FournisseurSummary(
                Long id,
                String nom,
                String prenom,
                String email,
                String telephone,
                String adresse,
                String ville,
                String pays,
                String numeroIdentificationFiscale,
                String registreCommerce
        ) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
            this.telephone = telephone;
            this.adresse = adresse;
            this.ville = ville;
            this.pays = pays;
            this.numeroIdentificationFiscale = numeroIdentificationFiscale;
            this.registreCommerce = registreCommerce;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CommandeAchatSummary {
        private Long id;
        private String reference;

        public CommandeAchatSummary(Long id, String reference) {
            this.id = id;
            this.reference = reference;
        }
    }
}
