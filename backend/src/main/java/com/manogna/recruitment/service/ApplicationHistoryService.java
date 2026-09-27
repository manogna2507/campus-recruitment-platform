package com.manogna.recruitment.service;

import com.manogna.recruitment.dynamo.ApplicationStatusHistoryItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationHistoryService {

    private final DynamoDbTable<ApplicationStatusHistoryItem> table;

    public ApplicationHistoryService(DynamoDbEnhancedClient enhancedClient,
                                      @Value("${aws.dynamodb.table-name}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(ApplicationStatusHistoryItem.class));
    }

    public void recordTransition(Long applicationId, String previousStatus, String newStatus, String changedByEmail) {
        ApplicationStatusHistoryItem item = new ApplicationStatusHistoryItem();
        item.setApplicationId(String.valueOf(applicationId));
        item.setTimestamp(Instant.now().toString());
        item.setPreviousStatus(previousStatus);
        item.setNewStatus(newStatus);
        item.setChangedBy(changedByEmail);
        table.putItem(item);
    }

    public List<ApplicationStatusHistoryItem> getHistory(Long applicationId) {
        QueryConditional condition = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(String.valueOf(applicationId)).build());
        return table.query(condition).items().stream().collect(Collectors.toList());
    }
}
