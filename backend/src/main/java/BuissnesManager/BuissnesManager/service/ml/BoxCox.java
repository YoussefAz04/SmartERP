package BuissnesManager.BuissnesManager.service.ml;


public final class BoxCox {


    static final double[] LAMBDA_GRID;
    static {
        double[] g = new double[21];
        for (int i = 0; i < g.length; i++) g[i] = -1.0 + i * 0.1;
        LAMBDA_GRID = g;
    }

    
    private static final double SHIFT_EPS = 1.0;

    
    public static final class Fit {
        public final double lambda;
        public final double shift;
        Fit(double lambda, double shift) {
            this.lambda = lambda;
            this.shift = shift;
        }
        
        public double forward(double y) { return BoxCox.forward(y, lambda, shift); }
   
        public double inverse(double z) { return Math.max(0.0, BoxCox.inverse(z, lambda, shift)); }
    }

    private BoxCox() {}

   
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

    
    static double forward(double y, double lambda, double shift) {
        double v = y + shift;
        if (!(v > 0)) v = SHIFT_EPS;
        return forwardRaw(v, lambda);
    }

    private static double forwardRaw(double v, double lambda) {
        if (Math.abs(lambda) < 1e-9) return Math.log(v);
        return (Math.pow(v, lambda) - 1.0) / lambda;
    }

    
    static double inverse(double z, double lambda, double shift) {
        double y;
        if (Math.abs(lambda) < 1e-9) {
            y = Math.exp(z);
        } else {
            double base = lambda * z + 1.0;
            if (base <= 0) {
                
                y = SHIFT_EPS;
            } else {
                y = Math.pow(base, 1.0 / lambda);
            }
        }
        return y - shift;
    }
}
