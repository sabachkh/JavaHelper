import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TextField;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {

    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextField quantityField;
    @FXML private PieChart pieChart;

    @FXML
    public void initialize() {
        loadChartData();
    }

    @FXML
    public void addProduct() {
        String name = nameField.getText();
        String category = categoryField.getText();
        int quantity = Integer.parseInt(quantityField.getText());

        String sql = "INSERT INTO product(name, category, quantity) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionHelper.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();

            loadChartData();

            nameField.clear();
            categoryField.clear();
            quantityField.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadChartData() {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM product";

        try (Connection conn = ConnectionHelper.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                productList.add(new Product(
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getInt("quantity")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Integer> categorySummary = productList.stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.summingInt(Product::getQuantity)));

        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();

        categorySummary.forEach((category, totalQuantity) -> {
            chartData.add(new PieChart.Data(category, totalQuantity));
        });

        pieChart.setData(chartData);
    }
}
