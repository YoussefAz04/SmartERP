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


public interface Predictor {

    
    String name();

    
    double predict(double[] features);

    
    double[] fittedValues();

    
    double[] residuals();

    
    double rSquared();

    











    default double[] rawImportances() { return null; }

    
    default boolean importancesSigned() { return false; }

    
    static Predictor fitOls(DataFrame df, StructType schema) {
        return new LinearAdapter("OLS", OLS.fit(Formula.lhs("y"), df), schema);
    }

    
    static Predictor fitRidge(DataFrame df, StructType schema, double lambda) {
        return new LinearAdapter("Ridge",
                RidgeRegression.fit(Formula.lhs("y"), df, lambda), schema);
    }

 
    
    static Predictor fitElasticNet(DataFrame df, StructType schema,
                                   double lambda1, double lambda2) {
        return new LinearAdapter("ElasticNet",
                ElasticNet.fit(Formula.lhs("y"), df, lambda1, lambda2), schema);
    }

    
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
        
        smile.math.MathEx.setSeed(seed);
        RandomForest rf = RandomForest.fit(Formula.lhs("y"), df, props);
        return new RandomForestAdapter(rf, df, schema);
    }

    
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
        @Override public double[] rawImportances() {
            
            
            try {
                double[] all = model.coefficients();
                if (all == null || all.length <= 1) return null;
                double[] beta = new double[all.length - 1];
                System.arraycopy(all, 1, beta, 0, beta.length);
                return beta;
            } catch (RuntimeException ex) {
                return null;
            }
        }
        @Override public boolean importancesSigned() { return true; }
        @SuppressWarnings("unused") public StructType schema() { return schema; }
    }

    
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
            
            double[] row = new double[schema.length()];
            
            
            System.arraycopy(features, 0, row, 1, Math.min(features.length, row.length - 1));
            return rf.predict(Tuple.of(row, schema));
        }
        @Override public double[] fittedValues() { return fitted; }
        @Override public double[] residuals() { return residuals; }
        @Override public double rSquared() { return rSquared; }
        @Override public double[] rawImportances() {
            try {
                double[] imp = rf.importance();
                return imp == null ? null : imp.clone();
            } catch (RuntimeException ex) {
                return null;
            }
        }
        @Override public boolean importancesSigned() { return false; }
    }
}
