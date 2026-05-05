package BuissnesManager.BuissnesManager.entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Canonical lifecycle states for {@link CommandeVente} (and, by mirror,
 * {@code CommandeAchat}). The persistent representation is the legacy
 * {@code String} column; this enum exists to centralise the rules around
 * which states represent <i>realised</i> revenue (vs. cancellations / drafts /
 * returns) so every consumer answers the same way.
 *
 * <p>Forecasting, KPI aggregation and dashboard reporting must all exclude
 * cancelled / draft / return rows or they produce biased numbers. Use
 * {@link #revenueStatutNames()} to pass the canonical list to JPQL queries
 * via {@code IN :validStatuts}.
 */
public enum CommandeStatut {

    /** Draft order — not yet committed by the user, MUST NOT count as revenue. */
    BROUILLON(false),
    /** Awaiting confirmation. Treated as a soft draft, not revenue. */
    EN_ATTENTE(false),
    /** Confirmed — counts as revenue. */
    CONFIRMEE(true),
    /** Delivered — counts as revenue. */
    LIVREE(true),
    /** Invoiced — counts as revenue. */
    FACTUREE(true),
    /** Cancelled — never counts as revenue. */
    ANNULEE(false),
    /** Customer return — net revenue effect handled separately. */
    RETOUR(false);

    private final boolean revenueQualifying;

    CommandeStatut(boolean revenueQualifying) {
        this.revenueQualifying = revenueQualifying;
    }

    /** True if rows in this state should appear in revenue / forecast aggregates. */
    public boolean isRevenueQualifying() {
        return revenueQualifying;
    }

    /**
     * Canonical statut strings that count as revenue, ready for JPQL
     * {@code IN :validStatuts} bindings. Unmodifiable.
     */
    public static List<String> revenueStatutNames() {
        return Collections.unmodifiableList(
                Arrays.stream(values())
                        .filter(CommandeStatut::isRevenueQualifying)
                        .map(Enum::name)
                        .toList());
    }

    /** Canonical statut strings representing customer returns (negative revenue). */
    public static List<String> returnStatutNames() {
        return Collections.singletonList(RETOUR.name());
    }

    /** Lookup that tolerates legacy lower/mixed case writes. */
    public static CommandeStatut fromString(String s) {
        if (s == null) return null;
        try { return CommandeStatut.valueOf(s.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { return null; }
    }
}
