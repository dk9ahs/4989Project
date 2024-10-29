//package com.book.BookProject;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import javax.annotation.PostConstruct;
//import java.io.IOException;
//
//@Controller
//public class MapController
//{
//    // 컨트롤러 초기화 시 Streamlit 서버 실행
//    @PostConstruct
//    public void startStreamlit() {
//        try {
//            ProcessBuilder processBuilder = new ProcessBuilder(
////                    "C:/FrontEnd/new2/BookProject/src/main/python/venv/Scripts/python",
//                    "src/main/python/venv/Scripts/python",
//                    "-m", "streamlit", "run",
//                    "src/main/python/map.py",
////                    "--server.port", "8501",          // 포트 설정
//                    "--server.headless", "true"        // headless 모드 활성화
//            );
//
//            processBuilder.redirectErrorStream(true); // 오류와 출력을 동일한 스트림으로 연결
//            processBuilder.inheritIO(); // 새로운 프로세스의 출력을 현재 프로세스에 전달
//            processBuilder.start();// 프로세스 시작
//
////            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
////            String line;
////            while ((line = reader.readLine()) != null) {
////                System.out.println(line); // 로그 출력
////            }
//
//            System.out.println("Streamlit 서버가 성공적으로 시작되었습니다.");
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.err.println("Streamlit 서버 실행에 실패했습니다.");
//        }
//    }
//
//    @RequestMapping("/librarymap")
//    public String libraryMap()
//    {
//        return "guest/libraryMap";
//    }
//}
//

package com.book.BookProject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Controller
public class MapController {
    private Process streamlitProcess; // Streamlit 프로세스를 저장할 변수

    // 컨트롤러 초기화 시 Streamlit 서버 실행
    @PostConstruct
    public void startStreamlit() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "src/main/python/venv/Scripts/python",
                    "-m", "streamlit", "run",
                    "src/main/python/map.py",
                    "--server.headless=true", // headless 모드 활성화
                    "--server.port=8501" // 특정 포트 설정
            );

            processBuilder.redirectErrorStream(true); // 오류와 출력을 동일한 스트림으로 연결
            streamlitProcess = processBuilder.start(); // 프로세스 시작
            System.out.println("Streamlit 서버가 성공적으로 시작되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Streamlit 서버 실행에 실패했습니다.");
        }
    }

    // 애플리케이션 종료 시 Streamlit 서버 종료
    @PreDestroy
    public void stopStreamlit() {
        if (streamlitProcess != null) {
            streamlitProcess.destroyForcibly(); // 프로세스를 강제로 종료
            System.out.println("Streamlit 서버가 종료되었습니다.");
        }
    }

    @RequestMapping("/librarymap")
    public String libraryMap() {
        return "guest/libraryMap";
    }
}

