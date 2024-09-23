package tests;

import dao.CityDAO;
import domain.City;
import domain.Language;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class TestMysql {

    final private SessionFactory sessionFactory;
    final private CityDAO cityDAO;

    public void testMysqlData(final List<Integer> ids) {
        try (final Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            for (final Integer id : ids) {
                final City city = cityDAO.getById(id);
                Set<Language> languages = city.getCountry().getLanguages();
            }
            session.getTransaction().commit();
        }
    }
}
