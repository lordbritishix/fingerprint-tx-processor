package org.lordbritishix.processor.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lordbritishix.processor.model.Transaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class TransactionProcessorTest {
    private TransactionProcessor processor;
    private static final CsvMapper CSV_MAPPER = new CsvMapper();
    private static List<Transaction> TEST_TRANSACTIONS = new ArrayList<>();

    public static Stream<Arguments> testData() {
        return Stream.of(
            Arguments.of("us", 0L, new BigDecimal("0")),
            Arguments.of("us", 50L, new BigDecimal("4139.43")),
            Arguments.of("us", 60L, new BigDecimal("1915.37")),
            Arguments.of("us", 90L, new BigDecimal("3474.55")),
            Arguments.of("us", 1000L, new BigDecimal("5763.62"))
        );
    }

    @BeforeEach
    public void setup() {
        processor = new TransactionProcessor();
    }

    @BeforeAll
    public static void initialize() {
        TEST_TRANSACTIONS = loadTestTransactions();
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void prioritizeReturnsCorrectPriority(String countryCode,
                                                 long timeInMillis,
                                                 BigDecimal expectedMaxAmount) {
        List<Transaction> transactions = processor.prioritize(TEST_TRANSACTIONS, timeInMillis);

        BigDecimal amountProcessedForCountry = transactions.stream()
                .filter(tx -> tx.getBankCountryCode().equals(countryCode))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal amountProcessed = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Amount processed for target time={}ms countryCode={} amount={} (took {}ms)",
                timeInMillis,
                countryCode,
                amountProcessedForCountry.toPlainString(),
                transactions.stream()
                        .filter(p -> p.getBankCountryCode().equals(countryCode))
                        .map(p -> TransactionProcessor.getHistoricalLatency(p.getBankCountryCode()))
                        .reduce(0, Integer::sum));
        log.info("Amount processed for target time={}ms countryCode={} amount={} (took {}ms)",
                timeInMillis,
                "all",
                amountProcessed,
                transactions.stream()
                        .map(p -> TransactionProcessor.getHistoricalLatency(p.getBankCountryCode()))
                        .reduce(0, Integer::sum));

        assertEquals(0, expectedMaxAmount.compareTo(amountProcessedForCountry));
    }

    private static List<Transaction> loadTestTransactions() {
        CsvSchema schema = CSV_MAPPER.schemaFor(Transaction.class);
        try {
            MappingIterator<Transaction> iterator = CSV_MAPPER
                    .readerFor(Transaction.class)
                    .with(schema)
                    .readValues(Resources.getResource("transactions.csv"));

            return iterator.readAll();
        } catch (IOException e) {
            throw new RuntimeException("Unable to read test transactions", e);
        }
    }


}