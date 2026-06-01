package BuissnesManager.BuissnesManager.service.ml;

import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.regression.LinearModel;
import smile.regression.OLS;

import java.util.ArrayList;
import java.util.List;


public class SupplierRatingEngine {

    
    public static final class Input {
        public final double orderCount;
        public final double totalSpend;
        public final double deliveryRate;   
        public final double cancelledRatio; 
        public Input(double orderCount, double totalSpend,
                     double deliveryRate, double cancelledRatio) {
            this.orderCount = orderCount;
            this.totalSpend = totalSpend;
            this.deliveryRate = Math.max(0.0, Math.min(1.0, deliveryRate));
            this.cancelledRatio = Math.max(0.0, Math.min(1.0, cancelledRatio));
        }
    }

    
    public static final class Output {
        public final double expectedDeliveryRate;
        public final double ratingStars;    
        public final String modelName;
        public Output(double expected, double stars, String modelName) {
            this.expectedDeliveryRate = expected;
            this.ratingStars = stars;
            this.modelName = modelName;
        }
    }

    
    private static final int MIN_PEER_GROUP = 4;

    public List<Output> rate(List<Input> suppliers) {
        List<Output> out = new ArrayList<>(suppliers.size());
        int n = suppliers.size();
        if (n < MIN_PEER_GROUP) {
            
            for (Input s : suppliers) {
                out.add(new Output(s.deliveryRate, toStars(s.deliveryRate), "Baseline"));
            }
            return out;
        }

        
        double[][] frame = new double[n][4];
        for (int i = 0; i < n; i++) {
            Input s = suppliers.get(i);
            frame[i][0] = s.deliveryRate;                    
            frame[i][1] = Math.log1p(Math.max(0, s.orderCount));
            frame[i][2] = Math.log1p(Math.max(0, s.totalSpend));
            frame[i][3] = s.cancelledRatio;
        }
        DataFrame df = DataFrame.of(frame,
                new String[]{"y", "logOrders", "logSpend", "cancelled"});

        LinearModel model;
        try {
            model = OLS.fit(Formula.lhs("y"), df);
        } catch (RuntimeException ex) {
            for (Input s : suppliers) {
                out.add(new Output(s.deliveryRate, toStars(s.deliveryRate), "Baseline"));
            }
            return out;
        }

        double[] fitted = model.fittedValues();
        for (int i = 0; i < n; i++) {
            Input s = suppliers.get(i);
            double expected = Math.max(0.0, Math.min(1.0, fitted[i]));
            
            double residual = s.deliveryRate - expected;
            
            double score = 0.5 + residual;
            score = Math.max(0.1, Math.min(1.0, score));
            double stars = 1.0 + 4.0 * score;
            stars = Math.round(stars * 10.0) / 10.0;
            out.add(new Output(expected, stars, "OLS peer-group"));
        }
        return out;
    }

    private static double toStars(double deliveryRate) {
        double stars = 1.0 + 4.0 * Math.max(0.0, Math.min(1.0, deliveryRate));
        return Math.round(stars * 10.0) / 10.0;
    }
}
