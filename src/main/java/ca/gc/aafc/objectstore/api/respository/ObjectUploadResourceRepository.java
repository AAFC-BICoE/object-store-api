package ca.gc.aafc.objectstore.api.respository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.service.ObjectUploadService;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

@Repository
public class ObjectUploadResourceRepository extends ReadOnlyResourceRepositoryBase<ObjectUploadDto, UUID> {
    private final ObjectUploadService service;

    protected ObjectUploadResourceRepository(ObjectUploadService service) {
        super(ObjectUploadDto.class);
        this.service = service;
    }

    @Override
    public ResourceList<ObjectUploadDto> findAll(QuerySpec querySpec) {
        //this.service.findAll(entityClass, where, orderBy, startIndex, maxResult)
        return null;
    }
    
}