import http.server
import socketserver
import mimetypes

PORT = 5500

# 윈도우 환경에서 CSS MIME 타입이 깨지는 문제를 방지하기 위한 강제 설정
mimetypes.add_type('text/css', '.css')
mimetypes.add_type('application/javascript', '.js')

Handler = http.server.SimpleHTTPRequestHandler
with socketserver.TCPServer(("", PORT), Handler) as httpd:
    print(f"프론트엔드 서버가 http://localhost:{PORT} 에서 실행 중입니다...")
    httpd.serve_forever()
