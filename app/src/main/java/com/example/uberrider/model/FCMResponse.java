package com.example.uberrider.model;

import java.util.List;

public class FCMResponse {
    public long multicast_id;
    public int success;
    public int failure;
    public int canonical_ids;
    public List<FCMResult> results;

    public FCMResponse() {

    }

    public FCMResponse(long multicast_id, int success, int failure, int canonical_ids, List<FCMResult> FCMResults) {
        this.multicast_id = multicast_id;
        this.success = success;
        this.failure = failure;
        this.canonical_ids = canonical_ids;
        this.results = FCMResults;
    }

    public long getMulticast_id() {
        return multicast_id;
    }

    public void setMulticast_id(long multicast_id) {
        this.multicast_id = multicast_id;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }

    public int getCanonical_ids() {
        return canonical_ids;
    }

    public void setCanonical_ids(int canonical_ids) {
        this.canonical_ids = canonical_ids;
    }

    public List<FCMResult> getResults() {
        return results;
    }

    public void setResults(List<FCMResult> FCMResults) {
        this.results = FCMResults;
    }
}
