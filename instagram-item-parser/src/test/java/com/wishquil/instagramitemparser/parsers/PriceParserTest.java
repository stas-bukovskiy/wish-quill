package com.wishquil.instagramitemparser.parsers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PriceParserTest {

    public static Stream<Arguments> validInputToParseProvider() {
        return Stream.of(
                Arguments.of("For 6 mm ring price is 350.99 grn", BigDecimal.valueOf(350.99)),
                Arguments.of("The price is $20", BigDecimal.valueOf(20)),
                Arguments.of("Price: 15 EUR", BigDecimal.valueOf(15)),
                Arguments.of("The cost is 10.5 GBP", BigDecimal.valueOf(10.5)),
                Arguments.of("The amount is 100,000 UAH", BigDecimal.valueOf(100_000)),
                Arguments.of("The price is 123 grn", BigDecimal.valueOf(123)),
                Arguments.of("Ціна за товар: 0.99 грн", BigDecimal.valueOf(0.99)),
                Arguments.of("Ціна в грн 100", BigDecimal.valueOf(100)),
                Arguments.of("There are not any price", BigDecimal.valueOf(0.00).setScale(2, RoundingMode.HALF_UP)),
                Arguments.of("There are some number: 123, but price is 650 uah", BigDecimal.valueOf(650))
        );
    }

    @ParameterizedTest
    @MethodSource("validInputToParseProvider")
    void testParsePriceWithValidInput_shouldParseSuccessfully(String input, BigDecimal expected) {
        BigDecimal res = PriceParser.parsePrice(input);
        assertThat(res).isEqualTo(expected);
    }
}