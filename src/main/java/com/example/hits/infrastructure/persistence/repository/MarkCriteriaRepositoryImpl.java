package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.domain.entity.markCriteria.MarkCriteria;
import com.example.hits.domain.repository.MarkCriteriaRepository;
import com.example.hits.infrastructure.persistence.entity.MarkCriteriaEntity;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.infrastructure.persistence.mapper.MarkCriteriaPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MarkCriteriaRepositoryImpl implements MarkCriteriaRepository {

    private final JpaMarkCriteriaRepository jpaMarkCriteriaRepository;
    private final JpaCriteriaScoreRepository jpaCriteriaScoreRepository;
    private final PostRepository postRepository;
    private final MarkCriteriaPersistenceMapper markCriteriaPersistenceMapper;

    @Override
    public List<MarkCriteria> findAllByPostId(UUID postId) {
        return jpaMarkCriteriaRepository.findAllByPostEntity_IdOrderById(postId).stream()
                .map(markCriteriaPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<MarkCriteria> findByIdAndPostId(UUID markCriteriaId, UUID postId) {
        return jpaMarkCriteriaRepository.findByIdAndPostEntity_Id(markCriteriaId, postId)
                .map(markCriteriaPersistenceMapper::toDomain);
    }

    @Override
    public void save(MarkCriteria markCriteria) {
        PostEntity post = postRepository.findById(markCriteria.getPostId())
                .orElseThrow(() -> new IllegalStateException("Post must exist when saving mark criteria"));
        MarkCriteriaEntity entity = jpaMarkCriteriaRepository.findById(markCriteria.getId())
                .orElseGet(() -> new MarkCriteriaEntity().setId(markCriteria.getId()));
        markCriteriaPersistenceMapper.copyToEntity(markCriteria, entity, post);
        jpaMarkCriteriaRepository.save(entity);
    }

    @Override
    public boolean deleteWithScores(UUID markCriteriaId, UUID postId) {
        return jpaMarkCriteriaRepository.findByIdAndPostEntity_Id(markCriteriaId, postId)
                .map(entity -> {
                    jpaCriteriaScoreRepository.deleteAllByMarkCriteriaId(markCriteriaId);
                    jpaMarkCriteriaRepository.delete(entity);
                    return true;
                })
                .orElse(false);
    }
}
