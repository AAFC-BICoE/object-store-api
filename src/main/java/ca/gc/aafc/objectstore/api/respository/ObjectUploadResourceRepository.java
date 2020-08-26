package ca.gc.aafc.objectstore.api.respository;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.NoLinkInformation;
import ca.gc.aafc.dina.repository.SelectionHandler;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.service.ObjectUploadService;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.DefaultPagedMetaInformation;
import lombok.Getter;
import lombok.Setter;

@Repository
public class ObjectUploadResourceRepository
    extends ReadOnlyResourceRepositoryBase<ObjectUploadDto, UUID> {
  private final ObjectUploadService service;
  private final DinaFilterResolver filterResolver;
  private static final NoLinkInformation NO_LINK_INFORMATION = new NoLinkInformation();
  private DinaMapper<ObjectUploadDto, ObjectUpload> dinaMapper;

  private static final int DEFAULT_OFFSET = 0;
  private static final int DEFAULT_LIMIT = 100;

  @Getter
  @Setter(onMethod_ = @Override)
  private ResourceRegistry resourceRegistry;

  protected ObjectUploadResourceRepository(ObjectUploadService service,
      DinaFilterResolver filterResolver) {
    super(ObjectUploadDto.class);
    this.service = service;
    this.filterResolver = filterResolver;
    this.dinaMapper = new DinaMapper<>(ObjectUploadDto.class);
  }

  @Override
  public ResourceList<ObjectUploadDto> findAll(QuerySpec querySpec) {

    String idName = SelectionHandler.getIdAttribute(ObjectUploadDto.class, resourceRegistry);

    List<ObjectUpload> returnedEntities = service.findAll(ObjectUpload.class,
      (cb, root) -> filterResolver.buildPredicates(querySpec, cb, root, null, idName),
      (cb, root) -> DinaFilterResolver.getOrders(querySpec, cb, root),
        Optional.ofNullable(querySpec.getOffset()).orElse(Long.valueOf(DEFAULT_OFFSET)).intValue(),
        Optional.ofNullable(querySpec.getLimit()).orElse(Long.valueOf(DEFAULT_LIMIT)).intValue());

    Set<String> includedRelations = querySpec.getIncludedRelations().stream()
        .map(ir -> ir.getAttributePath().get(0)).collect(Collectors.toSet());

    Map<Class<?>, Set<String>> entityFieldsPerClass = new HashMap<Class<?>, Set<String>>();

    List<Field> attributeFields = FieldUtils.getAllFieldsList(ObjectUpload.class).stream()
        .collect(Collectors.toList());

    Set<String> fieldsToInclude = attributeFields.stream().map(af -> af.getName())
        .collect(Collectors.toSet());

    entityFieldsPerClass.put(ObjectUpload.class, fieldsToInclude);

    List<ObjectUploadDto> dtos = returnedEntities.stream()
        .map(e -> dinaMapper.toDto(e, entityFieldsPerClass, includedRelations))
        .collect(Collectors.toList());

    Long resourceCount = service.getResourceCount(ObjectUpload.class,
      (cb, root) -> filterResolver.buildPredicates(querySpec, cb, root, null, idName));

    DefaultPagedMetaInformation metaInformation = new DefaultPagedMetaInformation();
    metaInformation.setTotalResourceCount(resourceCount);
    return new DefaultResourceList<>(dtos, metaInformation, NO_LINK_INFORMATION);

  }
  
  @Override
  public ObjectUploadDto findOne(UUID id, QuerySpec querySpec) {
    RegistryEntry entry = resourceRegistry.findEntry(ObjectUploadDto.class);
    String idName = entry.getResourceInformation().getIdField().getUnderlyingName();

    QuerySpec idQuerySpec = querySpec.clone();
    idQuerySpec.addFilter(new FilterSpec(Arrays.asList(idName), FilterOperator.EQ, id));
    Collection<ObjectUploadDto> collection = findAll(idQuerySpec);
    Iterator<ObjectUploadDto> iterator = collection.iterator();
    if (iterator.hasNext()) {
      ObjectUploadDto resource = iterator.next();
      PreconditionUtil.verify(!iterator.hasNext(), "expected unique result for id=%s, querySpec=%s", id, querySpec);
      return resource;
    } else {
      throw new ResourceNotFoundException("resource not found: " + id);
    }
  }  

}
