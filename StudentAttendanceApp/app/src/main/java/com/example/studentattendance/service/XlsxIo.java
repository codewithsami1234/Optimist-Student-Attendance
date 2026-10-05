package com.example.studentattendance.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Minimal XLSX reader/writer (Office Open XML) so imports and exports work
 * offline without Apache POI or a network.
 */
public final class XlsxIo {

    private XlsxIo() {
    }

    public static boolean looksLikeZip(byte[] bytes) {
        return bytes != null && bytes.length >= 2 && bytes[0] == 'P' && bytes[1] == 'K';
    }

    public static void write(OutputStream out, List<Sheet> sheets) throws IOException {
        if (sheets == null || sheets.isEmpty()) {
            throw new IOException("No sheets to write.");
        }
        try (ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            writeEntry(zip, "[Content_Types].xml", contentTypes(sheets.size()));
            writeEntry(zip, "_rels/.rels", rootRels());
            writeEntry(zip, "xl/workbook.xml", workbook(sheets));
            writeEntry(zip, "xl/_rels/workbook.xml.rels", workbookRels(sheets.size()));
            writeEntry(zip, "xl/styles.xml", styles());
            for (int i = 0; i < sheets.size(); i++) {
                writeEntry(zip, "xl/worksheets/sheet" + (i + 1) + ".xml", sheetXml(sheets.get(i)));
            }
        }
    }

