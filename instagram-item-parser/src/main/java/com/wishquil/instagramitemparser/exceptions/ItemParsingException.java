package com.wishquil.instagramitemparser.exceptions;


public class ItemParsingException extends RuntimeException {
    public ItemParsingException(Exception e) {
        super(e);
    }

    public ItemParsingException(String message) {
        super(message);
    }

    public ItemParsingException(String message, Object... args) {
        super(String.format(message, args));
    }

}
