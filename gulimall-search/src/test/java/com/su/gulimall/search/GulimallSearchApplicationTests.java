package com.su.gulimall.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.su.common.to.es.SkuEsModel;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private ElasticsearchClient client;

    @Test
    void testIndex() throws IOException {
        User user = new User();
        user.setId(String.valueOf(1L));
        user.setName("张三");
        user.setAge(18);
        IndexResponse response = client.index(i -> i
                .index("user")
                .id(user.getId())
                .document(user));
        System.out.println(response);
    }

    @Test
    void testGet() throws IOException {
        GetResponse<ObjectNode> response = client.get(g -> g
                        .index("user")
                        .id("1"),
                ObjectNode.class //目标类是一个原始的JSON对象
        );
        if (response.found()) {
            ObjectNode jsonNodes = response.source();
            String name = jsonNodes.get("name").asText();
            System.out.println(name);
        } else {
            System.out.println("User not found");
        }
    }

    @Test
    void testSearch() throws IOException {
        String searchText = "张三";
        SearchResponse<ObjectNode> response = client.search(s -> s
                        .index("user")
                        .query(q -> q
                                .match(t -> t
                                        .field("name")
                                        .query(searchText)
                                )
                        ),
                ObjectNode.class
        );
        TotalHits total = response.hits().total();
        boolean isExactResult = total.relation() == TotalHitsRelation.Eq;
        if (isExactResult) {
            System.out.println("There are " + total.value() + " results");
        } else {
            System.out.println("There are more than " + total.value() + " results");
        }
        List<Hit<ObjectNode>> hits = response.hits().hits();
        for (Hit<ObjectNode> hit : hits) {
            ObjectNode jsonNodes = hit.source();
            System.out.println("Found user id " + jsonNodes.get("id") + ", score " + hit.score());
        }
    }

    @Test
    void testAggregations() throws IOException {
        Query query = MatchQuery.of(m -> m
                .field("address")
                .query("mill")
        )._toQuery();
        SearchResponse<ObjectNode> response = client.search(search -> search
                        .index("test")
                        .query(query)
                        .aggregations("ageAgg", ageAgg -> ageAgg
                                .terms(terms -> terms
                                        .field("age")
                                        .size(10)
                                )
                                .aggregations("balanceAgg", balanceAgg -> balanceAgg
                                        .avg(avg -> avg
                                                .field("balance")
                                        )
                                )
                        ),
                ObjectNode.class
        );
        HitsMetadata<ObjectNode> OutHits = response.hits();
        List<Hit<ObjectNode>> innerHits = OutHits.hits();
        for (Hit<ObjectNode> hit : innerHits) {
            ObjectNode source = hit.source();
            Account account = JSON.parseObject(source.toString(), Account.class);
            System.out.println(account);
        }
        Map<String, Aggregate> aggregations = response.aggregations();
        LongTermsAggregate ageAgg = aggregations.get("ageAgg").lterms();
        for (LongTermsBucket bucket : ageAgg.buckets().array()) {
            System.out.println(
                    "年龄：" + bucket.key() + "，频数：" + bucket.docCount() + "，平均工资：" + bucket.aggregations().get("balanceAgg").avg().value()
            );
        }
    }

    @Test
    void testBulk() throws IOException {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setId(String.valueOf(i));
            user.setName("张三" + i);
            user.setAge(18 + i);
            users.add(user);
        }
        BulkRequest.Builder br = new BulkRequest.Builder();
        for (User user : users) {
            br.operations(op -> op
                    .index(idx -> idx
                            .index("user")
                            .id(user.getId())
                            .document(user)
                    )
            );
        }
        client.bulk(br.build());
    }

    @Data
    class User {
        private String id;
        private String name;
        private Integer age;
    }

    @Data
    class Account {
        private int accountNumber;
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