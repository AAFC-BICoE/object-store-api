package ca.gc.aafc.objectstore.api.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import ca.gc.aafc.dina.entity.ControlledVocabularyItem;

@Entity(name = "controlled_vocabulary_item")
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class ObjectStoreControlledVocabularyItem extends ControlledVocabularyItem {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = CONTROLLED_VOCABULARY_COL_NAME)
  private ObjectStoreControlledVocabulary controlledVocabulary;

}
