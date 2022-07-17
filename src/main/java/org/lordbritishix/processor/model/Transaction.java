package org.lordbritishix.processor.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({ "id", "amount", "bankCountryCode" })
public class Transaction {
    String id;
    BigDecimal amount;
    String bankCountryCode;
}
