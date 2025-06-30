// 简单的编译测试文件
import burp.utils.HttpRequestResponse;
import burp.utils.RequestFilter;
import burp.BurpExtender;
import burp.IHttpService;

public class TestCompilation {
    public static void main(String[] args) {
        System.out.println("Testing compilation...");
        
        // 测试HttpRequestResponse类的使用
        HttpRequestResponse httpRequestResponse = new HttpRequestResponse();
        
        // 测试RequestFilter类的使用
        byte[] testRequest = "GET /test HTTP/1.1\r\nHost: example.com\r\n\r\n".getBytes();
        boolean isDangerous = RequestFilter.isDangerousRequest(testRequest);
        
        System.out.println("Request is dangerous: " + isDangerous);
        System.out.println("Compilation test completed successfully!");
    }
}