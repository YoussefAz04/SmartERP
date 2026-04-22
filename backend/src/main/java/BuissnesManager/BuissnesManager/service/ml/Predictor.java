package BuissnesManager.BuissnesManager.service.ml;

import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.formula.Formula;
import smile.data.type.StructType;
import smile.regression.ElasticNet;
import smile.regression.LinearModel;
import smile.regression.OLS;
import smile.regression.RandomForest;
import smile.regression.RidgeRegression;

import java.util.Properties;

/**
 * Uniform predictor façade over Smile's linear and tree-based regression
 * families so {@link MlForecastEngine} can treat them interchangeably in
 * cross-validation and in the recursive forecast loop.
 *
 * <p>Why this exists: Smile 3.1.1 exposes {@code LinearModel}
 * (OLS / Ridge / LASSO / ElasticNet) and {@code RandomForest} as unrelated
 * classes with slightly different predict signatures. The adapters below
 * hide that heterogeneity behind {@link #predict(double[])} / fitted /
 * residuals / r² so the engine only has to reason about a common interface.
 */
public interface Predictor {

    /** Human-readable name of the underlying learner. */
    String name();

    /** One-step-ahead prediction for a single feature vector. */
    double predict(double[] features);

    /** In-sample fitted values, one per training row, aligned with training order. */
    double[] fittedValues();

    /** In-sample residuals (y − ŷ), one per training row. */
    double[] residuals();

    /** Coefficient of determination on the training set, in [−∞, 1]. */
    double rSquared();

    /** Factory: fit an {@link OLS} regression. */
    static Predictor fitOls(DataFrame df, StructType schema) {
        return new LinearAdapter("OLS", OLS.fit(Formula.lhs("y"), df), schema);
    }

    /** Factory: fit a {@link RidgeRegression} with the given L2 strength. */
    static Predictor fitRidge(DataFrame df, StructType schema, double lambda) {
        return new LinearAdapter("Ridge",
                RidgeRegression.fit(Formula.lhs("y"), df, lambda), schema);
    }

    /**
     * Factory: fit an {@link ElasticNet} with L1 and L2 penalties. On small
     * samples ElasticNet blends Ridge's stability and LASSO's feature
     * selection, which is often the sweet spot for engineered lag/seasonal
     * features that are partly correlated.
     */
    static Predictor fitElasticNet(DataFrame df, StructType schema,
                                   double lambda1, double lambda2) {
        return new LinearAdapter("ElasticNet",
                ElasticNet.fit(Formula.lhs("y"), df, lambda1, lambda2), schema);
    }

    /**
     * Factory: fit a {@link RandomForest}. Tree ensembles pick up
     * non-linear interactions the linear family can't, yet stay well-behaved
     * on small samples when the number of trees is modest.
     *
     * @param ntrees    number of trees in the forest
     * @param maxDepth  maximum depth per tree
     * @param mtry      number of features to sample at each split; ≤ 0 lets
     *                  Smile default to sqrt(p)
     * @param seed      RNG seed for reproducibility
     */
    static Predictor fitRandomForest(DataFrame df, StructType schema,
                                     int ntrees, int maxDepth, int mtry, long seed) {
        Properties props = new Properties();
        props.setProperty("smile.random.forest.trees", Integer.toString(ntrees));
        props.setProperty("smile.random.forest.max.depth", Integer.toString(maxDepth));
        props.setProperty("smile.random.forest.node.size", "2");
        props.setProperty("smile.random.forest.sampling.rate", "0.9");
        if (mtry > 0) {
            props.setProperty("smile.random.forest.mtry", Integer.toString(mtry));
        }
        // Seed Smile's shared RNG so bagging is reproducible.
        smile.math.MathEx.setSeed(seed);
        RandomForest rf = RandomForest.fit(Formula.lhs("y"), df, props);
        return new RandomForestAdapter(rf, df, schema);
    }

    /** Adapter for any Smile {@link LinearModel} (OLS / Ridge / LASSO / ElasticNet). */
    final class LinearAdapter implements Predictor {
        private final String name;
        private final LinearModel model;
        private final StructType schema;
        LinearAdapter(String name, LinearModel model, StructType schema) {
            this.name = name;
            this.model = model;
            this.schema = schema;
        }
        @Override public String name() { return name; }
        @Override public double predict(double[] features) { return model.predict(features); }
        @Override public double[] fittedValues() { return model.fittedValues(); }
        @Override public double[] residuals() { return model.residuals(); }
        @Override public double rSquared() { return model.RSquared(); }
        @SuppressWarnings("unused") public StructType schema() { return schema; }
    }

    /** Adapter for Smile's {@link RandomForest}. */
    final class RandomForestAdapter implements Predictor {
        private final RandomForest rf;
        private final StructType schema;
        private final double[] fitted;
        private final double[] residuals;
        private final double rSquared;

        RandomForestAdapter(RandomForest rf, DataFrame df, StructType schema) {
            this.rf = rf;
            this.schema = schema;
            int n = df.size();
            this.fitted = new double[n];
            this.residuals = new double[n];
            // Random Forest doesn't expose fittedValues()/residuals() like LinearModel,
            // so we compute them from the training frame. For our use case (small
            // n ≤ ~30) this is cheap enough to run eagerly.
            double[] y = new double[n];
            double ym = 0.0;
            for (int i = 0; i < n; i++) {
                Tuple t = df.get(i);
                y[i] = t.getDouble("y");
                fitted[i] = rf.predict(t);
                residuals[i] = y[i] - fitted[i];
                ym += y[i];
            }
            ym /= Math.max(1, n);
            double ssRes = 0.0, ssTot = 0.0;
            for (int i = 0; i < n; i++) {
                ssRes += residuals[i] * residuals[i];
                ssTot += (y[i] - ym) * (y[i] - ym);
            }
            this.rSquared = ssTot > 0 ? 1.0 - ssRes / ssTot : 0.0;
        }

        @Override public String name() { return "RandomForest"; }
        @Override public double predict(double[] features) {
            // RandomForest.predict takes a Tuple — build one on the fly.
            double[] row = new double[schema.length()];
            // Column 0 of the training frame is always the target "y"; features
            // live in columns 1..p. Pad index 0 with 0.0 — the forest was
            // trained with "y" as the label via Formula.lhs("y") so it will
            // never inspect that column at predict time.
            System.arraycopy(features, 0, row, 1, Math.min(features.length, row.length - 1));
            return rf.predict(Tuple.of(row, schema));
        }
        @Override public double[] fittedValues() { return fitted; }
        @Override public double[] residuals() { return residuals; }
        @Override public double rSquared() { return rSquared; }
    }
}
