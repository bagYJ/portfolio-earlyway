package com.vicc.scrap.infra.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vicc.scrap.application.ProductRepository
import com.vicc.scrap.application.ProductSearchFilter
import com.vicc.scrap.application.ProductSearchParam
import com.vicc.scrap.domain.Product
import com.vicc.scrap.logger
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.util.Locale

@Primary
@Service
class ProductRepositoryImpl(
    private val esRestHighLevelClient: RestHighLevelClient,
    private val streamBridge: StreamBridge
): ProductRepository {
    private val mapper = jacksonObjectMapper()

    private fun getIndex(locale: Locale): String {
        return "product-${locale.country.lowercase()}"
    }

    override fun search(param: ProductSearchParam): List<Product> {
        val searchRequest = SearchRequest(getIndex(param.locale ?: Locale.KOREA))
        searchRequest.source()
            .query(param.toQueryBuilder())
            .size(param.size)


        val searchHits = esRestHighLevelClient.search(searchRequest, RequestOptions.DEFAULT).hits
        val hits = searchHits.hits


        return hits.map { mapper.convertValue(it.sourceAsMap, Product::class.java) }
    }

    override fun getById(id: String, locale: Locale): Product? {
        return esRestHighLevelClient.get(GetRequest(getIndex(locale), "_doc", id), RequestOptions.DEFAULT).sourceAsMap.let {
            mapper.convertValue(it, Product::class.java)
        }
    }

    override fun save(product: Product, productKr: Product?) {
        index(product, Locale.CHINA)
        productKr?.let { saveKr(it) }
    }

    override fun saveKr(productKr: Product) {
        index(productKr, Locale.KOREA)
    }

    private fun index(product: Product, locale: Locale) {
//        if (getById(product.id, locale) == null) {
            index(product, getIndex(locale))
//        } else {
//            update(product, getIndex(locale))
//        }
    }

//    private fun update(product: Product, index: String) {
//        val request = UpdateRequest(index, "_doc", product.id)
//        val source = mapper.writeValueAsString(product)
//        request.upsert(request, XContentType.JSON)
//        try {
//            esRestHighLevelClient.update(request, RequestOptions.DEFAULT)
//            if (index == getIndex(Locale.KOREA)) {
//                streamBridge.send("indexProduct-out-0", product)
//            }
//            logger().info("index success: $source")
//        } catch (e: ElasticsearchStatusException) {
//            logger().error(e.stackTraceToString())
//            throw e
//        }
//    }

    private fun index(product: Product, index: String) {
        val request = IndexRequest(index, "_doc", product.id)
        val source = mapper.writeValueAsString(product)
        request.source(source, XContentType.JSON)
        try {
            esRestHighLevelClient.index(request, RequestOptions.DEFAULT)
            if (index == getIndex(Locale.KOREA)) {
                streamBridge.send("indexProduct-out-0", product)
            }
        } catch (e: ElasticsearchStatusException) {
            logger().error(e.stackTraceToString())
            throw e
        }
    }

}

fun ProductSearchParam.toQueryBuilder(): QueryBuilder {
    val query = QueryBuilders.boolQuery()

    val filter = this.filter
    filter.id?.let { query.must(QueryBuilders.termQuery("id", it)) }
    filter.name?.let { query.must(QueryBuilders.termQuery("name.keyword", it)) }

    return query
}