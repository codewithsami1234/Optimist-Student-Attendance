package com.example.studentattendance.utils;

import android.app.Activity;
import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.List;
import java.util.Locale;

import com.example.studentattendance.model.StudentReport;

/**
 * Printable / PDF attendance report using the system PrintManager and a
 * WebView. No extra PDF library is required.
 */
public final class PdfReportHelper {

    private PdfReportHelper() {
    }

    public static void printStudentReports(Activity activity, List<StudentReport> reports, double overallRate) {
        if (activity == null || reports == null) {
            return;
        }
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='utf-8'/><style>")
                .append("body{font-family:sans-serif;padding:16px;color:#111}")
                .append("h1{font-size:20px} table{width:100%;border-collapse:collapse}")
                .append("th,td{border:1px solid #ccc;padding:6px;font-size:12px;text-align:left}")
                .append("th{background:#f3f3f3}</style></head><body>")
                .append("<h1>Attendance Report</h1>")
                .append("<p>Overall rate: ").append(String.format(Locale.US, "%.1f%%", overallRate))
                .append(" · Policy: Present + Late count as attended; Absent counts against; Leave is excused.</p>")
                .append("<table><tr><th>Roll</th><th>Name</th><th>Present</th><th>Absent</th>")
                .append("<th>Late</th><th>Leave</th><th>Adjustment</th><th>%</th></tr>");
        for (StudentReport r : reports) {
            html.append("<tr><td>").append(esc(r.getRollNumber())).append("</td><td>")
                    .append(esc(r.getStudentName())).append("</td><td>")
                    .append(r.getPresentDays()).append("</td><td>")
                    .append(r.getAbsentDays()).append("</td><td>")
                    .append(r.getLateDays()).append("</td><td>")
                    .append(r.getLeaveDays()).append("</td><td>")
                    .append(r.getAdjustmentDelta()).append("</td><td>")
                    .append(String.format(Locale.US, "%.1f", r.getAttendancePercentage()))
                    .append("</td></tr>");
        }
        html.append("</table></body></html>");

        WebView webView = new WebView(activity);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                PrintManager printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);
                if (printManager != null) {
                    printManager.print("Attendance Report",
                            view.createPrintDocumentAdapter("Attendance Report"),
                            new PrintAttributes.Builder().build());
                }
            }
        });
        webView.loadDataWithBaseURL(null, html.toString(), "text/html", "UTF-8", null);
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
