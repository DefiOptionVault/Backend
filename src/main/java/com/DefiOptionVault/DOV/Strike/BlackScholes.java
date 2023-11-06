package com.DefiOptionVault.DOV.Strike;

import java.math.BigDecimal;
import java.math.MathContext;
import org.apache.commons.math3.special.Erf;

public class BlackScholes {

    private static final MathContext MC = MathContext.DECIMAL64;

    public static BigDecimal sqrt(BigDecimal value) {
        return BigDecimal.valueOf(Math.sqrt(value.doubleValue()));
    }

    public static BigDecimal exp(BigDecimal value) {
        return BigDecimal.valueOf(Math.exp(value.doubleValue()));
    }

    public static BigDecimal ln(BigDecimal value) {
        return BigDecimal.valueOf(Math.log(value.doubleValue()));
    }

    public static BigDecimal d1(
            BigDecimal S, BigDecimal K, BigDecimal T, BigDecimal r, BigDecimal sigma) {
        return (ln(S.divide(K, MC))
                .add(r.add(sigma.multiply(sigma)
                        .multiply(BigDecimal.valueOf(0.5), MC))
                        .multiply(T)))
                .divide(sigma.multiply(sqrt(T), MC), MC);
    }

    public static BigDecimal d2(
            BigDecimal S, BigDecimal K, BigDecimal T, BigDecimal r, BigDecimal sigma) {
        return d1(S, K, T, r, sigma)
                .subtract(sigma.multiply(sqrt(T), MC));
    }

    public static BigDecimal normalDistribution(BigDecimal x) {
        double value = 0.5 * (1.0 + Erf.erf(x.divide(sqrt(BigDecimal.valueOf(2.0)), MC).doubleValue()));
        return BigDecimal.valueOf(value);
    }

    public static BigDecimal callOptionPrice(
            BigDecimal S, BigDecimal K, BigDecimal T, BigDecimal r, BigDecimal sigma) {
        return S.multiply(normalDistribution(d1(S, K, T, r, sigma)))
                .subtract(K.multiply(exp(r.negate().multiply(T)))
                        .multiply(normalDistribution(d2(S, K, T, r, sigma))));
    }

    public static BigDecimal putOptionPrice(
            BigDecimal S, BigDecimal K, BigDecimal T, BigDecimal r, BigDecimal sigma) {
        return K.multiply(exp(r.negate().multiply(T)))
                .multiply(normalDistribution(d2(S, K, T, r, sigma).negate()))
                .subtract(S.multiply(normalDistribution(d1(S, K, T, r, sigma).negate())));
    }
}