    public static List<List<String>> readFirstSheet(byte[] bytes) throws IOException {
        String shared = null;
        String styles = null;
        String workbook = null;
        String workbookRels = null;
        java.util.Map<String, String> worksheets = new java.util.LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName() == null ? "" : entry.getName().replace('\\', '/');
                if (name.startsWith("/")) {
                    name = name.substring(1);
                }
                if ("xl/sharedStrings.xml".equalsIgnoreCase(name)) {
                    shared = readFully(zip);
                } else if ("xl/styles.xml".equalsIgnoreCase(name)) {
                    styles = readFully(zip);
                } else if ("xl/workbook.xml".equalsIgnoreCase(name)) {
                    workbook = readFully(zip);
                } else if ("xl/_rels/workbook.xml.rels".equalsIgnoreCase(name)) {
                    workbookRels = readFully(zip);
                } else if (isWorksheetPart(name)) {
                    worksheets.put(name.toLowerCase(java.util.Locale.US), readFully(zip));
                }
            }
        }
        String sheet = worksheetXml(worksheets, workbook, workbookRels);
        if (sheet == null) {
            throw new IOException("The Excel file has no worksheet.");
        }
        List<String> sharedStrings = parseSharedStrings(shared);
        return parseSheet(sheet, sharedStrings, parseStyleFormats(styles));
    }

    private static boolean isWorksheetPart(String name) {
        return name != null && name.toLowerCase(java.util.Locale.US).matches("xl/worksheets/sheet\\d+\\.xml");
    }

    private static String worksheetXml(java.util.Map<String, String> worksheets, String workbook, String rels) {
        if (worksheets == null || worksheets.isEmpty()) {
            return null;
        }
        String target = firstSheetTarget(workbook, rels);
        if (target != null) {
            String xml = worksheets.get(target.toLowerCase(java.util.Locale.US));
            if (xml != null) {
                return xml;
            }
        }
        String first = worksheets.get("xl/worksheets/sheet1.xml");
        if (first != null) {
            return first;
        }
        return worksheets.values().iterator().next();
    }

    private static String firstSheetTarget(String workbook, String rels) {
        if (workbook == null || rels == null) {
            return null;
        }
        int sheet = indexOfIgnoreCase(workbook, "<sheet");
        if (sheet < 0) {
            return null;
        }
        int end = workbook.indexOf('>', sheet);
        if (end < 0) {
            return null;
        }
        String tag = workbook.substring(sheet, end + 1);
        String rid = attr(tag, "r:id");
        if (rid.isEmpty()) {
            return null;
        }
        int pos = 0;
        while (true) {
            int rel = indexOfIgnoreCase(rels, "<Relationship", pos);
            if (rel < 0) {
                return null;
            }
            int relEnd = rels.indexOf('>', rel);
            if (relEnd < 0) {
                return null;
            }
            String relTag = rels.substring(rel, relEnd + 1);
            if (rid.equals(attr(relTag, "Id"))) {
                String target = attr(relTag, "Target").replace('\\', '/');
                while (target.startsWith("/")) {
                    target = target.substring(1);
                }
                if (target.toLowerCase(java.util.Locale.US).startsWith("worksheets/")) {
                    target = "xl/" + target;
                }
                return target;
            }
            pos = relEnd + 1;
        }
    }

    public static class Sheet {
        public final String name;
        public final List<String[]> rows;
        /** When >= 0, freezes rows above this 0-based header and enables AutoFilter. */
        public final int tableHeaderRowIndex;
        public final boolean professionalReport;

        public Sheet(String name, List<String[]> rows) {
            this(name, rows, -1, false);
        }

        public Sheet(String name, List<String[]> rows, int tableHeaderRowIndex, boolean professionalReport) {
            this.name = name == null || name.isEmpty() ? "Sheet1" : name;
            this.rows = rows == null ? new ArrayList<>() : rows;
            this.tableHeaderRowIndex = tableHeaderRowIndex;
            this.professionalReport = professionalReport;
        }
    }

    private static void writeEntry(ZipOutputStream zip, String path, String xml) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(xml.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String contentTypes(int sheetCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">");
        sb.append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>");
        sb.append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>");
        sb.append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>");
        sb.append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>");
        for (int i = 1; i <= sheetCount; i++) {
            sb.append("<Override PartName=\"/xl/worksheets/sheet").append(i)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        sb.append("</Types>");
        return sb.toString();
    }

    private static String rootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    private static String workbook(List<Sheet> sheets) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" ");
        sb.append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">");
        sb.append("<sheets>");
        for (int i = 0; i < sheets.size(); i++) {
            sb.append("<sheet name=\"").append(escape(sheets.get(i).name))
                    .append("\" sheetId=\"").append(i + 1)
                    .append("\" r:id=\"rId").append(i + 1).append("\"/>");
        }
        sb.append("</sheets></workbook>");
        return sb.toString();
    }

    private static String workbookRels(int sheetCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        for (int i = 1; i <= sheetCount; i++) {
            sb.append("<Relationship Id=\"rId").append(i)
                    .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet")
                    .append(i).append(".xml\"/>");
        }
        sb.append("<Relationship Id=\"rId").append(sheetCount + 1)
                .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>");
        sb.append("</Relationships>");
        return sb.toString();
    }

    private static String styles() {
        // 0 default, 1 title bold, 2 meta label bold, 3 table header, 4 bordered body
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<fonts count=\"3\">"
                + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"16\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "</fonts>"
                + "<fills count=\"3\">"
                + "<fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFD9E2F3\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "</fills>"
                + "<borders count=\"2\">"
                + "<border/>"
                + "<border>"
                + "<left style=\"thin\"/><right style=\"thin\"/><top style=\"thin\"/><bottom style=\"thin\"/>"
                + "</border>"
                + "</borders>"
                + "<cellStyleXfs count=\"1\"><xf/></cellStyleXfs>"
                + "<cellXfs count=\"5\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\"/>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\"/>"
                + "</cellXfs>"
                + "</styleSheet>";
    }

    private static String sheetXml(Sheet sheet) {
        if (sheet.professionalReport) {
            return reportSheetXml(sheet);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        for (int r = 0; r < sheet.rows.size(); r++) {
            String[] row = sheet.rows.get(r);
            if (row == null) {
                continue;
            }
            sb.append("<row r=\"").append(r + 1).append("\">");
            for (int c = 0; c < row.length; c++) {
                String ref = colName(c) + (r + 1);
                sb.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                        .append(escape(row[c] == null ? "" : row[c]))
                        .append("</t></is></c>");
            }
            sb.append("</row>");
        }
        sb.append("</sheetData></worksheet>");
        return sb.toString();
    }

    private static String reportSheetXml(Sheet sheet) {
        int colCount = 0;
        for (String[] row : sheet.rows) {
            if (row != null) {
                colCount = Math.max(colCount, row.length);
            }
        }
        if (colCount <= 0) {
            colCount = 1;
        }
        int headerIdx = sheet.tableHeaderRowIndex;
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" ");
        sb.append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">");
        sb.append("<sheetPr/><dimension ref=\"A1:").append(colName(colCount - 1))
                .append(Math.max(1, sheet.rows.size())).append("\"/>");
        sb.append("<sheetViews><sheetView workbookViewId=\"0\">");
        if (headerIdx >= 0) {
            sb.append("<pane ySplit=\"").append(headerIdx + 1)
                    .append("\" topLeftCell=\"A").append(headerIdx + 2)
                    .append("\" activePane=\"bottomLeft\" state=\"frozen\"/>");
        }
        sb.append("</sheetView></sheetViews>");
        sb.append("<cols>");
        double[] widths = {6, 18, 22, 18, 12, 10, 14, 22, 12, 10, 8, 10, 8, 12};
        for (int c = 0; c < colCount; c++) {
            double width = c < widths.length ? widths[c] : 14;
            sb.append("<col min=\"").append(c + 1).append("\" max=\"").append(c + 1)
                    .append("\" width=\"").append(width).append("\" customWidth=\"1\"/>");
        }
        sb.append("</cols><sheetData>");
        for (int r = 0; r < sheet.rows.size(); r++) {
            String[] row = sheet.rows.get(r);
            if (row == null) {
                continue;
            }
            sb.append("<row r=\"").append(r + 1).append("\">");
            for (int c = 0; c < row.length; c++) {
                int style = 0;
                if (r == 0) {
                    style = 1;
                } else if (r < headerIdx && c == 0) {
                    style = 2;
                } else if (r == headerIdx) {
                    style = 3;
                } else if (r > headerIdx) {
                    style = 4;
                }
                String ref = colName(c) + (r + 1);
                sb.append("<c r=\"").append(ref).append("\" s=\"").append(style)
                        .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                        .append(escape(row[c] == null ? "" : row[c]))
                        .append("</t></is></c>");
            }
            sb.append("</row>");
        }
        sb.append("</sheetData>");
        if (headerIdx >= 0 && headerIdx < sheet.rows.size()) {
            int lastRow = Math.max(headerIdx + 1, sheet.rows.size());
            String[] header = sheet.rows.get(headerIdx);
            int lastCol = header == null ? 0 : Math.max(0, header.length - 1);
            sb.append("<autoFilter ref=\"A").append(headerIdx + 1).append(":")
                    .append(colName(lastCol)).append(lastRow).append("\"/>");
        }
        sb.append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.75\" bottom=\"0.75\" header=\"0.3\" footer=\"0.3\"/>");
        sb.append("<pageSetup orientation=\"landscape\" fitToPage=\"1\" fitToWidth=\"1\" fitToHeight=\"0\"/>");
        sb.append("</worksheet>");
        return sb.toString();
    }

    static String colName(int index) {
        StringBuilder sb = new StringBuilder();
        int n = index + 1;
        while (n > 0) {
            int rem = (n - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            n = (n - 1) / 26;
        }
        return sb.toString();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toString("UTF-8");
    }

    /**
     * One shared-string item is one cell value. Excel stores formatted headers
     * as several text runs inside a single {@code si}; those runs must stay one string.
     */
    private static List<String> parseSharedStrings(String xml) {
        List<String> values = new ArrayList<>();
        if (xml == null || xml.isEmpty()) {
            return values;
        }
        int pos = 0;
        while (true) {
            int start = indexOfIgnoreCase(xml, "<si", pos);
            if (start < 0) {
                break;
            }
            int end = indexOfIgnoreCase(xml, "</si>", start);
            if (end < 0) {
                break;
            }
            String item = xml.substring(start, end);
            item = stripTag(item, "rPh");
            item = stripTag(item, "phoneticPr");
            values.add(concatenateText(item));
            pos = end + 5;
        }
        return values;
    }

    private static String concatenateText(String xml) {
        StringBuilder text = new StringBuilder();
        int pos = 0;
        while (true) {
            int t = indexOfIgnoreCase(xml, "<t", pos);
            if (t < 0) {
                break;
            }
            int gt = xml.indexOf('>', t);
            if (gt < 0) {
                break;
            }
            if (xml.charAt(gt - 1) == '/') {
                pos = gt + 1;
                continue;
            }
            int end = indexOfIgnoreCase(xml, "</t>", gt);
            if (end < 0) {
                break;
            }
            text.append(unescape(xml.substring(gt + 1, end)));
            pos = end + 4;
        }
        return text.toString();
    }

    private static String stripTag(String xml, String tag) {
        String open = "<" + tag;
        String close = "</" + tag + ">";
        StringBuilder out = new StringBuilder();
        int pos = 0;
        while (true) {
            int start = indexOfIgnoreCase(xml, open, pos);
            if (start < 0) {
                out.append(xml.substring(pos));
                break;
            }
            out.append(xml, pos, start);
            int end = indexOfIgnoreCase(xml, close, start);
            if (end < 0) {
                break;
            }
            pos = end + close.length();
        }
        return out.toString();
    }

    private static List<List<String>> parseSheet(String xml, List<String> shared, List<String> styleFormats) {
        List<List<String>> rows = new ArrayList<>();
        int pos = 0;
        while (true) {
            int rowStart = indexOfIgnoreCase(xml, "<row", pos);
            if (rowStart < 0) {
                break;
            }
            int rowEnd = indexOfIgnoreCase(xml, "</row>", rowStart);
            if (rowEnd < 0) {
                break;
            }
            String rowXml = xml.substring(rowStart, rowEnd);
            List<String> cells = new ArrayList<>();
            int cpos = 0;
            int maxCol = -1;
            java.util.Map<Integer, String> byCol = new java.util.TreeMap<>();
            while (true) {
                int cStart = indexOfIgnoreCase(rowXml, "<c ", cpos);
                if (cStart < 0) {
                    cStart = indexOfIgnoreCase(rowXml, "<c>", cpos);
                }
                if (cStart < 0) {
                    break;
                }
                int cTagEnd = rowXml.indexOf('>', cStart);
                if (cTagEnd < 0) {
                    break;
                }
                String open = rowXml.substring(cStart, cTagEnd + 1);
                boolean selfClosing = open.endsWith("/>");
                int cEnd = -1;
                if (!selfClosing) {
                    cEnd = indexOfIgnoreCase(rowXml, "</c>", cTagEnd);
                    if (cEnd < 0) {
                        break;
                    }
                }
                String body = selfClosing ? "" : rowXml.substring(cTagEnd + 1, cEnd);
                int col = columnIndex(attr(open, "r"));
                boolean sharedRef = open.contains("t=\"s\"") || open.contains("t='s'");
                String value = cellText(body, sharedRef, shared, numberFormat(open, styleFormats));
                if (col >= 0) {
                    byCol.put(col, value);
                    maxCol = Math.max(maxCol, col);
                } else {
                    cells.add(value);
                }
                cpos = selfClosing ? cTagEnd + 1 : cEnd + 4;
            }
            if (!byCol.isEmpty()) {
                for (int i = 0; i <= maxCol; i++) {
                    cells.add(byCol.getOrDefault(i, ""));
                }
            }
            if (!cells.isEmpty()) {
                rows.add(cells);
            }
            pos = rowEnd + 6;
        }
        return rows;
    }

    private static String cellText(String body, boolean sharedRef, List<String> shared, String numberFormat) {
        String inline = extractTagged(body, "t");
        if (inline != null) {
            return unescape(inline);
        }
        String v = extractTagged(body, "v");
        if (v == null) {
            return "";
        }
        if (sharedRef) {
            try {
                int idx = Integer.parseInt(v.trim());
                if (idx >= 0 && shared != null && idx < shared.size()) {
                    return shared.get(idx);
                }
            } catch (NumberFormatException ignored) {
                return v;
            }
            return v;
        }
        return applyLeadingZeroFormat(unescape(v).trim(), numberFormat);
    }

    /**
     * Excel stores a text roll such as {@code 001} as a shared string. A numeric
     * cell with a zero-padded format such as {@code 000} is stored as {@code 1};
     * apply that format so the leading zeros survive.
     */
    static String applyLeadingZeroFormat(String value, String format) {
        if (value == null) {
            return "";
        }
        if (format == null) {
            return value;
        }
        String fmt = format.trim();
        if (!fmt.matches("0+")) {
            return value;
        }
        String digits = value.trim();
        int dot = digits.indexOf('.');
        if (dot > 0 && digits.substring(dot).matches("\\.0+")) {
            digits = digits.substring(0, dot);
        }
        if (!digits.matches("\\d+")) {
            return value;
        }
        if (digits.length() >= fmt.length()) {
            return digits;
        }
        StringBuilder padded = new StringBuilder(fmt.length());
        for (int i = digits.length(); i < fmt.length(); i++) {
            padded.append('0');
        }
        padded.append(digits);
        return padded.toString();
    }

    private static String numberFormat(String openTag, List<String> styleFormats) {
        if (styleFormats == null || styleFormats.isEmpty()) {
            return null;
        }
        String style = attr(openTag, "s");
        if (style.isEmpty()) {
            return null;
        }
        try {
            int index = Integer.parseInt(style.trim());
            if (index >= 0 && index < styleFormats.size()) {
                return styleFormats.get(index);
            }
        } catch (NumberFormatException ignored) {
            return null;
        }
        return null;
    }

    private static List<String> parseStyleFormats(String xml) {
        List<String> formats = new ArrayList<>();
        if (xml == null || xml.isEmpty()) {
            return formats;
        }
        String lower = xml.toLowerCase(java.util.Locale.US);
        java.util.Map<Integer, String> custom = new java.util.HashMap<>();
        int search = 0;
        while (true) {
            int tag = lower.indexOf("<numfmt", search);
            if (tag < 0) {
                break;
            }
            int end = xml.indexOf('>', tag);
            if (end < 0) {
                break;
            }
            String open = xml.substring(tag, end + 1);
            try {
                int id = Integer.parseInt(attr(open, "numFmtId").trim());
                custom.put(id, unescape(attr(open, "formatCode")));
            } catch (NumberFormatException ignored) {
                // skip a malformed format entry
            }
            search = end + 1;
        }
        int xfs = lower.indexOf("<cellxfs");
        int xfsEnd = xfs < 0 ? -1 : lower.indexOf("</cellxfs>", xfs);
        if (xfs < 0 || xfsEnd < 0) {
            return formats;
        }
        search = xfs;
        while (search < xfsEnd) {
            int tag = lower.indexOf("<xf", search);
            if (tag < 0 || tag >= xfsEnd) {
                break;
            }
            int end = xml.indexOf('>', tag);
            if (end < 0) {
                break;
            }
            String open = xml.substring(tag, end + 1);
            String format = null;
            try {
                int id = Integer.parseInt(attr(open, "numFmtId").trim());
                if (custom.containsKey(id)) {
                    format = custom.get(id);
                } else {
                    format = builtinNumberFormat(id);
                }
            } catch (NumberFormatException ignored) {
                format = null;
            }
            formats.add(format);
            search = end + 1;
        }
        return formats;
    }

    private static String builtinNumberFormat(int id) {
        switch (id) {
            case 1:
                return "0";
            case 2:
                return "0.00";
            case 49:
                return "@";
            default:
                return null;
        }
    }

    private static String extractTagged(String haystack, String tag) {
        String open = "<" + tag;
        int a = indexOfIgnoreCase(haystack, open);
        if (a < 0) {
            return null;
        }
        int gt = haystack.indexOf('>', a);
        if (gt < 0) {
            return null;
        }
        String close = "</" + tag + ">";
        int b = indexOfIgnoreCase(haystack, close, gt);
        if (b < 0) {
            return null;
        }
        return haystack.substring(gt + 1, b);
    }

    private static int columnIndex(String cellRef) {
        if (cellRef == null || cellRef.isEmpty()) {
            return -1;
        }
        int n = 0;
        for (int i = 0; i < cellRef.length(); i++) {
            char ch = cellRef.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                n = n * 26 + (ch - 'A' + 1);
            } else if (ch >= 'a' && ch <= 'z') {
                n = n * 26 + (ch - 'a' + 1);
            } else {
                break;
            }
        }
        return n - 1;
    }

    private static String attr(String tag, String name) {
        String needle = name + "=\"";
        int i = tag.indexOf(needle);
        if (i < 0) {
            needle = name + "='";
            i = tag.indexOf(needle);
            if (i < 0) {
                return "";
            }
            int end = tag.indexOf('\'', i + needle.length());
            return end < 0 ? "" : tag.substring(i + needle.length(), end);
        }
        int end = tag.indexOf('"', i + needle.length());
        return end < 0 ? "" : tag.substring(i + needle.length(), end);
    }

    private static int indexOfIgnoreCase(String haystack, String needle) {
        return indexOfIgnoreCase(haystack, needle, 0);
    }

    private static int indexOfIgnoreCase(String haystack, String needle, int from) {
        return haystack.toLowerCase().indexOf(needle.toLowerCase(), from);
    }

    private static String unescape(String value) {
        return value.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&#39;", "'");
    }
}
