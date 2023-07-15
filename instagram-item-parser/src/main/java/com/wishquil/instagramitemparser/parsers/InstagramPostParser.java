package com.wishquil.instagramitemparser.parsers;

import com.wishquil.instagramitemparser.exceptions.ItemParsingException;
import com.wishquil.instagramitemparser.models.InstagramPost;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class InstagramPostParser {

    private static final String[] PROFILE_NAME_CSS_PATHS = new String[]{
            "div[id^='mount'] div div div div div div div section main div div article div div div div div header div div div span a",
            "div[id^='mount'] div div div div div div div section main div div div div div header div div div div div span a"
    };

    private static final String[] IMAGE_URI_CSS_PATHS = new String[]{
            // for video content:
            "div[id^='mount'] div div div div div div section main div article div div div div div div div div div video",
            // for image content:
            "div[id^='mount'] div div div div div div div section main div div article div div._aatk div div div div img",
            // for image content when video is present
            "div[id^='mount'] div div div div section main article div div div div div div div div div div div img"
    };
    private static final String POST_DESCRIPTION_CSS_QUERY = "div[id^='mount'] div div div div div div div section main article div div div div div li div div div div h1";

    private InstagramPostParser() {
    }

    public static InstagramPost parse(String htmlContent, String url) throws ItemParsingException {
        Document postDocument = Jsoup.parse(htmlContent);
        postDocument.outputSettings().prettyPrint(false);

        Elements imageList = getImageList(postDocument);
        Set<String> imageUrls = parseImageUrls(imageList);
        if (imageUrls.isEmpty()) {
            throw new ItemParsingException("Could not fetch any image");
        }

        String profileName = parseProfileName(postDocument);

        String title = "";
        String description = "";
        BigDecimal price = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        Optional<Element> postDescriptionElementOptional = getPostDescription(postDocument);
        if (postDescriptionElementOptional.isPresent()) {
            Element postDescriptionElement = postDescriptionElementOptional.get();
            postDescriptionElement.select("br").before("\\n");
            postDescriptionElement.select("p").before("\\n");
            String postDescription = Jsoup.clean(postDescriptionElement.html().replaceAll("\\\\n", "\n"), "", Safelist.none(), postDocument.outputSettings());
            if (postDescriptionElement.hasText()) {
                title = parseTitle(postDescription);
                description = postDescription;
                price = PriceParser.parsePrice(postDescription);
            }
        }
        return InstagramPost.builder()
                .url(url)
                .profileName(profileName)
                .title(title)
                .description(description)
                .price(price)
                .imageUrls(imageUrls)
                .build();

    }

    private static String parseProfileName(Document postDocument) {
        for (String profileNameCssPath : PROFILE_NAME_CSS_PATHS) {
            Element profileNameElements = postDocument.selectFirst(profileNameCssPath);
            if (profileNameElements != null)
                return profileNameElements.text();
        }
        return "";
    }

    private static Elements getImageList(Document document) {
        Elements res = new Elements();
        for (String cssPath : IMAGE_URI_CSS_PATHS) {
            Elements elements = document.select(cssPath);
            res.addAll(elements);
        }
        return res;
    }

    private static Set<String> parseImageUrls(Elements imageList) {
        Set<String> imageUrls = new LinkedHashSet<>();
        for (Element img : imageList) {
            if (img.hasAttr("src"))
                imageUrls.add(img.attr("src"));
        }
        return imageUrls;
    }

    private static Optional<Element> getPostDescription(Document postDocument) {
        Element postDescriptionElement = postDocument.selectFirst(POST_DESCRIPTION_CSS_QUERY);
        return Optional.ofNullable(postDescriptionElement);
    }

    private static String parseTitle(String postDescription) {
        return SentenceParser.parseFirstSentence(postDescription).trim();
    }

}
