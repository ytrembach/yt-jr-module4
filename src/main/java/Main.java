import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.CityDAO;
import dao.CountryDAO;
import domain.City;
import domain.Country;
import domain.Language;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import lombok.SneakyThrows;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import redis.CityCountry;
import tests.TestMysql;
import tests.TestRedis;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class Main {
    final private SessionFactory sessionFactory;
    final private RedisClient redisClient;
    final ObjectMapper mapper;

    final private CountryDAO countryDAO;
    final private CityDAO cityDAO;

    public Main() {
        sessionFactory = prepareMysql();
        redisClient = prepareRedis();
        mapper = new ObjectMapper();

        cityDAO = new CityDAO(sessionFactory);
        countryDAO = new CountryDAO(sessionFactory);
    }

    @SneakyThrows
    private SessionFactory prepareMysql() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final Properties properties = new Properties();
        properties.load(classLoader.getResourceAsStream("mysql.properties"));

        return new Configuration()
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(Language.class)
                .addProperties(properties)
                .buildSessionFactory();
    }

    private RedisClient prepareRedis() {
        final RedisClient redisClient = RedisClient.create(RedisURI.create("localhost", 6379));
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()){
            return redisClient;
        }
    }

    private void shutdown() {
        if (nonNull(sessionFactory)) {
            sessionFactory.close();
        }

        if (nonNull(redisClient)) {
            redisClient.shutdown();
        }
    }

    private List<City> fetchData() {
        final List<City> cities = new ArrayList<>();
        final Session session = sessionFactory.getCurrentSession();

        session.beginTransaction();
        final List<Country> countries = countryDAO.getCountries(); // fetch countries - optimization
        final int citiesCount = cityDAO.getCitiesCount();
        final int step = 500;
        for (int i = 0; i < citiesCount; i += step) {
            cities.addAll(cityDAO.getCities(i, step));
        }
        session.getTransaction().commit();
        return cities;
    }

    private List<CityCountry> prepareCityCountry(final List<City> cities) {
        return cities.stream()
                .map(Converters::convertCityCountry)
                .collect(Collectors.toList());
    }

    private void saveToRedis(final List<CityCountry> cityCountryList) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (final CityCountry cityCountry : cityCountryList) {
                try {
                    sync.set(String.valueOf(cityCountry.getId()), mapper.writeValueAsString(cityCountry));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        final Main main = new Main();
        final List<City> allCities = main.fetchData();
        final List<CityCountry> allCityCountries = main.prepareCityCountry(allCities);
        main.saveToRedis(allCityCountries);

        main.sessionFactory.getCurrentSession().close();
        final List<Integer> ids = List.of (12, 56, 76, 44, 88, 123, 2123, 888, 1345, 999);

        final long startRedis = System.currentTimeMillis();
        new TestRedis(main.redisClient, main.mapper).testRedisData(ids);
        final long stopRedis = System.currentTimeMillis();

        final long startMysql = System.currentTimeMillis();
        new TestMysql(main.sessionFactory, main.cityDAO).testMysqlData(ids);
        final long stopMysql = System.currentTimeMillis();

        System.out.printf("%s:\t%d ms\n", "Redis", (stopRedis - startRedis));
        System.out.printf("%s:\t%d ms\n", "MySQL", (stopMysql - startMysql));

        main.shutdown();
    }
}
