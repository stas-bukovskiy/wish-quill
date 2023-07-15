package com.wishquil.instagramitemparser.parsers;

import com.wishquil.instagramitemparser.models.InstagramPost;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class InstagramPostParserTest {

    static Stream<Arguments> validDataProvider() throws IOException {
        return Stream.of(
                Arguments.of(
                        readFileFromResources("theaim.ua.html"),
                        InstagramPost.builder()
                                .title("SHOPPER AIM\uD83E\uDD18\uD83C\uDFFC")
                                .profileName("theaim.ua")
                                .description("SHOPPER AIM\uD83E\uDD18\uD83C\uDFFC\n" +
                                        "\n" +
                                        "\uD83E\uDD1A\uD83C\uDFFB різних кольорів\n" +
                                        "\n" +
                                        "Розмір: 40х40см\n" +
                                        "Внутрішній карман\n" +
                                        "Карабін для ключів\n" +
                                        "\n" +
                                        "\uD83C\uDFF7\uFE0F 650 uah\n" +
                                        "\n" +
                                        "#українськийбренд #сумкиукраина #сумки #стежтезанашимисторіс #ukraine #ukrainebrand #brand #бананки #чоловічісумки #сумканапояс #аксесуари #new")
                                .price(BigDecimal.valueOf(650))
                                .build()
                ), Arguments.of(
                        readFileFromResources("eco_priority.html"),
                        InstagramPost.builder()
                                .title("\uD83D\uDD0E ДОСЬЄ НА «СІТКА ШОПЕР»")
                                .profileName("eco_priority")
                                .description("\uD83D\uDD0E ДОСЬЄ НА «СІТКА ШОПЕР»\n" +
                                        "⠀\n" +
                                        "МЕТА\n" +
                                        " СТВОРЕННЯ: дати альтернативний багаторазовий пакет для тих, кому «не \n" +
                                        "заходять» мішечки на стрічках. Вони заходять всім, хто починає \n" +
                                        "користуватись і бачить їх зручність. Гарний варіант для старту вашого \n" +
                                        "шляху \"пакет не треба.\"\n" +
                                        "⠀\n" +
                                        "\n" +
                                        "МАТЕРІАЛ: створені з легкої, але міцної сітчастої тканини. Дуже компактно складаються навіть у найменшу сумочку.\n" +
                                        "\n" +
                                        "⠀\n" +
                                        "КОМПЛЕКТАЦІЯ: можна придбати як одну, так і набір зі знижкою до 15%\n" +
                                        "⠀\n" +
                                        "ОСОБЛИВІ\n" +
                                        " ПРИКМЕТИ: витримують до 4 кг, супер місткі, інстаграмно-привабливі \n" +
                                        "(гарно дивляться в кадрі), привертають до себе увагу.\n" +
                                        "⠀\n" +
                                        "ТЕРМІН ПРИДАТНОСТІ: необмежений. Гарантуємо, що витримають 1,5 року використання.\n" +
                                        "\n" +
                                        "1\n" +
                                        " багаторазова торбинка заміняє приблизно 1000 одноразових пакетів, тому \n" +
                                        "порахувавши загальну ціну в магазинах за них можна зрозуміти, що \n" +
                                        "торбинки в 3-5 разів дешевше\uD83D\uDE09\n" +
                                        "⠀\n" +
                                        "ЦІНА: 1 шт - 145 грн\n" +
                                        "(-10%) 3 шт - 390 грн\n" +
                                        "(-15%) 4 шт - 490 грн\n" +
                                        "⠀\n" +
                                        "КОЛЬОРИ: жовтий, блакитний, рожевий, синій, зелений, бордо, сливовий, темно-синій, чорний.\n" +
                                        "⠀\n" +
                                        "ДЛЯ\n" +
                                        " ЧОГО ВОНИ ТОБІ: набір майок за рік використання рятує від звалища \n" +
                                        "1000-1500 пакетів (в середньому). При твоїй прямій участі \uD83D\uDD25\n" +
                                        "З ними просто зменшити кількість зайвого пакування, менше викидати. \uD83D\uDC9A\n" +
                                        "⠀\n" +
                                        "Майки в магазині понад рік, і завжди лідирують у ваших замовленнях.\n" +
                                        "⠀\n" +
                                        "✏\uFE0F Хто вже користується - поділіться в коментарях як вам Майки Екосітки. Ми цінуємо ваші відгуки \uD83D\uDE4C\uD83C\uDFFD")
                                .price(BigDecimal.valueOf(145))
                                .build()
                ), Arguments.of(
                        readFileFromResources("touchof.cities.html"),
                        InstagramPost.builder()
                                .title("A touch of playfulness")
                                .profileName("touchof.cities")
                                .description("A touch of playfulness\n" +
                                        "⠀\n" +
                                        "Cap — 1099 UAH/35 EUR\n" +
                                        "touchof.shop")
                                .price(BigDecimal.valueOf(1099))
                                .build()
                ), Arguments.of(
                        readFileFromResources("broq.world.html"),
                        InstagramPost.builder()
                                .title("x @mot.")
                                .profileName("broq.world")
                                .description("x @mot.object  palm bracelet \n" +
                                        "⠀\n" +
                                        "capsule collection available at MOT giftstore in Kyiv, Ukraine.\n" +
                                        "\n" +
                                        "vision: @vova.morrow")
                                .price(BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP))
                                .build()
                )
        );
    }

    static String readFileFromResources(String fileName) throws IOException {
        String filePath = Objects.requireNonNull(InstagramPostParserTest.class.getClassLoader().getResource(fileName)).getPath();
        byte[] encodedBytes = Files.readAllBytes(Paths.get(filePath));
        return new String(encodedBytes, StandardCharsets.UTF_8);
    }


    @ParameterizedTest
    @MethodSource("validDataProvider")
    void testParseInstagramPostWitValidData_shouldReturnsItems(String htmlContent, InstagramPost expected) {
        InstagramPost result = InstagramPostParser.parse(htmlContent, null);
        assertThat(result.getTitle()).isEqualTo(expected.getTitle());
        assertThat(result.getDescription()).isEqualTo(expected.getDescription());
        assertThat(result.getProfileName()).isEqualTo(expected.getProfileName());
        assertThat(result.getPrice()).isEqualTo(expected.getPrice());
        assertThat(result.getImageUrls()).size().isGreaterThan(0);
    }


}