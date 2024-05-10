package com.example.my_app.screens.merchandiser;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.example.my_app.R;

import androidx.annotation.NonNull;

import android.graphics.Color;

import com.example.my_app.shared.GlobalVariable;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.view.Gravity;
import android.widget.Toast;

public class Revenue extends AppCompatActivity {

    private String uid;
    private BarChart barChart;
    private FirebaseFirestore db;
    private Map<Integer, Integer> monthlyRevenueMap;
    private Map<String, Integer> nameAndTotalQuantity;
    private Map<String, Integer> nameAndTotalRevenue;
    private boolean isSearched = false;
    private int totalRevenue2 = 0;
    EditText edtYear;
    Button btnSearchYear, btnExport;
    TextView tvRevenue, tvVND, tvTitle;
    TableLayout tlRevenue;
    Calendar calendar;

    NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // lúc gộp code với nhóm mới sử dụng được code comment phía dưới
        uid = GlobalVariable.userInfo.getUid();
//        uid = "lQxK1UIVefjhVDpNyA34";

        setControl();
        setEvent();

    }

    private void setEvent() {
        btnSearchYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validYear()){
                    return;
                }

                // Lấy trình quản lý bàn phím
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                // Ẩn bàn phím
                inputMethodManager.hideSoftInputFromWindow(edtYear.getWindowToken(), 0);

                // xử lý tính toán
                calculateAndDrawRevenue();

                // Đã tìm kiếm, đặt cờ là true
                isSearched = true;
            }

        });

        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validYear()){
                    return;
                }
                // Lấy trình quản lý bàn phím
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                // Ẩn bàn phím
                inputMethodManager.hideSoftInputFromWindow(edtYear.getWindowToken(), 0);

                // Kiểm tra xem đã tìm kiếm trước đó hay chưa
                if (!isSearched) {
                    Toast.makeText(Revenue.this, "You haven't searched yet", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Check xem năm đó có doanh thu hay không
                if(totalRevenue2==0){
                    Toast.makeText(Revenue.this, "No data", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    createPdf();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private boolean validYear(){
        if (edtYear.getText().toString().equals("")) {
            edtYear.setError("Chưa nhập số năm");
            edtYear.requestFocus();
            return false;
        }

        try {
            int year = Integer.parseInt(edtYear.getText().toString());
            // Nếu năm <2000 và lớn hơn năm hiện tại
            if (year < 2000 || year > Calendar.getInstance().get(Calendar.YEAR)) {
                edtYear.setError("Năm không hợp lệ");
                return false;
            }
        } catch (NumberFormatException e) {
            //text nhập vào ko phải chữ số
            edtYear.setError("Nhập số năm hợp lệ");
            return false;
        }
        return true;
    }

    private void createPdf() throws FileNotFoundException{

        String pdfPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        File file = new File(pdfPath, "RevenueStatistics"+edtYear.getText().toString()+".pdf");
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        PdfWriter writer = new PdfWriter(fileOutputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        float columnWidth[] = {150f, 200f, 350f};
        Table table1 = new Table(columnWidth);

        table1.addCell("");

        Drawable d = getDrawable(R.drawable.shop_logo_nonbackground);
        Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapData = stream.toByteArray();

        ImageData imageData = ImageDataFactory.create(bitmapData);
        Image image = new Image(imageData);
        image.setAutoScale(true);

        //row1
        table1.addCell(image);
        table1.addCell(new Cell()
                .setTextAlignment(TextAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(new Paragraph("E-Commerce")
                        .setItalic()
                        .setFontSize(18)));

        //row2
        table1.addCell(new Cell(1,3).add(new Paragraph("")).setHeight(15));

        String tmpName = GlobalVariable.userInfo.getDisplayName();
        String tmpPhoneNumber = GlobalVariable.userInfo.getPhone();

        //row3
        table1.addCell("Merchandiser:");
        table1.addCell(new Cell(1,2).add(new Paragraph(tmpName)));

        //row4
        table1.addCell("Phone number:");
        table1.addCell(new Cell(1,2).add(new Paragraph(tmpPhoneNumber)));

        // lấy thời gian hiện tại
        calendar = Calendar.getInstance();
        // Định dạng ngày giờ
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy HH:mm:ss", Locale.getDefault());

        //row5
        table1.addCell("Created Date:");
        table1.addCell(new Cell(1,2).add(new Paragraph(sdf.format(calendar.getTime()))));

        //row6
        table1.addCell(new Cell(1,3).add(new Paragraph("")).setHeight(20));

        //row7
        table1.addCell(new Cell(1,3).add(new Paragraph("REVENUE STATISTICS "+edtYear.getText().toString()))
                .setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(20)
                .setFontColor(new DeviceRgb(238, 90, 36))
                .setBackgroundColor(new DeviceRgb(241, 242, 246)));

        //row8
        table1.addCell(new Cell(1,3).add(new Paragraph("")).setHeight(10));

        // table2
        float columnWidth2[] = {50f, 300f, 120f, 200f};
        Table table2 = new Table(columnWidth2);

        //rowTitle
        table2.addCell(new Cell().setBackgroundColor(new DeviceRgb(255, 121, 63))
                .add(new Paragraph("No")).setFontColor(new DeviceRgb(255, 255, 255))
                .setBold().setFontSize(16));
        table2.addCell(new Cell().setBackgroundColor(new DeviceRgb(255, 121, 63))
                .add(new Paragraph("Product name")).setFontColor(new DeviceRgb(255, 255, 255))
                .setBold().setFontSize(16));
        table2.addCell(new Cell().setBackgroundColor(new DeviceRgb(255, 121, 63))
                .add(new Paragraph("Quantity")).setFontColor(new DeviceRgb(255, 255, 255))
                .setBold()).setFontSize(16);
        table2.addCell(new Cell().setBackgroundColor(new DeviceRgb(255, 121, 63))
                .add(new Paragraph("Revenue")).setFontColor(new DeviceRgb(255, 255, 255))
                .setBold().setFontSize(16));

        // Chuyển Map thành một danh sách các phần tử (entries)
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(nameAndTotalRevenue.entrySet());

        // Sắp xếp danh sách các phần tử theo giá trị giảm dần
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
                // So sánh giá trị của các phần tử và trả về kết quả tương ứng
                return entry2.getValue().compareTo(entry1.getValue());
            }
        });

        // Tạo một Map mới để lưu trữ các phần tử đã sắp xếp
        Map<String, Integer> sortedMapRevenue2 = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entries) {
            sortedMapRevenue2.put(entry.getKey(), entry.getValue());
        }

        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedMapRevenue2.entrySet()) {
            String sortedKey = entry.getKey();
            Integer sortedValue2 = entry.getValue();
            Integer sortedValue1 = nameAndTotalQuantity.getOrDefault(sortedKey, 0);
            count++;

            //add row
            table2.addCell(String.valueOf(count));
            table2.addCell(sortedKey);
            table2.addCell(formatter.format(sortedValue1));
            table2.addCell(formatter.format(sortedValue2));

        }

        //row final
        table2.addCell("").addCell("");
        table2.addCell(new Cell().setBackgroundColor(new DeviceRgb(0, 148, 50))
                .add(new Paragraph("Total:")).setFontColor(new DeviceRgb(255, 255, 255))
                .setBold().setFontSize(16));
        table2.addCell(new Cell().setBackgroundColor(new DeviceRgb(255, 71, 87))
                .add(new Paragraph(formatter.format(totalRevenue2))).setFontColor(new DeviceRgb(255, 255, 255))
                .setBold().setFontSize(16));

        // Vẽ barchart pdf
        // Tạo bitmap từ biểu đồ
        Bitmap chartBitmap = Bitmap.createBitmap(barChart.getWidth(), barChart.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(chartBitmap);
        barChart.draw(canvas);//vẽ biểu đồ lên bitmap

        // Chuyển đổi bitmap thành byte array
        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
        chartBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream2);
        byte[] chartBitmapData = stream2.toByteArray();

        // Tạo hình ảnh từ byte array và thêm vào tài liệu PDF
        Image chartImage = new Image(ImageDataFactory.create(chartBitmapData));
        chartImage.setHorizontalAlignment(HorizontalAlignment.CENTER);

        document.add(table1);
        document.add(table2);
        document.add(chartImage);

        document.close();
        Toast.makeText(this, "Pdf created", Toast.LENGTH_SHORT).show();
    }

    private void setControl() {
        barChart = findViewById(R.id.barChart);
        edtYear =  findViewById(R.id.edtYear);
        btnSearchYear = findViewById(R.id.btnSearchYear);
        tvRevenue = findViewById(R.id.tvRevenue);
        tvVND = findViewById(R.id.tvVND);
        tlRevenue = findViewById(R.id.tlRevenue);
        tvTitle = findViewById(R.id.tvTitle);
        btnExport = findViewById(R.id.btnExport);
    }

    private void calculateAndDrawRevenue() {
        // Map để lưu tổng doanh thu của mỗi tháng
        monthlyRevenueMap = new HashMap<>();
        //Gán giá trị ban đầu
        for (int month = 1; month <= 12; month++) {
            monthlyRevenueMap.put(month, 0);
        }

        //Khởi tạo biến totalRevenue để lưu lại tổng doanh thu trong năm được lấy từ edtYear
        final int[] totalRevenue = {0};

        //Su dụng map để lưu <tên sản phẩm, tổng số lượng đã bán>, <tên sản phẩm, tổng doanh thu từ sp đó
        nameAndTotalQuantity = new HashMap<>();
        nameAndTotalRevenue = new HashMap<>();

        // Truy vấn tất cả các sản phẩm mà mechandiser bán
        // Chỉ lấy các sản phẩm đang bán của uid người bán đang đăng nhập
        db.collection("products")
                .whereEqualTo("uid", uid)
                //.whereEqualTo("uid", GlobalVariable.userInfo.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot productQueryDocumentSnapshots) {
                        if (!productQueryDocumentSnapshots.isEmpty()) {
                            // Lặp qua từng sản phẩm mà merchandiser đang bán
                            for (QueryDocumentSnapshot productSnapshot : productQueryDocumentSnapshots) {
                                // truy vấn chi tiết đơn đặt hàng
                                // Lấy các đơn đặt hàng có các sản phẩm mà merchandiser đang bán
                                db.collection("order_detail")
                                        .whereEqualTo("productId", productSnapshot.getId())
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot orderDetailQueryDocumentSnapshots) {
                                                if (!orderDetailQueryDocumentSnapshots.isEmpty()) {
                                                    // Lặp qua từng đơn đặt hàng
                                                    for (QueryDocumentSnapshot orderDetailSnapshot : orderDetailQueryDocumentSnapshots) {
                                                        int price;
                                                        try {
                                                            //lấy giá đơn hàng
                                                            Number numberPrice = formatter.parse(orderDetailSnapshot.getString("price"));
                                                            price = numberPrice.intValue();
                                                        } catch (ParseException e) {
                                                            throw new RuntimeException(e);
                                                        }

                                                        int quantity = Math.toIntExact(orderDetailSnapshot.getLong("quantity"));

                                                        String orderIdStr = orderDetailSnapshot.getString("orderId");

                                                        db.collection("delivery_status")
                                                                .whereEqualTo("statusName", "Đã hoàn thành")
                                                                .get()
                                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(QuerySnapshot deliveryStatusQueryDocumentSnapshots) {
                                                                        if (!deliveryStatusQueryDocumentSnapshots.isEmpty()) {
                                                                            String statusIdStr = deliveryStatusQueryDocumentSnapshots.getDocuments().get(0).getId();
                                                                            //System.out.println(statusIdStr);

                                                                            //chỉ lấy đơn hàng đã hoàn thành
                                                                            db.collection("ds_detail")
                                                                                    .whereEqualTo("orderId", orderIdStr)
                                                                                    .whereEqualTo("statusId", statusIdStr)
                                                                                    .get()
                                                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                                        @Override
                                                                                        public void onSuccess(QuerySnapshot dsDetailQueryDocumentSnapshots) {
                                                                                            if (!dsDetailQueryDocumentSnapshots.isEmpty()) {
                                                                                                // Lấy thời gian hoàn thành đơn hàng
                                                                                                Date createDate = dsDetailQueryDocumentSnapshots.getDocuments().get(0).getTimestamp("dateOfStatus").toDate();
                                                                                                //System.out.println(createDate);
                                                                                                calendar = Calendar.getInstance();
                                                                                                calendar.setTimeInMillis(createDate.getTime());

                                                                                                if (Integer.parseInt(edtYear.getText().toString()) == calendar.get(calendar.YEAR)) {
                                                                                                    //System.out.println(createDate);
                                                                                                    int completedMonth = calendar.get(Calendar.MONTH) + 1;
                                                                                                    //System.out.println(completedMonth);

                                                                                                    // Cập nhật tổng doanh thu của tháng
                                                                                                    int newRevenue = monthlyRevenueMap.getOrDefault(completedMonth, 0);
                                                                                                    newRevenue += price * quantity;
                                                                                                    monthlyRevenueMap.put(completedMonth, newRevenue);

                                                                                                    totalRevenue[0] += price * quantity;

                                                                                                    String newKey = productSnapshot.getString("productName"); // Đây là khóa muốn thêm vào Map
                                                                                                    if (!nameAndTotalQuantity.containsKey(newKey)) {
                                                                                                        // Nếu khóa không tồn tại trong Map, thêm khóa mới với giá trị quantity
                                                                                                        nameAndTotalQuantity.put(newKey, quantity);
                                                                                                        nameAndTotalRevenue.put(newKey, price * quantity);
                                                                                                    } else {
                                                                                                        // Nếu khóa đã tồn tại trong Map, không thực hiện ghi đè giá trị
                                                                                                        // Cập nhật tổng doanh thu của sản pham
                                                                                                        int tempQuantity = nameAndTotalQuantity.getOrDefault(newKey, 0);
                                                                                                        tempQuantity += quantity;
                                                                                                        nameAndTotalQuantity.put(newKey, tempQuantity);

                                                                                                        int tempRevenue = nameAndTotalRevenue.getOrDefault(newKey, 0);
                                                                                                        tempRevenue += price * quantity;
                                                                                                        nameAndTotalRevenue.put(newKey, tempRevenue);
                                                                                                    }

                                                                                                }

                                                                                            }
                                                                                            // Vẽ biểu đồ cột sau khi đã tính toán xong
                                                                                            drawBarChart(monthlyRevenueMap);

                                                                                            //Gán tổng doanh thu vào text view
                                                                                            tvRevenue.setText(formatter.format(totalRevenue[0]));

                                                                                            totalRevenue2 = totalRevenue[0];

                                                                                            //Vẽ bảng
                                                                                            drawTable(nameAndTotalQuantity, nameAndTotalRevenue);

                                                                                        }
                                                                                    });

                                                                        }
                                                                    }
                                                                });

                                                    }
                                                }
                                            }
                                        });

                            }
                        }
                    }
                });

    }

    private void drawTable(Map<String, Integer> nameAndTotalQuantity, Map<String, Integer> nameAndTotalRevenue) {
        tlRevenue.setPadding(8,8,8,8);
        tvTitle.setText("Bảng: Top 5 sản phẩm có doanh thu cao nhất năm "+edtYear.getText().toString());

        // Chuyển Map thành một danh sách các phần tử (entries)
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(nameAndTotalRevenue.entrySet());

        // Sắp xếp danh sách các phần tử theo giá trị giảm dần
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
                // So sánh giá trị của các phần tử và trả về kết quả tương ứng
                return entry2.getValue().compareTo(entry1.getValue());
            }
        });

        // Tạo một Map mới để lưu trữ các phần tử đã sắp xếp
        Map<String, Integer> sortedMapRevenue = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entries) {
            sortedMapRevenue.put(entry.getKey(), entry.getValue());
        }

        // Xóa tất cả các TableRow hiện có khỏi TableLayout, đỡ trùng lặp dữ liệu
        tlRevenue.removeAllViews();
        int count = 0; // Biến đếm số lượng phần tử đã in ra
        for (Map.Entry<String, Integer> entry : sortedMapRevenue.entrySet()) {
            if (count < 5) {
                String sortedKey = entry.getKey();
                Integer sortedValue2 = entry.getValue();
                Integer sortedValue1 = nameAndTotalQuantity.getOrDefault(sortedKey,0);
                count++;

                //Tạo một TableRow mới
                TableRow row = new TableRow(Revenue.this);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(layoutParams);
                row.setBackgroundColor(Color.parseColor("#F5F5F5"));

                // Thêm TextViews vào TableRow mới
                TextView tvProductName = new TextView(Revenue.this);
                tvProductName.setLayoutParams(new TableRow.LayoutParams(300, TableRow.LayoutParams.WRAP_CONTENT)); // Đặt kích thước
                tvProductName.setText(sortedKey);
                tvProductName.setPadding(8, 8, 8, 8);
                row.addView(tvProductName);

                TextView tvQuantity = new TextView(Revenue.this);
                tvQuantity.setLayoutParams(new TableRow.LayoutParams(85, TableRow.LayoutParams.WRAP_CONTENT)); // Đặt kích thước
                tvQuantity.setText(formatter.format(sortedValue1));
                tvQuantity.setPadding(8, 8, 8, 8);
                row.addView(tvQuantity);

                TextView tvRevenueSP = new TextView(Revenue.this);
                tvRevenueSP.setLayoutParams(new TableRow.LayoutParams(105, TableRow.LayoutParams.WRAP_CONTENT)); // Đặt kích thước
                tvRevenueSP.setText(formatter.format(sortedValue2));
                tvRevenueSP.setPadding(8, 8, 8, 8);
                row.addView(tvRevenueSP);

                // Thêm TableRow mới vào TableLayout
                tlRevenue.addView(row);

            } else {
                break; // Thoát khỏi vòng lặp nếu đã in ra 5 phần tử
            }
        }

    }

    private void drawBarChart(Map<Integer, Integer> monthlyRevenueMap) {
        // Chuẩn bị dữ liệu
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            int revenue = monthlyRevenueMap.getOrDefault(i, 0);
            //System.out.println(revenue);
            entries.add(new BarEntry(i, revenue));
        }

        // Tạo DataSet từ dữ liệu
        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu theo tháng");

        barChart = findViewById(R.id.barChart);
        // Tạo dữ liệu cho biểu đồ
        BarData barData = new BarData(dataSet);

        barChart.setData(barData);

        // Thiết lập các thuộc tính cho biểu đồ
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        Description description = new Description();
        description.setText("Tháng");
        barChart.getDescription().setEnabled(true); // bật mô tả
        barChart.setDescription(description);
        barChart.setTouchEnabled(true); // Cho phép chạm để phóng to/thu nhỏ
        barChart.setDragEnabled(true); // Cho phép kéo biểu đồ
        barChart.setScaleEnabled(true); // Cho phép zoom
        barChart.setPinchZoom(true); // Cho phép zoom bằng cử chỉ pinch
        barChart.animateY(1000); // Animation khi hiển thị biểu đồ

        // Thiết lập trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return String.valueOf((int) value); // Hiển thị số tháng
            }
        });
        xAxis.setGranularity(1f); // Đảm bảo hiển thị mỗi tháng
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Đặt trục X ở dưới biểu đồ

        // Thiết lập trục Y
        YAxis yAxisRight = barChart.getAxisRight();
        yAxisRight.setEnabled(false); // Tắt trục Y bên phải
        barChart.getAxisLeft().setAxisMinimum(0);
        //setText cho trục y của biểu đồ
        tvVND.setText("vnd");

    }
}