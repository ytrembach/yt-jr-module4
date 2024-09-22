package redis;

import domain.Continent;
import domain.Language;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
public class CityCountry {

    // City fields
    private Integer id;

    private String name;

    private String district;

    private Integer population;

    // Country fields
    private String countryCode; // code in Country

    private String countryAlternativeCode;

    private String countryName;

    private Continent countryContinent;

    private String countryRegion;

    private BigDecimal countrySurfaceArea;

    private Integer countryPopulation;

    // languages
    private Set<ShortLanguage> countryLanguages;
}
