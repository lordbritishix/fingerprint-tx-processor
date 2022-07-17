package org.lordbritishix.processor.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.lordbritishix.processor.model.Transaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class TransactionProcessor {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, Integer> LATENCIES = readHistoricalLatencies();

    /**
     * Returns the list of transactions that can be completed within the provided maxDurationInMillis while prioritizing
     * the transactions that have the greater amount.
     */
    public List<Transaction> prioritize(List<Transaction> transactions, long maxDurationInMillis) {
        // Setup the lookup table that is used to store previous results
        BigDecimal[][] table = new BigDecimal[transactions.size() + 1][(int) maxDurationInMillis + 1];

        Integer[] latencies = transactions.stream().map(p -> LATENCIES.get(p.getBankCountryCode())).toArray(Integer[]::new);
        BigDecimal[] values = transactions.stream().map(Transaction::getAmount).toArray(BigDecimal[]::new);

        // Compute the max $ that can be attained given the duration constraint using dynamic programming
        int i, j;
        for (i = 0; i <= transactions.size(); i++) {
            for (j = 0; j <= maxDurationInMillis; j++) {
                if (i == 0 || j == 0) {
                    table[i][j] = BigDecimal.ZERO;
                } else if (latencies[i - 1] <= j) {
                    table[i][j] = max(
                            values[i - 1].add(table[i - 1][j - latencies[i - 1]]),
                            table[i - 1][j]);
                } else {
                    table[i][j] = table[i - 1][j];
                }
            }
        }

        BigDecimal result = table[transactions.size()][(int) maxDurationInMillis];

        // Fetch the transactions that contributed to the max $ using back tracking
        int ctr = (int) maxDurationInMillis;
        List<Transaction> prioritizedTransactions = new ArrayList<>();
        for (i = transactions.size(); i > 0 && result.compareTo(BigDecimal.ZERO) > 0; i--) {
            if (result.compareTo(table[i - 1][ctr]) != 0) {
                prioritizedTransactions.add(transactions.get(i - 1));
                result = result.subtract(values[i - 1]);
                ctr = ctr - latencies[i - 1];
            }
        }

        return prioritizedTransactions;
    }

    public static int getHistoricalLatency(String countryCode) {
        return LATENCIES.get(countryCode);
    }

    private BigDecimal max(BigDecimal left, BigDecimal right) {
        if (left.compareTo(right) > 0) {
            return left;
        } else {
            return right;
        }
    }

    private static Map<String, Integer> readHistoricalLatencies() {
        try {
            return MAPPER.readValue(
                    Resources.getResource("latencies.json"),
                    new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Unable to fetch historical latencies", e);
        }
    }
}
