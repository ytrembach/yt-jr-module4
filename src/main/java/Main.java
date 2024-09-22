import dao.CityDAO;
import dao.CountryDAO;
import domain.City;
import domain.Country;
import domain.CountryLanguage;
import lombok.SneakyThrows;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.util.Objects.nonNull;

public class Main {
    final private SessionFactory sessionFactory;

    final private CountryDAO countryDAO;
    final private CityDAO cityDAO;

    public Main() {
        sessionFactory = prepareMysql();

        cityDAO = new CityDAO(sessionFactory);
        countryDAO = new CountryDAO(sessionFactory);
    }

    @SneakyThrows
    private SessionFactory prepareMysql() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Properties properties = new Properties();
        properties.load(classLoader.getResourceAsStream("mysql.properties"));

        return new Configuration()
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(CountryLanguage.class)
                .addProperties(properties)
                .buildSessionFactory();
    }

    private void shutdown() {
        if (nonNull(sessionFactory)) {
            sessionFactory.close();
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

    public static void main(String[] args) {
        Main main = new Main();
        List<City> all = main.fetchData();
        main.shutdown();
    }
}
