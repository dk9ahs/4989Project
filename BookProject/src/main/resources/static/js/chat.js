	function chatPopup(){
		var s_width = window.screen.width;
		var s_height = window.screen.height;

		var leftVar = s_width/2 - 500/2;
		var topVar = s_height/2 - 500/2;

		window.open("chat/chatPopup", "popup",
			"width=600,height=250,left="+leftVar+",top="+topVar);
	}

	function chatPoplist(){
		var s_width = window.screen.width;
		var s_height = window.screen.height;

		var leftVar = s_width/2 - 500/2;
		var topVar = s_height/2 - 500/2;

		window.open("chat/chatRoomList", "popup",
			"width=600,height=250,left="+leftVar+",top="+topVar);
	}

	function loginForm(){
       var answer = window.confirm("로그인 후 이용가능합니다. 로그인 하시겠습니까?");
       if(answer == true){
            location.href="../guest/loginform";
       }
    }