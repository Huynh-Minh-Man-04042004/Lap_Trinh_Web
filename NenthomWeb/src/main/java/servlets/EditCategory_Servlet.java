package servlets;

import java.io.IOException;
import java.sql.Connection;

import dao.CategorieDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.Categorie;
import services.ConnectionUtil;

@WebServlet("/servlets/EditCategory_Servlet")
public class EditCategory_Servlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy thông tin từ form
        int categoryId = Integer.parseInt(request.getParameter("categoryID"));
        String name = request.getParameter("name");
        String description = request.getParameter("description");

        Categorie updatedCategory = new Categorie(categoryId, name, description);

        try (Connection connection = ConnectionUtil.DB()) {
            CategorieDAO categorieDAO = new CategorieDAO(connection);
            boolean isUpdated = categorieDAO.updateCategory(updatedCategory); // Cập nhật sản phẩm trong DB

            if (isUpdated) {
                response.sendRedirect("/NenthomWeb/servlets/DSProduct_Servlet?page=admin&message=success&action=category");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không thể cập nhật danh mục.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi khi kết nối với cơ sở dữ liệu.");
        }
    }
}