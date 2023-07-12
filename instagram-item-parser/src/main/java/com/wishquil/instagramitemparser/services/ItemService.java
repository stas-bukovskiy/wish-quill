package com.wishquil.instagramitemparser.services;

import com.wishquil.instagramitemparser.models.Item;

public interface ItemService {
    Item getItemFromWebPage(String url);
}
