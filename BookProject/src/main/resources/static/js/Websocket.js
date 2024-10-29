    let socket;

    // 페이지 로드 시 WebSocket 연결
    window.onload = function() {

        // WebSocket 서버 URL
        socket = new WebSocket("ws://localhost:8083/test");

        // WebSocket 연결이 열리면
        socket.onopen = function() {
//            console.log("WebSocket 연결 성공");
            ViewCount();
        };

        // 메세지 도착시
        socket.onmessage = function(event) {

            const message = event.data;

            try {
                console.log('메세지 도착');
                const jsonResponse = JSON.parse(message);
                const unreadCount = jsonResponse.unreadCount;

                const messagesCount = document.getElementById("messagesCount");
                if(messagesCount) {
                    messagesCount.textContent = unreadCount;
                }

                const messagesContainer = document.getElementById("messagesContainer");
                messagesContainer.textContent = unreadCount;

                const messageListTable = $('#messageListTable');
                if(messageListTable){
                    updateMessageList(messageListTable);
                }

            } catch (e) {
//                console.error("메시지 파싱 실패:", e);
            }

        };

        // WebSocket 오류 발생 시
        socket.onerror = function(error) {
//            console.error("WebSocket 오류:", error);
        };

        // WebSocket 연결이 닫히면
        socket.onclose = function(event) {
            if (event.wasClean) {
//                console.log("WebSocket 연결 종료 (정상 종료)");
            } else {
//                console.error("WebSocket 연결 종료 (비정상 종료)");
            }
        };
    };

    function ViewCount(){
        var message = {
            action:"count"
        };
        socket.send(JSON.stringify(message));
    }

    function updateMessageList(table) {
        $.ajax({
            url: '/messages/relist', // 메시지 목록을 가져오는 URL
            type: 'GET',
            success: function(data) {
                // 반환된 데이터를 사용하여 테이블 내용을 업데이트
                table.html(data);
            },
            error: function(xhr, status, error) {
//                console.error('메시지 목록을 가져오는 중 오류 발생: ' + error);
            }
        });
    }




