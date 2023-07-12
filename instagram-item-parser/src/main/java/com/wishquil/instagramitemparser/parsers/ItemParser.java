package com.wishquil.instagramitemparser.parsers;

import com.wishquil.instagramitemparser.exceptions.ItemParsingException;
import com.wishquil.instagramitemparser.models.Item;

public interface ItemParser {
    Item parse(String htmlContent, String url) throws ItemParsingException;
}
