package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import dao.CartDAO;
import dao.ProductDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import models.Cart;
import models.Product;
import services.ConnectionUtil;
import utils.CSRFUtil;

@WebServlet("/servlets/AddToCart_Servlet")
public class AddToCart_Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public AddToCart_Servlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!CSRFUtil.isValid(request)) {
	        request.getRequestDispatcher("/views/csrf_error.jsp").forward(request, response);
	        return;
	    }
        // Lấy giá trị productID từ request body (POST)
        String productIDStr = request.getParameter("productID");
        int quantity = Integer.parseInt(request.getParameter("quantity"));
        // Kiểm tra nếu productID hợp lệ
        if (productIDStr == null || productIDStr.isEmpty()) {
            response.getWriter().write("Error: Missing product ID.");
            return;
        }

        // Chuyển đổi productID từ String sang int
        int productID;
        try {
            productID = Integer.parseInt(productIDStr);
        } catch (NumberFormatException e) {
            response.getWriter().write("Error: Invalid product ID format.");
            return;
        }

        // Tiến hành các xử lý tiếp theo
        HttpSession session = request.getSession();
        int userID = (int) session.getAttribute("userID");

        // Thêm vào giỏ hàng
        Cart cart = new Cart(userID, productID, quantity);
        try (Connection connection = ConnectionUtil.DB()) {
            CartDAO cartDAO = new CartDAO(connection);
            cartDAO.addToCart(cart);
            request.setAttribute("successMessage", "Product added to cart successfully!");
            
            ProductDAO productDao = new ProductDAO(connection);
            List<Product> products = productDao.getAllProducts();
            request.setAttribute("products", products);
            CSRFUtil.attachToken(request);
            request.getRequestDispatcher("/views/product.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            // Thông báo lỗi nếu có
            request.setAttribute("errorMessage", "Error adding to cart.");
            CSRFUtil.attachToken(request);
            request.getRequestDispatcher("/views/product.jsp").forward(request, response);
        }
    }
}
