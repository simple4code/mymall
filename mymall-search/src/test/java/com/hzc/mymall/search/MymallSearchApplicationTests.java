package com.hzc.mymall.search;

import com.alibaba.fastjson.JSON;
import com.hzc.mymall.search.config.MyMallElasticsearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MymallSearchApplicationTests {

	@Autowired
	private RestHighLevelClient esClient;

	@Test
	public void searchData() throws IOException {
		// 1. 创建检索请求
		SearchRequest searchRequest = new SearchRequest();
		// 指定索引
		searchRequest.indices("bank");
		// 指定DSL，检索条件
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// 1.1 构造检索条件
		sourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));

		// 1.2 按照年龄进行聚合
		TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
		sourceBuilder.aggregation(ageAgg);

		// 1.3 计算平均薪资
		AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
		sourceBuilder.aggregation(balanceAvg);

		searchRequest.source(sourceBuilder);

		// 2. 执行检索
		SearchResponse searchResponse = esClient.search(searchRequest, MyMallElasticsearchConfig.COMMON_OPTIONS);

		// 3. 分析结果
		System.out.println(searchResponse.toString());
		// 3.1 获取所有查到的记录
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHits = hits.getHits();
		for (SearchHit hit : searchHits) {
			String sourceAsString = hit.getSourceAsString();
			Account account = JSON.parseObject(sourceAsString, Account.class);
			System.out.println(account);
		}

		// 3.2 获取聚合数据
		Aggregations aggregations = searchResponse.getAggregations();
		Terms ageAgg1 = aggregations.get("ageAgg");
		for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
			System.out.println("年龄：" + bucket.getKeyAsString());
		}

		Avg avg = aggregations.get("balanceAvg");
		System.out.println("平均薪资是：" + avg.getValue());
	}

	/**
	 * 测试存储数据到 es
	 */
	@Test
	public void indexData() throws IOException {
		IndexRequest indexRequest = new IndexRequest("users");
		indexRequest.id("1");
		User user = new User();
		user.setUserName("Jack");
		user.setAge(20);
		user.setGender("M");
		String jsonString = JSON.toJSONString(user);
		indexRequest.source(jsonString, XContentType.JSON);

		// 执行操作
		IndexResponse indexResponse = esClient.index(indexRequest, MyMallElasticsearchConfig.COMMON_OPTIONS);
		System.out.println(indexResponse);
	}

	@Data
	class User {
		private String userName;
		private Integer age;
		private String gender;
	}

	@Data
	@ToString
	static class Account {
		private int account_number;
		private int balance;
		private String firstname;
		private String lastname;
		private int age;
		private String gender;
		private String address;
		private String employer;
		private String email;
		private String city;
		private String state;
	}

}
