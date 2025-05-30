package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import models.Order;
import models.OrderDetail;

public class OrderDAO {
    private Connection connection;

    public OrderDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT OrderID, UserID, TotalPrice, OrderStatus, ShippingAddress, CreatedAt FROM Orders";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order order = new Order();
                order.setOrderID(rs.getInt("OrderID"));
                order.setUserID(rs.getInt("UserID"));
                order.setTotalPrice(rs.getBigDecimal("TotalPrice"));
                order.setOrderStatus(rs.getString("OrderStatus"));
                order.setShippingAddress(rs.getString("ShippingAddress"));
                order.setCreatedAt(rs.getTimestamp("CreatedAt"));
                orders.add(order);
            }
        } catch (SQLException e) {
			e.printStackTrace();
		}
        return orders;
    }
    
    public Order getOrderById(int orderId) throws SQLException {
        Order order = null;

        String orderQuery = "SELECT o.OrderID, o.UserID, o.TotalPrice, o.OrderStatus, o.ShippingAddress, o.CreatedAt, u.Username "
                           + "FROM Orders o JOIN Users u ON o.UserID = u.UserID WHERE o.OrderID = ?";

        try (PreparedStatement orderStmt = connection.prepareStatement(orderQuery)) {
            orderStmt.setInt(1, orderId);
            ResultSet rs = orderStmt.executeQuery();

            if (rs.next()) {
                order = new Order();
                order.setOrderID(rs.getInt("OrderID"));
                order.setUserID(rs.getInt("UserID"));
                order.setTotalPrice(rs.getBigDecimal("TotalPrice"));
                order.setOrderStatus(rs.getString("OrderStatus"));
                order.setShippingAddress(rs.getString("ShippingAddress"));
                order.setCreatedAt(rs.getTimestamp("CreatedAt"));
                order.setUsername(rs.getString("Username"));
                
                List<OrderDetail> items = getOrderDetails(orderId);
                order.setOrderItems(items);
            }
        }
        return order;
    }


    public List<OrderDetail> getOrderDetails(int orderId) throws SQLException {
        List<OrderDetail> items = new ArrayList<>();

        String itemQuery = "SELECT od.ProductID, p.Name AS ProductName, od.Quantity, od.Price "
                           + "FROM OrderDetails od JOIN Products p ON od.ProductID = p.ProductID WHERE od.OrderID = ?";

        try (PreparedStatement itemStmt = connection.prepareStatement(itemQuery)) {
            itemStmt.setInt(1, orderId);
            ResultSet rs = itemStmt.executeQuery();

            while (rs.next()) {
                OrderDetail item = new OrderDetail();
                item.setProductID(rs.getInt("ProductID"));
                item.setProductName(rs.getString("ProductName"));
                item.setQuantity(rs.getInt("Quantity"));
                item.setPrice(rs.getBigDecimal("Price"));
                items.add(item);
            }
        }
        return items;
    }

    public int createOrder(Order order) throws SQLException {
        String sqlInsert = "INSERT INTO Orders (UserID, TotalPrice, OrderStatus, ShippingAddress, CreatedAt) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, order.getUserID());
            preparedStatement.setBigDecimal(2, order.getTotalPrice());
            preparedStatement.setString(3, order.getOrderStatus());
            preparedStatement.setString(4, order.getShippingAddress());
            preparedStatement.setTimestamp(5, order.getCreatedAt());

            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1); // Trả về ID của đơn hàng vừa tạo
            } else {
                throw new SQLException("Tạo đơn hàng không thành công, không lấy được orderID.");
            }
        }
    }
    
    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT OrderID, UserID, TotalPrice, OrderStatus, ShippingAddress, CreatedAt FROM Orders WHERE UserID = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderID(rs.getInt("OrderID"));
                    order.setUserID(rs.getInt("UserID"));
                    order.setTotalPrice(rs.getBigDecimal("TotalPrice"));
                    order.setOrderStatus(rs.getString("OrderStatus"));
                    order.setShippingAddress(rs.getString("ShippingAddress"));
                    order.setCreatedAt(rs.getTimestamp("CreatedAt"));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

}
