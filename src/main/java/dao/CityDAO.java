package dao;

import domain.City;
import lombok.AllArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

@AllArgsConstructor
public class CityDAO {
    final private SessionFactory sessionFactory;

    public City getById(final Integer id) {
        Query<City> query = sessionFactory.getCurrentSession()
                .createQuery("select c from City c join fetch c.country where c.id = :ID", City.class);
        query.setParameter("ID", id);
        return query.getSingleResult();
    }

    public List<City> getCities(final int offset, final int limit) {
        Query<City> query = sessionFactory.getCurrentSession()
                .createQuery("select c from City c", City.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.list();
    }

    public int getCitiesCount() {
        Query<Long> query = sessionFactory.getCurrentSession()
                .createQuery("select count(c) from City c", Long.class);
        return Math.toIntExact(query.uniqueResult());
    }
}
