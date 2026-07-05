package code.practice.URLShorten.repository;

import code.practice.URLShorten.model.UrlMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlMetadataRepository extends MongoRepository<UrlMetadata, String> {
}
