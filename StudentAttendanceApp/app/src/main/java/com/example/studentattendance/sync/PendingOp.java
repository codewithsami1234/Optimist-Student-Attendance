package com.example.studentattendance.sync;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PendingOp {

    public String id;
    public String type;
    public String localId;
    public Map<String, Object> payload = new LinkedHashMap<>();
    public int attempts;
    public long createdAtMillis;

    public static PendingOp create(String type, String localId, Map<String, Object> payload) {
        PendingOp op = new PendingOp();
        op.id = "op_" + UUID.randomUUID();
        op.type = type;
        op.localId = localId;
        op.payload = payload == null ? new LinkedHashMap<>() : payload;
        op.attempts = 0;
        op.createdAtMillis = System.currentTimeMillis();
        return op;
    }
}
