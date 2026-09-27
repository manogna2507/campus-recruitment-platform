package com.manogna.recruitment.dynamo;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * One row per status transition, stored in DynamoDB rather than the
 * relational DB: it's an append-only audit trail (write-heavy, rarely
 * queried by anything except "history for application X"), which is exactly
 * the access pattern DynamoDB's partition/sort key model is built for.
 *
 * Partition key: applicationId (all history for one application together)
 * Sort key:      timestamp (naturally ordered, ISO-8601 string)
 */
@DynamoDbBean
public class ApplicationStatusHistoryItem {

    private String applicationId;
    private String timestamp;
    private String previousStatus;
    private String newStatus;
    private String changedBy;

    @DynamoDbPartitionKey
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @DynamoDbSortKey
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }
}
