package com.vicc.scrap.infra.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vicc.scrap.application.ShopRepository
import com.vicc.scrap.domain.Product
import com.vicc.scrap.domain.Shop
import com.vicc.scrap.logger
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.util.*

@Primary
@Service
class ShopRepositoryImpl(
    private val esRestHighLevelClient: RestHighLevelClient
): ShopRepository {
    private val mapper = jacksonObjectMapper()

    private fun getIndex(): String {
        return "shop"
    }

    override fun byId(id: String): Shop? {
        val searchRequest = SearchRequest(getIndex())

        val query = QueryBuilders.boolQuery()
        query.must(QueryBuilders.termQuery("id", id))
        searchRequest.source()
            .query(query)

        val searchHits = esRestHighLevelClient.search(searchRequest, RequestOptions.DEFAULT).hits
        val hits = searchHits.hits


        return hits.map { mapper.convertValue(it.sourceAsMap, Shop::class.java) }?.firstOrNull()
    }

    override fun save(shop: Shop): Shop {
        val request = IndexRequest(getIndex(), "_doc", shop.id)
        val source = mapper.writeValueAsString(shop)
        request.source(source, XContentType.JSON)
        try {
            esRestHighLevelClient.index(request, RequestOptions.DEFAULT)
        } catch (e: ElasticsearchStatusException) {
            logger().error(e.stackTraceToString())
            throw e
        }
        return shop
    }
}