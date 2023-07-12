package com.wishquil.instagramitemparser.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PriceParser {

    private static final String CURRENCY_PATTERN = "(USD|EUR|GBP|UAH|GRN|ГРН|грн|¥|₩|₹|₴)";
    private static final String NUMBER_PATTERN = "(\\d{1,3}(,\\d{3})*(\\.\\d+)?)";

    private PriceParser() {
    }


    public static BigDecimal parsePrice(String s) {
        Pattern rightPositionedCurrencyPattern = Pattern.compile(String.format("%s( )?%s", NUMBER_PATTERN, CURRENCY_PATTERN), Pattern.CASE_INSENSITIVE);
        Pattern leftPositionedCurrencyPattern = Pattern.compile(String.format("%s( )?%s", CURRENCY_PATTERN, NUMBER_PATTERN), Pattern.CASE_INSENSITIVE);

        BigDecimal res = parsePriceWithPatter(rightPositionedCurrencyPattern, s);
        if (res != null)
            return res;
        res = parsePriceWithPatter(leftPositionedCurrencyPattern, s);
        if (res != null)
            return res;

        // If no indicator is found, return any number from the text
        Pattern numberPattern = Pattern.compile(NUMBER_PATTERN);
        Matcher numberMatcher = numberPattern.matcher(s);

        if (numberMatcher.find()) {
            String priceString = numberMatcher.group(0);
            return new BigDecimal(priceString.replaceAll("[^\\d.]", ""));
        }

        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal parsePriceWithPatter(Pattern pattern, String s) {
        Matcher matcher = pattern.matcher(s);

        if (matcher.find()) {
            String priceString = matcher.group(0);
            return new BigDecimal(priceString.replaceAll("[^\\d.]", ""));
        }
        return null;
    }

}
