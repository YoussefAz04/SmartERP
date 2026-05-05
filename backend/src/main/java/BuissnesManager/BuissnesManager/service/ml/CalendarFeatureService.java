package BuissnesManager.BuissnesManager.service.ml;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Calendar exogenous regressors for the supervised forecast engine.
 *
 * <p>For SMEs in MENA / EU the dominant explanatory variables for monthly
 * revenue are <i>not</i> autoregressive lags but calendar effects: the number
 * of working days, whether the month overlaps Ramadan, end-of-quarter pushes,
 * year-end clearance, etc. Including them as exogenous columns gives the
 * regression a chance to attribute variance correctly instead of attributing
 * everything to lag-1.
 *
 * <p>All values returned by {@link #features(YearMonth, String)} are
 * deterministic (no DB hits, no randomness) so they can be computed for
 * future months at forecast time without compounding errors.
 *
 * <p>Holiday tables are intentionally minimal — we only encode the dates
 * that materially affect the working-days count. This is a heuristic, not a
 * legal calendar.
 */
@Component
public class CalendarFeatureService {

    /** Default ISO-3166-1 alpha-2 country code if no entreprise locale is set. */
    private static final String DEFAULT_COUNTRY = "TN";

    /** Feature column names emitted in the order returned by {@link #features}. */
    public static final String[] FEATURE_NAMES = {
            "working_days", "is_ramadan", "is_eoq", "is_year_end"
    };

    /** Country code preference, configured by {@code forecast.calendar.country}. */
    @Value("${forecast.calendar.country:TN}")
    private String configuredCountry;

    /**
     * Returns the four calendar features for a given month, in the order
     * advertised by {@link #FEATURE_NAMES}. {@code null} country falls back
     * to the configured default.
     */
    public double[] features(YearMonth ym, String country) {
        String cc = country == null || country.isBlank() ? configuredCountry : country;
        if (cc == null || cc.isBlank()) cc = DEFAULT_COUNTRY;
        cc = cc.toUpperCase(java.util.Locale.ROOT);

        double workingDays = workingDays(ym, cc);
        // Normalise working days around 22 (typical month) so the coefficient
        // scale matches the other 0/1 indicators.
        double wdNorm = (workingDays - 22.0) / 4.0;
        double ramadan = ramadanFraction(ym);
        double eoq = isEndOfQuarter(ym) ? 1.0 : 0.0;
        double yearEnd = ym.getMonthValue() == 12 ? 1.0 : 0.0;
        return new double[]{ wdNorm, ramadan, eoq, yearEnd };
    }

    /** Working-days count for the month: business week minus public holidays. */
    public int workingDays(YearMonth ym, String country) {
        Set<LocalDate> hols = publicHolidays(ym.getYear(), country);
        Set<DayOfWeek> weekend = weekendFor(country);
        int n = 0;
        int last = ym.lengthOfMonth();
        for (int d = 1; d <= last; d++) {
            LocalDate date = ym.atDay(d);
            if (weekend.contains(date.getDayOfWeek())) continue;
            if (hols.contains(date)) continue;
            n++;
        }
        return n;
    }

    /** Fraction of the month overlapping Ramadan (0..1). */
    public double ramadanFraction(YearMonth ym) {
        LocalDate[] window = ramadanWindow(ym.getYear());
        if (window == null) return 0.0;
        // We look at this year and the previous year because Ramadan slides.
        double frac = overlapFraction(ym, window[0], window[1]);
        LocalDate[] prev = ramadanWindow(ym.getYear() - 1);
        if (prev != null) frac += overlapFraction(ym, prev[0], prev[1]);
        return Math.min(1.0, frac);
    }

    /** True if the month is the last of a calendar quarter (Mar/Jun/Sep/Dec). */
    public boolean isEndOfQuarter(YearMonth ym) {
        int m = ym.getMonthValue();
        return m == 3 || m == 6 || m == 9 || m == 12;
    }

    /* ------------------------- Internals ------------------------- */

    private static double overlapFraction(YearMonth ym, LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0.0;
        LocalDate mStart = ym.atDay(1);
        LocalDate mEnd = ym.atEndOfMonth();
        if (end.isBefore(mStart) || start.isAfter(mEnd)) return 0.0;
        LocalDate ovStart = start.isBefore(mStart) ? mStart : start;
        LocalDate ovEnd = end.isAfter(mEnd) ? mEnd : end;
        long days = ovEnd.toEpochDay() - ovStart.toEpochDay() + 1;
        return Math.max(0.0, (double) days / ym.lengthOfMonth());
    }

    private static Set<DayOfWeek> weekendFor(String country) {
        Set<DayOfWeek> w = new HashSet<>();
        // Default Western weekend.
        w.add(DayOfWeek.SATURDAY);
        w.add(DayOfWeek.SUNDAY);
        // Some MENA economies historically used Friday/Saturday — Tunisia and
        // most North-African SMEs we serve are Sat/Sun. Keep the hook for
        // future locales without changing public API.
        return w;
    }

    /**
     * Static table of public holidays for the supported countries. Only
     * fixed-date holidays + a handful of calendar-stable observances are
     * encoded — moveable Islamic feasts are folded into Ramadan.
     */
    private Set<LocalDate> publicHolidays(int year, String country) {
        Set<LocalDate> set = new HashSet<>();
        if ("TN".equals(country)) {
            // Tunisian fixed-date public holidays.
            add(set, year, 1, 1);   // New Year
            add(set, year, 1, 14);  // Revolution Day
            add(set, year, 3, 20);  // Independence Day
            add(set, year, 4, 9);   // Martyrs' Day
            add(set, year, 5, 1);   // Labour Day
            add(set, year, 7, 25);  // Republic Day
            add(set, year, 8, 13);  // Women's Day
            add(set, year, 10, 15); // Evacuation Day
            add(set, year, 12, 17); // Revolution & Youth Day
        } else if ("FR".equals(country)) {
            add(set, year, 1, 1);
            add(set, year, 5, 1);
            add(set, year, 5, 8);
            add(set, year, 7, 14);
            add(set, year, 8, 15);
            add(set, year, 11, 1);
            add(set, year, 11, 11);
            add(set, year, 12, 25);
            add(set, year, 12, 26);
        } else {
            // EU baseline.
            add(set, year, 1, 1);
            add(set, year, 5, 1);
            add(set, year, 12, 25);
            add(set, year, 12, 26);
        }
        return set;
    }

    private static void add(Set<LocalDate> set, int year, int month, int day) {
        try { set.add(LocalDate.of(year, month, day)); }
        catch (RuntimeException ignored) { /* invalid date — skip */ }
    }

    /**
     * Approximate Ramadan window per year (Gregorian). We round to whole days;
     * the model only consumes the monthly overlap fraction, so a ±1-day error
     * is well below the noise floor.
     */
    private static LocalDate[] ramadanWindow(int year) {
        // Hand-curated table for years 2018–2035.
        Map<Integer, int[]> table = RAMADAN;
        int[] startEnd = table.get(year);
        if (startEnd == null) return null;
        try {
            return new LocalDate[]{
                    LocalDate.of(year, startEnd[0], startEnd[1]),
                    LocalDate.of(year, startEnd[2], startEnd[3])
            };
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static final Map<Integer, int[]> RAMADAN = new HashMap<>();
    static {
        // {startMonth, startDay, endMonth, endDay} for the 30-day Ramadan window.
        RAMADAN.put(2018, new int[]{ 5, 16,  6, 14});
        RAMADAN.put(2019, new int[]{ 5,  6,  6,  4});
        RAMADAN.put(2020, new int[]{ 4, 24,  5, 23});
        RAMADAN.put(2021, new int[]{ 4, 13,  5, 12});
        RAMADAN.put(2022, new int[]{ 4,  2,  5,  1});
        RAMADAN.put(2023, new int[]{ 3, 22,  4, 20});
        RAMADAN.put(2024, new int[]{ 3, 11,  4,  9});
        RAMADAN.put(2025, new int[]{ 2, 28,  3, 29});
        RAMADAN.put(2026, new int[]{ 2, 17,  3, 18});
        RAMADAN.put(2027, new int[]{ 2,  7,  3,  8});
        RAMADAN.put(2028, new int[]{ 1, 27,  2, 25});
        RAMADAN.put(2029, new int[]{ 1, 15,  2, 13});
        RAMADAN.put(2030, new int[]{ 1,  5,  2,  3});
        RAMADAN.put(2031, new int[]{12, 25, 12, 31});
        RAMADAN.put(2032, new int[]{ 1, 14,  2, 12});
        RAMADAN.put(2033, new int[]{ 1,  3,  2,  1});
        RAMADAN.put(2034, new int[]{12, 12, 12, 31});
        RAMADAN.put(2035, new int[]{11, 30, 12, 29});
    }

    /** Static glossary used for the methodology panel — keys match {@link #FEATURE_NAMES}. */
    public static Map<String, String> glossary() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("working_days",
                "Nombre de jours ouvrés du mois (hors week-end et jours fériés), centré sur 22.");
        m.put("is_ramadan",
                "Fraction du mois recouvrant le mois de Ramadan (0 à 1).");
        m.put("is_eoq",
                "Indicateur de fin de trimestre (mars/juin/septembre/décembre).");
        m.put("is_year_end",
                "Indicateur de mois de décembre (effet de fin d'année).");
        return m;
    }
}
