package BuissnesManager.BuissnesManager.service.ml;

/**
 * Box-Cox power transform used to stabilise the variance of revenue series
 * before regression.
 *
 * <p>SME revenue is multiplicative: a 10% drop in November looks small in
 * absolute terms but huge in log-space. Fitting OLS/Ridge directly on the
 * raw scale gives equal weight to a 1 000 TND error in February and a
 * 100 000 TND error in December — that's wrong. The Box-Cox family includes
 * the log (λ=0), square-root (λ=0.5), and identity (λ=1) transforms; we pick
 * the λ that maximises the profile log-likelihood on the training data.
 *
 * <p>All values are shifted by a positive constant so the transform stays
 * defined when the series contains zeros. The shift is captured in the
 * returned {@link Fit} so the inverse can undo it exactly.
 */
public final class BoxCox {

    /** Search grid for λ — fine enough for stable selection on short series. */
    static final double[] LAMBDA_GRID;
    static {
        double[] g = new double[21];
        for (int i = 0; i < g.length; i++) g[i] = -1.0 + i * 0.1;
        LAMBDA_GRID = g;
    }

    /** Tiny floor added to every value so log(0) is never evaluated. */
    private static final double SHIFT_EPS = 1.0;

    /** Fitted Box-Cox parameters captured at training time. */
    public static final class Fit {
        public final double lambda;
        public final double shift;
        Fit(double lambda, double shift) {
            this.lambda = lambda;
            this.shift = shift;
        }
        /** Forward transform: y → z. */
        public double forward(double y) { return BoxCox.forward(y, lambda, shift); }
        /** Inverse transform: z → y. Clamped at zero (sales never negative). */
        public double inverse(double z) { return Math.max(0.0, BoxCox.inverse(z, lambda, shift)); }
    }

    private BoxCox() {}

    /**
     * Picks λ ∈ {@link #LAMBDA_GRID} that maximises the profile log-likelihood
     * of a Box-Cox transform of {@code y}. Falls back to identity (λ=1) when
     * the series is too short / degenerate.
     */
    public static Fit fit(double[] y) {
        if (y == null || y.length < 4) return new Fit(1.0, 0.0);
        double minY = Double.POSITIVE_INFINITY;
        for (double v : y) if (v < minY) minY = v;
        double shift = minY <= 0 ? (-minY + SHIFT_EPS) : 0.0;

        double bestLambda = 1.0;
        double bestLL = Double.NEGATIVE_INFINITY;
        for (double lambda : LAMBDA_GRID) {
            double ll = profileLogLikelihood(y, lambda, shift);
            if (Double.isFinite(ll) && ll > bestLL) {
                bestLL = ll;
                bestLambda = lambda;
            }
        }
        return new Fit(bestLambda, shift);
    }

    /**
     * Box-Cox profile log-likelihood (ignoring constants), as a function of λ:
     * <pre>
     *   L(λ) = -n/2 · ln(σ̂²(λ)) + (λ−1) · Σ ln(y_i + shift)
     * </pre>
     * where σ̂²(λ) is the variance of the transformed series.
     */
    static double profileLogLikelihood(double[] y, double lambda, double shift) {
        int n = y.length;
        double[] z = new double[n];
        double sumLnY = 0.0;
        for (int i = 0; i < n; i++) {
            double v = y[i] + shift;
            if (!(v > 0)) return Double.NEGATIVE_INFINITY;
            z[i] = forwardRaw(v, lambda);
            sumLnY += Math.log(v);
        }
        double mean = 0.0;
        for (double v : z) mean += v;
        mean /= n;
        double sigma2 = 0.0;
        for (double v : z) {
            double d = v - mean;
            sigma2 += d * d;
        }
        sigma2 /= n;
        if (!(sigma2 > 0)) return Double.NEGATIVE_INFINITY;
        return -0.5 * n * Math.log(sigma2) + (lambda - 1.0) * sumLnY;
    }

    /** Forward transform y &gt; -shift → z, with shift already applied. */
    static double forward(double y, double lambda, double shift) {
        double v = y + shift;
        if (!(v > 0)) v = SHIFT_EPS;
        return forwardRaw(v, lambda);
    }

    private static double forwardRaw(double v, double lambda) {
        if (Math.abs(lambda) < 1e-9) return Math.log(v);
        return (Math.pow(v, lambda) - 1.0) / lambda;
    }

    /** Inverse transform z → y (un-shifted). May return values below {@code -shift}. */
    static double inverse(double z, double lambda, double shift) {
        double y;
        if (Math.abs(lambda) < 1e-9) {
            y = Math.exp(z);
        } else {
            double base = lambda * z + 1.0;
            if (base <= 0) {
                // Out-of-domain extrapolation — clamp to a tiny positive value
                // so we don't propagate NaN into the forecast bounds.
                y = SHIFT_EPS;
            } else {
                y = Math.pow(base, 1.0 / lambda);
            }
        }
        return y - shift;
    }
}
