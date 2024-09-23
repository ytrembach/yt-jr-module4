package redis;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ShortLanguage {
    private String language;

    private Boolean ifOfficial;

    private BigDecimal percentage;
}
