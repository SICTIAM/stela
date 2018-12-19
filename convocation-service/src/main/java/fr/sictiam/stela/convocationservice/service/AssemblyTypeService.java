package fr.sictiam.stela.convocationservice.service;

import fr.sictiam.stela.convocationservice.dao.AssemblyTypeRepository;
import fr.sictiam.stela.convocationservice.model.AssemblyType;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import fr.sictiam.stela.convocationservice.model.exception.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

@Service
public class AssemblyTypeService {

    private static Logger LOGGER = LoggerFactory.getLogger(AssemblyTypeService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AssemblyTypeRepository assemblyTypeRepository;

    @Autowired
    private ExternalRestService externalRestService;


    public AssemblyType getAssemblyType(String uuid) {

        return assemblyTypeRepository.findByUuid(uuid).orElseThrow(NotFoundException::new);
    }

    public Long countAllWithQuery(String multifield, String name, Boolean active, String currentLocalAuthUuid) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<AssemblyType> assemblyRoot = query.from(AssemblyType.class);

        List<Predicate> predicates = getQueryPredicates(builder, assemblyRoot, multifield, name, active, currentLocalAuthUuid);
        query.select(builder.count(assemblyRoot));
        query.where(predicates.toArray(new Predicate[predicates.size()]));

        return entityManager.createQuery(query).getSingleResult();
    }

    public List<AssemblyType> findAllWithQuery(String multifield, String name,
            Boolean active, Integer limit, Integer offset, String column, String direction,
            String currentLocalAuthUuid) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssemblyType> query = builder.createQuery(AssemblyType.class);
        Root<AssemblyType> assemblyRoot = query.from(AssemblyType.class);

        query.select(assemblyRoot);
        String columnAttribute = StringUtils.isEmpty(column) ? "name" : column;
        List<Predicate> predicates = getQueryPredicates(builder, assemblyRoot, multifield, name, active, currentLocalAuthUuid);

        query.where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(!StringUtils.isEmpty(direction) && direction.equals("ASC")
                        ? builder.asc(assemblyRoot.get(columnAttribute))
                        : builder.desc(assemblyRoot.get(columnAttribute)));

        return entityManager.createQuery(query).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    private List<Predicate> getQueryPredicates(CriteriaBuilder builder, Root<AssemblyType> assemblyRoot, String multifield,
            String name, Boolean active, String currentLocalAuthUuid) {

        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(multifield)) {
            predicates.add(
                    builder.or(
                            builder.like(builder.lower(assemblyRoot.get("name")), "%" + multifield.toLowerCase() + "%")));
        }
        if (StringUtils.isNotBlank(name))
            predicates.add(
                    builder.and(builder.like(builder.lower(assemblyRoot.get("name")), "%" + name.toLowerCase() + "%")));


        if (StringUtils.isNotBlank(currentLocalAuthUuid)) {
            Join<LocalAuthority, LocalAuthority> LocalAuthorityJoin = assemblyRoot.join("localAuthority");
            LocalAuthorityJoin.on(builder.equal(LocalAuthorityJoin.get("uuid"), currentLocalAuthUuid));
        }

        if (active != null)
            predicates.add(builder.and(builder.equal(assemblyRoot.get("active"), active)));

        return predicates;
    }
}
