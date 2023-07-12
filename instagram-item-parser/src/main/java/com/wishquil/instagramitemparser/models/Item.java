package com.wishquil.instagramitemparser.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Item {
    private String wishlistId;
    private String url;
    private String profileName;
    private String title;
    private String description;
    private Set<String> imageUrls;
    private BigDecimal price;
}
