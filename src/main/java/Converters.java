import domain.City;
import domain.Country;
import domain.Language;
import redis.CityCountry;
import redis.ShortLanguage;

import java.util.Set;
import java.util.stream.Collectors;

public class Converters {

    public static ShortLanguage convertLanguage(final Language language) {
        final ShortLanguage shortLanguage = new ShortLanguage();

        shortLanguage.setLanguage(language.getLanguage());
        shortLanguage.setIfOfficial(language.getIsOfficial());
        shortLanguage.setPercentage(language.getPercentage());

        return shortLanguage;
    }

    public static CityCountry convertCityCountry(final City city) {
        final CityCountry cityCountry = new CityCountry();

        // convert city part
        cityCountry.setId(city.getId());
        cityCountry.setName(city.getName());
        cityCountry.setDistrict(city.getDistrict());
        cityCountry.setPopulation(city.getPopulation());

        // country part
        final Country country = city.getCountry();
        cityCountry.setCountryCode(country.getCode());
        cityCountry.setCountryAlternativeCode(country.getAlternativeCode());
        cityCountry.setCountryName(country.getName());
        cityCountry.setCountryContinent(country.getContinent());
        cityCountry.setCountryRegion(country.getRegion());
        cityCountry.setCountrySurfaceArea(country.getSurfaceArea());
        cityCountry.setCountryPopulation(country.getPopulation());

        // lang part
        final Set<Language> countryLanguages = country.getLanguages();
        final Set<ShortLanguage> shortLanguages = countryLanguages.stream()
                .map(Converters::convertLanguage)
                .collect(Collectors.toSet());
        cityCountry.setCountryLanguages(shortLanguages);

        return cityCountry;
    }
}
