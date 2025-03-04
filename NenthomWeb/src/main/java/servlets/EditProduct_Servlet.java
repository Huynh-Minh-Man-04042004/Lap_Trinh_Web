package servlets;

import java.io.IOException;
import java.sql.Connection;

import dao.ProductDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.Product;
import services.ConnectionUtil;

@WebServlet("/servlets/EditProduct_Servlet")
public class EditProduct_Servlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public EditProduct_Servlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	String name = request.getParameter("name");
        double price = Double.parseDouble(request.getParameter("price"));
        String description = request.getParameter("description");
        int stock = Integer.parseInt(request.getParameter("stock"));
        String imageBase64 = request.getParameter("imageBase64");
        
        Product product = new Product(0, name, description, price, stock, imageBase64);

        try (Connection connection = ConnectionUtil.DB()) {
            ProductDAO productDAO = new ProductDAO(connection);
            boolean isUpdated = productDAO.updateProductByName(product); // Cập nhật sản phẩm trong DB

            if (isUpdated) {
                response.sendRedirect("/NenthomWeb/servlets/DSProduct_Servlet?page=admin&message=success&action=product");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không thể cập nhật sản phẩm.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi khi kết nối với cơ sở dữ liệu.");
        }
    }

}
