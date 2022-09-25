package com.su.gulimall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.su.common.to.es.SkuEsModel;
import com.su.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ProductSaveSeriveImpl implements ProductSaveService {

    @Autowired
    private ElasticsearchClient client;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        BulkRequest.Builder br = new BulkRequest.Builder();
        for (SkuEsModel model : skuEsModels) {
            br.operations(op -> op
                    .index(idx -> idx
                            .index("product")
                            .id(model.getSkuId().toString())
                            .document(model)
                    )
            );
        }
        BulkResponse bulkResponse = client.bulk(br.build());
        List<String> list = bulkResponse.items().stream().map(BulkResponseItem::id).toList();
        log.info("商品上架完成：{}", list);
        return bulkResponse.errors();
    }
}
