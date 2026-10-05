package com.example.studentattendance.repository.impl;

import java.util.List;

import android.content.Context;

import com.example.studentattendance.model.AttendanceAdjustment;
import com.example.studentattendance.network.ApiCallback;
import com.example.studentattendance.network.ApiClient;
import com.example.studentattendance.network.ApiService;
import com.example.studentattendance.network.RetrofitExec;
import com.example.studentattendance.network.dto.AttendanceAdjustmentRequestDto;
import com.example.studentattendance.repository.AttendanceAdjustmentRepository;

public class RetrofitAttendanceAdjustmentRepository implements AttendanceAdjustmentRepository {

    private final Context appContext;

    public RetrofitAttendanceAdjustmentRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    private ApiService api() {
        return ApiClient.getApiService(appContext);
    }

    @Override
    public void createAdjustment(String studentId, int delta, String reason, ApiCallback<AttendanceAdjustment> callback) {
        RetrofitExec.enqueue(api().createAdjustment(studentId, new AttendanceAdjustmentRequestDto(delta, reason)), callback);
    }

    @Override
    public void getHistory(String studentId, ApiCallback<List<AttendanceAdjustment>> callback) {
        RetrofitExec.enqueue(api().getAdjustmentHistory(studentId), callback);
    }
}
