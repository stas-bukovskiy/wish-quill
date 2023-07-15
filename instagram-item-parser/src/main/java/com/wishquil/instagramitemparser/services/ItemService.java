package com.wishquil.instagramitemparser.services;

import com.wishquil.instagramitemparser.models.InstagramPost;

public interface ItemService {
    InstagramPost getItemFromWebPage(String url);
}
