package com.example.application;

import java.io.*;
import java.sql.*;
import jakarta.servlet.http.*;

public class NewTransactionServlet extends HttpServlet {
    private final String URL = "jdbc:postgresql://localhost:5432/application_db";
    private final String USER = "postgres";
    private final String PASSWORD = "1";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        Integer playerId = Integer.parseInt(request.getParameter("playerId"));
        Integer clanId = Integer.parseInt(request.getParameter("clanId"));
        Integer money = Integer.parseInt(request.getParameter("moneyAmount"));
        String action = request.getParameter("action");
        Long transId = newTrans(playerId, clanId, action, money);
        Integer checkUpd = updateClansGold(clanId, money);
        String message = "Транзакция выполнена успешно, номер транзакции: " + transId +
                "\nИндикатор обновления: " + checkUpd;

        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + message + "</h1>");
        out.println("</body></html>");
    }

    public Connection connect() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        }
        catch (ClassNotFoundException eConnection) {
            eConnection.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }



    public long newTrans(Integer playerId, Integer clanId, String action, Integer money) {
        String SQL = "INSERT INTO transactions (player_id, clan_id, action, money) "
                + "VALUES(?,?,?,?)";
        long id = 0;
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL,
                     Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, playerId);
            pstmt.setInt(2,clanId);
            pstmt.setString(3, action);
            pstmt.setInt(4, money);

            int affectedRows = pstmt.executeUpdate();
            // check the affected rows
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        id = rs.getLong(1);
                    }
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return id;
    }

    public Integer updateClansGold(Integer clanId, Integer money) {
        Integer affectedrows = 0;
        String SQL = "UPDATE clan "
                + "SET gold = gold + ?"
                + "WHERE clan_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, money);
            pstmt.setInt(2, clanId);
            affectedrows = pstmt.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return affectedrows;
    }
}