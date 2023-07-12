package com.wishquil.instagramitemparser.parsers;

import com.wishquil.instagramitemparser.models.Item;
import org.junit.jupiter.api.BeforeEach;
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

    private static InstagramPostParser postParser;

    static Stream<Arguments> validDataProvider() throws IOException {
        return Stream.of(
                Arguments.of(
                        readFileFromResources("tserkovnij.html"),
                        Item.builder()
                                .title("")
                                .profileName("tserkovnij")
                                .description("")
                                .price(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                                .build()
                ),
                Arguments.of(
                        readFileFromResources("melisa_jewerly.html"),
                        Item.builder()
                                .title("Каблучка, що залишається бестселлером\uD83D\uDE0D")
                                .profileName("melisa_jewerly")
                                .description("Каблучка, що залишається бестселлером\uD83D\uDE0D\n\nІдеальна ниточка, з якою кожна дівчина відчуває себе тендітною принцесою, здатною підкорити світ!\n\n▪\uFE0Fсрібло 925\n▪\uFE0Fвставка кубічний цирконій\n▪\uFE0Fрозмір 15,16,17, 17.5\n\uD83D\uDD25ціна ̶5̶9̶0̶ ̶г̶р̶н̶  350 грн\uD83D\uDD25\n\nДля замовлення напиши нам у дірект!\n\n#каблучка #кольцо #кольца #колечко #серебро #серебряныеукрашения #серебро_925_пробы #серебро925пробы #срібло #срібло925 #срібніприкраси")
                                .price(BigDecimal.valueOf(350))
                                .build()
                )
        );
    }

    static String readFileFromResources(String fileName) throws IOException {
        String filePath = Objects.requireNonNull(InstagramPostParserTest.class.getClassLoader().getResource(fileName)).getPath();
        byte[] encodedBytes = Files.readAllBytes(Paths.get(filePath));
        return new String(encodedBytes, StandardCharsets.UTF_8);
    }

    @BeforeEach
    void setUp() {
        postParser = new InstagramPostParser();
    }

    @ParameterizedTest
    @MethodSource("validDataProvider")
    void testParseInstagramPostWitValidData_shouldReturnsItems(String htmlContent, Item expected) {
        Item result = postParser.parse(htmlContent, null);
        assertThat(result.getTitle()).isEqualTo(expected.getTitle());
        assertThat(result.getDescription()).isEqualTo(expected.getDescription());
        assertThat(result.getProfileName()).isEqualTo(expected.getProfileName());
        assertThat(result.getPrice()).isEqualTo(expected.getPrice());
        assertThat(result.getImageUrls()).size().isGreaterThan(0);
    }


}