package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.dao.GenericAccountRepository;
import fr.sictiam.stela.admin.model.GenericAccount;
import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GenericAccountService {

    @PersistenceContext
    private EntityManager entityManager;

    private final GenericAccountRepository genericAccountRepository;

    public GenericAccountService(GenericAccountRepository genericAccountRepository) {
        this.genericAccountRepository = genericAccountRepository;
    }

    public GenericAccount save(GenericAccount genericAccount) {
        return genericAccountRepository.save(genericAccount);
    }

    public GenericAccount getByUuid(String uuid) {
        return genericAccountRepository.findById(uuid).orElseThrow(NotFoundException::new);
    }

    public Optional<GenericAccount> getBySerialAndVendor(String serial, String vendor) {
        return genericAccountRepository.findBySerialAndVendor(serial, vendor);
    }

    public Optional<GenericAccount> getByEmail(String email) {
        return genericAccountRepository.findByEmailIgnoreCase(email);
    }

    public List<GenericAccount> getAllWithPagination(String search, String software, String email, Integer limit,
            Integer offset, String column, String direction) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GenericAccount> query = builder.createQuery(GenericAccount.class);
        Root<GenericAccount> genericAccountRoot = query.from(GenericAccount.class);

        String columnAttribute = StringUtils.isEmpty(column) ? "familyName" : column;
        List<Predicate> predicates = getPredicates(search, software, email, builder, genericAccountRoot);

        query.select(builder.construct(GenericAccount.class, genericAccountRoot.get("uuid"),
                genericAccountRoot.get("software"), genericAccountRoot.get("email")));
        query.where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(!StringUtils.isEmpty(direction) && direction.equals("ASC")
                        ? builder.asc(genericAccountRoot.get(columnAttribute))
                        : builder.desc(genericAccountRoot.get(columnAttribute)));

        return entityManager.createQuery(query).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    public Long countAll(String search, String software, String email) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<GenericAccount> genericAccountRoot = query.from(GenericAccount.class);

        List<Predicate> predicates = getPredicates(search, software, email, builder, genericAccountRoot);

        query.select(builder.count(genericAccountRoot));
        query.where(predicates.toArray(new Predicate[predicates.size()]));

        return entityManager.createQuery(query).getSingleResult();
    }

    private List<Predicate> getPredicates(String search, String software, String email, CriteriaBuilder builder,
            Root<GenericAccount> genericAccountRoot) {
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(search)) {
            predicates.add(builder.or(
                    builder.like(builder.lower(genericAccountRoot.get("software")), "%" + search.toLowerCase() + "%"),
                    builder.like(builder.lower(genericAccountRoot.get("email")), "%" + search.toLowerCase() + "%")));
        }
        if (StringUtils.isNotBlank(software)) {
            predicates.add(builder.and(
                    builder.like(builder.lower(genericAccountRoot.get("software")), "%" + software.toLowerCase() + "%")));
        }
        if (StringUtils.isNotBlank(email)) {
            predicates.add(builder.and(
                    builder.like(builder.lower(genericAccountRoot.get("email")), "%" + email.toLowerCase() + "%")));
        }
        return predicates;
    }

    public void deleteByUuid(String uuid) {
        genericAccountRepository.delete(getByUuid(uuid));
    }
}
