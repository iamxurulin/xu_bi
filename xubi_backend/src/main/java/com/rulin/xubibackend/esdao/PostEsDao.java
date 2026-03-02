package com.rulin.xubibackend.esdao;

import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.rulin.xubibackend.model.dto.post.PostEsDTO;


/**
 * PostEsDao接口，用于操作Elasticsearch中的PostEsDTO数据
 * 该接口继承自ElasticsearchRepository，提供基本的CRUD操作以及自定义查询方法
 *
 */
public interface PostEsDao extends ElasticsearchRepository<PostEsDTO, Long> {

    /**
     * 根据用户ID查询帖子列表
     *
     * @param userId 用户ID，用于筛选特定用户的帖子
     * @return 返回匹配的PostEsDTO列表，包含用户ID为指定值的所有帖子
     */
    List<PostEsDTO> findByUserId(Long userId);
}