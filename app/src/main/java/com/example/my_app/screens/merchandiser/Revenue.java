package com.example.my_app.screens.merchandiser;

import androidx.appcompat.app.AppCompatActivity;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
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

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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
    private Map<String, Integer> nameAndTotalAvenue;
    private Map<String, Integer> uniqueQuantity= new HashMap<>();
    private Map<String, Integer> uniqueAvenue = new HashMap<>();

    EditText edtYear;
    Button btnSearchYear, btnExport;
    TextView tvRevenue, tvVND, tvTitle;
    TableLayout tlAvenue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        uid = GlobalVariable.userInfo.getUid();

        System.out.println(uid);
        setControl();
        setEvent();

    }
    private void setEvent() {
        edtYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    try {
                        int year = Integer.parseInt(s.toString());
                        // Nếu năm không âm và không lớn hơn năm hiện tại
                        if (year <= 0 || year > Calendar.getInstance().get(Calendar.YEAR)) {
                            edtYear.setError("Năm không hợp lệ");
                        }
                    } catch (NumberFormatException e) {
                        edtYear.setError("Nhập số năm hợp lệ");
                    }
                }
            }
        });

        btnSearchYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtYear.getText().toString().equals("")) {
                    edtYear.setError("Chưa nhập số năm");
                    edtYear.requestFocus();
                    return;
                }
                calculateAndDrawRevenue();
            }

        });

    }
    private void setControl() {
        barChart = findViewById(R.id.barChart);
        edtYear =  findViewById(R.id.edtYear);
        btnSearchYear = findViewById(R.id.btnSearchYear);
        tvRevenue = findViewById(R.id.tvRevenue);
        tvVND = findViewById(R.id.tvVND);
        tlAvenue = findViewById(R.id.tlAvenue);
        tvTitle = findViewById(R.id.tvTitle);
    }

    private void calculateAndDrawRevenue() {
        // Lấy thời gian hiện tại để tính thống kê theo tháng
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Tháng tính từ 0 đến 11, cần cộng 1 để lấy tháng hiện tại

        // Map để lưu tổng doanh thu của mỗi tháng
        monthlyRevenueMap = new HashMap<>();
        for (int month = 1; month <= 12; month++) {
            monthlyRevenueMap.put(month, 0);

        }
        final int[] totalRevenue = {0};

        nameAndTotalQuantity = new HashMap<>();
        nameAndTotalAvenue = new HashMap<>();

        // Truy vấn tất cả các sản phẩm mà mechandiser bán
        db.collection("products")
                .whereEqualTo("uid", GlobalVariable.userInfo.getUid()) // Chỉ lấy các sản phẩm đang bán của uid hiện tại
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot productQueryDocumentSnapshots) {
                        if (!productQueryDocumentSnapshots.isEmpty()) {
                            // Lặp qua từng sản phẩm mà merchandiser đang bán
                            for (QueryDocumentSnapshot productSnapshot : productQueryDocumentSnapshots) {
                                //System.out.println(productSnapshot.getId());

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

                                                        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
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
                                                                                                Calendar calendar = Calendar.getInstance();
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
                                                                                                        nameAndTotalAvenue.put(newKey, price * quantity);
                                                                                                    } else {
                                                                                                        // Nếu khóa đã tồn tại trong Map, không thực hiện ghi đè giá trị
                                                                                                        // Cập nhật tổng doanh thu của tháng
                                                                                                        int tempQuantity = nameAndTotalQuantity.getOrDefault(newKey, 0);
                                                                                                        tempQuantity += quantity;
                                                                                                        nameAndTotalQuantity.put(newKey, tempQuantity);

                                                                                                        int tempAvenue = nameAndTotalAvenue.getOrDefault(newKey, 0);
                                                                                                        tempAvenue += price * quantity;
                                                                                                        nameAndTotalAvenue.put(newKey, tempAvenue);
                                                                                                    }

                                                                                                }

                                                                                            }
                                                                                            // Vẽ biểu đồ cột sau khi đã tính toán xong
                                                                                            drawBarChart(monthlyRevenueMap);

                                                                                            //Gán tổng doanh thu vào text view
                                                                                            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                                                                                            tvRevenue.setText(formatter.format(totalRevenue[0]));

                                                                                            uniqueQuantity = nameAndTotalQuantity;
                                                                                            uniqueAvenue = nameAndTotalAvenue;


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

        //setText cho trục y của biểu đồ
        tvVND.setText("vnd");
//        if(totalRevenue[0]==0){
//            Toast.makeText(this, "No Data !!! :(", Toast.LENGTH_SHORT).show();
//        }

        drawTable(uniqueQuantity, uniqueAvenue);
        tlAvenue.setPadding(8,8,8,8);
        tvTitle.setText("Bảng: Top 5 sản phẩm có doanh thu cao nhất năm "+edtYear.getText().toString());

    }

    private void drawTable(Map<String, Integer> uniqueQuantity, Map<String, Integer> uniqueAvenue) {
        // Chuyển Map thành một danh sách các phần tử (entries)
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(uniqueAvenue.entrySet());

        // Sắp xếp danh sách các phần tử theo giá trị giảm dần
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
                // So sánh giá trị của các phần tử và trả về kết quả tương ứng
                return entry2.getValue().compareTo(entry1.getValue());
            }
        });

        // Tạo một Map mới để lưu trữ các phần tử đã sắp xếp
        Map<String, Integer> sortedMapAvenue = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entries) {
            sortedMapAvenue.put(entry.getKey(), entry.getValue());
        }
        // Bây giờ sortedMap chứa các phần tử của uniqueAvenue được sắp xếp theo giá trị giảm dần

        // Xóa tất cả các TableRow hiện có khỏi TableLayout, đỡ trùng lặp dữ liệu
        tlAvenue.removeAllViews();
        int count = 0; // Biến đếm số lượng phần tử đã in ra
        for (Map.Entry<String, Integer> entry : sortedMapAvenue.entrySet()) {
            if (count < 5) {
                String sortedKey = entry.getKey();
                Integer sortedValue2 = entry.getValue();
                Integer sortedValue1 = uniqueQuantity.getOrDefault(sortedKey,0);
                count++;

                //Tạo một TableRow mới
                TableRow row = new TableRow(Revenue.this);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(layoutParams);
                row.setBackgroundColor(Color.parseColor("#F5F5F5"));

                // Thêm TextViews vào TableRow mới
                TextView tvProductName = new TextView(Revenue.this);
                tvProductName.setLayoutParams(new TableRow.LayoutParams(300, TableRow.LayoutParams.WRAP_CONTENT)); // Đặt kích thước
                tvProductName.setText(sortedKey);
                tvProductName.setPadding(8, 8, 8, 8);
                row.addView(tvProductName);


                NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

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
                tlAvenue.addView(row);

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

    }
}