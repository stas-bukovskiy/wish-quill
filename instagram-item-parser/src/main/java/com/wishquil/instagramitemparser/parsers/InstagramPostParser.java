package com.wishquil.instagramitemparser.parsers;

import com.wishquil.instagramitemparser.exceptions.ItemParsingException;
import com.wishquil.instagramitemparser.models.Item;
import com.wishquil.instagramitemparser.util.PriceParser;
import com.wishquil.instagramitemparser.util.SentenceParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class InstagramPostParser implements ItemParser {

    private static final String PROFILE_NAME_CSS_QUERY = "div[id^='mount'] div div div.x9f619.x1n2onr6.x1ja2u2z div.x78zum5.xdt5ytf.x1n2onr6.x1ja2u2z div.x78zum5.xdt5ytf.x1n2onr6 div.x78zum5.xdt5ytf.x1n2onr6.xat3117.xxzkxad div.x78zum5.xdt5ytf.x10cihs4.x1t2pt76.x1n2onr6.x1ja2u2z section.x78zum5.xdt5ytf.x1iyjqo2.xg6iff7.x6ikm8r.x10wlt62 main._a993._a994 div._aa6b._ad9f._aa6d div._aa6e article._aa6a._aatb._aate._aatg._aath._aati div.x9f619.xjbqb8w.x78zum5.x168nmei.x13lgxp2.x5pf9jr.xo71vjh.x1n2onr6.x1plvlek.xryxfnj.x1c4vz4f.x2lah0s.x1q0g3np.xqjyukv.x1qjc9v5.x1oa3qoh.xl56j7k div.x9f619.xjbqb8w.x78zum5.x168nmei.x13lgxp2.x5pf9jr.xo71vjh.x1n2onr6.x1plvlek.xryxfnj.x1c4vz4f.x2lah0s.xdt5ytf.xqjyukv.x1qjc9v5.x1oa3qoh.x1nhvcw1 div._ae1h._ae1j div._aasi div.x9f619.xjbqb8w.x78zum5.x168nmei.x13lgxp2.x5pf9jr.xo71vjh.x1n2onr6.x1plvlek.xryxfnj.x1c4vz4f.x2lah0s.x1q0g3np.xqjyukv.x6s0dn4.x1oa3qoh.x1qughib header._aaqw div._aaqy._aaqz div._aar0._ad95._aar1 div.x78zum5 div._aaqt div.x9f619.xjbqb8w.x78zum5.x168nmei.x13lgxp2.x5pf9jr.xo71vjh.x1n2onr6.x1plvlek.xryxfnj.x1c4vz4f.x2lah0s.x1q0g3np.xqjyukv.x6s0dn4.x1oa3qoh.x1nhvcw1 span.x1lliihq.x1plvlek.xryxfnj.x1n2onr6.x193iq5w.xeuugli.x1fj9vlw.x13faqbe.x1vvkbs.x1s928wv.xhkezso.x1gmr53x.x1cpjm7i.x1fgarty.x1943h6x.x1i0vuye.xvs91rp.x1s688f.x5n08af.x10wh9bi.x1wdrske.x8viiok.x18hxmgj a.x1i10hfl.xjbqb8w.x6umtig.x1b1mbwd.xaqea5y.xav7gou.x9f619.x1ypdohk.xt0psk2.xe8uvvx.xdj266r.x11i5rnm.xat24cr.x1mh8g0r.xexx8yu.x4uap5.x18d9i69.xkhd6sd.x16tdsg8.x1hl2dhg.xggy1nq.x1a2a7pz._acan._acao._acat._acaw._aj1-._a6hd";
    private static final String IMAGE_LIST_CSS_QUERY = "div[id^='mount'] div div div.x9f619.x1n2onr6.x1ja2u2z div.x78zum5.xdt5ytf.x1n2onr6.x1ja2u2z div.x78zum5.xdt5ytf.x1n2onr6 div.x78zum5.xdt5ytf.x1n2onr6.xat3117.xxzkxad div.x78zum5.xdt5ytf.x10cihs4.x1t2pt76.x1n2onr6.x1ja2u2z section.x78zum5.xdt5ytf.x1iyjqo2.xg6iff7.x6ikm8r.x10wlt62 main._a993._a994 div._aa6b._ad9f._aa6d div._aa6e article div div._aatk._aatn div._aamm div._aamn div.x9f619.xjbqb8w.x78zum5.x168nmei.x13lgxp2.x5pf9jr.xo71vjh.x10l6tqk.x1ey2m1c.x13vifvy.x17qophe.xds687c.x1plvlek.xryxfnj.x1c4vz4f.x2lah0s.xdt5ytf.xqjyukv.x1qjc9v5.x1oa3qoh.x1nhvcw1 div._aao_ div._aap0 div._aap1 ul._acay li._acaz div.x9f619.xjbqb8w.x78zum5.x168nmei.x13lgxp2.x5pf9jr.xo71vjh.x1n2onr6.x1plvlek.xryxfnj.x1c4vz4f.x2lah0s.xdt5ytf.xqjyukv.x1qjc9v5.x1oa3qoh.x1nhvcw1 div.x1qjc9v5.x6umtig.x1b1mbwd.xaqea5y.xav7gou.x9f619.x78zum5.xdt5ytf.x2lah0s.xk390pu.xdj266r.x11i5rnm.xat24cr.x1mh8g0r.xexx8yu.x4uap5.x18d9i69.xkhd6sd.x1n2onr6.xggy1nq.x11njtxf div div._aagu._aato div._aagv img";
    private static final String POST_DESCRIPTION_CSS_QUERY = "div[id^='mount'] div div div.x9f619.x1n2onr6.x1ja2u2z div.x78zum5.xdt5ytf.x1n2onr6.x1ja2u2z div.x78zum5.xdt5ytf.x1n2onr6 div.x78zum5.xdt5ytf.x1n2onr6.xat3117.xxzkxad div.x78zum5.xdt5ytf.x10cihs4.x1t2pt76.x1n2onr6.x1ja2u2z section.x78zum5.xdt5ytf.x1iyjqo2.xg6iff7.x6ikm8r.x10wlt62 main._a993._a994 div._aa6b._ad9f._aa6d div._aa6e article._aa6a._aatb._aate._aatg._aath._aati div.x9f619.xjbqb8w.x78zum5.x168nmei.x13lgxp2.x5pf9jr.xo71vjh.x1n2onr6.x1plvlek.xryxfnj.x1c4vz4f.x2lah0s.x1q0g3np.xqjyukv.x1qjc9v5.x1oa3qoh.xl56j7k div.x9f619.xjbqb8w.x78zum5.x168nmei.x13lgxp2.x5pf9jr.xo71vjh.x1n2onr6.x1plvlek.xryxfnj.x1c4vz4f.x2lah0s.xdt5ytf.xqjyukv.x1qjc9v5.x1oa3qoh.x1nhvcw1 div._ae1h._ae1j div._ae2s._ae3v._ae3w div._ae5q._akdn._ae5r._ae5s ul div.x1qjc9v5.x6umtig.x1b1mbwd.xaqea5y.xav7gou.x9f619.x78zum5.xdt5ytf.x2lah0s.xk390pu.xdj266r.x11i5rnm.xat24cr.x1mh8g0r.xexx8yu.x4uap5.x18d9i69.xkhd6sd.x1n2onr6.xggy1nq.x11njtxf li._a9zj._a9zl._a9z5 div._a9zm div._a9zn._a9zo div._a9zr div._a9zs h1._aacl._aaco._aacu._aacx._aad7._aade";


    @Override
    public Item parse(String htmlContent, String url) throws ItemParsingException {
        Document postDocument = Jsoup.parse(htmlContent);
        postDocument.outputSettings().prettyPrint(false);

        Elements imageList = getImageList(postDocument);
        Set<String> imageUrls = parseImageUrls(imageList);
        if (imageUrls.isEmpty() || imageUrls.size() != imageList.size()) {
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
        return Item.builder()
                .url(url)
                .profileName(profileName)
                .title(title)
                .description(description)
                .price(price)
                .imageUrls(imageUrls)
                .build();

    }

    private String parseProfileName(Document postDocument) {
        Element profileNameElements = postDocument.selectFirst(PROFILE_NAME_CSS_QUERY);
        if (profileNameElements != null) {
            return profileNameElements.text();
        } else {
            throw new ItemParsingException("Could not parse profile name");
        }
    }

    private Elements getImageList(Document document) {
        return document.select(IMAGE_LIST_CSS_QUERY);
    }

    private Set<String> parseImageUrls(Elements imageList) {
        Set<String> imageUrls = new LinkedHashSet<>();
        for (Element img : imageList) {
            if (img.hasAttr("src"))
                imageUrls.add(img.attr("src"));
        }
        return imageUrls;
    }

    private Optional<Element> getPostDescription(Document postDocument) {
        Element postDescriptionElement = postDocument.selectFirst(POST_DESCRIPTION_CSS_QUERY);
        return Optional.ofNullable(postDescriptionElement);
    }

    private String parseTitle(String postDescription) {
        return SentenceParser.getFirstSentence(postDescription).trim();
    }

}
