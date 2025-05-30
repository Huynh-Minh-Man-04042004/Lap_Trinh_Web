package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import dao.TaiKhoanDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import models.TaiKhoan;
import services.AuthCodeUtil;
import services.ConnectionUtil;
import utils.CSRFUtil;

@WebServlet("/login")
public class Login_Servlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CSRFUtil.attachToken(request); 
		AuthCodeUtil.refreshVerificationCode(request.getSession());
		request.getRequestDispatcher("/views/login.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CSRFUtil.isValid(request)) {
            request.getRequestDispatcher("/views/csrf_error.jsp").forward(request, response);
            return;
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String message = "";
        boolean error = false;
        boolean isValid = true;
       
        TaiKhoan tk = new TaiKhoan(username, password);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<TaiKhoan>> violations = validator.validate(tk);

        StringBuilder errorMessages = new StringBuilder();

        if (!violations.isEmpty()) {
            isValid = false;
            for (ConstraintViolation<TaiKhoan> violation : violations) {
                errorMessages.append(violation.getMessage()).append("<br>");
            }
        }

        if (!AuthCodeUtil.isVerificationCodeValid(request)) {
            isValid = false;
            errorMessages.append("Mã xác thực không đúng!<br>");
        }

        if (!isValid) {
            AuthCodeUtil.refreshVerificationCode(request.getSession());
            request.setAttribute("message", errorMessages.toString());
            request.setAttribute("error", true);
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }


        try (Connection conn = ConnectionUtil.DB()) {
            TaiKhoanDAO taiKhoanDao = new TaiKhoanDAO(conn);
            UserDAO userDao = new UserDAO(conn);

            if (!taiKhoanDao.isUsernameExist(username)) {
                message = "Tài khoản không tồn tại!";
                error = true;
            } else {
                String storedPassword = taiKhoanDao.getPasswordByUsername(username);
                if (storedPassword.equals(password)) {
                    String role = taiKhoanDao.getRoleByUsername(username);
                    int userID = userDao.getUserIDByUsername(username);

                    request.getSession().invalidate();
                    HttpSession session = request.getSession(true);
                    session.setAttribute("username", username);
                    session.setAttribute("userID", userID);
                    session.setAttribute("role", role);


                    // Chuyển hướng đến trang quản lý theo quyền
                    if ("manager".equals(role)) {
                        request.getRequestDispatcher("/servlets/DSProduct_Servlet?page=admin").forward(request, response);
                    } else {
                        request.getRequestDispatcher("/views/TrangChu.jsp").forward(request, response);
                    }
                    return; // Đảm bảo không chạy tiếp phần xử lý lỗi
                } else {
                    message = "Mật khẩu không đúng!";
                    error = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            message = "Lỗi kết nối cơ sở dữ liệu.";
            error = true;
        }

        if (error) {
            request.setAttribute("message", message);
            request.setAttribute("error", error);
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
        }
    }
}
