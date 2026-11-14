package repository;

import model.Link;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LinkRepository {

    Optional<Link> findByCode(String code);

    List<Link> findByOwner(UUID ownerUuid);

    boolean existsByCode(String code);

    void save(Link link);

    void deleteByCode(String code);

    Optional<Link> findByOwnerAndUrl(UUID ownerUuid, String originalUrl);

    List<Link> findAll();
}
