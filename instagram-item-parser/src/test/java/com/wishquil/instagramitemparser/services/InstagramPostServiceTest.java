package com.wishquil.instagramitemparser.services;

import com.wishquil.instagramitemparser.models.InstagramPost;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class InstagramPostServiceTest {

    private static InstagramPostService postService;
    private static WebDriver webDriver;

    @BeforeAll
    static void beforeAll() {
        WebDriverManager.firefoxdriver().setup();
        webDriver = WebDriverManager.firefoxdriver().create();
        postService = new InstagramPostService(webDriver);
    }

    @AfterAll
    static void afterAll() {
        webDriver.quit();
    }

    static Stream<Arguments> validPostDateProvider() {
        return Stream.of(
                Arguments.of(
                        "https://www.instagram.com/p/Crd-if8Na04/",
                        "tserkovnij",
                        "",
                        "",
                        BigDecimal.valueOf(0.00).setScale(2, RoundingMode.HALF_UP)
                ),
                Arguments.of(
                        "https://www.instagram.com/p/Cs9Lu2NNw7D/",
                        "u.do.design",
                        "new T-SHIRT «SUNRISE CLUB»",
                        "new T-SHIRT «SUNRISE CLUB»\n\nQUA: 95%бавовна 5%еластан \n\nSize:\n- standard \n- tall \n- tall+\n\n750₴",
                        BigDecimal.valueOf(750)
                ),
                Arguments.of(
                        "https://www.instagram.com/p/Cs9CPJks6gd/",
                        "dazedhouse",
                        "AUTHENTIC BAGGY PANTS",
                        "AUTHENTIC BAGGY PANTS\n\nУрізноманітнюємо наш асортимент автентичними варіаціями штанів \uD83C\uDFA7\n\n-дизайн, що поєднує у собі дві найпопулярніші моделі - cargo та baggy jeans \n\nРозміри: S/M/L/XL\nЗа розмірною сіткою звертайтесь у дірект або залишайте «+» у коментарі \uD83D\uDC8C\n\nЦіна: 1790 грн.",
                        BigDecimal.valueOf(1790)
                ),
                Arguments.of(
                        "https://www.instagram.com/p/CqnzoymNwQP/",
                        "melisa_jewerly",
                        "Наша тендітна новинка\uD83E\uDD0D",
                        "Наша тендітна новинка\uD83E\uDD0D\n" +
                                "\n" +
                                "Керамічна каблучка, шириною 6 та 2 мм\n" +
                                "Неймовірно стильна та міцна\uD83E\uDD0D\n" +
                                "\n" +
                                "Гарно виглядає у комплекті зі срібними прикрасами або ж сама по собі\uD83D\uDE4C\uD83C\uDFFB\n" +
                                "\n" +
                                "У наявності є 16, 17, 18 розмір\n" +
                                "\uD83D\uDD25ціна 6 мм -350 грн\n" +
                                "\uD83D\uDD25ціна 2 мм -300 грн\n" +
                                "\n" +
                                "Для замовлення напишіть нам у дірект!",
                        BigDecimal.valueOf(350)
                ),
                Arguments.of(
                        "https://www.instagram.com/p/CufD4iwNEso/",
                        "deep.ua",
                        "Білий верх, чорний низ - класика.",
                        "Білий верх, чорний низ - класика. Проте в цьому образі комірець створив додатковий акцент, що робить його цікавим та стильним.\n\nКОМІР\n\uD83C\uDFF7\uFE0F 790 грн\n\nДоступний у двох кольорах: молочний та чорний.\nНа завʼязках.\n\nВесь образ доступний для замовлення.",
                        BigDecimal.valueOf(790)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("validPostDateProvider")
    void testParseWithValidData(String postUrl, String expectedProfileName, String expectedTitle,
                                String expectedDescription, BigDecimal expectedPrice) {
        InstagramPost parsedInstagramPost = postService.getItemFromWebPage(postUrl);

        assertThat(parsedInstagramPost.getUrl()).isEqualTo(postUrl);
        assertThat(parsedInstagramPost.getProfileName()).isEqualTo(expectedProfileName);
        assertThat(parsedInstagramPost.getTitle()).isEqualTo(expectedTitle);
        assertThat(parsedInstagramPost.getDescription()).isEqualTo(expectedDescription);
        assertThat(parsedInstagramPost.getPrice()).isEqualTo(expectedPrice);
    }


}
