package xsolla;

import io.github.cdimascio.dotenv.Dotenv;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@WebServlet("/client-side")
public class ClientSide extends HttpServlet {
    private final String projectId;
    private final String loginId;
    private final String backendPort;
    private final String webhookSecretKey;

    public ClientSide() {
        Dotenv dotenv = Dotenv.load();
        this.projectId = dotenv.get("XSOLLA_PROJECT_ID");
        this.loginId = dotenv.get("XSOLLA_LOGIN_ID");
        this.backendPort = dotenv.get("BACKEND_PORT");
        this.webhookSecretKey = dotenv.get("XSOLLA_WEBHOOK_SECRET_KEY");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("xsollaProjectId", this.projectId);
        request.setAttribute("xsollaLoginId", this.loginId);
        request.setAttribute("backendPort", this.backendPort);
        request.setAttribute("xsollaWebhookSecretKey", this.webhookSecretKey);

        request.getRequestDispatcher("/client-side.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String projectId = request.getParameter("project");
        String itemSku = request.getParameter("itemSku");
        String userToken = request.getParameter("userToken");
        String body = request.getParameter("body");

        String apiUrl = "https://store.xsolla.com/api/v2/project/" + projectId + "/payment/item/" + itemSku;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + userToken);
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            StringBuilder apiResponse = new StringBuilder();
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                while (scanner.hasNextLine()) {
                    apiResponse.append(scanner.nextLine());
                }
            }

            String responseBody = apiResponse.toString();
            String token = extractToken(responseBody);

            boolean isSandbox = body.contains("\"sandbox\": true");
            String baseUrl = isSandbox ? "https://sandbox-secure.xsolla.com/paystation4/?token=" : "https://secure.xsolla.com/paystation4/?token=";
            String fullUrl = baseUrl + token;

            String formattedLink = "<a href=\"" + fullUrl + "\" target=\"_blank\">" + fullUrl + "</a>";
            String completeResponse = "<h3>API Response:</h3><pre>" + escapeHtml(responseBody) + "</pre>" + "<h3>Payment Link:</h3>" + formattedLink;

            request.setAttribute("apiResponse", completeResponse);
            request.setAttribute("xsollaProjectId", this.projectId);
            request.setAttribute("xsollaLoginId", this.loginId);
            request.setAttribute("backendPort", this.backendPort);
            request.setAttribute("userToken", userToken);
            request.setAttribute("xsollaWebhookSecretKey", this.webhookSecretKey);

            request.getRequestDispatcher("/client-side.jsp").forward(request, response);

        } catch (IOException e) {
            e.printStackTrace();
            request.setAttribute("apiResponse", "Error: " + e.getMessage());
            request.getRequestDispatcher("/client-side.jsp").forward(request, response);
        }
    }

    private String extractToken(String json) {
        int tokenStart = json.indexOf("\"token\":\"") + 9;
        int tokenEnd = json.indexOf("\"", tokenStart);
        return json.substring(tokenStart, tokenEnd);
    }

    private String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
