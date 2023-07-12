package com.wishquil.instagramitemparser.services;

import com.wishquil.instagramitemparser.exceptions.ItemParsingException;
import com.wishquil.instagramitemparser.models.Item;
import com.wishquil.instagramitemparser.parsers.InstagramPostParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstagramPostService implements ItemService {

    private final InstagramPostParser parser;
    private final WebDriver webDriver;
    @Value("${item-service.timeout}")
    private Duration timeout = Duration.of(7, ChronoUnit.SECONDS);
    @Value("${item-service.attempts}")
    private int attempts = 3;

    @Override
    public Item getItemFromWebPage(String url) {
        webDriver.get(url);
        try {
            return getItemFromWebPageWithTimeout(url);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Item getItemFromWebPageWithTimeout(String url) throws InterruptedException {
        List<ItemParsingException> exceptions = new LinkedList<>();
        for (int i = attempts; i > 0; i--) {
            try {
                return parser.parse(webDriver.getPageSource(), url);
            } catch (ItemParsingException e) {
                exceptions.add(e);
                log.warn("Parsing error occurred: <{}>; Attempts left: {}", e.getMessage(), i);
                Thread.sleep(timeout.toMillis());
            }
        }
        return throwDetailException(exceptions);
    }

    private Item throwDetailException(List<ItemParsingException> exceptions) throws ItemParsingException {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(attempts).append(" attempts were made:\n");
        for (int i = 0; i < exceptions.size(); i++)
            messageBuilder.append("\t").append(i).append(". ").append(exceptions.get(i)).append("\n.");
        throw new ItemParsingException(messageBuilder.toString());
    }
}
